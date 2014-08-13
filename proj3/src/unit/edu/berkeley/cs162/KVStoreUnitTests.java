package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class KVStoreUnitTests {

    @Test
    public void toXmlTest() {
        KVStore kvStore = new KVStore();
        try {
            for (int i = 0; i < 3; i++) {
                kvStore.put("key" + i, "value" + i);
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
            sb.append("<KVStore>");
            sb.append("<KVPair>");
            sb.append("<Key>key2</Key>");
            sb.append("<Value>value2</Value>");
            sb.append("</KVPair>");
            sb.append("<KVPair>");
            sb.append("<Key>key1</Key>");
            sb.append("<Value>value1</Value>");
            sb.append("</KVPair>");
            sb.append("<KVPair>");
            sb.append("<Key>key0</Key>");
            sb.append("<Value>value0</Value>");
            sb.append("</KVPair>");
            sb.append("</KVStore>");
            assertEquals(sb.toString(), kvStore.toXML());
        } catch (KVException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void dumpAndRestoreFileTest() {
        KVStore kvStore = new KVStore();
        try {
            for (int i = 0; i < 3; i++) {
                kvStore.put("key" + i, "value" + i);
            }
            final String fileName = "KVStore.xml";
            kvStore.dumpToFile(fileName);
            kvStore.restoreFromFile(fileName);

            for (int i = 2; i >= 0; i--) {
                assertEquals(kvStore.get("key" + i), "value" + i);
            }

            File file = new File(fileName);
            file.deleteOnExit();

        } catch (KVException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void dumpToXMLandRestoreTest() {
        Random random;
        random = new Random();
        final Map<String, String> randomKVPairs = new HashMap<String, String>();
        random.setSeed(System.currentTimeMillis());
        final String fileName = "KV.xml";
        try {
            KVStore kvStoreA = new KVStore();

            // come up with some random data
            for (int i = random.nextInt(100 + 1); i > 0; i--) {
                randomKVPairs.put("" + random.nextInt(100 + 1), "" + random.nextInt(100 + 1));
            }

            for (final Map.Entry<String, String> entry : randomKVPairs.entrySet())  {
                kvStoreA.put(entry.getKey(), entry.getValue());
            }


            kvStoreA.dumpToFile(fileName);
            KVStore kvStoreB = new KVStore();
            kvStoreB.restoreFromFile(fileName);
            //System.out.println("kvStoreA: " + kvStoreA.getStore()); // just so I can see whats acctually in the hashmaps
            //System.out.println("kvStoreB: " + kvStoreB.getStore());

            for (final Map.Entry<String, String> entry: randomKVPairs.entrySet()) {
                assertEquals(entry.getValue(), kvStoreB.get(entry.getKey()));
            }

            // Rios: you can't compare the stores like this. You need to compare the actual data in the stores
            //assertEquals(kvStoreA.getStore(), kvStoreB.getStore());
            File file = new File(fileName);
            file.deleteOnExit();
        } catch (KVException e) {
            e.printStackTrace();
        }
    }
}