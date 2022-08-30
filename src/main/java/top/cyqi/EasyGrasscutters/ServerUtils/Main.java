package top.cyqi.EasyGrasscutters.ServerUtils;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.GadgetData;
import emu.grasscutter.data.excels.ItemData;
import emu.grasscutter.data.excels.MonsterData;
import emu.grasscutter.game.entity.EntityItem;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.EntityVehicle;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.utils.Position;
import io.javalin.websocket.WsMessageContext;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.Event.KillEntityEvent;
import top.cyqi.EasyGrasscutters.Event.PositionEvent;
import top.cyqi.EasyGrasscutters.Event.QuestEvent;

import static emu.grasscutter.Grasscutter.getLogger;
import static emu.grasscutter.config.Configuration.GAME_OPTIONS;
import static top.cyqi.EasyGrasscutters.EasyGrasscutters.getGameServer;

public class Main {

    public static void DealMessage(@NotNull JSONObject object, WsMessageContext wsMessageContext) {

        String type = object.getString("type");
        Player player = null;
        String player_uid = "";
        if (object.has("player_uid")) {
            player_uid = object.getString("player_uid");
            player = getGameServer().getPlayerByUid(Integer.parseInt(player_uid), false);
        }


        switch (type) {
            case "CMD" -> {
                /*
                 * { "type":"CMD","执行的命令":"help","player_uid":"执行者uid(不存在则是在控制台执行)","msg_id":"随机数" }
                 * */
                String rawMessage = object.getString("cmd");
                if (object.has("player_uid")) {
                    if (player == null) {
                        JSONObject temp = new JSONObject();
                        temp.put("type", "error");
                        temp.put("msg_id", object.getString("msg_id"));
                        temp.put("data", "玩家不在线");
                        wsMessageContext.send(temp.toString());
                        return;
                    }
                    QMessageHandler resultCollector = new QMessageHandler();
                    resultCollector.wsMessageContext = wsMessageContext;
                    resultCollector.player = player;
                    resultCollector.msg_id = object.getString("msg_id");
                    resultCollector.player_uid = player_uid;
                    player.setMessageHandler(resultCollector);
                    ExecuteCommand(player, rawMessage);
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

                if (player == null) {
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
                temp.put("X", player.getPlayerLocationInfo().getPos().getX());
                temp.put("Y", player.getPlayerLocationInfo().getPos().getY());
                temp.put("Z", player.getPlayerLocationInfo().getPos().getZ());
                temp.put("scene", player.getSceneId());
                temp.put("player_uid", player_uid);
                wsMessageContext.send(temp.toString());
            }

            case "GetPlayerBirthday" -> {
                player = getGameServer().getPlayerByUid(Integer.parseInt(player_uid), true);
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
                temp.put("player_uid", player_uid);
                wsMessageContext.send(temp.toString());
            }

            case "QuestAction" -> {
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
                temp.put("data", que != null);
                temp.put("player_uid", player_uid);
                wsMessageContext.send(temp.toString());
            }

            case "QuestFinish" -> {
                if (player == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不在线");
                    wsMessageContext.send(temp.toString());
                    return;
                }
                GameQuest quest = player.getQuestManager().addQuest(object.getInt("Quest_id"));
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
                temp.put("player_uid", player_uid);
                wsMessageContext.send(temp.toString());
            }

            case "OnPosition" -> {
                String msg_id = object.getString("msg_id");

                if (object.has("del")) {
                    PositionEvent.All_position.remove(msg_id);
                    PositionEvent.All_radius.remove(msg_id);
                    PositionEvent.All_sceneId.remove(msg_id);
                    return;
                }

                float X = object.getFloat("X");
                float Y = object.getFloat("Y");
                float Z = object.getFloat("Z");
                int scene = object.getInt("scene");
                float R = object.getFloat("R");

                Position position = new Position(X, Y, Z);

                PositionEvent.All_radius.put(msg_id, R);
                PositionEvent.All_sceneId.put(msg_id, scene);
                PositionEvent.All_position.put(msg_id, position);
            }
            case "ChangePosition" -> {
                if (player == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不在线");
                    wsMessageContext.send(temp.toString());
                    return;
                }

                float X = object.getFloat("X");
                float Y = object.getFloat("Y");
                float Z = object.getFloat("Z");
                int scene = object.getInt("scene");
                Position position = new Position(X, Y, Z);

                boolean result = player.getWorld().transferPlayerToScene(player, scene, position);

                JSONObject temp = new JSONObject();
                temp.put("type", "QuestFinish");
                temp.put("msg_id", object.getString("msg_id"));
                temp.put("data", result);
                temp.put("player_uid", player_uid);
                wsMessageContext.send(temp.toString());
            }
            case "OnKillEntity" -> {
                String msg_id = object.getString("msg_id");
                if (object.has("del")) {
                    KillEntityEvent.All_Entity.remove(msg_id);
                    return;
                }
                int Entity_id = object.getInt("Entity");
                KillEntityEvent.All_Entity.put(msg_id, Entity_id);
            }
            case "CreateEntity" -> {
                if (player == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "玩家不在线");
                    wsMessageContext.send(temp.toString());
                    return;
                }
                int id = object.getInt("id");
                int amount = object.getInt("amount");
                int level = object.getInt("level");
                MonsterData monsterData = GameData.getMonsterDataMap().get(id);
                GadgetData gadgetData = GameData.getGadgetDataMap().get(id);
                ItemData itemData = GameData.getItemDataMap().get(id);
                if (monsterData == null && gadgetData == null && itemData == null) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "无效实体ID");
                    wsMessageContext.send(temp.toString());
                    return;
                }
                Scene scene = player.getScene();
                if (scene.getEntities().size() + amount > GAME_OPTIONS.sceneEntityLimit) {
                    amount = Math.max(Math.min(GAME_OPTIONS.sceneEntityLimit - scene.getEntities().size(), amount), 0);
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", "当前场景实体数量超过最大值:" + amount);
                    wsMessageContext.send(temp.toString());
                    if (amount <= 0) {
                        return;
                    }
                }
                Position center = (player.getPosition());
                double maxRadius = Math.sqrt(amount * 0.2 / Math.PI);
                for (int i = 0; i < amount; i++) {
                    Position pos = GetRandomPositionInCircle(center, maxRadius).addY(3);
                    GameEntity entity = null;
                    if (itemData != null) {
                        entity = new EntityItem(scene, null, itemData, pos, 1, true);
                    }
                    if (gadgetData != null) {
                        pos.addY(-3);
                        entity = new EntityVehicle(scene, player, id, 0, pos, player.getRotation());
                    }
                    if (monsterData != null) {
                        entity = new EntityMonster(scene, monsterData, pos, level);
                    }

                    scene.addEntity(entity);
                    JSONObject temp = new JSONObject();
                    temp.put("type", "CreateEntity");
                    temp.put("msg_id", object.getString("msg_id"));
                    temp.put("data", entity.getId());
                    temp.put("player_uid", player_uid);
                    wsMessageContext.send(temp.toString());
                }
            }

            case "OnQuestChange" -> {
                String msg_id = object.getString("msg_id");
                int id = object.getInt("id");
                if (object.has("del")) {
                    QuestEvent.All_Quest.remove(id);
                    QuestEvent.All_state.remove(id);
                    return;
                }

                QuestState state;
                String state_str = object.getString("state");
                switch (state_str) {
                    case "UNSTARTED" -> state = QuestState.QUEST_STATE_UNSTARTED;
                    case "UNFINISHED" -> state = QuestState.QUEST_STATE_UNFINISHED;
                    case "FINISHED" -> state = QuestState.QUEST_STATE_FINISHED;
                    case "FAILED" -> state = QuestState.QUEST_STATE_FAILED;
                    default -> state = QuestState.QUEST_STATE_NONE;
                }
                QuestEvent.All_Quest.put(id, msg_id);
                QuestEvent.All_state.put(id, state);
            }
        }
    }

    private static Position GetRandomPositionInCircle(Position origin, double radius) {
        Position target = origin.clone();
        double angle = Math.random() * 360;
        double r = Math.sqrt(Math.random() * radius * radius);
        target.addX((float) (r * Math.cos(angle))).addZ((float) (r * Math.sin(angle)));
        return target;
    }

    public static void ExecuteCommand(Player player, String data) {
        try {
            CommandMap commandMap = Grasscutter.getCommandMap();
            commandMap.invoke(player, player, data);
        } catch (Exception e) {
            getLogger().info("[EasyGrasscutters] 执行命令:" + data + "发生错误:" + e.getMessage());
        }
    }
}
