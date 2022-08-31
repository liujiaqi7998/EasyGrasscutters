package top.cyqi.EasyGrasscutters.Event.content;

import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

public class KillEntity {
    public String msg_id;
    public Integer Entity_id;
    public WsMessageContext wsMessageContext;

    public KillEntity(String Msg_id, Integer entity_id, WsMessageContext wsMessageContext) {
        if (Msg_id == null || entity_id == null || wsMessageContext == null)
            throw new RuntimeException("KillEntity参数不能为空");
        this.msg_id = Msg_id;
        this.Entity_id = entity_id;
        this.wsMessageContext = wsMessageContext;
    }

    public boolean check(int death_id, int killed_id) {
        if (this.Entity_id == death_id) {
            JSONObject temp = new JSONObject();
            temp.put("type", "OnKillEntity");
            temp.put("msg_id", this.msg_id);
            temp.put("data", killed_id);
            wsMessageContext.send(temp.toString());
            return true;
        }
        return false;
    }
}
