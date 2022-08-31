package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.player.PlayerMoveEvent;
import emu.grasscutter.utils.EventConsumer;
import emu.grasscutter.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

import java.util.HashMap;
import java.util.Map;

public class PositionEvent implements EventConsumer<PlayerMoveEvent> {

    //坐标表
    public static Map<String, Position> All_position = new HashMap<>();
    //半径表
    public static Map<String, Float> All_radius = new HashMap<>();
    //场景表
    public static Map<String, Integer> All_sceneId = new HashMap<>();

    @Override
    public void consume(PlayerMoveEvent playerMoveEvent) {
        Player player = playerMoveEvent.getPlayer();
        for (String key : All_position.keySet()) {

            int sceneId = All_sceneId.get(key);
            if (player.getSceneId() != sceneId) {
                return;
            }

            Position position = All_position.get(key);
            Float radius = All_radius.get(key);
            if (getDistance(position, playerMoveEvent.getDestination()) <= radius) {
                JSONObject temp = new JSONObject();
                temp.put("type", "OnPosition");
                temp.put("msg_id", key);
                temp.put("data", player.getUid());
                WebSocketServer.ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(temp.toString()));
                All_position.remove(key);
                All_radius.remove(key);
                All_sceneId.remove(key);
            }
        }

    }

    // 两点间距离公式
    public static float getDistance(@NotNull Position p1, @NotNull Position p2) {
        return (float) Math.sqrt(Math.abs((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY())));
    }
}