package top.cyqi.EasyGrasscutters.Event.content;

import emu.grasscutter.game.player.Player;
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

public class PlayerExp_t {

    //是否带入玩家检测
    public Integer player_uid = -1;
    public Integer experience; //目标经验
    public String msg_id;
    public WsMessageContext wsMessageContext;

    public PlayerExp_t(String Msg_id, Integer experience, WsMessageContext wsMessageContext) {
        if (Msg_id == null || experience == null || wsMessageContext == null)
            throw new RuntimeException("position_t参数不能为空");
        this.msg_id = Msg_id;
        this.experience = experience;
        this.wsMessageContext = wsMessageContext;
    }

    public boolean check(Player player) {
        if (player_uid != -1) {
            if (player.getUid() != player_uid)
                return false;
        }

        if (player.getExp() >= this.experience) {
            // 玩家经验大于目标经验触发
            JSONObject temp = new JSONObject();
            temp.put("msg_id", msg_id);
            temp.put("type", "OnPlayerExp");
            temp.put("Exp", player.getExp());
            temp.put("data", player.getUid());
            wsMessageContext.send(temp.toString());
            return true;
        }
        return false;
    }
}
