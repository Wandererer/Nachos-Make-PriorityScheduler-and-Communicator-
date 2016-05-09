package nachos.threads;

import nachos.machine.*;

/**
 * A KThread is a thread that can be used to execute Nachos kernel code. Nachos
 * allows multiple threads to run concurrently.
 * 
 * 
 * k스레드는 스레드로서 nachos커널 코드를 실행하는 스레드 입니다. 나초스는 여러개의 스래드를 동시에 실행할 수 있게 해줍니다.
 * 새로운 스레드를 만들어서 실행시키려면 우선 러너블 인터페이스 임플리먼트 되있는 클래스를 선언합니다.
 * 메소드를 임플리먼트 하고 클래스의 인스턴스가 할당될수있고 k스레드와 포그를 만들때 인수를 넘깁니다.
 * 예를 들어 스레드는 아래와 같이 계산 될수 있습니다.
 * 
 * 
 * To create a new thread of execution, first declare a class that implements
 * the <tt>Runnable</tt> interface. That class then implements the <tt>run</tt>
 * method. An instance of the class can then be allocated, passed as an
 * argument when creating <tt>KThread</tt>, and forked. For example, a thread
 * that computes pi could be written as follows:
 *
 * <p><blockquote><pre>
 * class PiRun implements Runnable {
 *     public void run() {
 *         // compute pi
 *         ...
 *     }
 * }
 * </pre></blockquote>
 * <p>The following code would then create a thread and start it running: 다음 코드들이 스레드를 만들고 시작을 합니다.
 *
 * <p><blockquote><pre>
 * PiRun p = new PiRun();
 * new KThread(p).fork();
 * </pre></blockquote>
 */
public class KThread 
{
    /**
     * Get the current thread. 현재 스레드를 받아옴
     *
     * @return	the current thread. 새로운 스레드 
     */
    public static KThread currentThread()
    {
	Lib.assertTrue(currentThread != null);
	return currentThread;
    }
    
    /**
     * Allocate a new <tt>KThread</tt>. If this is the first <tt>KThread</tt>,
     * create an idle thread as well.
     * 새로운 K스레드를 할당하고 만약 새로운 k스레드라면 idle 스레드를 만듭니다.
     */
    public KThread()
    {
    	if (currentThread != null) 
    	{
    		tcb = new TCB();
    	}	    
    	else 
    	{
    		readyQueue = ThreadedKernel.scheduler.newThreadQueue(false);
    		readyQueue.acquire(this);	    
    		
    		currentThread = this;
    		tcb = TCB.currentTCB();
    		name = "main";
    		restoreState();
    		
    		createIdleThread();
	    
    	}
    
    }

    /**
     * Allocate a new KThread. 새로운 k스레드 할당
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     *           run 메소드를 부르는 오브젝트를 노립니다.
     */
    public KThread(Runnable target) 
    {
    	this();
    	this.target = target;
    }

    /**
     * Set the target of this thread.  이 스레드를 목표로 잡습니다. 
     *
     * @param	target	the object whose <tt>run</tt> method is called.    run 메소드를 부르는 오브젝트를 노립니다.
     * @return	this thread.  이 스레드
     */
    public KThread setTarget(Runnable target) 
    {
    	Lib.assertTrue(status == statusNew);
	
    	this.target = target;
    	return this;
    }

    /**
     * Set the name of this thread. This name is used for debugging purposes
     * 이 스레드의 이름을 설정하고 이 이름은 오직 디버깅에서만 쓰입니다. 
     * only.
     *
     * @param	name	the name to give to this thread. 이 스레드의 이름을줌
     * @return	this thread.
     */
    public KThread setName(String name) 
    {
    	this.name = name;
    	return this;
    }

    /**
     * Get the name of this thread. This name is used for debugging purposes
     *  이 스레드의 이름을 설정하고 이 이름은 오직 디버깅에서만 쓰입니다. 
     * only.
     *
     * @return	the name given to this thread.
     */     
    public String getName() 
    {
    	return name;
    }

    /**
     * Get the full name of this thread. This includes its name along with its
     * 이 스레드 모든 이름을 가져오고. 그 이름은 수의 ID를 포함하고 이 이름은 오직 디버깅에만 쓰입니다. 
     * numerical ID. This name is used for debugging purposes only.
     *
     * @return	the full name given to this thread. 이 스레드에게 주어진 풀네임
     */
    public String toString()
    {
    	return (name + " (#" + id + ")");
    }

    /**
     * Deterministically and consistently compare this thread to another
     * 
     * 결정론적이고  지속적으로 이 스레드와 다른 스레드를 비교합니다.
     * 
     * thread.
     */
    public int compareTo(Object o) 
    {
    	KThread thread = (KThread) o;

    	if (id < thread.id)
    		return -1;
    	else if (id > thread.id)
    		return 1;
    	else
    		return 0;
    }

    /**
     * Causes this thread to begin execution. The result is that two threads
     * are running concurrently: the current thread (which returns from the
     * call to the <tt>fork</tt> method) and the other thread (which executes
     * its target's <tt>run</tt> method).
     * 
     * 이 스레드가 실행되게 되면 결과는 두 스레드가 동시에 실행됨: 현재 스레드 (포크메소드에 의해 불려온거)
     * 와 다른 스레드 (run메소드를 노림)
     */
    public void fork() 
    {
    	Lib.assertTrue(status == statusNew); // 현재 상태와 다른 상태가 다르면 안됨  	 
    	Lib.assertTrue(target != null); //스레드할게 널이아니면 안됨  
	
    	Lib.debug(dbgThread, "Forking thread: " + toString() + " Runnable: " + target);

    	boolean intStatus = Machine.interrupt().disable(); //인터럽트가 일어나지 않게 일단 변수로 지정

    	tcb.start(new Runnable()//Runnable(){ } 
    	{
    				public void run()
    				{
    						runThread();
    				}
	    });

    	ready();
	
		Machine.interrupt().restore(intStatus);
    }

    private void runThread() 
    {
    	begin();
    	target.run();
    	finish();
    }

    private void begin() {
	Lib.debug(dbgThread, "Beginning thread: " + toString());
	
	Lib.assertTrue(this == currentThread);

	restoreState();

	Machine.interrupt().enable();
    }

    /**
     * Finish the current thread and schedule it to be destroyed when it is
     * safe to do so. This method is automatically called when a thread's
     * <tt>run</tt> method returns, but it may also be called directly.
     * 
     * 현재 스레드를 끝내고 안전할경우 끝냅니다. 이 메소드는 자동적으로 스레드가 run메소드를 반환하면 불려집니다.
     * 하지만 직접 불려질수도 있습니다.
     * 
     * 현재 스레드는 바로 끝내질수 없습니다. 왜냐하면 이 스택과 다른 실행 상태들이 사용중이기 때문입니다. 
     * 대신에 이 스레드가 안전하게 지워질경우 다음 스레드가 실행되면 자동적으로 끝낼것입니다.
     *
     * The current thread cannot be immediately destroyed because its stack and
     * other execution state are still in use. Instead, this thread will be
     * destroyed automatically by the next thread to run, when it is safe to
     * delete this thread.
     */
    public static void finish() {
	Lib.debug(dbgThread, "Finishing thread: " + currentThread.toString());
	
	Machine.interrupt().disable();

	Machine.autoGrader().finishingCurrentThread();

	Lib.assertTrue(toBeDestroyed == null);
	toBeDestroyed = currentThread;


	currentThread.status = statusFinished;
	
	sleep();
    }

    /**
     * Relinquish the CPU if any other thread is ready to run. If so, put the
     * current thread on the ready queue, so that it will eventually be
     * rescheuled.
     * 
     * 다른 스레드가 run 준비가 되면 cpu를 포기하고 그렇다면 현재 스레드를 ready큐에 집어넣고 그러면 결국 스케쥴을 다시 짤것입니다.
     *
     * <p>
     * Returns immediately if no other thread is ready to run. Otherwise
     * returns when the current thread is chosen to run again by
     * <tt>readyQueue.nextThread()</tt>.
     *
     *다른 스레드가 실행준비된 것이 없으면 바로 반환을 하고 그렇지 않다면 현재 스레드가 레디큐.nextthread 에의해 다시 실행되도록 선택 될때 반환 합니다.
     *
     * <p>
     * Interrupts are disabled, so that the current thread can atomically add
     * itself to the ready queue and switch to the next thread. On return,
     * restores interrupts to the previous state, in case <tt>yield()</tt> was
     * called with interrupts disabled.
     * 
     * 인터럽트는 비활성화 되고 그렇다면 현재 스레드는 자동적으로 ready큐에 들어가고 다음 스레드랑 바뀌어 집니다.
     * 반환되면 이전 인터럽트 상태를 저장해놓고 이때에는 yield가 이전에 불러와져서 인터럽트가 비활성화 됩니다. (yield가 불러와야 비활성화됨)
     */
    public static void yield() {
	Lib.debug(dbgThread, "Yielding thread: " + currentThread.toString());
	
	Lib.assertTrue(currentThread.status == statusRunning);
	
	boolean intStatus = Machine.interrupt().disable();

	currentThread.ready();

	runNextThread();
	
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Relinquish the CPU, because the current thread has either finished or it
     * is blocked. This thread must be the current thread.
     *
     *현재 스레드가 끝나거나 막혀지지않았으면 cpu를 포기하고 이 스레드가 반드시 현재 스레드가 됩니다.
     *Semaphore= 여러 줄이 있을때 작업의 딜레이가 발생하더라도 다른 쪽으로 스레드를 넘기지 않고 한번에 작업
     * <p>
     * If the current thread is blocked (on a synchronization primitive, i.e.
     * a <tt>Semaphore</tt>, <tt>Lock</tt>, or <tt>Condition</tt>), eventually
     * some thread will wake this thread up, putting it back on the ready queue
     * so that it can be rescheduled. Otherwise, <tt>finish()</tt> should have
     * scheduled this thread to be destroyed by the next thread to run.
     * 
     * 만약 현재스레드가 막혔다면(상호배제) 결국 몇 스레드는 이 스레드 업에 의해 깨어날 것이고 레디큐에 다시 집어넣어져서 리슈케줄 할수 있습니다
     * 그렇지 않으면 finish()가 이 스레가 다음 run될 스레드에 의해 끝나도록 조정합니다.
     * 
     */
    public static void sleep() {
	Lib.debug(dbgThread, "Sleeping thread: " + currentThread.toString());
	
	Lib.assertTrue(Machine.interrupt().disabled());

	if (currentThread.status != statusFinished)
	    currentThread.status = statusBlocked;

	runNextThread();
    }

    /**
     * Moves this thread to the ready state and adds this to the scheduler's
     * ready queue.
     * 
     * 이 스레드를 대기 상태로 옮기고 상태가 레디큐 스케쥴러에 더합니다.
     */
    public void ready() {
	Lib.debug(dbgThread, "Ready thread: " + toString());
	
	Lib.assertTrue(Machine.interrupt().disabled());
	Lib.assertTrue(status != statusReady);
	
	status = statusReady;
	if (this != idleThread)
	    readyQueue.waitForAccess(this);
	
	Machine.autoGrader().readyThread(this);
    }

    /**
     * Waits for this thread to finish. If this thread is already finished,
     * return immediately. This method must only be called once; the second
     * call is not guaranteed to return. This thread must not be the current
     * thread.
     * 
     * 이 스레드가 끝나길 기다리고 만약 이 스레드가 이미 끝났다면 즉시 반환을 합니다
     * 이 메소드는 한번만 불려지고 다음에 불리면 반환되는 걸 장담할수 없습니다. 이 스레드는 반드시 현재스레드가 되면 안됩니다.
     */
    public void join()
    {
	Lib.debug(dbgThread, "Joining to thread: " + toString());

	Lib.assertTrue(this != currentThread);

    }

    /**
     * Create the idle thread. Whenever there are no threads ready to be run,
     * and <tt>runNextThread()</tt> is called, it will run the idle thread. The
     * idle thread must never block, and it will only be allowed to run when
     * all other threads are blocked.
     *
     *평시상태 스레드를 만들고 언제든지 run 준비된 스레드가 없으며 runnextthread도 불려지면 안됩니다. 그것은 idle스레드를 run할것입니다.
     *idle 스레드는 절대 막히면 안되고 다른 스레드가 모두 막혔을 경우에만 블락 될것입니다.
     *
     * <p>
     * Note that <tt>ready()</tt> never adds the idle thread to the ready set.
     * 
     * ready()는 절대 idle스레드를 readyset에 넣지 않습니다.
     */
    private static void createIdleThread() {
	Lib.assertTrue(idleThread == null);
	
	idleThread = new KThread(new Runnable() {
	    public void run() { while (true) yield(); }
	});
	idleThread.setName("idle");

	Machine.autoGrader().setIdleThread(idleThread);
	
	idleThread.fork();
    }
    
    /**
     * Determine the next thread to run, then dispatch the CPU to the thread
     * using <tt>run()</tt>.
     */
    private static void runNextThread() {
	KThread nextThread = readyQueue.nextThread();
	if (nextThread == null)
	    nextThread = idleThread;

	nextThread.run();
    }

    /**
     * Dispatch the CPU to this thread. Save the state of the current thread,
     * switch to the new thread by calling <tt>TCB.contextSwitch()</tt>, and
     * load the state of the new thread. The new thread becomes the current
     * thread.
     * 
     * cpu에 이 스레드를 올리고 현재 스레드의 상태를 저장하고 다른 스레드를 호줄하면서 바꿉니다. tcb.contextswitch이용
     * 그리고 새로운 스레드의 상태를 로드하고 새로운 스레드는 현재스레드가 됩니다.
     *
     * <p>
     * If the new thread and the old thread are the same, this method must
     * still call <tt>saveState()</tt>, <tt>contextSwitch()</tt>, and
     * <tt>restoreState()</tt>.
     * 
     * 새로운 스레드가 예전 스레드와 똑같아면  savestate, contextswitch restorestate메소드가 계속 불려와 져야합니다.
     * 
     *
     * <p>
     * The state of the previously running thread must already have been
     * changed from running to blocked or ready (depending on whether the
     * thread is sleeping or yielding).
     * 
     * 이전에 실행중이던 스레드는 실행모드에서 블락 레디 모드로 반드시 바껴야 합니다.(스레드가 자고있냐 기다리고 있느냐에 달렸음)
     *
     * @param	finishing	<tt>true</tt> if the current thread is
     *				finished, and should be destroyed by the new
     *				thread.
     *
     *끝 참 만약 현재 스레드가 끝나면 새로운 스레드에 의해 끝남 당해야함
     */
    private void run() {
	Lib.assertTrue(Machine.interrupt().disabled());

	Machine.yield();

	currentThread.saveState();

	Lib.debug(dbgThread, "Switching from: " + currentThread.toString()
		  + " to: " + toString());

	currentThread = this;

	tcb.contextSwitch();  

	currentThread.restoreState();
    }

    /**
     * Prepare this thread to be run. Set <tt>status</tt> to
     * <tt>statusRunning</tt> and check <tt>toBeDestroyed</tt>.
     * 
     * 이 스레드가 run될걸 준비해야하고 상태 설정은 statusruninig으로 하고 tobedestroyd 확인함
     */
    protected void restoreState()
    {
	Lib.debug(dbgThread, "Running thread: " + currentThread.toString());
	
	Lib.assertTrue(Machine.interrupt().disabled());
	Lib.assertTrue(this == currentThread);
	Lib.assertTrue(tcb == TCB.currentTCB());

	Machine.autoGrader().runningThread(this);
	
	status = statusRunning;

		if (toBeDestroyed != null)
		{
			toBeDestroyed.tcb.destroy();
	    	toBeDestroyed.tcb = null;
	    	toBeDestroyed = null;
		}
    }

    /**
     * Prepare this thread to give up the processor. Kernel threads do not
     * need to do anything here.
     * 
     * 이 스레드가 프로세서를 포기할 준비를 하고 커널 스레드는 여기서는 아무것도 하지 않습니다.
     */
    protected void saveState() 
    {
	Lib.assertTrue(Machine.interrupt().disabled());
	Lib.assertTrue(this == currentThread);
    }

    private static class PingTest implements Runnable 
    {
    	PingTest(int which) 
    	{
	    this.which = which;
    	}
	
    	public void run() 
    	{
    		for (int i=0; i<10; i++)
	    	{
    			System.out.println("*** thread " + which + " looped "   + i + " times");
    			currentThread.yield();
	    	}
    	}

			private int which;
    }	

    /**
     * Tests whether this module is working. 이 모듈이 실행되고 있는지 아닌지 테스트함
     */
    public static void selfTest() 
    {
    	Lib.debug(dbgThread, "Enter KThread.selfTest");
    	// 현재 스레드에서 자식스레드 포크(forked thread) 1. 1자식
    	//포크는 부모를 리턴하므로 현재 스레드는 부모
    	new KThread(new PingTest(1)).setName("forked thread").fork(); 
    	new PingTest(0).run(); //부모가 0 자식이 1임
    	//new PingTest(1).run();
    }

    private static final char dbgThread = 't';

    /**
     * Additional state used by schedulers. 부가적인 상태는 스케줄러에 의해 사용됨
     *
     * @see	nachos.threads.Prior ityScheduler.ThreadState
     */
    public Object schedulingState = null;

    private static final int statusNew = 0;
    private static final int statusReady = 1;
    private static final int statusRunning = 2;
    private static final int statusBlocked = 3;
    private static final int statusFinished = 4;

    /**
     * The status of this thread. A thread can either be new (not yet forked),
     * ready (on the ready queue but not running), running, or blocked (not
     * on the ready queue and not running).
     * 
     * 이 스레드의 상태입니다.  스레드는 새로(포크안됨) 준비(레디큐에만 있음) 실행, 블락(레디큐에도 없고 실행도 아님) 될수 있음
     */
    private int status = statusNew;
    private String name = "(unnamed thread)";
    private Runnable target;
    private TCB tcb;

    /**
     * Unique identifer for this thread. Used to deterministically compare
     * threads.
     * 
     * 이스레드의 특정 식별자는 결정론적으로 비교하곤 했다 
     */
    private int id = numCreated++;
    /** Number of times the KThread constructor was called. */
    private static int numCreated = 0;

    private static ThreadQueue readyQueue = null;
    private static KThread currentThread = null;
    private static KThread toBeDestroyed = null;
    private static KThread idleThread = null;
}

