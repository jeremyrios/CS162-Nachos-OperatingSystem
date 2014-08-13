package edu.berkeley.cs162;


import org.junit.Test;
import static org.junit.Assert.*;

public class CorrectExceptionIntTests extends BaseTest{
    /**
    @Test
    public final void sendDataErrorTest() throws Exception {
        try {
            startServer();
            KVClient client = new KVClient(host,8081);
            KVMessage message = new KVMessage("putreq");
            Socket socket = new Socket()
            message.sendMessage()
        } catch (KVException e) {
            assertEquals(e.getMsg().getMessage(), KVMessage.makeResponse(KVMessage.ResponseType.DATA_SEND_ERROR));
        } finally {
            stopServer();
        }
    }**/
    
    @Test
    public final void createSocketErrorTest() throws Exception {
        try {
            startServer();
            KVClient client = new KVClient(host,8081);
            client.put("FOO","BAR");
        } catch (KVException e) {
            assertEquals(KVMessage.ResponseType.SOCKET_ERROR.toString(), e.getMsg().getMessage());
        } finally {
            stopServer();
        }
    }
    
    @Test
    public final void overSizeKeyTest() throws Exception {
        try {
            startServer();
            KVClient client1 = newClient();
            KVClient client2 = newClient();
            String hugeKey = "";
            for(int i = 0; i < MAX_KEY_SIZE+1; i++){
                hugeKey += "k";
            }
            String value = "alphabet";

            client1.put(hugeKey, value);

        } catch (KVException e) {
            assertEquals(KVMessage.ResponseType.KEY_ERROR.toString(), e.getMsg().getMessage());
        } finally {
            stopServer();
        }
    }
    
    @Test
    public final void overSizeValueTest() throws Exception {
        try {
            startServer();
            KVClient client1 = newClient();
            KVClient client2 = newClient();
            String key = "alphabet";
            String hugeValue = "";
            for(int i = 0; i < MAX_VALUE_SIZE+1; i++){
                hugeValue += "v";
            }
            client1.put(key, hugeValue);

        } catch (KVException e) {
            assertEquals(KVMessage.ResponseType.VALUE_ERROR.toString(), e.getMsg().getMessage());
        } finally {
            stopServer();
        }
    }
    
    public static int MAX_KEY_SIZE = 256;
    public static int MAX_VALUE_SIZE = 1024*256;
    public String host = "localhost";
}