package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.player.PlayerMoveEvent;
import emu.grasscutter.utils.EventConsumer;
import top.cyqi.EasyGrasscutters.Event.content.position_t;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PositionEvent implements EventConsumer<PlayerMoveEvent> {

    //±í
    public static Map<String, position_t> All_position = new HashMap<>();


    @Override
    public void consume(PlayerMoveEvent playerMoveEvent) {
        Player player = playerMoveEvent.getPlayer();

        for (String key : All_position.keySet()) {
            position_t position_a = All_position.get(key);
            if (position_a.check(playerMoveEvent.getDestination(), player)) {
                All_position.remove(key);
            }
        }
    }

    public static void delete(String msg_id) {
        for (String key : All_position.keySet()) {
            position_t position_a = All_position.get(key);
            if (position_a.msg_id.equals(msg_id)) {
                All_position.remove(key);
            }
        }
    }

    public static void add_position(position_t position_a) {
        String uuid = UUID.randomUUID().toString();
        All_position.put(uuid, position_a);
    }

}