package nachos.threads;

import nachos.machine.*;

/**
 * Contains test for KThread
 */
public class KThreadTest {

    public KThreadTest() {
    }

    /**
     * A very simple test of KThread.join(). Joiner forks a joinee, calls join() then finishes.
     */
    public static void simpleJoinTest() {
        KThread simpleJoinTestJoinee = new KThread(new KThreadTest.SimpleJoinTestJoinee(10));
        KThread simpleJoinTestJoiner = new KThread(new KThreadTest.SimpleJoinTestJoiner(simpleJoinTestJoinee));
        simpleJoinTestJoiner.fork();
        simpleJoinTestJoiner.join();
    }

    /**
     * SimpleJoinTestJoiner will start, fork a joinee, then wait for it to finish.
     */
    public static class SimpleJoinTestJoiner implements Runnable {
        SimpleJoinTestJoiner(KThread joinee) {
            this.joinee = joinee;
        }

        public void run() {
            System.out.println("SimpleJoinTestJoiner is now executing.");
            System.out.println("Forking and joining SimpleJoinTestJoinee...");
            this.joinee.fork();
            this.joinee.join();
            System.out.println("SimpleJoinTestJoiner has finished executing.");
        }

        private KThread joinee;
    }

    /**
     * SimpleJoinTestJoinee will start and then spin for a few cycles. It will
     * be joined by SimpleJoinTestJoiner.
     */
    public static class SimpleJoinTestJoinee implements Runnable {
        SimpleJoinTestJoinee(int loopAmount) {
            this.loopAmount = loopAmount;
        }

        public void run() {
            System.out.println("SimpleJoinTestJoinee is now executing.");
            System.out.println("Starting busy work...");

            // This should just kill some cycles
            for (int i = 0; i < this.loopAmount; ++i) {
                System.out.println("SimpleJoinTestJoinee has looped " + i + " times");
                KThread.currentThread().yield();
            }
            System.out.println("SimpleJoinTestJoinee has finished executing.");
        }

        /**
         * Number of iterations to spend looping.
         */
        private int loopAmount;
    }
    
}
