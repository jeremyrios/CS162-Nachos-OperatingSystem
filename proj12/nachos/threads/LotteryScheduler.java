package nachos.threads;

import nachos.machine.*;

import java.util.Random;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 * <p/>
 * <p/>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 * <p/>
 * <p/>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 * <p/>
 * <p/>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }

    public static final int priorityDefault = 1;
    public static final int priorityMinimum = 0;
    public static final int priorityMaximum = Integer.MAX_VALUE;

    @Override
    public void setPriority(KThread thread, int priority) {
        Lib.assertTrue(Machine.interrupt().disabled());

        Lib.assertTrue(priority >= priorityMinimum &&
                       priority <= priorityMaximum);

        getThreadState(thread).setPriority(priority);
    }

     /**
     * Allocate a new lottery thread queue.
     *
     * @param    transferPriority    <tt>true</tt> if this queue should
     * transfer tickets from waiting threads
     * to the owning thread.
     * @return a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new LotteryQueue(transferPriority);
    }

    @Override
    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new LotteryThreadState(thread);

        return (ThreadState) thread.schedulingState;
    }

    protected class LotteryQueue extends PriorityQueue {
        LotteryQueue(boolean transferPriority) {
            super(transferPriority);
            this.entropy = new Random();
        }
        @Override
        public int getEffectivePriority() {
            if (!this.transferPriority) {
                return priorityMinimum;
            } else if (this.priorityChange) {
                // recalculate effective priorities
                this.effectivePriority = priorityMinimum;
                for (final ThreadState cur : this.threadsWaiting) {
                    Lib.assertTrue(cur instanceof LotteryThreadState);
                    this.effectivePriority += cur.getEffectivePriority();
                }
                this.priorityChange = false;
            }
            return effectivePriority;

        }
        @Override
        public ThreadState pickNextThread() {
            int totalTickets = this.getEffectivePriority();
            int winningTicket = totalTickets > 0 ? entropy.nextInt(totalTickets) : 0;
            for (final ThreadState thread : this.threadsWaiting) {
                Lib.assertTrue(thread instanceof LotteryThreadState);
                winningTicket -= thread.getEffectivePriority();
                if (winningTicket <= 0) {
                    return thread;
                }
            }

            return null;
        }

        private final Random entropy;

    }
    protected class LotteryThreadState extends ThreadState {
        public LotteryThreadState(KThread thread) {
            super(thread);
        }
        @Override
        public int getEffectivePriority() {
            if (this.resourcesIHave.isEmpty()) {
                return this.getPriority();
            } else if (this.priorityChange) {
                this.effectivePriority = this.getPriority();
                for (final PriorityQueue pq : this.resourcesIHave) {
                    Lib.assertTrue(pq instanceof LotteryQueue);
                    this.effectivePriority += pq.getEffectivePriority();
                }
                this.priorityChange = false;
            }
            return this.effectivePriority;
        }
    }
}
