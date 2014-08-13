package edu.berkeley.cs162;

import org.junit.Test;
import static org.junit.Assert.*;

public final class DeletionIntTests extends BaseTest {

    @Test public void simplePutGetDelTest() throws Exception {
        try {
            startServer();

            final KVClient client = newClient();

            final String testKey = "HELLO";
            final String testValue = "VALUE";

            client.put(testKey, testValue);
            assertEquals(client.get(testKey), testValue);
            client.del(testKey);

            String expectedNull = null;

            boolean exceptionThrown = false;

            try {
                expectedNull = client.get(testKey);
            } catch (final KVException e) {
                exceptionThrown = true;
                assertEquals(KVMessage.ResponseType.DNE_ERROR.toString(), e.getMsg().getMessage());
            }

            assertTrue(exceptionThrown);

            assertNull(expectedNull);

        } finally { stopServer(); }
    }

    @Test public void deleteNonExistentTest() throws Exception {
        try {
            startServer();

            final KVClient client = newClient();

            final String testKey = "HELLO";

            boolean exceptionThrown = false;

            try {
                client.del(testKey);
            } catch (final KVException e) {
                exceptionThrown = true;
                assertEquals(KVMessage.ResponseType.DNE_ERROR.toString(), e.getMsg().getMessage());
            }

            assertTrue(exceptionThrown);

        } finally {
            stopServer();
        }
    }
}
