package nachos.threads;

import nachos.machine.*;
import nachos.threads.*;

import java.util.*;

public final class PrioritySchedulerTest {
    /**
     * A very simple test of priority donation.
     */
    public static void simplePrioritySchedulerTest() {

        final boolean intStatus = Machine.interrupt().disable();

        final Scheduler s = new PriorityScheduler();

        // Enable priority donation.
        final ThreadQueue fakeRunQueue1 = s.newThreadQueue(false);
        final ThreadQueue fakeRunQueue2 = s.newThreadQueue(false);
        final ThreadQueue fakeResourceQueue = s.newThreadQueue(true);

        final Runnable dummyRunnable = new Runnable() {
                public void run() {
                // do nothing
                }
        };
        // Create dummy Threads
        final KThread lowPriorityThread = new KThread(dummyRunnable);
        final KThread mediumPriorityThread = new KThread(dummyRunnable);
        final KThread highPriorityThread = new KThread(dummyRunnable);

        // Link dummy Threads with dummy ThreadState
        s.setPriority(lowPriorityThread, PriorityScheduler.priorityMinimum);
        s.setPriority(mediumPriorityThread, PriorityScheduler.priorityDefault);
        s.setPriority(highPriorityThread, PriorityScheduler.priorityMaximum);

        // Test that effectivePriorities start as the initial priorities
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(mediumPriorityThread) == PriorityScheduler.priorityDefault);
        Lib.assertTrue(s.getEffectivePriority(highPriorityThread) == PriorityScheduler.priorityMaximum);


        // Low priority and Medium priority thread are on the ready queue
        fakeRunQueue1.waitForAccess(lowPriorityThread);
        fakeRunQueue1.waitForAccess(mediumPriorityThread);

        fakeRunQueue2.waitForAccess(lowPriorityThread);
        fakeRunQueue2.waitForAccess(mediumPriorityThread);

        // Now, medium priority thread should be the next Thread
        Lib.assertTrue(fakeRunQueue1.nextThread().equals(mediumPriorityThread),
                       "Medium priority thread should be the next thread");

        // Low priority thread acquires the lock
        fakeResourceQueue.acquire(lowPriorityThread);
        // High priority thread waits for the lock
        fakeResourceQueue.waitForAccess(highPriorityThread);

        // Check if priority is donated
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread) == PriorityScheduler.priorityMaximum,
                       "Priority should be donated from lowPriorityThread to highPriorityThread.");

        // Make sure Medium priority thread still has same priority
        Lib.assertTrue(s.getEffectivePriority(mediumPriorityThread) == PriorityScheduler.priorityDefault,
                       "Medium priority thread's effective priority should be unchanged.");

        // Now, low priority thread should be the next Thread
        Lib.assertTrue(fakeRunQueue2.nextThread().equals(lowPriorityThread),
                       "Low priority thread should be run next after its priority is increased");

        // This line will only be reached if all these assertions pass.
        System.out.println("simplePrioritySchedulerTest has passed successfully!");

        Machine.interrupt().restore(intStatus);
    }

    /** Forks a KThread of each priority and makes sure that it runs in the correct order. This test will
     *  only pass if PriorityScheduler is set as the Machine's scheduler in nachos.conf
     */
    public static void prioritySchedulerReadyQueueTest() {
        // The runnable for the 7 threads we will create

        // Threads will append to this list in the order they were run.
        // this can be modified by different threads since there is no actual parallelism.
        // if it were allowed I would wrap this with Collections.synchronizedList
        final List<Integer> resultList = new LinkedList<Integer>();

        final List<KThread> threads = new LinkedList<KThread>();

        final boolean intStatus = Machine.interrupt().disable();

        for (int curPriority = PriorityScheduler.priorityMinimum + 1; curPriority <= PriorityScheduler.priorityMaximum; ++curPriority) {
            final KThread curThread = new KThread(new AppenderTestThread(curPriority, resultList));
            ThreadedKernel.scheduler.setPriority(curThread, curPriority);
            threads.add(curThread);
        }

        Machine.interrupt().restore(intStatus);

        for (final KThread curThread : threads)
            curThread.fork();

        for (final KThread curThread : threads)
            curThread.join();

        Lib.assertTrue(resultList.equals(Arrays.asList(7, 6, 5, 4, 3, 2, 1)));

        System.out.println("prioritySchedulerReadyQueueTest has passed successfully!");

    }

    /** A simple test thread that appends its id to a list. */
    private static class AppenderTestThread implements Runnable {
        AppenderTestThread(int id, List<Integer> list) {
            this.id = id;
            this.appendList = list;
        }
        public void run() {
            appendList.add(id);
        }
        private int id;
        private final List<Integer> appendList;
    }

    public static void priorityDonationWithMutablePriorityTest() {
        final Scheduler s = new PriorityScheduler();

        final Runnable dummyRunnable = new Runnable() {
            public void run() {
                // do nothing
            }
        };

        final KThread resourceHolder = new KThread(dummyRunnable);
        final KThread prioritySwitcher = new KThread(dummyRunnable);

        // disable interrupts as we will be modifying priorities
        final boolean intStatus = Machine.interrupt().disable();

        s.setPriority(resourceHolder, 0);
        s.setPriority(prioritySwitcher, 4);

        // sanity checks
        Lib.assertTrue(s.getEffectivePriority(resourceHolder) == 0);
        Lib.assertTrue(s.getEffectivePriority(prioritySwitcher) == 4);

        // The resource, backed by a thread queue that donates priority
        final ThreadQueue resource = s.newThreadQueue(true);

        // Give the resource to resourceHolder
        resource.acquire(resourceHolder);

        // Priority should be unchanged
        Lib.assertTrue(s.getEffectivePriority(resourceHolder) == 0);

        // prioritySwitcher now waits for the resource
        resource.waitForAccess(prioritySwitcher);

        // Check that priority was donated
        Lib.assertTrue(s.getEffectivePriority(resourceHolder) == 4);

        // Now we increase the priority of prioritySwitcher
        s.setPriority(prioritySwitcher, 5);

        // Check to see that prioritySwitcher's priority was increased successfully
        Lib.assertTrue(s.getEffectivePriority(prioritySwitcher) == 5);

        // Check to see that priority donation is still correct
        Lib.assertTrue(s.getEffectivePriority(resourceHolder) == 5);

        // Change prioritySwitcher's priority once more
        s.setPriority(prioritySwitcher, 3);

        // Check to see if that worked
        Lib.assertTrue(s.getEffectivePriority(prioritySwitcher) == 3);
        Lib.assertTrue(s.getEffectivePriority(resourceHolder) == 3);



        // re-enable interrupts
        Machine.interrupt().restore(intStatus);

        // If this line is reached, the test has passed
        System.out.println("priorityDonationWithMutablePriorityTest has passed successfully!");
    }

    /**
     *  Tests priority donation with a complex dependency graph. This test currently fails,
     *  which means PriorityScheduler still has some bugs.
     */
    public static void complexPriorityDonationTest() {

        final Scheduler s = new PriorityScheduler();

        final Runnable dummyRunnable = new Runnable() {
            public void run() {
                // do nothing
            }
        };

        final boolean intStatus = Machine.interrupt().disable();

        KThread lowPriorityThread1 = new KThread(dummyRunnable);
        KThread lowPriorityThread2 = new KThread(dummyRunnable);
        KThread lowPriorityThread3 = new KThread(dummyRunnable);
        KThread lowPriorityThread4 = new KThread(dummyRunnable);
        KThread lowPriorityThread5 = new KThread(dummyRunnable);
        KThread lowPriorityThread6 = new KThread(dummyRunnable);
        KThread lowPriorityThread7 = new KThread(dummyRunnable);
        KThread lowPriorityThread8 = new KThread(dummyRunnable);
        KThread lowPriorityThread9 = new KThread(dummyRunnable);
        KThread lowPriorityThread10 = new KThread(dummyRunnable);
        KThread lowPriorityThread11 = new KThread(dummyRunnable);
        KThread lowPriorityThread12 = new KThread(dummyRunnable);
        KThread lowPriorityThread13 = new KThread(dummyRunnable);
        KThread lowPriorityThread14 = new KThread(dummyRunnable);
        KThread lowPriorityThread15 = new KThread(dummyRunnable);
        KThread lowPriorityThread16 = new KThread(dummyRunnable);
        KThread lowPriorityThread17 = new KThread(dummyRunnable);
        KThread lowPriorityThread18 = new KThread(dummyRunnable);
        KThread lowPriorityThread19 = new KThread(dummyRunnable);
        KThread lowPriorityThread20 = new KThread(dummyRunnable);

        KThread highPriorityThread = new KThread(dummyRunnable);

        ThreadQueue lock1 = s.newThreadQueue(true);
        ThreadQueue lock2 = s.newThreadQueue(true);
        ThreadQueue lock3 = s.newThreadQueue(true);
        ThreadQueue lock4 = s.newThreadQueue(true);
        ThreadQueue lock5 = s.newThreadQueue(true);
        ThreadQueue lock6 = s.newThreadQueue(true);
        ThreadQueue lock7 = s.newThreadQueue(true);
        ThreadQueue lock8 = s.newThreadQueue(true);
        ThreadQueue lock9 = s.newThreadQueue(true);
        ThreadQueue lock10 = s.newThreadQueue(true);
        ThreadQueue lock11 = s.newThreadQueue(true);
        ThreadQueue lock12 = s.newThreadQueue(true);
        ThreadQueue lock13 = s.newThreadQueue(true);
        ThreadQueue lock14 = s.newThreadQueue(true);
        ThreadQueue lock15 = s.newThreadQueue(true);
        ThreadQueue lock16 = s.newThreadQueue(true);
        ThreadQueue lock17 = s.newThreadQueue(true);
        ThreadQueue lock18 = s.newThreadQueue(true);
        ThreadQueue lock19 = s.newThreadQueue(true);
        ThreadQueue lock20 = s.newThreadQueue(true);

        s.setPriority(lowPriorityThread1, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread2, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread3, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread4, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread5, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread6, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread7, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread8, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread9, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread10, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread11, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread12, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread13, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread14, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread15, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread16, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread17, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread18, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread19, PriorityScheduler.priorityMinimum);
        s.setPriority(lowPriorityThread20, PriorityScheduler.priorityMinimum);

        s.setPriority(highPriorityThread, PriorityScheduler.priorityMaximum);

        lock1.acquire(lowPriorityThread1);
        lock2.acquire(lowPriorityThread2);
        lock3.acquire(lowPriorityThread3);
        lock4.acquire(lowPriorityThread4);
        lock5.acquire(lowPriorityThread5);
        lock6.acquire(lowPriorityThread6);
        lock7.acquire(lowPriorityThread7);
        lock8.acquire(lowPriorityThread8);
        lock9.acquire(lowPriorityThread9);
        lock10.acquire(lowPriorityThread10);
        lock11.acquire(lowPriorityThread11);
        lock12.acquire(lowPriorityThread12);
        lock13.acquire(lowPriorityThread13);
        lock14.acquire(lowPriorityThread14);
        lock15.acquire(lowPriorityThread15);
        lock16.acquire(lowPriorityThread16);
        lock17.acquire(lowPriorityThread17);
        lock18.acquire(lowPriorityThread18);
        lock19.acquire(lowPriorityThread19);
        lock20.acquire(lowPriorityThread20);

        lock2.waitForAccess(lowPriorityThread1);
        lock3.waitForAccess(lowPriorityThread2);
        lock4.waitForAccess(lowPriorityThread3);
        lock5.waitForAccess(lowPriorityThread4);
        lock6.waitForAccess(lowPriorityThread5);
        lock7.waitForAccess(lowPriorityThread6);
        lock8.waitForAccess(lowPriorityThread7);
        lock9.waitForAccess(lowPriorityThread8);
        lock10.waitForAccess(lowPriorityThread9);
        lock11.waitForAccess(lowPriorityThread10);
        lock12.waitForAccess(lowPriorityThread11);
        lock13.waitForAccess(lowPriorityThread12);
        lock14.waitForAccess(lowPriorityThread13);
        lock15.waitForAccess(lowPriorityThread14);
        lock16.waitForAccess(lowPriorityThread15);
        lock17.waitForAccess(lowPriorityThread16);
        lock18.waitForAccess(lowPriorityThread17);
        lock19.waitForAccess(lowPriorityThread18);
        lock20.waitForAccess(lowPriorityThread19);

        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread1) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread2) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread3) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread4) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread5) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread6) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread7) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread8) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread9) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread10) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread11) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread12) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread13) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread14) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread15) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread16) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread17) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread18) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread19) == PriorityScheduler.priorityMinimum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread20) == PriorityScheduler.priorityMinimum);

        lock1.waitForAccess(highPriorityThread);

        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread1) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread2) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread3) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread4) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread5) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread6) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread7) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread8) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread9) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread10) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread11) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread12) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread13) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread14) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread15) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread16) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread17) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread18) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread19) == PriorityScheduler.priorityMaximum);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread20) == PriorityScheduler.priorityMaximum);

        s.setPriority(highPriorityThread, 6);

        // Check that priority was set correctly
        Lib.assertTrue(s.getEffectivePriority(highPriorityThread) == 6);

        // Check that priority was donated correctly
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread1) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread2) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread3) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread4) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread5) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread6) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread7) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread8) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread9) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread10) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread11) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread12) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread13) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread14) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread15) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread16) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread17) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread18) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread19) == 6);
        Lib.assertTrue(s.getEffectivePriority(lowPriorityThread20) == 6);

        Machine.interrupt().restore(intStatus);

        System.out.println("complexPriorityDonationTest has passed successfully!");

    }

}
