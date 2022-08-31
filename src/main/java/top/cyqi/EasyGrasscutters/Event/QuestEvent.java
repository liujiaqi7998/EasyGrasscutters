package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.types.PlayerEvent;
import emu.grasscutter.utils.EventConsumer;
import top.cyqi.EasyGrasscutters.Event.content.Quest_t;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestEvent implements EventConsumer<PlayerEvent> {


    public static Map<String, Quest_t> All_Quest = new HashMap<>();

    @Override
    public void consume(PlayerEvent playerEvent) {
        Player player = playerEvent.getPlayer();

        for (String key : All_Quest.keySet()) {
            Quest_t Quest_a = All_Quest.get(key);
            if (Quest_a.check(player)) {
                All_Quest.remove(key);
            }
        }
    }

    public static void delete(String msg_id) {
        for (String key : All_Quest.keySet()) {
            Quest_t Quest_a = All_Quest.get(key);
            if (Quest_a.msg_id.equals(msg_id)) {
                All_Quest.remove(key);
            }

        }
    }

    public static void add_QuestEvent(Quest_t Quest_a) {
        String uuid = UUID.randomUUID().toString();
        All_Quest.put(uuid, Quest_a);
    }

}
