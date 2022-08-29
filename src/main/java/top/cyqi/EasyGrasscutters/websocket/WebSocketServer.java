package top.cyqi.EasyGrasscutters.websocket;

import emu.grasscutter.Grasscutter;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.EasyGrasscutters;
import top.cyqi.EasyGrasscutters.ServerUtils.Main;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServer {

    //用于保存所有的连接的的Map
    public static Map<WsContext, String> ClientContextMap = new ConcurrentHashMap<>();
    Javalin app = EasyGrasscutters.getDispatchServer().getHandle();

    public void start() {

        app.ws("/easy/" + EasyGrasscutters.config.token, ws -> {
            ws.onConnect(ctx -> {
                String ws_id = UUID.randomUUID().toString();
                ClientContextMap.put(ctx, ws_id);
                Grasscutter.getLogger().info("[EasyGrasscutters] 连接到服务器，ID:" + ws_id);
            });

            ws.onMessage(wsMessageContext -> {
                //获取消息
                String Ws_Msg = wsMessageContext.message();

                JSONObject object;

                try {
                    object = new JSONObject(Ws_Msg);
                } catch (Exception e) {
                    JSONObject temp = new JSONObject();
                    temp.put("type", "error");
                    temp.put("data", "json error");
                    wsMessageContext.send(temp);
                    Grasscutter.getLogger().error("[EasyGrasscutters] 异常数据:" + Ws_Msg + ",问题:" + e.getMessage());
                    return;
                }

                //调用核心处理函数
                Main.DealMessage(object, wsMessageContext);

            });

            ws.onClose(ctx -> {
                String ws_id = ClientContextMap.get(ctx);
                ClientContextMap.remove(ctx);
                Grasscutter.getLogger().info("[EasyGrasscutters] 连接断开，ID：" + ws_id);
            });
        });
    }


    public void stop() {
        //清除网页控制台的用户列表，关闭连接
        ClientContextMap.clear();
    }

    public void broadcast(JSONObject data) {
        ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(data));
    }
}
