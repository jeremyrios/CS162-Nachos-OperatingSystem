package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 * 
 * @author Jeremy Rios
 * 
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
	handShakeInProgress = false;
	waitingToListenQueueSize = 0;
	lock = new Lock();
	waitingToShakeHands = new Condition2(lock);
	waitingToSpeak = new Condition2(lock);
	waitingToListen = new Condition2(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     * <p/>
     * <p/>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param    word    the integer to transfer.
     */
    public void speak(int word) {
	lock.acquire();
	while(handShakeInProgress){
		waitingToSpeak.sleep();
	}
	
	handShakeInProgress = true;
        this.message = word;
	
	while(waitingToListenQueueSize == 0){
		waitingToShakeHands.sleep();
	}
	
	waitingToListen.wake();
	waitingToShakeHands.sleep();
	handShakeInProgress = false;
	waitingToSpeak.wake();
	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */
    public int listen() {
        lock.acquire();
	waitingToListenQueueSize++;
	
	if(waitingToListenQueueSize == 1 && handShakeInProgress){
		waitingToShakeHands.wake();
	}
	
	waitingToListen.sleep();
	waitingToShakeHands.wake();
	waitingToListenQueueSize--;
        int myMessage = this.message;
	lock.release();
	return myMessage;
    }

    public int getQueueSize(){
	return waitingToListenQueueSize;
    }
  
    

    private boolean handShakeInProgress;
    private int waitingToListenQueueSize;
    private int message;
    private Lock lock;
    private Condition2 waitingToShakeHands;
    private Condition2 waitingToSpeak;
    private Condition2 waitingToListen;
    
}
