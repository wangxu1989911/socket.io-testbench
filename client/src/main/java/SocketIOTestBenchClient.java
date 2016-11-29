import io.socket.client.Ack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketIOTestBenchClient {
    private static final String USER_ID = "userId";
    
    private static final String JOIN_ROOM = "joinRoom";
    private static final String ROOM_ID = "roomId";
    
    private static final String MESSAGE = "message";
    private static final String CONTENT = "content";
    
    private static final int USER_NUMBER = 300;
    private static List<User> userList = new ArrayList<>();
    
    public static void main (String[] args){
        System.out.println(String.format("Create %s of users...", USER_NUMBER));
        for (int i=0; i<USER_NUMBER; i++){
            User user = new User(i);
            userList.add(user);
        }
    
        System.out.println("Sleep 10s...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Join Room...");
        for (int i=0; i<USER_NUMBER; i++){
            User user=userList.get(i);
            new Thread(() -> {
                Map<String, String> message = new HashMap<>();
                message.put(ROOM_ID, "test");
                user.getSocket().emit(JOIN_ROOM, message);
            }).start();
        }
    
        System.out.println("Sleep 10s...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    
        
        System.out.println("Send message...");
        for (int i=0; i<USER_NUMBER; i=i+1){
            User user=userList.get(i);
            new Thread(() -> {
                user.getSentCounter().incrementAndGet();
                Map<String, String> message = new HashMap<>();
                message.put(ROOM_ID, "test");
                message.put(CONTENT, String.format("User %s send.",
                        user.getUserId()));
                final long startTime=System.currentTimeMillis();
                user.getSocket().emit(MESSAGE, message, (Ack) res -> {
                    if (res[0] != null){
                        System.out.println(String.format("Message Error: %s",
                                res[0]));
                        return;
                    }
        
                    long endTime = System.currentTimeMillis();
                    long roundTime = endTime - startTime;
                    user.getTimer().addAndGet(roundTime);
                });
            }).start();
        }
        
        System.out.println("Sleep 30s...");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        long sentAll = 0;
        long receivedAll = 0;
        long timeAll = 0;
        for (int i=0; i<USER_NUMBER; i++){
            sentAll += userList.get(i).getSentCounter().intValue();
            receivedAll += userList.get(i).getReceivedCounter().intValue();
            timeAll += userList.get(i).getTimer().longValue();
        }
    
        System.out.println("Sent " + sentAll);
        System.out.println("Received " + receivedAll);
        long expect = sentAll * (USER_NUMBER - 1);
        if (receivedAll == expect){
            System.out.println("All message received.");
        } else {
            System.out.println("Missing " + (expect - receivedAll));
        }
        
        System.out.println("Round trip time: " + (timeAll/sentAll));
    
        for (int i=0; i<USER_NUMBER; i++){
            userList.get(i).closeSocket();
        }
    }
}
