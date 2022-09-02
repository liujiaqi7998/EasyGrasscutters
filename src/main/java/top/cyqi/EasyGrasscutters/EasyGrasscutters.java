package top.cyqi.EasyGrasscutters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.auth.DefaultAuthentication;
import emu.grasscutter.config.ConfigContainer;
import emu.grasscutter.plugin.Plugin;
import emu.grasscutter.plugin.api.ServerHook;
import emu.grasscutter.server.event.EventHandler;
import emu.grasscutter.server.event.HandlerPriority;
import emu.grasscutter.server.event.entity.EntityDeathEvent;
import emu.grasscutter.server.event.game.SendPacketEvent;
import emu.grasscutter.server.event.player.PlayerJoinEvent;
import emu.grasscutter.server.event.player.PlayerMoveEvent;
import emu.grasscutter.server.event.player.PlayerQuitEvent;
import emu.grasscutter.server.event.types.PlayerEvent;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.server.http.HttpServer;
import top.cyqi.EasyGrasscutters.Event.*;
import top.cyqi.EasyGrasscutters.ServerUtils.QConsoleListAppender;
import top.cyqi.EasyGrasscutters.utils.Utils;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;


public class EasyGrasscutters extends Plugin {

    public static Config config;
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private File configFile;
    private WebSocketServer webSocketServer;

    //注册玩家位置事件监听器
    EventHandler<PlayerMoveEvent> serverPositionEvent;
    //注册实体死亡监听器
    EventHandler<EntityDeathEvent> serverKillEntityEvent;
    //注册剧情改变监听器
    EventHandler<PlayerEvent> serverQuestEvent;
    //注册玩家进入监听器
    EventHandler<PlayerJoinEvent> serverJoinEvent;
    //注册玩家退出监听器
    EventHandler<PlayerQuitEvent> serverQuitEvent;
    //玩家经验监听器
    EventHandler<PlayerEvent> serverPlayerExpEvent;
    //玩家聊天监听器
    EventHandler<SendPacketEvent> serverChatEvent;
    //注册NPC对话监听器
    EventHandler<SendPacketEvent> serverNpcTalkEvent;

    public static EasyGrasscutters getInstance() {
        return (EasyGrasscutters) Grasscutter.getPluginManager().getPlugin("EasyGrasscutters");
    }

    @Override
    public void onEnable() {

        String pic_str = """
                ╭━━━╮          ╭━━━╮                ╭╮ ╭╮
                ┃╭━━╯          ┃╭━╮┃               ╭╯╰┳╯╰╮
                ┃╰━━┳━━┳━━┳╮ ╭╮┃┃╱╰╋━┳━━┳━━┳━━┳━━┳╮┣╮╭┻╮╭╋━━┳━┳━━╮
                ┃╭━━┫╭╮┃━━┫┃ ┃┃┃┃╭━┫╭┫╭╮┃━━┫━━┫╭━┫┃┃┃┃╱┃┃┃┃━┫╭┫━━┫
                ┃╰━━┫╭╮┣━━┃╰━╯┃┃╰┻━┃┃┃╭╮┣━━┣━━┃╰━┫╰╯┃╰╮┃╰┫┃━┫┃┣━━┃
                ╰━━━┻╯╰┻━━┻━╮╭╯╰━━━┻╯╰╯╰┻━━┻━━┻━━┻━━┻━╯╰━┻━━┻╯╰━━╯
                          ╭━╯┃
                          ╰━━╯""";
        System.out.println(pic_str);

        webSocketServer = new WebSocketServer();
        configFile = new File(getDataFolder().toPath() + "/config.json");
        if (!configFile.exists()) {
            try {
                Files.createDirectories(configFile.toPath().getParent());
            } catch (IOException e) {
                getLogger().error("Failed to create config.json");
            }
        }

        loadConfig();
        if (config.token == null || config.token.equals("")) {
            getLogger().info("[EasyGrasscutters] 未读取到配置文件，生成配置文件");
            config.token = Utils.generateRandomString(8);
        }
        saveConfig();
        getLogger().info("[EasyGrasscutters] 配置文件加载完成");

        //注册玩家位置事件监听器
        Registered_monitor();
        getLogger().info("[EasyGrasscutters] 事件监听器注册完成");

        //注册websocket服务
        webSocketServer.start();
        getLogger().info("[EasyGrasscutters] 启动成功！");
        getLogger().info("[EasyGrasscutters] 前端安装方法: https://flows.nodered.org/node/node-red-easy-grasscutters");
        getLogger().info("[EasyGrasscutters] 服务器地址: " + Utils.GetDispatchAddress() + "/easy/" + config.token);
        System.out.println("---------------------------------------");
    }

    public void Registered_monitor() {
        try {
            ListAppender<ILoggingEvent> listAppender = new QConsoleListAppender<>();
            listAppender.start();
            listAppender.setName("EasyGrasscuttersConsole");
            listAppender.start();
            Grasscutter.getLogger().addAppender(listAppender);
        } catch (Exception e) {
            getLogger().error("远程日志注册失败，可能会无法获取服务器日志：" + e.getMessage());
        }

        try {
            serverPositionEvent = new EventHandler<>(PlayerMoveEvent.class);
            serverPositionEvent.listener(new PositionEvent());
            serverPositionEvent.priority(HandlerPriority.NORMAL);
            serverPositionEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册玩家位置事件监听器出现错误，可能会导致玩家位置触发不可用：" + e.getMessage());
        }

        try {
            serverKillEntityEvent = new EventHandler<>(EntityDeathEvent.class);
            serverKillEntityEvent.listener(new KillEntityEvent());
            serverKillEntityEvent.priority(HandlerPriority.NORMAL);
            serverKillEntityEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册实体死亡监听器出现错误，可能会导致杀怪触发不可用：" + e.getMessage());
        }

        try {
            serverQuestEvent = new EventHandler<>(PlayerEvent.class);
            serverQuestEvent.listener(new QuestEvent());
            serverQuestEvent.priority(HandlerPriority.NORMAL);
            serverQuestEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册剧情改变监听器出现错误，可能会导致完成剧情触发不可用：" + e.getMessage());
        }

        try {
            serverJoinEvent = new EventHandler<>(PlayerJoinEvent.class);
            serverJoinEvent.listener(new JoinEvent());
            serverJoinEvent.priority(HandlerPriority.NORMAL);
            serverJoinEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册玩家进入监听器出现错误，可能会导致玩家进入触发不可用：" + e.getMessage());
        }

        try {
            serverQuitEvent = new EventHandler<>(PlayerQuitEvent.class);
            serverQuitEvent.listener(new QuitEvent());
            serverQuitEvent.priority(HandlerPriority.NORMAL);
            serverQuitEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册玩家退出监听器出现错误，可能会导致玩家退出触发不可用：" + e.getMessage());
        }

        try {
            serverPlayerExpEvent = new EventHandler<>(PlayerEvent.class);
            serverPlayerExpEvent.listener(new PlayerExpEvent());
            serverPlayerExpEvent.priority(HandlerPriority.NORMAL);
            serverPlayerExpEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册玩家经验监听器出现错误，可能会导致玩家经验触发不可用：" + e.getMessage());
        }

        //注册玩家聊天监听器
        try {
            serverChatEvent = new EventHandler<>(SendPacketEvent.class);
            serverChatEvent.listener(new ChatEvent());
            serverChatEvent.priority(HandlerPriority.NORMAL);
            serverChatEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册玩家聊天监听器出现错误，可能会导致玩家聊天触发不可用：" + e.getMessage());
        }

        //注册NPC对话监听器
        try {
            serverNpcTalkEvent = new EventHandler<>(SendPacketEvent.class);
            serverNpcTalkEvent.listener(new NpcTalkEvent());
            serverNpcTalkEvent.priority(HandlerPriority.NORMAL);
            serverNpcTalkEvent.register(this);
        } catch (Exception e) {
            getLogger().error("注册NPC对话监听器出现错误，可能会导致玩家聊天触发不可用：" + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        Grasscutter.setAuthenticationSystem(new DefaultAuthentication());
        webSocketServer.stop();
    }

    public void loadConfig() {
        try (FileReader file = new FileReader(configFile)) {
            config = gson.fromJson(file, Config.class);
            saveConfig();
        } catch (Exception e) {
            config = new Config();
            saveConfig();
        }
    }

    public void saveConfig() {
        try (FileWriter file = new FileWriter(configFile)) {
            file.write(gson.toJson(config));
        } catch (Exception e) {
            getLogger().error("无法保存配置文件!" + e.getMessage());
        }
    }

    public static GameServer getGameServer() {
        return EasyGrasscutters.getInstance().getServer();
    }

    public static ConfigContainer getServerConfig() {
        return Grasscutter.getConfig();
    }

    public static HttpServer getDispatchServer() {
        return ServerHook.getInstance().getHttpServer();
    }
}
