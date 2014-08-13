package nachos.threads;

import nachos.machine.*;
import java.util.*;
import java.lang.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
	
	   alarmHeap = new PriorityQueue<AlarmEntry>();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	boolean intStatus = Machine.interrupt().disable();
	
	while(!alarmHeap.isEmpty() && (alarmHeap.peek().wakeTime <= Machine.timer().getTime())){
		AlarmEntry thread = alarmHeap.remove();
		thread.waitThread.ready();
	}
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    


    public void waitUntil(long x) {

	boolean intStatus = Machine.interrupt().disable();
	long wakeTime = Machine.timer().getTime() + x;

	AlarmEntry thread = new AlarmEntry(KThread.currentThread(), wakeTime);
	alarmHeap.add(thread);
	KThread.sleep();

	Machine.interrupt().restore(intStatus);
    }
    

    //Instance variable: a Java PriorityQueue of AlarmEntry Objects
    private PriorityQueue<AlarmEntry> alarmHeap;

    /**
     *A container class for a KThread and a wakeTime(type long)      
     * Implements the Comparable interface for storage in a "JAVA" PriorityQueue
     */
     public class AlarmEntry implements Comparable<AlarmEntry> {
	 /**
    	  * Allocate a new AlarmEntry with a KThread and its wakeTime 	  
   	  *  
    	  */

	public AlarmEntry(KThread waitThread, long wakeTime){
		this.waitThread = waitThread;
		this.wakeTime = wakeTime;
	}
	
	 /**
    	  * Use Java's Long compareTo method to compare the wakeTimes of "this" AlarmEntry  
   	  *  and another AlarmEntry object
    	  */

	public int compareTo(AlarmEntry other){
		return (new Long(this.wakeTime).compareTo(new Long(other.wakeTime)));
	}

	//Instance variables for AlarmEntry class
	private KThread waitThread;
	private long wakeTime;
     }
     
     public static void selfTest(int numOfTest, boolean prints){
         testPrints = prints;
         Runnable a = new Runnable() {
            public void run() {
                AlarmTestThread();
            }
         };
         
         int masterThreadSleepTime = 295000;
         Random rand = new Random();
         rand.setSeed(System.currentTimeMillis());
         for(int i=0; i<numOfTest; i++){
             
             int numOfThreads = randomIntInRange(10, 245, rand);
             if(testPrints)print("Creating " + numOfThreads + " num of threads");
             for(int j=0; j<numOfThreads; j++){
                 KThread thread = new KThread(a);
                 thread.setName("thread");
		 thread.fork();
             }
             
             ThreadedKernel.alarm.waitUntil(masterThreadSleepTime);
         } 
         print("     *-*-*-*  All Alarm Tests Passed  *-*-*-*");
     }
     
    static void AlarmTestThread(){
         Random randomTime = new Random();
         randomTime.setSeed(System.currentTimeMillis());
         long sleepTime = randomIntInRange(10, 29400, randomTime);       
         long timeBeforeSleep = Machine.timer().getTime();
         ThreadedKernel.alarm.waitUntil(sleepTime);
         long timeAfterSleep = Machine.timer().getTime();
         long actualSleepTime = timeAfterSleep - timeBeforeSleep;
         if(testPrints)print(KThread.currentThread().toString() + 
                    " went to sleep at " + timeBeforeSleep + 
                    " for " + sleepTime + " ticks");
         if(testPrints)print(KThread.currentThread().toString() + 
                    " woke up at " + Machine.timer().getTime() + 
                    "\n  -requested sleep time: " + sleepTime +
                    " ticks, actual sleep time: " + actualSleepTime);
         Lib.assertTrue(actualSleepTime >= sleepTime);
     }
     
    public static void print(String aMessage) {
        System.out.println(aMessage);
    }
    
    public static int randomIntInRange(int lowBound, int upBound, Random rand) {
        long range = (long) upBound - (long) lowBound + 1;
        long fraction = (long) (range * rand.nextDouble());
        int randomNumber = (int) (fraction + lowBound);
        return randomNumber;
    }


     
    static private boolean testPrints;
}
