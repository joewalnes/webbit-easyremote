package samples.easychatroom;

import org.webbitserver.WebServer;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.handler.logging.LoggingHandler;
import org.webbitserver.handler.logging.SimpleLogSink;

import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.easyremote.Magic.magic;

public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9877)
                .add(new LoggingHandler(new SimpleLogSink(ChatServer.USERNAME_KEY)))
                .add("/chatsocket", magic(ChatClient.class, new ChatServer()))
                .add(new StaticFileHandler("./src/test/java/samples/easychatroom/content"))
                .add(new StaticFileHandler("./src/main/java/org/webbitserver/easyremote"))
                .start();

        System.out.println("Chat room running on: " + webServer.getUri());
    }

}