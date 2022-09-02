package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.PlayerChatReqOuterClass;
import emu.grasscutter.server.event.game.SendPacketEvent;
import emu.grasscutter.utils.EventConsumer;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

import static emu.grasscutter.Grasscutter.getLogger;

public class ChatEvent implements EventConsumer<SendPacketEvent> {


    @Override
    public void consume(SendPacketEvent sendPacketEvent) {
        // TODO 确定数据包类型
        int Opcode = sendPacketEvent.getPacket().getOpcode();
        if (Opcode == PacketOpcodes.PrivateChatNotify) {
            // 确定是私聊数据包
            //getLogger().info("玩家发送私聊数据包");
            try {
                PlayerChatReqOuterClass.PlayerChatReq chatInfo = PlayerChatReqOuterClass.PlayerChatReq.parseFrom(sendPacketEvent.getPacket().getData());

                // 我不理解？ 为什么hasChatInfo()返回的是false？
                // 没办法只能 toString 了
                /* > 7: {
                          7: 99
                          13: 1662120300
                          15: 666777
                          1946: "3"
                        }
                 */

                //分割文本
//                Integer ToUid = Integer.valueOf(resolveString(chatInfo.toString(),"  7: "));
//                Integer FromUid = Integer.valueOf(resolveString(chatInfo.toString(),"  15: "));
//                int star = chatInfo.toString().indexOf("  1946: \"") + 9;
//                int end = chatInfo.toString().indexOf("\"\n",star);
//                String msg =  chatInfo.toString().substring(star, end);

                // 生成JSON
                JSONObject json = new JSONObject();
                json.put("type", "OnChat");
                json.put("data", chatInfo.toString());
                WebSocketServer.ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(json.toString()));


            } catch (Exception e) {
                getLogger().error("聊天监听器解包错误:" + e.getMessage());
            }

        }
    }

    // 手撕数据包
    public String resolveString(String str, String key) {
        int star = str.indexOf(key) + key.length();
        int end = str.indexOf("\n", star);
        return str.substring(star, end);
    }
}
