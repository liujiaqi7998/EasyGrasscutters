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
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.server.http.HttpServer;
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
            getLogger().error("Զ����־ע��ʧ�ܣ����ܻ��޷���ȡ��������־��" + e.getMessage());
        }


        webSocketServer.start();
        getLogger().info("Enabled �����ɹ���");
        getLogger().info("��������ַ:" + Utils.GetDispatchAddress() + "/easy/" + config.token);
    }

    @Override
    public void onDisable() {
        Grasscutter.setAuthenticationSystem(new DefaultAuthentication());
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