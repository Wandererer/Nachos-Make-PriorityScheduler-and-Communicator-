package nachos.threads;

import java.util.Vector;

import nachos.machine.Lib;
import nachos.machine.Machine;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	
	/**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
        

    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }
    

    public int getPriority(KThread thread) {	//�켱������ ���� �޼ҵ�. �� �޼ҵ带 ȣ���ϸ� PrioritSchedulerŬ������ ���ο� �ִ� ThreadStateŬ������ getPriority()�� ȣ���ؼ� �켱������ �޾ƿ´�.	-jmh-
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();	//�Ű������� �޾ƿ� �������� ThreadState�� ȣ���ؼ� �켱������ �޾ƿ´�.	-jmh-
    }

    public int getEffectivePriority(KThread thread) {	//���� �켱������ ���� �޼ҵ�, �� �޼ҵ带 ȣ���ϸ� PrioritSchedulerŬ������ ���ο� �ִ� ThreadStateŬ������ geteffectPriority()�� ȣ���ؼ� �켱������ �޾ƿ´�.	-jmh-
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();	//�Ű������� �޾ƿ� �������� ThreadState�� ȣ���ؼ� ���� �켱������ �޾ƿ´�.		-jmh-
    }

    public void setPriority(KThread thread, int priority) {	//�������� �켱������ �����ϴ� �޼ҵ�	-jmh-
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);	//�����ϰ��� �ϴ� �켱������ ���Ǵ� �켱������ ������ �ƴѰ�� ����ó���� ���ش�.	-jmh-
	
	getThreadState(thread).setPriority(priority);	//�Ű������� �������� ThreadState�� ȣ���ϰ� �Ű������� �켱������ ThreadState�� setPriority�� ȣ���Ͽ� �켱������ �������ش�.	-jmh-
    }

    public boolean increasePriority() {	//�켱���Ǹ� ������Ű�� �޼ҵ�	-jmh-
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();	//���罺���带 �޾ƿͼ� thread�� �����Ѵ�.	-jmh-

	int priority = getPriority(thread);	//priority�� �����ϰ� ���� �������� �켱������ �����Ѵ�.	-jmh-
	if (priority == priorityMaximum)	//���� ���� �������� �켱������ �켱������ �ְ� �����̸�		-jmh-
	    return false;	//�� �̻� �켱������ ������ �� �����Ƿ� false�� ��ȯ�Ѵ�.	-jmh-

	setPriority(thread, priority+1);	//�켱 ������ ���� �� �� �ִ� ��� �켱������ 1���� �����ش�.	-jmh-

	Machine.interrupt().restore(intStatus);
	return true;	//���������� �켱������ ������������ true�� ��ȯ�Ѵ�.	-jmh-
    }

    public boolean decreasePriority() {	//�켱������ ���ҽ�Ű�� �޼ҵ�	-jmh-
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();	//���罺���带 �޾ƿͼ� thread�� �����Ѵ�.	-jmh-

	int priority = getPriority(thread);	//priority�� �����ϰ� ���� �������� �켱������ �����Ѵ�.	-jmh-
	if (priority == priorityMinimum)	//���� ���� �������� �켱������ �켱������ �ּ� �����̸�	-jmh-
	    return false;	//�� �̻� �켱������ ���ұ�ų �� �����Ƿ� false�� ��ȯ�Ѵ�.	-jmh-

	setPriority(thread, priority-1);	//�켱 ������ ���� ��ų �� �ִ� ��� �켱������ 1���� �����ش�.	-jmh-

	Machine.interrupt().restore(intStatus);
	return true;	//���������� �켱������ ���ҽ������� true�� ��ȯ�Ѵ�.	-jmh-
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;	//�⺻ �켱������ 1�� ����	-jmh-
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;	//�ּ� �켱������ 0���� ����	-jmh-
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    //�ִ� �켱������ 7�� ����	-jmh-

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {	//�������� ���¸� ������ �޼ҵ� ��ȯ���� ThreadState	-jmh-
	if (thread.schedulingState == null)	//���� schedulingState�� �������� �ʾ����� ��, ������ �����ÿ� schedulingState�� �����ϴ� �κ��̴�.	-jmh-
	    thread.schedulingState = new ThreadState(thread);	//ThreadState�� �����Ѵ�.	-jmh-

	return (ThreadState) thread.schedulingState;	//�������� schedulingState�� ThreadState���·� ��ȯ��	-jmh-
    }


    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;	//�켱������ ���� ���ο� ���Ѱ��� �����Ѵ�.	-jmh-
	}

	public void waitForAccess(KThread thread) {	//�켱���� ť�� �����带 ����ֱ����� �޼ҵ�	-jmh-
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);	//thread�� ThreadState�� �ҷ��ͼ� �� thread�� �켱����ť�� ����ִ´�.	-jmh-
	}

	public void acquire(KThread thread) {	//�켱���� ť���� �����带 ������ ���� �޼ҵ�	-jmh-
	    Lib.assertTrue(Machine.interrupt().disabled());
	    //�߰�
	    this.PrioritywaitQueue.remove(thread);	//������ �����带 ť���� ����	-jmh-
	    if(transferPriority)	//PrioritywaitQueue�� ��ȭ�� ������, �� �����带 ����ְų� ������	-jmh-
	    	getThreadState(thread).acquire(this);	//thread�� ThreadState�� acquire�� PrioritywaitQueue�� �Ű������� �����Ѵ�.
	}

	public KThread nextThread() {	//�켱���� ť���� ������ ���� �ؾߵ� �����带 �����ϱ����� �޼ҵ�	-jmh-
	    Lib.assertTrue(Machine.interrupt().disabled());
	    //�߰�
	    if(currentState != null)	//���� �����ؾߵ� �����尡 ������		-jmh-
	    	currentState.calculaterPriority(this);	//���� �������� �켱������ �ְ�� �����ش�.	-jmh-
	    
	    ThreadState tempState = pickNextThread();	//tempState�� �����ϰ� ���� �����ؾ� �� �������� ���¸� �����Ѵ�.	-jmh-
	    if (tempState == null)	//���� �����ؾ� �� �������� ���°� null�̸� ��, ���� ���� �ؾ� �� �����尡 ������	-jmh-
	    	return null;	//null�� ��ȯ�Ѵ�.	-jmh-
	    acquire(tempState.thread);	//������ �����ؾ� �� �����带 PrioritywaitQueue���� �����Ѵ�.	-jmh-
	    return (KThread) tempState.thread;	//������ �����ؾߵ� �����带 KThreadŸ������ ��ȯ�Ѵ�.	-jmh-
	}
	
		
	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {		
		
		//����
		if (PrioritywaitQueue.isEmpty())	//PrioritywaitQueue�� �����ؾ� �� ���� �����尡 ���� ���	-jmh-
			return null;	//null�� ��ȯ�Ѵ�.	-jmh-
		int comparePriority = -1;	//�񱳿� �켱������ priorityMinimum���� ���� -1 �� ����	-jmh-
		KThread nextThread = null;	//������ ����� �����带 �����ϴ� nextThread�� ����	-jmh-

		for(int i = 0;i < PrioritywaitQueue.size();i++) {	//PrioritywaitQueue�� ����ִ� �����带 ��� ��		-jmh-
			if(getEffectivePriority(PrioritywaitQueue.get(i)) > comparePriority) {	//�񱳴���� �����尡 comparePrioirity���� �켱������ ������	-jmh-
				comparePriority = getEffectivePriority(PrioritywaitQueue.get(i));	//comparePriority�� �켱������ �񱳴���� �������� �켱������ �ٲ��ش�.	-jmh-
				nextThread = PrioritywaitQueue.get(i);	//�񱳴���� �����带 ������ ������ ������� �����Ѵ�.	-jmh-
			}
		}
		
		if(nextThread == null)	//PrioritywaitQueue�� ��� Ž���ߴµ��� ������ ������ �����尡 ������	-jmh-
			return null;	//null�� ��ȯ�Ѵ�.	-jmh-
		
		return getThreadState(nextThread);	//������ ������ �������� ���¸� ��ȯ�Ѵ�.	-jmh-
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;	//PrioritywaitQueue�� �������� ������ �ִ��� üũ�ϴ� ����	-jmh-
	
	private Vector<KThread> PrioritywaitQueue = new Vector<KThread>();	//�����带 ������ queue�� ����	-jmh-
	ThreadState currentState;	//���� ���¿� ���� ������ �̿��ϱ� ���� ThreadStateŸ������ currentState�� ����	-jmh-
    
   
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
   
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;	//�Ű������� �����带 ������ ������� ����	-jmh-

	    setPriority(priorityDefault);	//�켱������ �⺻ �켱������ ����		-jmh-

	}
	

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;	//�켱���� ��ȯ	-jmh-
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    return effectPriority;	//���� �켱���� ��ȯ	-jmh-
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {	//�켱������ �����ϴ� �޼ҵ�	-jmh
	    if (this.priority == priority)	//�����ϰ��� �ϴ� �켱������ ���� �켱������ ������ �״�� return�Ѵ�.	-jmh-
		return;
	    
	    this.priority = priority;	//�����ϰ��� �ϴ� �켱������ �������� �켱������ �������ش�.		-jmh-
	    this.effectPriority = priority;	//�����ϰ��� �ϴ� �켱������ �������� ���� �켱������ �������ش�.		-jmh-
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {	//PrioritywaitQueue�� �����带 �߰��ϱ����� �޼ҵ�	-jmh-
		waitQueue.PrioritywaitQueue.addElement(thread);		//PrioritywaitQueue�� �����带 �߰�	-jmh-
		calculaterPriority(waitQueue);	//PrioritywaitQueue�� �����带 �߰� �����Ƿ� �켱������ ����Ѵ�.	-jmh-		
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
		Lib.assertTrue(Machine.interrupt().disabled());
	    Lib.assertTrue(waitQueue.PrioritywaitQueue.isEmpty());	//PrioritywaitQueue�� ����ִ� ��� ����ó���� ���ش�.	-jmh-
	    
	    waitQueue.currentState = this;	//PrioritywaitQueue�� �����带 ������ ���¸� currentState�� �ݿ��Ѵ�.	-jmh-
	    calculaterPriority(waitQueue);	//PrioritywaitQueue�� �����带 ���� ������ �켱������ �ٽð���Ѵ�.	-jmh-
	}	
	
	private void calculaterPriority(PriorityQueue waitQueue) {	//�������� ������ ���� �켱������ �ٸ������庸�� ���� �����ϱ� ���� �޼ҵ�	-jmh-
		if (waitQueue.currentState != null) {	//������ �ؾߵ� �����尡 ������	-jmh-
			int highestPriority = waitQueue.currentState.getEffectivePriority();	//�����ؾߵ� �������� ���� �켱������ �޾Ƽ� �ٸ������庸�� ���� �켱������ �����ϴ� highestPriority�� ����	-jmh-
			
			for(KThread comp : waitQueue.PrioritywaitQueue) {	//PrioritywaitQueue�ȿ� �ִ� ��� �����带 ���ϱ� ���� foreach�� ����	-jmh-
				ThreadState temp = getThreadState(comp);	//PrioritywaitQueue�ȿ� �ִ� �������� �ϳ��� ���¸� �����´�.	-jmh-
				int compareP = temp.getEffectivePriority();		//���� �������� ���� �켱������ �����´�.	-jmh-
				
				if(compareP > highestPriority)	//������ �ʿ��� �������� �켱�������� PrioritywaitQueue�� �ִ� �������� �켱������ ������	-jmh
					highestPriority = compareP;		//�ٸ������庸�� ���� �켱������ �����ϴ� highestPriority�� �񱳴���� �������� �켱������ �ٲ��ش�.	-jmh-
			}
			
			waitQueue.currentState.effectPriority = highestPriority;	//�ݺ��� ������ �ٸ� �����庸�� ���� ���� �ɼ� ������ŭ ū �켱������ ������ �ʿ��� �������� �켱������ �����Ѵ�.	-jmh-
		}
	}
	
	
	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;		//ó�� �Ҵ�� �켱������ ������ ����		-jmh-
	protected int effectPriority;	//�켱���� ��꿡  ����� �켱���� ����		-jmh-
	
    }
    
    //Test�� ���ؼ� �߰��� �ۼ�	-jmh-
    
    private static class FirstStartTest implements Runnable {	//�켱���� ���� ������ �����ֱ� ���ؼ� �켱������ ����, lock�� ������ �ִ� �����带 �ѹ� ������� �ֱ� ���� �׽�Ʈ�� Ŭ����	-jmh-
    	String name ;	//������ �̸�	-jmh-
    	Lock lock;	//lock	-jmh-
    	 Semaphore sema;
    	FirstStartTest(String name, Lock lock) {
    		this.name = name;
    		this.lock = lock;
    	}
    	
    	FirstStartTest(String name) {
    		this.name = name;
    	
    	}
    	
    	
    		
    	
    	public void run() {

    		lock.acquire();		//lock�� ȹ��	-jmh-
    		PriorityTest testThread2 = new PriorityTest("testThread 2");	//lock�� ��û�ϴ� testThread2 ������ ����	-jmh
    		PriorityTest testThread3 = new PriorityTest("testThread 3");	//lock�� ��û���� �ʴ� testThread3 ������ ����	-jmh
    		
    		KThread two = new KThread(testThread2).setName("testThread 2");	//lock�� ��û�ϴ� ������ ����	-jmh-
    		KThread three = new KThread(testThread3).setName("testThread 3");	//lock�� ��û���� �ʴ� ������ ����	-jmh-
    		
    		Machine.interrupt().disable();	//���ͷ�Ʈ �߻��� ���´�.	-jmh-
    		ThreadedKernel.scheduler.setPriority(two, 4);	//lock�� ��û�ϴ� �������� two�� ���� �켱������ 5�� ����	-jmh-
    		ThreadedKernel.scheduler.setPriority(three,7);	//lock�� ��û���� �ʴ� �������� three�� ���� �켱������ 5�� ����	-jmh-
    		Machine.interrupt().enable();	//���ͷ�Ʈ�� �����ϵ��� ���ش�.	-jmh-
    		two.fork();	//two�����带 fork��Ų��. two������� PrioritywaitQueue�� ���Ե�		-jmh-
 
    		three.fork();	//three�����带 fork��Ų��. three������� PrioritywaitQueue�� ���Ե�		-jmh-
    
    		
    		for(int i = 0;i < 3;i++) {
    	
    			System.out.println(name + " looped " + i + " times");

    			KThread.yield();	//�ڽ��� ť�� ���� ������ ������ �����带 ������
    	
    		}
    		lock.release();		//lock�� �ݳ�	-jmh-
    	}
    }

    
    private static class PriorityLockTest implements Runnable {	//lock�� ��û�ϴ� �������� �׽�Ʈ�� Ŭ����	-jmh-
    	String name ;	//������ �̸�	-jmh-
    	Lock lock;	//lock	-jmh-
    	Condition c;
    	
    	PriorityLockTest(String name, Lock lock) {
    		this.name = name;
    		this.lock = lock;
    	}
    	
    	public void run() {
    		lock.acquire();		//lock�� ȹ��	-jmh-
    		c.sleep();
    		for(int i = 0;i < 3;i++) {
    		
    			System.out.println(name + " looped " + i + " times");
    			KThread.yield();	//�ڽ��� ť�� ���� ������ ������ �����带 ������
    			
    		}
    		c.wakeAll();
    		lock.release();		//lock�� �ݳ�	-jmh-
    	}
    }
    
    private static class PriorityTest implements Runnable {	//lock�� ��û���� �ʴ� �������� �׽�Ʈ�� Ŭ����	-jmh-
    	String name ;	//������ �̸�	-jmh-
    	
    	PriorityTest(String name) {
    		this.name = name;
    	}
    	
    	public void run() {
    		
    		for(int i = 0;i < 3;i++) {
    			System.out.println(name + " looped " + i + " times");
    			KThread.yield();	//�ڽ��� ť�� ���� ������ ������ �����带 ������
    		}
    	}
    }
    
    public static void selfTest() {	//ThreadedKernel.java���� selfTest()�ȿ��� ������ ������ �κ�.	���������� ���⼭ �׽�Ʈ�� ���۵�	-jmh-
    	Lock mutex = new Lock();	//lock�� ����	-jmh-
    	
    	FirstStartTest testThread1 = new FirstStartTest("testThread 1");	//���� ���� �ѹ� ���� ������ testThread1 ����
    	
    	KThread one = new KThread(testThread1).setName("testThread 1");		//lock�� ��û�ϰ� �� ���Ŀ� fork�Ǵ� �����庸�� �켱������ ���� ��������� one ������ ����	-jmh-
//FirstStartTest testThreadq = new FirstStartTest("testThread q");	//���� ���� �ѹ� ���� ������ testThread1 ����
    	
    	//KThread q = new KThread(testThreadq).setName("testThread q");		//lock�� ��û�ϰ� �� ���Ŀ� fork�Ǵ� �����庸�� �켱������ ���� ��������� one ������ ����	-jmh-
    	
    	Machine.interrupt().disable();	//���ͷ�Ʈ �߻��� ���´�.	-jmh-
		ThreadedKernel.scheduler.setPriority(one, 3);	//lock�� ��û�ϴ� �������̰� ���ʷ� ���� �ѹ� ����� �������� one�� ���� �켱������ 3���� ����	-jmh-
		//ThreadedKernel.scheduler.setPriority(one, 4);	//lock�� ��û�ϴ� �������̰� ���ʷ� ���� �ѹ� ����� �������� one�� ���� �켱������ 3���� ����	-jmh-
		Machine.interrupt().enable();	//���ͷ�Ʈ�� �����ϵ��� ���ش�.	-jmh-
		

		
		one.fork();	//one �����带 fork��Ų��. one������� PrioritywaitQueue�� ���Ե�		-jmh-
		
		for(int i = 0;i < 10;i++) {
			KThread.currentThread().yield();//���罺����(main)�� ť�� ���� ������ ������ �����带 ����(���������� fork�� ���� one�ۿ� �����Ƿ� one �����带 ����)	-jmh-

		}
    }
    
}
