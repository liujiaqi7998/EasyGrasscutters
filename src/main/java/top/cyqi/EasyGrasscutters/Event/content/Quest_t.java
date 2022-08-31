package top.cyqi.EasyGrasscutters.Event.content;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.enums.QuestState;
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

public class Quest_t {

    //监听剧情
    public Integer Quest;
    //剧情预期状态
    public QuestState state;
    //是否带入玩家检测
    public Integer player_uid = -1;

    public String msg_id;
    public WsMessageContext wsMessageContext;

    public Quest_t(String Msg_id, Integer quest, QuestState State, WsMessageContext wsMessageContext) {
        if (Msg_id == null || quest == null || State == null || wsMessageContext == null)
            throw new RuntimeException("Quest_t参数不能为空");
        this.msg_id = Msg_id;
        this.Quest = quest;
        this.state = State;
        this.wsMessageContext = wsMessageContext;
    }

    public boolean check(Player player) {
        if (player_uid != -1) {
            if (player.getUid() != player_uid)
                return false;
        }
        GameQuest tmp = player.getQuestManager().getQuestById(Quest);
        if (tmp != null) {
            if (tmp.getState() == this.state) {
                JSONObject temp = new JSONObject();
                temp.put("type", "OnQuestChange");
                temp.put("msg_id", msg_id);
                temp.put("data", player.getUid());
                wsMessageContext.send(temp.toString());
                return true;
            }
        }
        return false;
    }

}
