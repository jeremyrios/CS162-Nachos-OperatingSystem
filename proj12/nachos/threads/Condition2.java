package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * <p/>
 * <p/>
 * You must implement this.
 *
 * @see    nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param    conditionLock    the lock associated with this condition
     * variable. The current thread must hold this
     * lock whenever it uses <tt>sleep()</tt>,
     * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;
        this.waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     * 
     */
    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());        
        
        boolean initialStatus = Machine.interrupt().disable();
        waitQueue.waitForAccess(KThread.currentThread());
        conditionLock.release();
        KThread.sleep();
        conditionLock.acquire();
        Machine.interrupt().restore(initialStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        
        boolean initialStatus = Machine.interrupt().disable();
        KThread nextThread = waitQueue.nextThread();
        if (nextThread != null) {
            nextThread.ready();
        }        
        Machine.interrupt().restore(initialStatus);   
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        
        boolean initialStatus = Machine.interrupt().disable();
        KThread nextThread = waitQueue.nextThread();
        while (nextThread != null) {
            nextThread.ready();
            nextThread = waitQueue.nextThread();
        }        
        Machine.interrupt().restore(initialStatus);   
    }

    /**
      * A lock used for this condition variable.
      */
    private Lock conditionLock;
    /**
      * A queue of threads sleeping on this condition variable.
      */
    private ThreadQueue waitQueue = null;
}
