package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.server.event.types.PlayerEvent;
import emu.grasscutter.utils.EventConsumer;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

import java.util.HashMap;
import java.util.Map;

public class QuestEvent implements EventConsumer<PlayerEvent> {

    //¼àÌý¾çÇé
    public static Map<Integer, String> All_Quest = new HashMap<>();
    //¾çÇéÔ¤ÆÚ×´Ì¬
    public static Map<Integer, QuestState> All_state = new HashMap<>();

    @Override
    public void consume(PlayerEvent playerEvent) {
        Player player = playerEvent.getPlayer();
        for (int key : All_Quest.keySet()) {
            GameQuest tmp = player.getQuestManager().getQuestById(key);
            if (tmp != null) {
                QuestState state = All_state.get(key);
                if (tmp.getState() == state) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "OnQuestChange");
                    temp.put("msg_id", All_Quest.get(key));
                    temp.put("data", player.getUid());
                    WebSocketServer.ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(temp.toString()));
                    All_Quest.remove(key);
                    All_state.remove(key);
                }

            }
        }


    }
}
