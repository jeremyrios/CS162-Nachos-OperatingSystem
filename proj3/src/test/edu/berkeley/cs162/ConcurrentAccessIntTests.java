package edu.berkeley.cs162;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public final class ConcurrentAccessIntTests extends BaseTest {

    @Test
    public void clobberServerWithPutsTest() throws Exception {

        try {
           startServer();

           final int numThreads = 100;
           final int numKeys = 100;

           final List<String> testKeys = new ArrayList<String>();

           for (int i = 0; i < numKeys; ++i) {
               testKeys.add(i + "");
           }

           final Runnable putRunnable = new Runnable() {
               @Override
               public void run() {
                   try {
                       final KVClient myClient = newClient();
                       for (final String testKey : testKeys) {
                           myClient.put(testKey, testKey);
                       }
                   } catch (final Exception e) {
                       // die
                   }
               }
           };

           List<Thread> t = new ArrayList<Thread>();

           for (int i = 0; i < numThreads; ++i) {
               t.add(new Thread(putRunnable));
           }

           for (int i = 0; i < numThreads; ++i) {
               t.get(i).start();
           }

           for (int i = 0; i < numThreads; ++i) {
               t.get(i).join();
           }


           final KVClient checkClient = newClient();

           for (String testKey : testKeys) {
               assertEquals(testKey, checkClient.get(testKey));
           }


        } finally {
            stopServer();
        }
    }

}
