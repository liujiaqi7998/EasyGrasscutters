package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.types.PlayerEvent;
import emu.grasscutter.utils.EventConsumer;
import top.cyqi.EasyGrasscutters.Event.content.PlayerExp_t;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerExpEvent implements EventConsumer<PlayerEvent> {


    public static Map<String, PlayerExp_t> All_PlayerExpEvent = new HashMap<>();

    @Override
    public void consume(PlayerEvent playerEvent) {
        Player player = playerEvent.getPlayer();

        for (String key : All_PlayerExpEvent.keySet()) {
            PlayerExp_t PlayerExp_a = All_PlayerExpEvent.get(key);
            if (PlayerExp_a.check(player)) {
                All_PlayerExpEvent.remove(key);
            }
        }
    }

    public static void delete(String msg_id) {
        for (String key : All_PlayerExpEvent.keySet()) {
            PlayerExp_t PlayerExp_a = All_PlayerExpEvent.get(key);
            if (PlayerExp_a.msg_id.equals(msg_id)) {
                All_PlayerExpEvent.remove(key);
            }

        }
    }

    public static void add_PlayerExpEvent(PlayerExp_t PlayerExp_a) {
        String uuid = UUID.randomUUID().toString();
        All_PlayerExpEvent.put(uuid, PlayerExp_a);
    }

}
