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
    

    public int getPriority(KThread thread) {	//�켱������ ���� �޼ҵ�. �� �޼ҵ带 ȣ���ϸ� PrioritSchedulerŬ������ ���ο� �ִ� ThreadStateŬ������ getPriority()�� ȣ���ؼ� �켱������ �޾ƿ´�.	
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();	//�Ű������� �޾ƿ� �������� ThreadState�� ȣ���ؼ� �켱������ �޾ƿ´�.	
    }

    public int getEffectivePriority(KThread thread) {	//���� �켱������ ���� �޼ҵ�, �� �޼ҵ带 ȣ���ϸ� PrioritSchedulerŬ������ ���ο� �ִ� ThreadStateŬ������ geteffectPriority()�� ȣ���ؼ� �켱������ �޾ƿ´�.	
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();	//�Ű������� �޾ƿ� �������� ThreadState�� ȣ���ؼ� ���� �켱������ �޾ƿ´�.		
    }

    public void setPriority(KThread thread, int priority) {	//�������� �켱������ �����ϴ� �޼ҵ�	
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);	//�����ϰ��� �ϴ� �켱������ ���Ǵ� �켱������ ������ �ƴѰ�� ����ó���� ���ش�.	
	
	getThreadState(thread).setPriority(priority);	//�Ű������� �������� ThreadState�� ȣ���ϰ� �Ű������� �켱������ ThreadState�� setPriority�� ȣ���Ͽ� �켱������ �������ش�.	
    }

    public boolean increasePriority() {	//�켱���Ǹ� ������Ű�� �޼ҵ�	
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();	//���罺���带 �޾ƿͼ� thread�� �����Ѵ�.	

	int priority = getPriority(thread);	//priority�� �����ϰ� ���� �������� �켱������ �����Ѵ�.	
	if (priority == priorityMaximum)	//���� ���� �������� �켱������ �켱������ �ְ� �����̸�		
	    return false;	//�� �̻� �켱������ ������ �� �����Ƿ� false�� ��ȯ�Ѵ�.	

	setPriority(thread, priority+1);	//�켱 ������ ���� �� �� �ִ� ��� �켱������ 1���� �����ش�.	

	Machine.interrupt().restore(intStatus);
	return true;	//���������� �켱������ ������������ true�� ��ȯ�Ѵ�.	
    }

    public boolean decreasePriority() {	//�켱������ ���ҽ�Ű�� �޼ҵ�	
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();	//���罺���带 �޾ƿͼ� thread�� �����Ѵ�.	

	int priority = getPriority(thread);	//priority�� �����ϰ� ���� �������� �켱������ �����Ѵ�.	
	if (priority == priorityMinimum)	//���� ���� �������� �켱������ �켱������ �ּ� �����̸�	
	    return false;	//�� �̻� �켱������ ���ұ�ų �� �����Ƿ� false�� ��ȯ�Ѵ�.	

	setPriority(thread, priority-1);	//�켱 ������ ���� ��ų �� �ִ� ��� �켱������ 1���� �����ش�.	

	Machine.interrupt().restore(intStatus);
	return true;	//���������� �켱������ ���ҽ������� true�� ��ȯ�Ѵ�.	
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;	//�⺻ �켱������ 1�� ����	
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;	//�ּ� �켱������ 0���� ����	
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    //�ִ� �켱������ 7�� ����	

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {	//�������� ���¸� ������ �޼ҵ� ��ȯ���� ThreadState	
	if (thread.schedulingState == null)	//���� schedulingState�� �������� �ʾ����� ��, ������ �����ÿ� schedulingState�� �����ϴ� �κ��̴�.	
	    thread.schedulingState = new ThreadState(thread);	//ThreadState�� �����Ѵ�.	

	return (ThreadState) thread.schedulingState;	//�������� schedulingState�� ThreadState���·� ��ȯ��	
    }


    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;	//�켱������ ���� ���ο� ���Ѱ��� �����Ѵ�.	
	}

	public void waitForAccess(KThread thread) {	//�켱���� ť�� �����带 ����ֱ����� �޼ҵ�	
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);	//thread�� ThreadState�� �ҷ��ͼ� �� thread�� �켱����ť�� ����ִ´�.	
	}

	public void acquire(KThread thread) {	//�켱���� ť���� �����带 ������ ���� �޼ҵ�	
	    Lib.assertTrue(Machine.interrupt().disabled());
	    //�߰�
	    this.PrioritywaitQueue.remove(thread);	//������ �����带 ť���� ����	
	    if(transferPriority)	//PrioritywaitQueue�� ��ȭ�� ������, �� �����带 ����ְų� ������	
	    	getThreadState(thread).acquire(this);	//thread�� ThreadState�� acquire�� PrioritywaitQueue�� �Ű������� �����Ѵ�.
	}

	public KThread nextThread() {	//�켱���� ť���� ������ ���� �ؾߵ� �����带 �����ϱ����� �޼ҵ�	
	    Lib.assertTrue(Machine.interrupt().disabled());
	    //�߰�
	    if(currentState != null)	//���� �����ؾߵ� �����尡 ������		
	    	currentState.calculaterPriority(this);	//���� �������� �켱������ �ְ�� �����ش�.	
	    
	    ThreadState tempState = pickNextThread();	//tempState�� �����ϰ� ���� �����ؾ� �� �������� ���¸� �����Ѵ�.	
	    if (tempState == null)	//���� �����ؾ� �� �������� ���°� null�̸� ��, ���� ���� �ؾ� �� �����尡 ������	
	    	return null;	//null�� ��ȯ�Ѵ�.	
	    acquire(tempState.thread);	//������ �����ؾ� �� �����带 PrioritywaitQueue���� �����Ѵ�.	
	    return (KThread) tempState.thread;	//������ �����ؾߵ� �����带 KThreadŸ������ ��ȯ�Ѵ�.	
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
		if (PrioritywaitQueue.isEmpty())	//PrioritywaitQueue�� �����ؾ� �� ���� �����尡 ���� ���	
			return null;	//null�� ��ȯ�Ѵ�.	
		int comparePriority = -1;	//�񱳿� �켱������ priorityMinimum���� ���� -1 �� ����	
		KThread nextThread = null;	//������ ����� �����带 �����ϴ� nextThread�� ����	

		for(int i = 0;i < PrioritywaitQueue.size();i++) {	//PrioritywaitQueue�� ����ִ� �����带 ��� ��		
			if(getEffectivePriority(PrioritywaitQueue.get(i)) > comparePriority) {	//�񱳴���� �����尡 comparePrioirity���� �켱������ ������	
				comparePriority = getEffectivePriority(PrioritywaitQueue.get(i));	//comparePriority�� �켱������ �񱳴���� �������� �켱������ �ٲ��ش�.	
				nextThread = PrioritywaitQueue.get(i);	//�񱳴���� �����带 ������ ������ ������� �����Ѵ�.	
			}
		}
		
		if(nextThread == null)	//PrioritywaitQueue�� ��� Ž���ߴµ��� ������ ������ �����尡 ������	
			return null;	//null�� ��ȯ�Ѵ�.	
		
		return getThreadState(nextThread);	//������ ������ �������� ���¸� ��ȯ�Ѵ�.	
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;	//PrioritywaitQueue�� �������� ������ �ִ��� üũ�ϴ� ����	
	
	private Vector<KThread> PrioritywaitQueue = new Vector<KThread>();	//�����带 ������ queue�� ����	
	ThreadState currentState;	//���� ���¿� ���� ������ �̿��ϱ� ���� ThreadStateŸ������ currentState�� ����	
    
   
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
	    this.thread = thread;	//�Ű������� �����带 ������ ������� ����	

	    setPriority(priorityDefault);	//�켱������ �⺻ �켱������ ����		

	}
	

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;	//�켱���� ��ȯ	
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    return effectPriority;	//���� �켱���� ��ȯ	
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {	//�켱������ �����ϴ� �޼ҵ�	-jmh
	    if (this.priority == priority)	//�����ϰ��� �ϴ� �켱������ ���� �켱������ ������ �״�� return�Ѵ�.	
		return;
	    
	    this.priority = priority;	//�����ϰ��� �ϴ� �켱������ �������� �켱������ �������ش�.		
	    this.effectPriority = priority;	//�����ϰ��� �ϴ� �켱������ �������� ���� �켱������ �������ش�.		
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
	public void waitForAccess(PriorityQueue waitQueue) {	//PrioritywaitQueue�� �����带 �߰��ϱ����� �޼ҵ�	
		waitQueue.PrioritywaitQueue.addElement(thread);		//PrioritywaitQueue�� �����带 �߰�	
		calculaterPriority(waitQueue);	//PrioritywaitQueue�� �����带 �߰� �����Ƿ� �켱������ ����Ѵ�.			
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
	    Lib.assertTrue(waitQueue.PrioritywaitQueue.isEmpty());	//PrioritywaitQueue�� ����ִ� ��� ����ó���� ���ش�.	
	    
	    waitQueue.currentState = this;	//PrioritywaitQueue�� �����带 ������ ���¸� currentState�� �ݿ��Ѵ�.	
	    calculaterPriority(waitQueue);	//PrioritywaitQueue�� �����带 ���� ������ �켱������ �ٽð���Ѵ�.	
	}	
	
	private void calculaterPriority(PriorityQueue waitQueue) {	//�������� ������ ���� �켱������ �ٸ������庸�� ���� �����ϱ� ���� �޼ҵ�	
		if (waitQueue.currentState != null) {	//������ �ؾߵ� �����尡 ������	
			int highestPriority = waitQueue.currentState.getEffectivePriority();	//�����ؾߵ� �������� ���� �켱������ �޾Ƽ� �ٸ������庸�� ���� �켱������ �����ϴ� highestPriority�� ����	
			
			for(KThread comp : waitQueue.PrioritywaitQueue) {	//PrioritywaitQueue�ȿ� �ִ� ��� �����带 ���ϱ� ���� foreach�� ����	
				ThreadState temp = getThreadState(comp);	//PrioritywaitQueue�ȿ� �ִ� �������� �ϳ��� ���¸� �����´�.	
				int compareP = temp.getEffectivePriority();		//���� �������� ���� �켱������ �����´�.	
				
				if(compareP > highestPriority)	//������ �ʿ��� �������� �켱�������� PrioritywaitQueue�� �ִ� �������� �켱������ ������	-jmh
					highestPriority = compareP;		//�ٸ������庸�� ���� �켱������ �����ϴ� highestPriority�� �񱳴���� �������� �켱������ �ٲ��ش�.	
			}
			
			waitQueue.currentState.effectPriority = highestPriority;	//�ݺ��� ������ �ٸ� �����庸�� ���� ���� �ɼ� ������ŭ ū �켱������ ������ �ʿ��� �������� �켱������ �����Ѵ�.	
		}
	}
	
	
	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;		//ó�� �Ҵ�� �켱������ ������ ����		
	protected int effectPriority;	//�켱���� ��꿡  ����� �켱���� ����		
	
    }
    
    //Test�� ���ؼ� �߰��� �ۼ�	
    
    private static class FirstStartTest implements Runnable {	//�켱���� ���� ������ �����ֱ� ���ؼ� �켱������ ����, lock�� ������ �ִ� �����带 �ѹ� ������� �ֱ� ���� �׽�Ʈ�� Ŭ����	
    	String name ;	//������ �̸�	
    	Lock lock;	//lock	
    	 Semaphore sema;
    	FirstStartTest(String name, Lock lock) {
    		this.name = name;
    		this.lock = lock;
    	}
    	
    	FirstStartTest(String name) {
    		this.name = name;
    	
    	}
    	
    	
    		
    	
    	public void run() {

    		lock.acquire();		//lock�� ȹ��	
    		PriorityTest testThread2 = new PriorityTest("testThread 2");	//lock�� ��û�ϴ� testThread2 ������ ����	-jmh
    		PriorityTest testThread3 = new PriorityTest("testThread 3");	//lock�� ��û���� �ʴ� testThread3 ������ ����	-jmh
    		
    		KThread two = new KThread(testThread2).setName("testThread 2");	//lock�� ��û�ϴ� ������ ����	
    		KThread three = new KThread(testThread3).setName("testThread 3");	//lock�� ��û���� �ʴ� ������ ����	
    		
    		Machine.interrupt().disable();	//���ͷ�Ʈ �߻��� ���´�.	
    		ThreadedKernel.scheduler.setPriority(two, 4);	//lock�� ��û�ϴ� �������� two�� ���� �켱������ 5�� ����	
    		ThreadedKernel.scheduler.setPriority(three,7);	//lock�� ��û���� �ʴ� �������� three�� ���� �켱������ 5�� ����	
    		Machine.interrupt().enable();	//���ͷ�Ʈ�� �����ϵ��� ���ش�.	
    		two.fork();	//two�����带 fork��Ų��. two������� PrioritywaitQueue�� ���Ե�		
 
    		three.fork();	//three�����带 fork��Ų��. three������� PrioritywaitQueue�� ���Ե�		
    
    		
    		for(int i = 0;i < 3;i++) {
    	
    			System.out.println(name + " looped " + i + " times");

    			KThread.yield();	//�ڽ��� ť�� ���� ������ ������ �����带 ������
    	
    		}
    		lock.release();		//lock�� �ݳ�	
    	}
    }

    
    private static class PriorityLockTest implements Runnable {	//lock�� ��û�ϴ� �������� �׽�Ʈ�� Ŭ����	
    	String name ;	//������ �̸�	
    	Lock lock;	//lock	
    	Condition c;
    	
    	PriorityLockTest(String name, Lock lock) {
    		this.name = name;
    		this.lock = lock;
    	}
    	
    	public void run() {
    		lock.acquire();		//lock�� ȹ��	
    		c.sleep();
    		for(int i = 0;i < 3;i++) {
    		
    			System.out.println(name + " looped " + i + " times");
    			KThread.yield();	//�ڽ��� ť�� ���� ������ ������ �����带 ������
    			
    		}
    		c.wakeAll();
    		lock.release();		//lock�� �ݳ�	
    	}
    }
    
    private static class PriorityTest implements Runnable {	//lock�� ��û���� �ʴ� �������� �׽�Ʈ�� Ŭ����	
    	String name ;	//������ �̸�
    	
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
    
    public static void selfTest() {	//ThreadedKernel.java���� selfTest()�ȿ��� ������ ������ �κ�.	���������� ���⼭ �׽�Ʈ�� ���۵�	
    	Lock mutex = new Lock();	//lock�� ����	
    	
    	FirstStartTest testThread1 = new FirstStartTest("testThread 1");	//���� ���� �ѹ� ���� ������ testThread1 ����
    	
    	KThread one = new KThread(testThread1).setName("testThread 1");		//lock�� ��û�ϰ� �� ���Ŀ� fork�Ǵ� �����庸�� �켱������ ���� ��������� one ������ ����	
//FirstStartTest testThreadq = new FirstStartTest("testThread q");	//���� ���� �ѹ� ���� ������ testThread1 ����
    	
    	//KThread q = new KThread(testThreadq).setName("testThread q");		//lock�� ��û�ϰ� �� ���Ŀ� fork�Ǵ� �����庸�� �켱������ ���� ��������� one ������ ����	
    	
    	Machine.interrupt().disable();	//���ͷ�Ʈ �߻��� ���´�.	
		ThreadedKernel.scheduler.setPriority(one, 3);	//lock�� ��û�ϴ� �������̰� ���ʷ� ���� �ѹ� ����� �������� one�� ���� �켱������ 3���� ����	
		//ThreadedKernel.scheduler.setPriority(one, 4);	//lock�� ��û�ϴ� �������̰� ���ʷ� ���� �ѹ� ����� �������� one�� ���� �켱������ 3���� ����	
		Machine.interrupt().enable();	//���ͷ�Ʈ�� �����ϵ��� ���ش�.	
		

		
		one.fork();	//one �����带 fork��Ų��. one������� PrioritywaitQueue�� ���Ե�		
		
		for(int i = 0;i < 10;i++) {
			KThread.currentThread().yield();//���罺����(main)�� ť�� ���� ������ ������ �����带 ����(���������� fork�� ���� one�ۿ� �����Ƿ� one �����带 ����)	

		}
    }
    
}
