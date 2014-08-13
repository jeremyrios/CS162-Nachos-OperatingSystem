/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList;
import java.util.Random;
/**
 * CommunicatorTest creates a random number of speakers and listeners 
 * and pairs them up. The message that each speaker speaks is unique so that 
 * when the listener returns this message it is checked against all other 
 * messages returned by other listeners and an assertion is made on its 
 * uniqueness before adding it to the master list of return messages.
 * 
 * @author Jeremy Rios
 * 
 */


public class CommunicatorTest {
    
    public CommunicatorTest(){ 
	speakerMessage = 1;
        random = new Random();
        random.setSeed(System.currentTimeMillis());
        numOfSpeakers = random.nextInt(MAX_THREADS/2);
        numOfListeners = random.nextInt((MAX_THREADS/2) - numOfSpeakers + 1);
        messageList = new ArrayList<Integer>();
        communicator = new Communicator();
    }
    
    public void commTest(int num, boolean prints){
       testPrints = prints;
       if(testPrints)System.out.println("\nCommunicatorTest is now executing..."); 
       
       for (int i=0; i<num; i++){ 
           createSpeakers(numOfSpeakers);
           createListeners(numOfListeners);
           if(testPrints)print("\n Number of random speakers created: " + numOfSpeakers);
           if(testPrints)print(" Number of random listeners created: " + numOfListeners + "\n");
           sleep(numOfSpeakers+numOfListeners);
           if(testPrints)print("\n System is stable all random speakers \n  & listeners have been created\n");
           if(numOfSpeakers>numOfListeners){
               int additionListeners = numOfSpeakers-numOfListeners;
               createListeners(additionListeners);
               if(testPrints)print(" I had to create " + additionListeners + 
                       " addition listeners \n  so the speakers could exit the communicator\n");
               sleep(additionListeners);
           }
           else if(numOfListeners>numOfSpeakers){
               int additionalSpeakers = numOfListeners-numOfSpeakers;
               createSpeakers(additionalSpeakers);
               if(testPrints)print(" I had to create " + additionalSpeakers + 
                       " addition speakers \n so the listeners could exit the communicator\n");
               sleep(additionalSpeakers);
           }
       }
       print("     *-*-*-*  All Communicator Tests Passed  *-*-*-*");
    }
    
    public void sleep(int numThreadsCreated){
        ThreadedKernel.alarm.waitUntil(numThreadsCreated*100);
    }
    
    public class Listener implements Runnable{
        public void run(){
            int messageToRecieve = communicator.listen();
            if(testPrints)print("    -" + KThread.currentThread().getName() + " has received the message " + messageToRecieve);
            Lib.assertTrue(!messageList.contains(messageToRecieve));
            messageList.add(messageToRecieve);
        }
    }
    
    public class Speaker implements Runnable{
        public void run(){
            communicator.speak(speakerMessage++);
            if(testPrints)print("   " + KThread.currentThread().getName() + " spoke with the message " + speakerMessage);                  
        }
    }
    
    public void createSpeakers(int speakers){
        int j;
        for(j=1; j<=speakers; j++){               
                    KThread speakerThread = new KThread(new Speaker());
                    speakerThread.setName("Speaker_" + j);
                    speakerThread.fork();
           };
    }
    
    public void createListeners(int listeners){
        int k;
        for(k=1; k<=listeners; k++){        
                    KThread listenerThread = new KThread(new Listener());
                    listenerThread.setName("Listener_" + k);
                    listenerThread.fork();              
        }
    }
    
    public static void print(String aMessage) {
        System.out.println(aMessage);
    }
    
    public static int MAX_THREADS = 245;
    private int speakerMessage; 
    private Communicator communicator;
    private ArrayList<Integer> messageList;
    private boolean testPrints = false;
    private Random random;
    private int numOfSpeakers;
    private int numOfListeners;
    
}
