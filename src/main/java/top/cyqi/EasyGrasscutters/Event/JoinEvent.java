package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.player.PlayerJoinEvent;
import emu.grasscutter.utils.EventConsumer;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

public class JoinEvent implements EventConsumer<PlayerJoinEvent> {

    @Override
    public void consume(PlayerJoinEvent playerJoinEvent) {

        Player player = playerJoinEvent.getPlayer();

        JSONObject temp = new JSONObject();
        //玩家没有角色，播放开始的天理过场动画
        temp.put("type", "OnPlayerJoin");
        temp.put("is_first", player.getAvatars().getAvatarCount() == 0);
        temp.put("data", player.getUid());
        WebSocketServer.ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(temp.toString()));

    }
}