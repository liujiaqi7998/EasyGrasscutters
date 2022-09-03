package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ChatInfoOuterClass;
import emu.grasscutter.net.proto.PrivateChatNotifyOuterClass;
import emu.grasscutter.server.event.game.SendPacketEvent;
import emu.grasscutter.utils.EventConsumer;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.Event.content.ChatEvent_t;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static emu.grasscutter.Grasscutter.getLogger;

public class ChatEvent implements EventConsumer<SendPacketEvent> {

    public static Map<String, ChatEvent_t> All_Chat = new HashMap<>();

    @Override
    public void consume(SendPacketEvent sendPacketEvent) {
        // TODO 确定数据包类型
        int Opcode = sendPacketEvent.getPacket().getOpcode();
        if (Opcode == PacketOpcodes.PrivateChatNotify) {
            // 确定是私聊数据包
            //getLogger().info("玩家发送私聊数据包");
            try {
                PrivateChatNotifyOuterClass.PrivateChatNotify privateChatNotify = PrivateChatNotifyOuterClass.PrivateChatNotify.parseFrom(sendPacketEvent.getPacket().getData());//解析数据包
                if (!privateChatNotify.hasChatInfo()) {
                    //无效数据包
                    return;
                }

                ChatInfoOuterClass.ChatInfo chatInfo = privateChatNotify.getChatInfo();//提取聊天数据
                ChatInfoOuterClass.ChatInfo.ContentCase content = chatInfo.getContentCase();//获取聊天内容类型用于判断是文字还是表情
                JSONObject json = new JSONObject();
                json.put("type", "OnChat");
                json.put("from", chatInfo.getUid());
                json.put("to", chatInfo.getToUid());
                if (content == ChatInfoOuterClass.ChatInfo.ContentCase.TEXT) {
                    json.put("msg_type", "TEXT");
                    json.put("data", chatInfo.getText());

                    for (String key : All_Chat.keySet()) {
                        ChatEvent_t ChatEvent_a = All_Chat.get(key);
                        if (ChatEvent_a.check(String.valueOf(chatInfo.getUid()), String.valueOf(chatInfo.getToUid()), chatInfo.getText())) {
                            All_Chat.remove(key);
                        }
                    }

                } else if (content == ChatInfoOuterClass.ChatInfo.ContentCase.ICON) {
                    json.put("msg_type", "ICON");
                    json.put("data", chatInfo.getIcon());
                }

                WebSocketServer.ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(json.toString()));


            } catch (Exception e) {
                getLogger().error("聊天监听器解包错误:" + e.getMessage());
            }

        }
    }

    public static void delete(String msg_id) {
        for (String key : All_Chat.keySet()) {
            ChatEvent_t ChatEvent_a = All_Chat.get(key);
            if (ChatEvent_a.msg_id.equals(msg_id)) {
                All_Chat.remove(key);
            }

        }
    }

    public static void add_ChatEvent(ChatEvent_t ChatEvent_a) {
        String uuid = UUID.randomUUID().toString();
        All_Chat.put(uuid, ChatEvent_a);
    }

}
