package samples.easychatroom;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.easyremote.Remote;
import org.webbitserver.easyremote.Server;

import java.util.HashSet;
import java.util.Set;

public class ChatServer implements Server<ChatClient> {

    public static final String USERNAME_KEY = "username";

    private Set<ChatClient> clients = new HashSet<ChatClient>();

    @Override
    public void onOpen(WebSocketConnection connection, ChatClient client) throws Exception {
        clients.add(client);
    }

    @Override
    public void onClose(WebSocketConnection connection, ChatClient client) throws Exception {
        String username = (String) connection.data(USERNAME_KEY);
        if (username != null) {
            for (ChatClient other : clients) {
                other.leave(username);
            }
        }
        clients.remove(client);
    }

    @Remote
    public void login(WebSocketConnection connection, String username) {
        connection.data(USERNAME_KEY, username); // associate username with connection

        for (ChatClient other : clients) {
            other.join(username);
        }
    }

    @Remote
    public void say(WebSocketConnection connection, String message) {
        String username = (String) connection.data(USERNAME_KEY);
        if (username != null) {
            for (ChatClient other : clients) {
                other.say(username, message);
            }
        }
    }

}