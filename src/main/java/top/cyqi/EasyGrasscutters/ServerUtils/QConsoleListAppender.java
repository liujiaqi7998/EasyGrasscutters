package top.cyqi.EasyGrasscutters.ServerUtils;

import ch.qos.logback.core.read.ListAppender;
import org.json.JSONObject;
import top.cyqi.EasyGrasscutters.websocket.WebSocketServer;

import java.util.ArrayList;

public class QConsoleListAppender<ILoggingEvent> extends ListAppender<ILoggingEvent> {
    public ArrayList<ILoggingEvent> list = new ArrayList<>();

    public QConsoleListAppender() {
    }

    protected void append(ILoggingEvent e) {
        JSONObject temp = new JSONObject();
        temp.put("type", "log_return");
        temp.put("data", e.toString());
        WebSocketServer.ClientContextMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> session.send(temp.toString()));
        this.list.add(e);
    }
}
