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
    

    public int getPriority(KThread thread) {	//우선순위를 얻어내는 메소드. 이 메소드를 호출하면 PrioritScheduler클래스의 내부에 있는 ThreadState클래스의 getPriority()를 호출해서 우선순위를 받아온다.	
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();	//매개변수로 받아온 스레드의 ThreadState를 호출해서 우선순위를 받아온다.	
    }

    public int getEffectivePriority(KThread thread) {	//계산용 우선순위를 얻어내는 메소드, 이 메소드를 호출하면 PrioritScheduler클래스의 내부에 있는 ThreadState클래스의 geteffectPriority()를 호출해서 우선순위를 받아온다.	
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();	//매개변수로 받아온 스레드의 ThreadState를 호출해서 계산용 우선순위를 받아온다.		
    }

    public void setPriority(KThread thread, int priority) {	//스레드의 우선순위를 설정하는 메소드	
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);	//설정하고자 하는 우선순위가 허용되는 우선순위의 범위가 아닌경우 예외처리를 해준다.	
	
	getThreadState(thread).setPriority(priority);	//매개변수의 스레드의 ThreadState를 호출하고 매개변수의 우선순위로 ThreadState의 setPriority를 호출하여 우선순위를 설정해준다.	
    }

    public boolean increasePriority() {	//우선순의를 증가시키는 메소드	
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();	//현재스레드를 받아와서 thread에 지정한다.	

	int priority = getPriority(thread);	//priority를 선언하고 현재 스레드의 우선순위를 지정한다.	
	if (priority == priorityMaximum)	//만약 현재 스레드의 우선순위가 우선순위의 최고 범위이면		
	    return false;	//더 이상 우선순위를 증가할 수 없으므로 false를 반환한다.	

	setPriority(thread, priority+1);	//우선 순위를 증가 할 수 있는 경우 우선순위를 1증가 시켜준다.	

	Machine.interrupt().restore(intStatus);
	return true;	//정상적으로 우선순위를 증가시켰으면 true를 반환한다.	
    }

    public boolean decreasePriority() {	//우선순위를 감소시키는 메소드	
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();	//현재스레드를 받아와서 thread에 지정한다.	

	int priority = getPriority(thread);	//priority를 선언하고 현재 스레드의 우선순위를 지정한다.	
	if (priority == priorityMinimum)	//만약 현재 스레드의 우선순위가 우선순위의 최소 범위이면	
	    return false;	//더 이상 우선순위를 감소기킬 수 없으므로 false를 반환한다.	

	setPriority(thread, priority-1);	//우선 순위를 감소 시킬 수 있는 경우 우선순위를 1감소 시켜준다.	

	Machine.interrupt().restore(intStatus);
	return true;	//정상적으로 우선순위를 감소시켰으면 true를 반환한다.	
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;	//기본 우선순위는 1로 설정	
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;	//최소 우선순위는 0으로 설정	
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    //최대 우선순위는 7로 설정	

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {	//스레드의 상태를 얻어오는 메소드 반환형은 ThreadState	
	if (thread.schedulingState == null)	//만약 schedulingState가 설정되지 않았으면 즉, 스레드 생성시에 schedulingState를 생성하는 부분이다.	
	    thread.schedulingState = new ThreadState(thread);	//ThreadState를 생성한다.	

	return (ThreadState) thread.schedulingState;	//스레드의 schedulingState를 ThreadState형태로 반환함	
    }


    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;	//우선순위의 변동 여부에 대한것을 설정한다.	
	}

	public void waitForAccess(KThread thread) {	//우선순위 큐에 스레드를 집어넣기위한 메소드	
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);	//thread의 ThreadState를 불러와서 이 thread를 우선순위큐에 집어넣는다.	
	}

	public void acquire(KThread thread) {	//우선순위 큐에서 스레드를 꺼내기 위한 메소드	
	    Lib.assertTrue(Machine.interrupt().disabled());
	    //추가
	    this.PrioritywaitQueue.remove(thread);	//실행할 스레드를 큐에서 제거	
	    if(transferPriority)	//PrioritywaitQueue에 변화가 있으면, 즉 스레드를 집어넣거나 뺐으면	
	    	getThreadState(thread).acquire(this);	//thread의 ThreadState의 acquire를 PrioritywaitQueue를 매개변수로 실행한다.
	}

	public KThread nextThread() {	//우선순위 큐에서 다음에 실행 해야될 스레드를 선택하기위한 메소드	
	    Lib.assertTrue(Machine.interrupt().disabled());
	    //추가
	    if(currentState != null)	//현재 수행해야될 스레드가 있으면		
	    	currentState.calculaterPriority(this);	//현재 스레드의 우선순위를 최고로 높여준다.	
	    
	    ThreadState tempState = pickNextThread();	//tempState를 선언하고 다음 실행해야 될 스레드의 상태를 지정한다.	
	    if (tempState == null)	//다음 실행해야 될 스레드의 상태가 null이면 즉, 다음 실행 해야 될 스레드가 없으면	
	    	return null;	//null을 반환한다.	
	    acquire(tempState.thread);	//다음에 실행해야 될 스레드를 PrioritywaitQueue에서 제거한다.	
	    return (KThread) tempState.thread;	//다음에 실행해야될 스레드를 KThread타입으로 반환한다.	
	}
	
		
	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {		
		
		//수정
		if (PrioritywaitQueue.isEmpty())	//PrioritywaitQueue에 실행해야 될 다음 스레드가 없는 경우	
			return null;	//null을 반환한다.	
		int comparePriority = -1;	//비교용 우선순위를 priorityMinimum보다 작은 -1 로 설정	
		KThread nextThread = null;	//다음에 수행될 스레드를 저장하는 nextThread를 선언	

		for(int i = 0;i < PrioritywaitQueue.size();i++) {	//PrioritywaitQueue에 들어있는 스레드를 모두 비교		
			if(getEffectivePriority(PrioritywaitQueue.get(i)) > comparePriority) {	//비교대상의 스레드가 comparePrioirity보다 우선순위가 높으면	
				comparePriority = getEffectivePriority(PrioritywaitQueue.get(i));	//comparePriority의 우선순위를 비교대상의 스레드의 우선순위로 바꿔준다.	
				nextThread = PrioritywaitQueue.get(i);	//비교대상의 스레드를 다음에 수행할 스레드로 지정한다.	
			}
		}
		
		if(nextThread == null)	//PrioritywaitQueue를 모두 탐색했는데도 다음에 수행할 스레드가 없으면	
			return null;	//null을 반환한다.	
		
		return getThreadState(nextThread);	//다음에 수행할 스레드의 상태를 반환한다.	
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;	//PrioritywaitQueue에 스레드의 변동이 있는지 체크하는 변수	
	
	private Vector<KThread> PrioritywaitQueue = new Vector<KThread>();	//스레드를 저장할 queue를 선언	
	ThreadState currentState;	//현재 상태에 대한 정보를 이용하기 위한 ThreadState타입으로 currentState를 선언	
    
   
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
	    this.thread = thread;	//매개변수의 스레드를 현재의 스레드로 지정	

	    setPriority(priorityDefault);	//우선순위를 기본 우선순위로 설정		

	}
	

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;	//우선순위 반환	
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    return effectPriority;	//계산용 우선순위 반환	
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {	//우선순위를 설정하는 메소드	-jmh
	    if (this.priority == priority)	//설정하고자 하는 우선순위가 현재 우선순위랑 같으면 그대로 return한다.	
		return;
	    
	    this.priority = priority;	//설정하고자 하는 우선순위로 스레드의 우선순위를 설정해준다.		
	    this.effectPriority = priority;	//설정하고자 하는 우선순위로 스레드의 계산용 우선순위를 설정해준다.		
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
	public void waitForAccess(PriorityQueue waitQueue) {	//PrioritywaitQueue에 스레드를 추가하기위한 메소드	
		waitQueue.PrioritywaitQueue.addElement(thread);		//PrioritywaitQueue에 스레드를 추가	
		calculaterPriority(waitQueue);	//PrioritywaitQueue에 스레드를 추가 했으므로 우선순위를 계산한다.			
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
	    Lib.assertTrue(waitQueue.PrioritywaitQueue.isEmpty());	//PrioritywaitQueue가 비어있는 경우 예외처리를 해준다.	
	    
	    waitQueue.currentState = this;	//PrioritywaitQueue에 스레드를 제거한 상태를 currentState에 반영한다.	
	    calculaterPriority(waitQueue);	//PrioritywaitQueue에 스레드를 제거 했으니 우선순위를 다시계산한다.	
	}	
	
	private void calculaterPriority(PriorityQueue waitQueue) {	//스레드의 실행을 위해 우선순위를 다른스레드보다 높게 변경하기 위한 메소드	
		if (waitQueue.currentState != null) {	//수행을 해야될 스레드가 있으면	
			int highestPriority = waitQueue.currentState.getEffectivePriority();	//수행해야될 스레드의 계산용 우선순위를 받아서 다른스레드보다 높은 우선순위를 저장하는 highestPriority에 지정	
			
			for(KThread comp : waitQueue.PrioritywaitQueue) {	//PrioritywaitQueue안에 있는 모든 스레드를 비교하기 위한 foreach문 선언	
				ThreadState temp = getThreadState(comp);	//PrioritywaitQueue안에 있는 스레드중 하나의 상태를 가져온다.	
				int compareP = temp.getEffectivePriority();		//비교할 스레드의 계산용 우선순위를 가져온다.	
				
				if(compareP > highestPriority)	//수행이 필요한 스레드의 우선순위보다 PrioritywaitQueue에 있는 스레드의 우선순위가 높으면	-jmh
					highestPriority = compareP;		//다른스레드보다 높은 우선순위를 저장하는 highestPriority를 비교대상의 스레드의 우선순위로 바꿔준다.	
			}
			
			waitQueue.currentState.effectPriority = highestPriority;	//반복이 끝나고 다른 스레드보다 먼저 실행 될수 있을많큼 큰 우선순위를 수행이 필요한 스레드의 우선순위로 지정한다.	
		}
	}
	
	
	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;		//처음 할당된 우선순위를 저장할 변수		
	protected int effectPriority;	//우선순위 계산에  사용할 우선순위 변수		
	
    }
    
    //Test를 위해서 추가로 작성	
    
    private static class FirstStartTest implements Runnable {	//우선순위 역전 현상을 보여주기 위해서 우선순위가 낮고, lock을 가지고 있는 스레드를 한번 실행시켜 주기 위한 테스트용 클래스	
    	String name ;	//스레드 이름	
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

    		lock.acquire();		//lock을 획득	
    		PriorityTest testThread2 = new PriorityTest("testThread 2");	//lock을 요청하는 testThread2 스레드 생성	-jmh
    		PriorityTest testThread3 = new PriorityTest("testThread 3");	//lock을 요청하지 않는 testThread3 스레드 생성	-jmh
    		
    		KThread two = new KThread(testThread2).setName("testThread 2");	//lock을 요청하는 스레드 생성	
    		KThread three = new KThread(testThread3).setName("testThread 3");	//lock을 요청하지 않는 스레드 생성	
    		
    		Machine.interrupt().disable();	//인터럽트 발생을 막는다.	
    		ThreadedKernel.scheduler.setPriority(two, 4);	//lock을 요청하는 스레드인 two의 최초 우선순위를 5로 설정	
    		ThreadedKernel.scheduler.setPriority(three,7);	//lock을 요청하지 않는 스레드인 three의 최초 우선순위를 5로 설정	
    		Machine.interrupt().enable();	//인터럽트가 가능하도록 해준다.	
    		two.fork();	//two스레드를 fork시킨다. two스레드는 PrioritywaitQueue에 들어가게됨		
 
    		three.fork();	//three스레드를 fork시킨다. three스레드는 PrioritywaitQueue에 들어가게됨		
    
    		
    		for(int i = 0;i < 3;i++) {
    	
    			System.out.println(name + " looped " + i + " times");

    			KThread.yield();	//자신은 큐에 들어가고 다음에 실행할 스레드를 실행함
    	
    		}
    		lock.release();		//lock을 반납	
    	}
    }

    
    private static class PriorityLockTest implements Runnable {	//lock을 요청하는 스레드의 테스트용 클래스	
    	String name ;	//스레드 이름	
    	Lock lock;	//lock	
    	Condition c;
    	
    	PriorityLockTest(String name, Lock lock) {
    		this.name = name;
    		this.lock = lock;
    	}
    	
    	public void run() {
    		lock.acquire();		//lock을 획득	
    		c.sleep();
    		for(int i = 0;i < 3;i++) {
    		
    			System.out.println(name + " looped " + i + " times");
    			KThread.yield();	//자신은 큐에 들어가고 다음에 실행할 스레드를 실행함
    			
    		}
    		c.wakeAll();
    		lock.release();		//lock을 반납	
    	}
    }
    
    private static class PriorityTest implements Runnable {	//lock을 요청하지 않는 스레드의 테스트용 클래스	
    	String name ;	//스레드 이름
    	
    	PriorityTest(String name) {
    		this.name = name;
    	}
    	
    	public void run() {
    		
    		for(int i = 0;i < 3;i++) {
    			System.out.println(name + " looped " + i + " times");
    			KThread.yield();	//자신은 큐에 들어가고 다음에 실행할 스레드를 실행함
    		}
    	}
    }
    
    public static void selfTest() {	//ThreadedKernel.java에서 selfTest()안에서 수행을 시켜줄 부분.	실질적으로 여기서 테스트가 시작됨	
    	Lock mutex = new Lock();	//lock을 생성	
    	
    	FirstStartTest testThread1 = new FirstStartTest("testThread 1");	//제일 먼저 한번 실행 시켜줄 testThread1 생성
    	
    	KThread one = new KThread(testThread1).setName("testThread 1");		//lock을 요청하고 이 이후에 fork되는 스레드보다 우선순위가 낮은 스레드용인 one 스레드 생성	
//FirstStartTest testThreadq = new FirstStartTest("testThread q");	//제일 먼저 한번 실행 시켜줄 testThread1 생성
    	
    	//KThread q = new KThread(testThreadq).setName("testThread q");		//lock을 요청하고 이 이후에 fork되는 스레드보다 우선순위가 낮은 스레드용인 one 스레드 생성	
    	
    	Machine.interrupt().disable();	//인터럽트 발생을 막는다.	
		ThreadedKernel.scheduler.setPriority(one, 3);	//lock을 요청하는 스레드이고 최초로 먼저 한번 실행될 스레드인 one의 최초 우선순위를 3으로 설정	
		//ThreadedKernel.scheduler.setPriority(one, 4);	//lock을 요청하는 스레드이고 최초로 먼저 한번 실행될 스레드인 one의 최초 우선순위를 3으로 설정	
		Machine.interrupt().enable();	//인터럽트가 가능하도록 해준다.	
		

		
		one.fork();	//one 스레드를 fork시킨다. one스레드는 PrioritywaitQueue에 들어가게됨		
		
		for(int i = 0;i < 10;i++) {
			KThread.currentThread().yield();//현재스레드(main)을 큐에 들어가고 다음에 실행할 스레드를 실행(실질적으로 fork된 것은 one밖에 없으므로 one 스레드를 실행)	

		}
    }
    
}
