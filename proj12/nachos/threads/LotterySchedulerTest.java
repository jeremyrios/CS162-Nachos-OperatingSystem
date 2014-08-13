package nachos.threads;


import nachos.machine.Lib;
import nachos.machine.Machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LotterySchedulerTest {

    // test pickNextThread to make sure it is picking the threads correctly
    public static void testPickNextThread() {

        final int NUM_ITERATIONS = 10000000;
        final double TOLERANCE = 0.01;

        final boolean intStatus = Machine.interrupt().disable();

        final Scheduler s = new LotteryScheduler();

        // fake thread Queue
        final ThreadQueue queue = s.newThreadQueue(true);

        final KThread waiter20 = new KThread(null);
        final KThread waiter25 = new KThread(null);
        final KThread waiter50 = new KThread(null);
        final KThread waiter5 = new KThread(null);

        s.setPriority(waiter20,  20);
        s.setPriority(waiter25, 25);
        s.setPriority(waiter50, 50);
        s.setPriority(waiter5 , 5);

        queue.waitForAccess(waiter20);
        queue.waitForAccess(waiter25);
        queue.waitForAccess(waiter50);
        queue.waitForAccess(waiter5);

        final Map<KThread, Integer> pickFrequency = new HashMap<KThread, Integer>();
        pickFrequency.put(waiter20, 0);
        pickFrequency.put(waiter25, 0);
        pickFrequency.put(waiter50, 0);
        pickFrequency.put(waiter5, 0);

        System.out.println("Running priority scheduler pickNextThreadTest for " + NUM_ITERATIONS + " iterations.");
        System.out.println("This will take a second or two....");

        for (int i = 0; i < NUM_ITERATIONS; ++i) {
            final KThread wasPicked = queue.nextThread();
            Lib.assertTrue(wasPicked != null);
            pickFrequency.put(wasPicked, pickFrequency.get(wasPicked) + 1);
            queue.waitForAccess(wasPicked);
        }

        try {
            // Testing nondeterministic code is FUN!!!
            for (final Map.Entry<KThread, Integer> e : pickFrequency.entrySet()) {
                final int expectedPicks = (int) (((double) s.getPriority(e.getKey()) / 100.0) * NUM_ITERATIONS);
                final int actualPicks = e.getValue();
                Lib.assertTrue(Math.abs(actualPicks - expectedPicks) < (TOLERANCE * NUM_ITERATIONS));
            }
        }  catch (final Exception e) {
            System.err.println("Priority scheduler pickNextThreadTest failed!");
            System.exit(-1);
        }

        System.out.println("Actual values are within " + TOLERANCE + "% of expected!");
        System.out.println("Priority scheduler pickNextThreadTest passed successfully!");

        Machine.interrupt().restore(intStatus);
    }

    public static void simplePriorityDonationTest() {

        final Scheduler s = new LotteryScheduler();

        final boolean intStatus = Machine.interrupt().disable();

        System.out.println("Running simple test of LotteryScheduler priority donation...");

        int totalPriority = 0;
        List<KThread> threadList = new ArrayList<KThread>();
        for (int i = 10; i < 100; i+=10) {
            totalPriority += i;
            KThread cur = new KThread(null);
            s.setPriority(cur, i);
            threadList.add(cur);
        }

        final ThreadQueue queue = s.newThreadQueue(true);

        for (final KThread thread : threadList) {
            queue.waitForAccess(thread);
        }

        final KThread resourceHolder = new KThread(null);

        final int initialPriority = s.getEffectivePriority(resourceHolder);

        queue.acquire(resourceHolder);
        try {
            Lib.assertTrue(s.getEffectivePriority(resourceHolder) - initialPriority == totalPriority);
        } catch (Exception e) {
            System.err.println("Simple priority donation test failed!");
            System.exit(-1);
        }
        System.out.println("Simple priority donation test passed!");

        Machine.interrupt().restore(intStatus);

    }
}
