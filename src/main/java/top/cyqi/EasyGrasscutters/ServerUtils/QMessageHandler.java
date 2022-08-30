package top.cyqi.EasyGrasscutters.ServerUtils;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.utils.MessageHandler;
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

public class QMessageHandler extends MessageHandler {

    public WsMessageContext wsMessageContext;
    public Player player;
    public String msg_id;
    public String player_uid;

    public void append(String message) {
        JSONObject temp = new JSONObject();
        temp.put("type", "cmd_return");
        temp.put("msg_id", msg_id);
        temp.put("data", message);
        temp.put("player_uid", player_uid);
        wsMessageContext.send(temp.toString());
        player.setMessageHandler(null);
    }

}
