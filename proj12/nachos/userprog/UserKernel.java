package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import java.util.*;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
	super();
	
    }

    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler.
     */
    public void initialize(String[] args) {
	super.initialize(args);

	console = new SynchConsole(Machine.console());
	
	Machine.processor().setExceptionHandler(new Runnable() {
		public void run() { exceptionHandler(); }
	    });

	//initialize list of free pages and page lock
	pageLock = new Lock();
	freePages = new LinkedList<Integer>();
	
	//get total number of pages in memory
	int totalPages = Machine.processor().getNumPhysPages();
	
	//add them to the list of free pages
	for(int i = 0; i < totalPages; i++){
		freePages.add(i);
	}
    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
    LotterySchedulerTest.testPickNextThread();
    LotterySchedulerTest.simplePriorityDonationTest();

	super.selfTest();

	System.out.println("Testing the console device. Typed characters");
	System.out.println("will be echoed until q is typed.");

	char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	System.out.println("");
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();
	
	String shellProgram = Machine.getShellProgramName();	
	//Lib.assertTrue(process.execute(shellProgram, new String[] {"a","b","c","d","e","f","g","h","i","j"}));
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));
	KThread.currentThread().finish();
    }

    /**
     * Allocate pages when a process needs memory. Ensure no overlapping in memory.
     */  
    public List<Integer> malloc(int numPages) {
	pageLock.acquire();
	
	//make sure number of pages needed is a valid amount.
	if (numPages <= 0 || numPages > freePages.size()) {
        pageLock.release();
		return null;
	}

	List<Integer> toRet = new ArrayList<Integer>();

	for(int i = 0; i < numPages; i++) {
		toRet.add(freePages.remove());
	}

	pageLock.release();

	return toRet;
    }	
    

    /**
     * Free pages when a process's resources are released.
     */ 
    public void free(List<Integer> allocatedPages) {
	pageLock.acquire();

	//free allocated pages and add them to freePages
	for(Integer page: allocatedPages) {
		freePages.add(page);
	}

	pageLock.release();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }

    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;

    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
	
    //List of available pages
    private LinkedList<Integer> freePages;

    //Lock for synchronization while allocating/freeing pages
    private Lock pageLock;

}
