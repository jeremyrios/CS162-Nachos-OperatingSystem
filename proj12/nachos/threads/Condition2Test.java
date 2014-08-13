/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nachos.threads;
import nachos.machine.*;

/**
 *
 * @author Anastasia
 */
public class Condition2Test {
    private int sleepingThreads = 0;
    
    Lock lock = new Lock();
    Condition2 c2test = new Condition2(lock);
    
    public Condition2Test(){    
    }
    
    
    public void simpleCondition2Test() {
        
        System.out.println("\n ****Condition2Test is now executing.****");
            
        
        KThread thread1 = new KThread(new Runnable() {
            public void run() {
                System.out.println("Thread 1 goes to sleep");
                lock.acquire();
		c2test.sleep();
		System.out.println("Thread 1 reacquires lock when woken.");
		lock.release();
                System.out.println("Thread 1 is awake!!!");
            }
	});
        
        KThread thread2 = new KThread(new Runnable() {
            public void run() {
                System.out.println("Thread 2 goes to sleep");
                lock.acquire();
		c2test.sleep();
		System.out.println("Thread 2 reacquires lock when woken.");
		lock.release();
                System.out.println("Thread 2 is awake!!!");
            }
	});
		
	KThread thread3 = new KThread(new Runnable() {
            public void run() {
                System.out.println("Waking up the thread");
		lock.acquire();
		c2test.wakeAll();
		lock.release();
                System.out.println("Thread 1 and 2 woke up by wakeAll");
            }
	});
		
	thread1.fork();
	thread2.fork();
        thread3.fork();
	thread1.join();
        thread2.join();
        thread3.join();
               
        System.out.println("****Condition2Test finished.****\n");

    }
}
   



