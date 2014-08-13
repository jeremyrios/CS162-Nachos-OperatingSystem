package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.*;
import java.net.*;

public final class KVServerUnitTests {

    @Test
    public void simpleServerTest() {
        KVServer kvServer = new KVServer(1, 1);
        String key = "key";
        String value = "value";
        try {
            kvServer.put(key, value);
            assertTrue(kvServer.get(key).equals("value"));
            kvServer.del(key);
        } catch (KVException e) {
            System.err.println(e.getMsg().getMessage());
        }
    }

    @Test
    public void storageTester() {
        KVServer kvServer = new KVServer(3, 2);
        try {
            for (int i = 0; i < 6; i++) {
                kvServer.put("key" + i, "value" + i);
            }
            for (int i = 0; i < 6; i++) {
                assertTrue(kvServer.get("key" + i).equals("value" + i));
                kvServer.del("key" + i);
            }
        } catch (KVException e) {
            System.err.println(e.getMsg().getMessage());
        }
    }


    @Test
    public void testDataIsBeingCached() throws Exception {

        final KVServer server = new KVServer(100, 10);
        final KVStore store = server.getDataStore();
        final KVCache cache = server.getDataCache();

        final String key = "KEY";
        final String value = "VALUE";

        cache.getWriteLock(key).lock();

        assertNull(cache.get(key));

        server.put(key, value);

        assertEquals(value, server.get(key));

        assertEquals(value, cache.get(key));

        assertEquals(value, store.get(key));

        // manually change the entry in the store to check that data is coming from the cache

        store.put(key, "WRONG VALUE");

        assertEquals(value, server.get(key));

        cache.getWriteLock(key).unlock();

    }

    @Test public void testDataIsWrittenThroughCache() throws Exception {

        final KVServer server = new KVServer(100, 10);
        final KVStore store = server.getDataStore();
        final KVCache cache = server.getDataCache();

        final String key = "KEY";
        final String value = "VALUE";
        final String wrongValue = "WRONG VALUE";

        cache.getWriteLock(key).lock();
        cache.put(key, wrongValue);

        assertEquals(wrongValue, cache.get(key));

        server.put(key, value);

        assertEquals(value, cache.get(key));

        cache.getWriteLock(key).unlock();

    }
}