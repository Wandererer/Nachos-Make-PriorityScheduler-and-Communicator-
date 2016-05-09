package nachos.threads;

import nachos.machine.*;

/**
 * A KThread is a thread that can be used to execute Nachos kernel code. Nachos
 * allows multiple threads to run concurrently.
 * 
 * 
 * k������� ������μ� nachosĿ�� �ڵ带 �����ϴ� ������ �Դϴ�. ���ʽ��� �������� �����带 ���ÿ� ������ �� �ְ� ���ݴϴ�.
 * ���ο� �����带 ���� �����Ű���� �켱 ���ʺ� �������̽� ���ø���Ʈ ���ִ� Ŭ������ �����մϴ�.
 * �޼ҵ带 ���ø���Ʈ �ϰ� Ŭ������ �ν��Ͻ��� �Ҵ�ɼ��ְ� k������� ���׸� ���鶧 �μ��� �ѱ�ϴ�.
 * ���� ��� ������� �Ʒ��� ���� ��� �ɼ� �ֽ��ϴ�.
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
 * <p>The following code would then create a thread and start it running: ���� �ڵ���� �����带 ����� ������ �մϴ�.
 *
 * <p><blockquote><pre>
 * PiRun p = new PiRun();
 * new KThread(p).fork();
 * </pre></blockquote>
 */
public class KThread 
{
    /**
     * Get the current thread. ���� �����带 �޾ƿ�
     *
     * @return	the current thread. ���ο� ������ 
     */
    public static KThread currentThread()
    {
	Lib.assertTrue(currentThread != null);
	return currentThread;
    }
    
    /**
     * Allocate a new <tt>KThread</tt>. If this is the first <tt>KThread</tt>,
     * create an idle thread as well.
     * ���ο� K�����带 �Ҵ��ϰ� ���� ���ο� k�������� idle �����带 ����ϴ�.
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
     * Allocate a new KThread. ���ο� k������ �Ҵ�
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     *           run �޼ҵ带 �θ��� ������Ʈ�� �븳�ϴ�.
     */
    public KThread(Runnable target) 
    {
    	this();
    	this.target = target;
    }

    /**
     * Set the target of this thread.  �� �����带 ��ǥ�� ����ϴ�. 
     *
     * @param	target	the object whose <tt>run</tt> method is called.    run �޼ҵ带 �θ��� ������Ʈ�� �븳�ϴ�.
     * @return	this thread.  �� ������
     */
    public KThread setTarget(Runnable target) 
    {
    	Lib.assertTrue(status == statusNew);
	
    	this.target = target;
    	return this;
    }

    /**
     * Set the name of this thread. This name is used for debugging purposes
     * �� �������� �̸��� �����ϰ� �� �̸��� ���� ����뿡���� ���Դϴ�. 
     * only.
     *
     * @param	name	the name to give to this thread. �� �������� �̸�����
     * @return	this thread.
     */
    public KThread setName(String name) 
    {
    	this.name = name;
    	return this;
    }

    /**
     * Get the name of this thread. This name is used for debugging purposes
     *  �� �������� �̸��� �����ϰ� �� �̸��� ���� ����뿡���� ���Դϴ�. 
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
     * �� ������ ��� �̸��� ��������. �� �̸��� ���� ID�� �����ϰ� �� �̸��� ���� ����뿡�� ���Դϴ�. 
     * numerical ID. This name is used for debugging purposes only.
     *
     * @return	the full name given to this thread. �� �����忡�� �־��� Ǯ����
     */
    public String toString()
    {
    	return (name + " (#" + id + ")");
    }

    /**
     * Deterministically and consistently compare this thread to another
     * 
     * ���������̰�  ���������� �� ������� �ٸ� �����带 ���մϴ�.
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
     * �� �����尡 ����ǰ� �Ǹ� ����� �� �����尡 ���ÿ� �����: ���� ������ (��ũ�޼ҵ忡 ���� �ҷ��°�)
     * �� �ٸ� ������ (run�޼ҵ带 �븲)
     */
    public void fork() 
    {
    	Lib.assertTrue(status == statusNew); // ���� ���¿� �ٸ� ���°� �ٸ��� �ȵ�  	 
    	Lib.assertTrue(target != null); //�������Ұ� ���̾ƴϸ� �ȵ�  
	
    	Lib.debug(dbgThread, "Forking thread: " + toString() + " Runnable: " + target);

    	boolean intStatus = Machine.interrupt().disable(); //���ͷ�Ʈ�� �Ͼ�� �ʰ� �ϴ� ������ ����

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
     * ���� �����带 ������ �����Ұ�� �����ϴ�. �� �޼ҵ�� �ڵ������� �����尡 run�޼ҵ带 ��ȯ�ϸ� �ҷ����ϴ�.
     * ������ ���� �ҷ������� �ֽ��ϴ�.
     * 
     * ���� ������� �ٷ� �������� �����ϴ�. �ֳ��ϸ� �� ���ð� �ٸ� ���� ���µ��� ������̱� �����Դϴ�. 
     * ��ſ� �� �����尡 �����ϰ� ��������� ���� �����尡 ����Ǹ� �ڵ������� �������Դϴ�.
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
     * �ٸ� �����尡 run �غ� �Ǹ� cpu�� �����ϰ� �׷��ٸ� ���� �����带 readyť�� ����ְ� �׷��� �ᱹ �������� �ٽ� ©���Դϴ�.
     *
     * <p>
     * Returns immediately if no other thread is ready to run. Otherwise
     * returns when the current thread is chosen to run again by
     * <tt>readyQueue.nextThread()</tt>.
     *
     *�ٸ� �����尡 �����غ�� ���� ������ �ٷ� ��ȯ�� �ϰ� �׷��� �ʴٸ� ���� �����尡 ����ť.nextthread ������ �ٽ� ����ǵ��� ���� �ɶ� ��ȯ �մϴ�.
     *
     * <p>
     * Interrupts are disabled, so that the current thread can atomically add
     * itself to the ready queue and switch to the next thread. On return,
     * restores interrupts to the previous state, in case <tt>yield()</tt> was
     * called with interrupts disabled.
     * 
     * ���ͷ�Ʈ�� ��Ȱ��ȭ �ǰ� �׷��ٸ� ���� ������� �ڵ������� readyť�� ���� ���� ������� �ٲ�� ���ϴ�.
     * ��ȯ�Ǹ� ���� ���ͷ�Ʈ ���¸� �����س��� �̶����� yield�� ������ �ҷ������� ���ͷ�Ʈ�� ��Ȱ��ȭ �˴ϴ�. (yield�� �ҷ��;� ��Ȱ��ȭ��)
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
     *���� �����尡 �����ų� ���������ʾ����� cpu�� �����ϰ� �� �����尡 �ݵ�� ���� �����尡 �˴ϴ�.
     *Semaphore= ���� ���� ������ �۾��� �����̰� �߻��ϴ��� �ٸ� ������ �����带 �ѱ��� �ʰ� �ѹ��� �۾�
     * <p>
     * If the current thread is blocked (on a synchronization primitive, i.e.
     * a <tt>Semaphore</tt>, <tt>Lock</tt>, or <tt>Condition</tt>), eventually
     * some thread will wake this thread up, putting it back on the ready queue
     * so that it can be rescheduled. Otherwise, <tt>finish()</tt> should have
     * scheduled this thread to be destroyed by the next thread to run.
     * 
     * ���� ���罺���尡 �����ٸ�(��ȣ����) �ᱹ �� ������� �� ������ ���� ���� ��� ���̰� ����ť�� �ٽ� ����־����� �������� �Ҽ� �ֽ��ϴ�
     * �׷��� ������ finish()�� �� ������ ���� run�� �����忡 ���� �������� �����մϴ�.
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
     * �� �����带 ��� ���·� �ű�� ���°� ����ť �����췯�� ���մϴ�.
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
     * �� �����尡 ������ ��ٸ��� ���� �� �����尡 �̹� �����ٸ� ��� ��ȯ�� �մϴ�
     * �� �޼ҵ�� �ѹ��� �ҷ����� ������ �Ҹ��� ��ȯ�Ǵ� �� ����Ҽ� �����ϴ�. �� ������� �ݵ�� ���罺���尡 �Ǹ� �ȵ˴ϴ�.
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
     *��û��� �����带 ����� �������� run �غ�� �����尡 ������ runnextthread�� �ҷ����� �ȵ˴ϴ�. �װ��� idle�����带 run�Ұ��Դϴ�.
     *idle ������� ���� ������ �ȵǰ� �ٸ� �����尡 ��� ������ ��쿡�� ��� �ɰ��Դϴ�.
     *
     * <p>
     * Note that <tt>ready()</tt> never adds the idle thread to the ready set.
     * 
     * ready()�� ���� idle�����带 readyset�� ���� �ʽ��ϴ�.
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
     * cpu�� �� �����带 �ø��� ���� �������� ���¸� �����ϰ� �ٸ� �����带 ȣ���ϸ鼭 �ٲߴϴ�. tcb.contextswitch�̿�
     * �׸��� ���ο� �������� ���¸� �ε��ϰ� ���ο� ������� ���罺���尡 �˴ϴ�.
     *
     * <p>
     * If the new thread and the old thread are the same, this method must
     * still call <tt>saveState()</tt>, <tt>contextSwitch()</tt>, and
     * <tt>restoreState()</tt>.
     * 
     * ���ο� �����尡 ���� ������� �Ȱ��Ƹ�  savestate, contextswitch restorestate�޼ҵ尡 ��� �ҷ��� �����մϴ�.
     * 
     *
     * <p>
     * The state of the previously running thread must already have been
     * changed from running to blocked or ready (depending on whether the
     * thread is sleeping or yielding).
     * 
     * ������ �������̴� ������� �����忡�� ��� ���� ���� �ݵ�� �ٲ��� �մϴ�.(�����尡 �ڰ��ֳ� ��ٸ��� �ִ��Ŀ� �޷���)
     *
     * @param	finishing	<tt>true</tt> if the current thread is
     *				finished, and should be destroyed by the new
     *				thread.
     *
     *�� �� ���� ���� �����尡 ������ ���ο� �����忡 ���� ���� ���ؾ���
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
     * �� �����尡 run�ɰ� �غ��ؾ��ϰ� ���� ������ statusruninig���� �ϰ� tobedestroyd Ȯ����
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
     * �� �����尡 ���μ����� ������ �غ� �ϰ� Ŀ�� ������� ���⼭�� �ƹ��͵� ���� �ʽ��ϴ�.
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
     * Tests whether this module is working. �� ����� ����ǰ� �ִ��� �ƴ��� �׽�Ʈ��
     */
    public static void selfTest() 
    {
    	Lib.debug(dbgThread, "Enter KThread.selfTest");
    	// ���� �����忡�� �ڽĽ����� ��ũ(forked thread) 1. 1�ڽ�
    	//��ũ�� �θ� �����ϹǷ� ���� ������� �θ�
    	new KThread(new PingTest(1)).setName("forked thread").fork(); 
    	new PingTest(0).run(); //�θ� 0 �ڽ��� 1��
    	//new PingTest(1).run();
    }

    private static final char dbgThread = 't';

    /**
     * Additional state used by schedulers. �ΰ����� ���´� �����ٷ��� ���� ����
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
     * �� �������� �����Դϴ�.  ������� ����(��ũ�ȵ�) �غ�(����ť���� ����) ����, ���(����ť���� ���� ���൵ �ƴ�) �ɼ� ����
     */
    private int status = statusNew;
    private String name = "(unnamed thread)";
    private Runnable target;
    private TCB tcb;

    /**
     * Unique identifer for this thread. Used to deterministically compare
     * threads.
     * 
     * �̽������� Ư�� �ĺ��ڴ� ������������ ���ϰ� �ߴ� 
     */
    private int id = numCreated++;
    /** Number of times the KThread constructor was called. */
    private static int numCreated = 0;

    private static ThreadQueue readyQueue = null;
    private static KThread currentThread = null;
    private static KThread toBeDestroyed = null;
    private static KThread idleThread = null;
}

