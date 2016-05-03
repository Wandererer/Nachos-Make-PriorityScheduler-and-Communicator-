package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 { // 모니터 사용을 위한 조건변수 작성
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock the lock associated with this condition variable.
	 * The current thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock; // 모니터 내부에서 사용될 lock 지정
		this.waitQueue = ThreadedKernel.scheduler.newThreadQueue(false); // 모니터 내부 큐 생성
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() { // 모니터 내부의 현재 스레드를 sleep 상태로 만든다. 스레드가 sleep 상태에 빠지는 이유는 스레드가
						  // 원하는 조건을 모니터 내부에서 만족시키지 못하는 경우, 다른 스레드가 모니터 내부로 들어와 원하는 조건을
                          // 만들어 주기까지 대기 하기 위함이다. 모니터 내부에는 단 하나의 스레드만이 작업을 수행할 수 있으므로
                          // 다른 스레드가 모니터에 들어오게 하기 위해서는 (1)lock를 반납하고, (2)스스로 큐에 들어가 sleep 상태가
                          // 되며, (3)sleep상태에서 깨어나면 다시 lock를 획득한다.
		
		Lib.assertTrue(conditionLock.isHeldByCurrentThread()); 
		
		boolean intStatus = Machine.interrupt().disable(); // 외부 인터럽트 해제
		
		conditionLock.release(); // 현재 스레드가 가지고 있던 lock을 release 한다. 
		
		waitQueue.waitForAccess(KThread.currentThread()); // 모니터 내부 큐에 현재 스레드를 저장한 뒤,
		KThread.sleep(); // 현재 스레드가 sleep 된다.
		
		conditionLock.acquire(); // sleep 상태가 해제되면 lock를 획득한다(?)
		
		Machine.interrupt().restore(intStatus); // 인터럽트 활성화
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {  // 모니터 내부 큐에서 sleep 상태에 있던 스레드 하나를 깨운다. 스레드를 ready 상태로 만든다.
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean intStatus = Machine.interrupt().disable(); // 외부 인터럽트 해제
		
		KThread next = waitQueue.nextThread();
		if(next!=null){
			next.ready();
		}
		
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean intStatus = Machine.interrupt().disable();
		KThread next = waitQueue.nextThread();
		while(next!=null){
			next.ready();
			next = waitQueue.nextThread();
		}
		
		Machine.interrupt().restore(intStatus);
	}

	private Lock conditionLock; // 컨디션 변수에 사용될 lock을 선언
	private ThreadQueue waitQueue; // ThreadQueue 선언
}