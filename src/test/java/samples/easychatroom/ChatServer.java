package samples.easychatroom;

import org.webbitserver.easyremote.Remote;
import org.webbitserver.easyremote.Server;

import java.util.HashSet;
import java.util.Set;

public class ChatServer implements Server<ChatClient> {

    public static final String USERNAME_KEY = "username";

    private Set<ChatClient> clients = new HashSet<ChatClient>();

    @Override
    public void onOpen(ChatClient client) throws Exception {
        clients.add(client);
    }

    @Override
    public void onClose(ChatClient client) throws Exception {
        String username = (String) client.connection().data(USERNAME_KEY);
        if (username != null) {
            for (ChatClient other : clients) {
                other.leave(username);
            }
        }
        clients.remove(client);
    }

    @Remote
    public void login(ChatClient client, String username) {
        client.connection().data(USERNAME_KEY, username); // associate username with connection

        for (ChatClient other : clients) {
            other.join(username);
        }
    }

    @Remote
    public void say(ChatClient client, String message) {
        String username = (String) client.connection().data(USERNAME_KEY);
        if (username != null) {
            for (ChatClient other : clients) {
                other.say(username, message);
            }
        }
    }

}