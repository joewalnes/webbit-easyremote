package samples.easychatroom;

import org.webbitserver.easyremote.Client;
import org.webbitserver.easyremote.Remote;

@Remote
interface ChatClient extends Client {

    void say(String username, String message);

    void leave(String username);

    void join(String username);

}
