import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

class User implements Runnable{
    private static Logger logger = Logger.getLogger(User.class.getName());
    
    private static final String HOST = "http://10.53.18.99:6666";
    
    private static final String USER_ID = "userId";
    
    private int userId;
    private Socket socket;
    private AtomicInteger sentCounter = new AtomicInteger(0);
    private AtomicInteger receivedCounter = new AtomicInteger(0);
    private AtomicLong timer = new AtomicLong(0);
    
    User(int userId) {
        this.userId = userId;
        Map<String, String> query = new HashMap<>();
        query.put(USER_ID, String.valueOf(userId));
        
        IO.Options opts = new IO.Options();
        opts.query = new JSONObject(query).toString();
        try {
            socket = IO.socket(HOST, opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    
        socket.on(Socket.EVENT_CONNECT, args -> {
            logger.info(String.format("User %s has connected to socket.",
                    userId));
        }).on(Socket.EVENT_CONNECT_ERROR, args -> {
            logger.warning(String.format("Connect Error: %s", args[0]));
        }).on(Socket.EVENT_RECONNECT, args -> {
            logger.warning(String.format("User %s has reconnected to socket.",
                    userId));
        }).on(Socket.EVENT_RECONNECT_ERROR, args -> {
            logger.warning(String.format("Reconnect Error: %s", args[0]));
        }).on(Socket.EVENT_DISCONNECT, args -> {
            /*logger.info(String.format("User %s has disconnected from socket.",
                    userId));*/
        }).on(Socket.EVENT_ERROR, args -> {
            logger.warning(String.format("Error: %s", args[0]));
        }).on(Socket.EVENT_MESSAGE, args -> {
            receivedCounter.incrementAndGet();
        });
        socket.open();
    }
    
    public void run() {
        
        
    }
    
    public void closeSocket(){
        socket.close();
    }
    
    public int getUserId() {
        return userId;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    AtomicInteger getSentCounter() {
        return sentCounter;
    }
    
    AtomicInteger getReceivedCounter() {
        return receivedCounter;
    }
    
    AtomicLong getTimer() {
        return timer;
    }
}
