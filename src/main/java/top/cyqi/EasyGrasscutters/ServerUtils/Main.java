package top.cyqi.EasyGrasscutters.ServerUtils;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

import static top.cyqi.EasyGrasscutters.EasyGrasscutters.getGameServer;

public class Main {

    public static void DealMessage(JSONObject object, WsMessageContext wsMessageContext) {

        String type = object.getString("type");

        switch (type) {
            case "CMD" -> {
                /*
                 * { "type":"CMD","执行的命令":"help","player_uid":"执行者uid(不存在则是在控制台执行)","msg_id":"随机数" }
                 * */
                String rawMessage = object.getString("cmd");
                if (object.has("player_uid")) {
                    String player_uid = object.getString("player_uid");

                    Player tmp = getGameServer().getPlayers().get(Integer.valueOf(player_uid));
                    if (tmp == null) {
                        JSONObject temp = new JSONObject();
                        temp.put("type", "error");
                        temp.put("msg_id", object.getString("msg_id"));
                        temp.put("data", "玩家不在线");
                        wsMessageContext.send(temp.toString());
                        return;
                    }
                    QMessageHandler resultCollector = new QMessageHandler();
                    resultCollector.wsMessageContext = wsMessageContext;
                    resultCollector.player = tmp;
                    resultCollector.msg_id = object.getString("msg_id");
                    tmp.setMessageHandler(resultCollector);
                    ExecuteCommand(tmp, rawMessage);
                } else {
                    ExecuteCommand(null, rawMessage);
                }
            }
            case "GetPlayerNum" -> {
                int number = getGameServer().getPlayers().size();
                JSONObject temp = new JSONObject();
                temp.put("type", "GetPlayerNum");
                temp.put("msg_id", object.getString("msg_id"));
                temp.put("data", number);
                wsMessageContext.send(temp.toString());
            }
            case "GetPlayerLocation" -> {
                String player_uid = object.getString("player_uid");
                Player tmp = getGameServer().getPlayers().get(Integer.valueOf(player_uid));
                if (tmp == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不在线");
                    wsMessageContext.send(temp.toString());
                    return;
                }

                JSONObject temp = new JSONObject();
                temp.put("type", "GetPlayerLocation");
                temp.put("msg_id", object.getString("msg_id"));
                temp.put("X", tmp.getPlayerLocationInfo().getPos().getX());
                temp.put("Y", tmp.getPlayerLocationInfo().getPos().getY());
                temp.put("Z", tmp.getPlayerLocationInfo().getPos().getZ());
                temp.put("scene", tmp.getSceneId());
                wsMessageContext.send(temp.toString());
            }

            case "GetPlayerBirthday" -> {
                String player_uid = object.getString("player_uid");
                Player player = getGameServer().getPlayerByUid(Integer.parseInt(player_uid), true);
                if (player == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不存在");
                    wsMessageContext.send(temp.toString());
                    return;
                }
                JSONObject temp = new JSONObject();
                temp.put("type", "GetPlayerBirthday");
                temp.put("msg_id", object.getString("msg_id"));
                temp.put("Month", player.getBirthday().getMonth());
                temp.put("Day", player.getBirthday().getDay());
                wsMessageContext.send(temp.toString());
            }

            case "QuestAction" -> {
                String player_uid = object.getString("player_uid");
                Player player = getGameServer().getPlayerByUid(Integer.parseInt(player_uid), false);
                if (player == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不在线");
                    wsMessageContext.send(temp.toString());
                    return;
                }
                GameQuest que = player.getQuestManager().addQuest(object.getInt("Quest_id"));
                JSONObject temp = new JSONObject();
                temp.put("type", "QuestAction");
                temp.put("msg_id", object.getString("msg_id"));
                if (que != null) {
                    temp.put("data", true);
                } else {
                    temp.put("data", false);
                }
                wsMessageContext.send(temp.toString());
            }

            case "QuestFinish" -> {
                String player_uid = object.getString("player_uid");
                Grasscutter.getLogger().info("1");
                Player player = getGameServer().getPlayerByUid(Integer.parseInt(player_uid), false);
                if (player == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不在线");
                    wsMessageContext.send(temp.toString());
                    return;
                }
                Grasscutter.getLogger().info("2");
                GameQuest quest = player.getQuestManager().addQuest(object.getInt("Quest_id"));
                Grasscutter.getLogger().info("3");
                if (quest == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不存在该剧情");
                    wsMessageContext.send(temp.toString());
                    return;
                }
                quest.finish();
                JSONObject temp = new JSONObject();
                temp.put("type", "QuestFinish");
                temp.put("msg_id", object.getString("msg_id"));
                temp.put("data", true);
                wsMessageContext.send(temp.toString());
            }
        }
    }

    public static void ExecuteCommand(Player player, String data) {
        try {
            CommandMap commandMap = Grasscutter.getCommandMap();
            commandMap.invoke(player, player, data);
        } catch (Exception e) {
            Grasscutter.getLogger().info("[EasyGrasscutters] 执行命令:" + data + "发生错误:" + e.getMessage());
        }
    }
}
