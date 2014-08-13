package nachos.threads;

import nachos.ag.BoatGrader;
import java.util.Random;

/**
 * 
 * @author Jeremy Rios
 * 
 */
public class Boat {

    static BoatGrader bg;
    static String boatLocation;
    static boolean isThereAtLeast1childOnMolokai;
    static boolean done;
    static boolean amIpilot;
    static boolean adultHasBoat;
    static int numOfChildrenOnOahu;
    static int numOfChildrenOnMolokai;
    static int numOfChildrenWaitingForBoat;
    static int boatPasses;
    static int numOfAdultsOnOahu;
    static int numOfAdultsOnMolokai;
    static int message;
    static Lock lock;
    static Condition2 childOnOahu;
    static Condition2 childOnMolokai;
    static Condition2 adultOnOahu;
    static Condition2 boat;
    static Alarm alarm;
    static Communicator walkyTalky;
    
    // Testing & Debuging Varibles
    static boolean prints = false;
    static boolean bgPrints = true;

    public static void selfTest(int numTest, boolean testingPrints) {
        BoatGrader b = new BoatGrader();
        bgPrints = false;
        int ch, ad;
        int minNumOfChidren = 2;
        int maxNumOfThreads = 242;
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        for (int i = 1; i < numTest; i++) {
            ch = randomIntInRange(minNumOfChidren, maxNumOfThreads, random);
            ad = random.nextInt(maxNumOfThreads - ch + 1);
            if (testingPrints) {
                print("   Testing Boats with " + ch + " children, " + ad + " adults");
            }
            begin(ad, ch, b);
        }

        print("     *-*-*-*  All Boat Test Passed  *-*-*-*");
    }

    public static int randomIntInRange(int lowBound, int upBound, Random rand) {
        long range = (long) upBound - (long) lowBound + 1;
        long fraction = (long) (range * rand.nextDouble());
        int randomNumber = (int) (fraction + lowBound);
        return randomNumber;
    }

    public static void print(String aMessage) {
        System.out.println(aMessage);
    }

    public static void begin(int adults, int children, BoatGrader b) {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here
        boatLocation = "Oahu";

        isThereAtLeast1childOnMolokai = false;
        done = false;
        amIpilot = false;
        adultHasBoat = false;

        numOfChildrenOnOahu = 0;
        numOfChildrenOnMolokai = 0;
        numOfChildrenWaitingForBoat = 0;
        boatPasses = 0;
        numOfAdultsOnOahu = 0;
        numOfAdultsOnMolokai = 0;
        message = 0;

        lock = new Lock();

        childOnOahu = new Condition2(lock);
        childOnMolokai = new Condition2(lock);
        adultOnOahu = new Condition2(lock);
        boat = new Condition2(lock);

        alarm = new Alarm();

        walkyTalky = new Communicator();

        if (prints) {
            print("***Created Instance varibles***");
        }

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

        Runnable a = new Runnable() {
            public void run() {
                AdultItinerary();
            }
        };

        if (prints) {
            print("***Created Adult Itinerary***");
        }

        Runnable c = new Runnable() {
            public void run() {
                ChildItinerary();
            }
        };

        if (prints) {
            print("***Created Child Itinerary***");
        }

        for (int i = 1; i <= children; i++) {

            KThread child = new KThread(c);
            child.setName("Child: " + i);
            if (prints) {
                print("***Child " + i + " created***");
            }
            child.fork();
        }

        for (int j = 1; j <= adults; j++) {
            KThread adult = new KThread(a);
            adult.setName("Adult: " + j);
            if (prints) {
                print("***Adult " + j + " created***");
            }
            adult.fork();
        }

        int threadsCraeated = children + adults;

        while (threadsCraeated != message) {
            if (prints) {
                print("***Parent Thread went to sleep***");
            }
            message = walkyTalky.listen();
        }
        if (prints) {
            print("***Everybody got across***");
        }
        done = true;

        alarm.waitUntil(threadsCraeated * 150);
        if (prints) {
            print("***All Threads joined***");
        }
    }

    /**
     * When an Adult is created he will be placed in the ready queue.
     * Upon running for the 1st time, the adult will increment a counter for the 
     * the # of adults on Oahu and then go strait to sleep. An adult can only be
     * woken up by a child returning from Molokai at which time he will take the 
     * boat across, decrement the counter for the # of adults on Oahu,
     * increment a counter for the the # of adults on Molokai, wake 
     * up a child, & finish execution. 
     */
    static void AdultItinerary() {
        /* This is where you should put your solutions. Make calls
         to the BoatGrader to show that it is synchronized. For
         example:
         bg.AdultRowToMolokai();
         indicates that an adult has rowed the boat across to Molokai
         */
        lock.acquire();
        numOfAdultsOnOahu++;
        if (prints) {
            print("***Adult went to sleep on Oahu***");
        }
        adultOnOahu.sleep();
        if (prints) {
            print("***Adult woke up***");
        }
        if (bgPrints) {
            bg.AdultRowToMolokai();
        }
        if (prints) {
            print("***Adult rowed to Molokai***");
        }
        numOfAdultsOnOahu--;
        boatLocation = "Molokai";
        numOfAdultsOnMolokai++;
        if (prints) {
            print("***Adult tried to wake up a child on Molokai***");
        }
        childOnMolokai.wake();
        adultHasBoat = false;
        if (prints) {
            print("***Adult Thead terminated***");
        }
        lock.release();
    }

    
    /**
     * When a child is created he will be placed in the ready queue.
     * Upon running for the 1st time, the child will increment a counter for the 
     * the # of children on Oahu. The child will then check to see if the boat 
     * is on Oahu and if so, if the is already 2 other children or an adult 
     * in the boat. If so the child will go to sleep. If the boat is on Oahu and
     * there is no other children in the boat the child will sleep go to sleep 
     * in the boat and wait for another child to arrive on Oahu. If there is 
     * already a child sleeping in the boat the child will wake him up and they 
     * will both cross. Up landing on Oahu the children will in decrement the 
     * counter for the # of children on Oahu, increment a counter for the the # 
     * of children on Molokai, and one of them will go to sleep. The child that 
     * is still awake knows how many people were on the Oahu when he left so if 
     * there were none he will signal the Parent Thread, set an alarm and go to 
     * sleep. If there were still people on Oahu when he left or the alarm set 
     * when signaling the Parent Thread goes of the child will return to Oahu 
     * with the boat and increment/decrement the associated counters. If the 
     * Parent Thread determines that the children have got everybody across he 
     * will set the boolean variable done to true and when the signaling child's
     * alarm goes off he will break the loop, wake all of the other children
     * sleeping on Molokai up, and finish execution. Meanwhile the Parent Thread
     * will set an alarm and go to sleep to give the sleeping children time to 
     * wake up, break the loop, & finish executing.
     */
    static void ChildItinerary() {
        lock.acquire();
        while (!done) {
            numOfChildrenOnOahu++;
            boatPasses++;
            if (prints) {
                print("***Child arived on Oahu***");
            }

            while (boatLocation.equals("Molokai") || boatPasses > 2 || adultHasBoat) {

                boatPasses--;
                if (prints) {
                    print("***Child went to sleep on Oahu***");
                }
                childOnOahu.sleep();
                if (prints) {
                    print("***Child woke up on Oahu***");
                }
                boatPasses++;
            }

            childOnOahu.wake();

            numOfChildrenWaitingForBoat++;

            if (numOfChildrenWaitingForBoat < 2 | boatLocation.equals("Molokai")) {
                if (numOfChildrenOnOahu == 1 && isThereAtLeast1childOnMolokai) {

                    adultOnOahu.wake();
                    if (prints) {
                        print("***Child woke up Adult***");
                    }
                    adultHasBoat = true;
                }
                if (prints) {
                    print("***Child Pilot went to sleep on boat***");
                }
                boat.sleep();
                boatPasses -= 2;
                if (bgPrints) {
                    bg.ChildRideToMolokai();
                }
                if (prints) {
                    print("***Child Pilot woke up in boat & went to Molokai***");
                }
                numOfChildrenOnOahu -= 2;
                numOfChildrenOnMolokai += 2;
                numOfChildrenWaitingForBoat--;
            }
            boat.wake();

            if (!amIpilot) {
                amIpilot = true;
                numOfChildrenWaitingForBoat--;
                if (bgPrints) {
                    bg.ChildRowToMolokai();
                }
                if (prints) {
                    print("***Child passenger rode to Molokai***");
                }
                boatLocation = "Molokai";
                if (prints) {
                    print("***Child Passenger went to sleep on Molokai***");
                }
                childOnMolokai.sleep();
                if (prints) {
                    print("***Child old passenger woke up on Molokai***");
                }
            } else if (numOfChildrenOnOahu + numOfAdultsOnOahu == 0) {
                walkyTalky.speak(numOfChildrenOnMolokai + numOfAdultsOnMolokai);
                if (prints) {
                    print("***Child Pilot signaled Parent that he's done***");
                }
                alarm.waitUntil(50);

            }
            if (!done) {
                amIpilot = false;
                if (prints) {
                    print("***Child rowed back to Oahu***");
                }
                if (bgPrints) {
                    bg.ChildRowToOahu();
                }
                boatLocation = "Oahu";
                numOfChildrenOnMolokai--;
                isThereAtLeast1childOnMolokai = (numOfChildrenOnMolokai > 0);

                if (prints) {
                    print("***there is at least 1 child on Molokai? " + isThereAtLeast1childOnMolokai + " ***");
                }
            }
        }
        childOnMolokai.wakeAll();
        if (prints) {
            print("***Child Thead terminated***");
        }
        lock.release();

    }
}
