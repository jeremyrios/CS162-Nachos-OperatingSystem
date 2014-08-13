package edu.berkeley.cs162;


import org.junit.Test;
import static org.junit.Assert.*;

public final class SimplePutGetIntTests extends BaseTest {

    @Test public final void simplePutGetIntTest() throws Exception {
        try {
            startServer();

            final KVClient client = newClient();

            final String testKey = "HELLO";
            final String testValue = "WORLD";

            client.put(testKey, testValue);

            assertEquals(client.get(testKey), testValue);

        } finally { stopServer();  }
    }

    @Test public final void simplePutGetIntTest2() throws Exception {
        try {
            startServer();

            final KVClient client1 = newClient();
            final KVClient client2 = newClient();

            final String testKey1 = "HELLO";
            final String testValue1 = "WORLD";
            final String testKey2 = "FOO";
            final String testValue2 = "BAR";

            client1.put(testKey1, testValue1);
            client2.put(testKey2, testValue2);

            assertEquals(client2.get(testKey1), testValue1);
            assertEquals(client1.get(testKey2), testValue2);

        } finally { stopServer(); }
    }

}