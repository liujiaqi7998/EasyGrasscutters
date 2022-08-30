package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.server.event.entity.EntityDeathEvent;
import emu.grasscutter.utils.EventConsumer;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

import java.util.HashMap;
import java.util.Map;


public class KillEntityEvent implements EventConsumer<EntityDeathEvent> {

    //º‡Ã˝ µÃÂID±Ì
    public static Map<String, Integer> All_Entity = new HashMap<>();


    @Override
    public void consume(EntityDeathEvent entityDeathEvent) {
        for (String key : All_Entity.keySet()) {
            int Entity = All_Entity.get(key);
            if (entityDeathEvent.getEntity().getId() == Entity) {
                JSONObject temp = new JSONObject();
                temp.put("type", "OnKillEntity");
                temp.put("msg_id", key);
                temp.put("data", entityDeathEvent.getKiller().getId());
                WebSocketServer.ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(temp.toString()));
            }
        }
    }
}