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
public class Condition2 { // ����� ����� ���� ���Ǻ��� �ۼ�
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock the lock associated with this condition variable.
	 * The current thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock; // ����� ���ο��� ���� lock ����
		this.waitQueue = ThreadedKernel.scheduler.newThreadQueue(false); // ����� ���� ť ����
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() { // ����� ������ ���� �����带 sleep ���·� �����. �����尡 sleep ���¿� ������ ������ �����尡
						  // ���ϴ� ������ ����� ���ο��� ������Ű�� ���ϴ� ���, �ٸ� �����尡 ����� ���η� ���� ���ϴ� ������
                          // ����� �ֱ���� ��� �ϱ� �����̴�. ����� ���ο��� �� �ϳ��� �����常�� �۾��� ������ �� �����Ƿ�
                          // �ٸ� �����尡 ����Ϳ� ������ �ϱ� ���ؼ��� (1)lock�� �ݳ��ϰ�, (2)������ ť�� �� sleep ���°�
                          // �Ǹ�, (3)sleep���¿��� ����� �ٽ� lock�� ȹ���Ѵ�.
		
		Lib.assertTrue(conditionLock.isHeldByCurrentThread()); 
		
		boolean intStatus = Machine.interrupt().disable(); // �ܺ� ���ͷ�Ʈ ����
		
		conditionLock.release(); // ���� �����尡 ������ �ִ� lock�� release �Ѵ�. 
		
		waitQueue.waitForAccess(KThread.currentThread()); // ����� ���� ť�� ���� �����带 ������ ��,
		KThread.sleep(); // ���� �����尡 sleep �ȴ�.
		
		conditionLock.acquire(); // sleep ���°� �����Ǹ� lock�� ȹ���Ѵ�(?)
		
		Machine.interrupt().restore(intStatus); // ���ͷ�Ʈ Ȱ��ȭ
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {  // ����� ���� ť���� sleep ���¿� �ִ� ������ �ϳ��� �����. �����带 ready ���·� �����.
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean intStatus = Machine.interrupt().disable(); // �ܺ� ���ͷ�Ʈ ����
		
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

	private Lock conditionLock; // ����� ������ ���� lock�� ����
	private ThreadQueue waitQueue; // ThreadQueue ����
}