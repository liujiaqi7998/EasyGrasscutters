package top.cyqi.EasyGrasscutters.Event.content;

import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

public class ChatEvent_t {
    public String msg_id;
    public WsMessageContext wsMessageContext;

    public String player_uid;
    public String player_to_uid;
    public String msg_data;

    public ChatEvent_t(String Msg_id, String player_uid, String player_to_uid, String msg_data, WsMessageContext wsMessageContext) {
        if (Msg_id == null || wsMessageContext == null)
            throw new RuntimeException("ChatEvent参数不能为空");
        this.msg_id = Msg_id;
        this.player_uid = player_uid;
        this.player_to_uid = player_to_uid;
        this.msg_data = msg_data;
        this.wsMessageContext = wsMessageContext;
    }

    public boolean check(String player_uid, String player_to_uid, String msg_data) {
        if (this.msg_data.equals(msg_data)) {
            if (this.player_uid.equals(player_uid) || this.player_uid.equals("0")) {
                //如果要是 player_to_uid 为0，则为与任何人聊天都有效触发
                if (this.player_to_uid.equals(player_to_uid) || this.player_to_uid.equals("0")) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "OnChatMsg");
                    temp.put("msg_id", this.msg_id);
                    temp.put("data", true);
                    temp.put("player_uid", player_uid);
                    temp.put("to", player_to_uid);
                    wsMessageContext.send(temp.toString());
                    return true;
                }
            }
        }
        return false;
    }
}
