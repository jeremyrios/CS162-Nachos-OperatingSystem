package edu.berkeley.cs162;

// Junit
import static org.junit.Assert.*;
import org.junit.Test;

// Java
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPoolUnitTests {

    private static final int NUM_THREADS = 10;

    @Test public final void testAddToQueueIsNonBlocking() {
        final ThreadPool testThreadPool = new ThreadPool(NUM_THREADS);
        final AtomicInteger atom = new AtomicInteger();
        final Runnable sleepyTime = new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(100); // sleep for 100ms
                    } catch (final InterruptedException e) {
                        // Eat it
                    }
                    atom.getAndIncrement(); // then increment shared variable
                }
            };
        final long startTime = System.currentTimeMillis();
        try {
            testThreadPool.addToQueue(sleepyTime);
        } catch (final InterruptedException e) {
            // Eat it
        }
        final long finishTime = System.currentTimeMillis();
        assertTrue(finishTime - startTime < 10L); // check that addToQueue is non blocking
        try {
            Thread.sleep(200); // wait for runnable to finish
        } catch (final InterruptedException e) {
            // Eat it
        }
        assertTrue(atom.get() == 1); // check that the task was actually executed
    }

}
