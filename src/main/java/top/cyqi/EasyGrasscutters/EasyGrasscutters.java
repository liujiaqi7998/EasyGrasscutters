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
import emu.grasscutter.server.event.player.PlayerMoveEvent;
import emu.grasscutter.server.event.types.PlayerEvent;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.server.http.HttpServer;
import top.cyqi.EasyGrasscutters.Event.KillEntityEvent;
import top.cyqi.EasyGrasscutters.Event.PositionEvent;
import top.cyqi.EasyGrasscutters.Event.QuestEvent;
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

    public static EasyGrasscutters getInstance() {
        return (EasyGrasscutters) Grasscutter.getPluginManager().getPlugin("EasyGrasscutters");
    }

    @Override
    public void onEnable() {
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
            config.token = Utils.generateRandomString(8);
        }

        saveConfig();

        try {
            ListAppender<ILoggingEvent> listAppender = new QConsoleListAppender<>();
            listAppender.start();
            listAppender.setName("EasyGrasscuttersConsole");
            listAppender.start();
            Grasscutter.getLogger().addAppender(listAppender);
        } catch (Exception e) {
            getLogger().error("远程日志注册失败，可能会无法获取服务器日志：" + e.getMessage());
        }

        serverPositionEvent = new EventHandler<>(PlayerMoveEvent.class);
        serverPositionEvent.listener(new PositionEvent());
        serverPositionEvent.priority(HandlerPriority.NORMAL);
        serverPositionEvent.register(this);

        serverKillEntityEvent = new EventHandler<>(EntityDeathEvent.class);
        serverKillEntityEvent.listener(new KillEntityEvent());
        serverKillEntityEvent.priority(HandlerPriority.NORMAL);
        serverKillEntityEvent.register(this);


        serverQuestEvent = new EventHandler<>(PlayerEvent.class);
        serverQuestEvent.listener(new QuestEvent());
        serverQuestEvent.priority(HandlerPriority.NORMAL);
        serverQuestEvent.register(this);

        webSocketServer.start();
        getLogger().info("Enabled 启动成功！");
        getLogger().info("服务器地址:" + Utils.GetDispatchAddress() + "/easy/" + config.token);
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
            getLogger().error("Unable to save config file.");
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
