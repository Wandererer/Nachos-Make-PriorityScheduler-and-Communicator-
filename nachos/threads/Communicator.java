package nachos.threads;

public class Communicator {

	public Communicator() {
	}

	public void speak(int word) {
		lock.acquire(); // lock ȹ��
		while (WatingListener == 0 || SpeakingSpeaker != 0) { //���� ������� listener�� ���ų�, �̹� speaking����  speaker�� ���� ���,
			SpeakerRoom.sleep(); // ���� speaker�� speaking�� �� �� ���� ��Ȳ�̹Ƿ� sleep ���¿� ������.                               
		}
		SpeakingSpeaker = 1; // ���� speaker �ϳ��� speaking������ ��Ÿ����. 
		message = word; // id�� message�� �ִ´�. message�� listener���� ���޵ȴ�. 
		ListenerRoom.wake(); //sleep ���¿� �ִ� ������ listener�� �ϳ� �����.
		lock.release(); // lock ����
	}

	public int listen() {
		lock.acquire(); //lock ȹ��
		WatingListener++; // listening�ϱ� ���� ������� listener ���� 1 ������Ų��.
		while (SpeakingSpeaker == 0) { // ���� speaking���� speaker�� ���� ���,
			SpeakerRoom.wake(); // sleep ���¿� �ִ� speaker �ϳ��� ���� ��,
			ListenerRoom.sleep(); // ���� listener�� sleep ���¿� ������.
		}
		SpeakingSpeaker--; // �� while���� ����ٴ� ���� speaking & listening�� �Ͼ�ٴ� �ǹ��̴�.
		WatingListener--;  // �׷��Ƿ� speaking���� speaker, ������� listener���� ���� 1�� ���ҽ�Ų��. 
		lock.release(); // lock ����
		return message; // message ����
	}

	private static int message; // speaker�� listener���� ������ �޽���
	private static int SpeakingSpeaker; //���� speaking���� speaker ��
	private static int WatingListener;  // ���� ������� listener ��

	private static class CommunicatorTester implements Runnable {
		
		Communicator Comm = new Communicator(); 
		
		CommunicatorTester(int id, String name) {
			this.id = id; //id �ʱ�ȭ. ¦�� id�� speaker, Ȧ�� id�� listener�� �ȴ�.
			this.name = name; // �̸� �ʱ�ȭ
		}
	
		public void run() {
			for (int i = 0; i < 3; i++) { // ���� 3���� ���ϰ� ��´�.
				if (id % 2 == 0) { // id�� ¦���̸� speaker ������ �Ѵ�.
					Comm.speak(id);  
					System.out.println(name + " says    "+ message);
				} else { // id�� Ȧ���̸� listener ������ �Ѵ�.
					Comm.listen(); 
					System.out.println(name + " listens " + message);
				}
			}
		}
		String name;
		private int id;
	}

	/**
	 �ùٸ� Communicator �۵� Ȯ���� ���� selfTest()
	 */
	public static void selfTest1() {

		KThread communicator1, communicator2;

		/*
		 * Ŀ�´������� ��ü�� �����Ѵ�. �� Ŀ�´������ʹ� ID(������)�� �̸��� ������.
		 * ¦�� ID�� ���� Ŀ�´������ʹ� speaker�� �ǰ� Ȧ�� ID�� ���� Ŀ�´������ʹ� listener�� �ȴ�.
		 * speaker�� �ڽ��� ID�� listener���� ���ϰ�, listener�� �װ��� ��´�.
		 */
		
		communicator1 = new KThread(new CommunicatorTester(0, "Speaker1 "));


		communicator2 = new KThread(new CommunicatorTester(1, "Listener1"));	
		/*
		 * ��ü fork
		 */
		communicator1.fork();


		communicator2.fork();



		for (int i = 0; i < 20; i++) { 
			KThread.currentThread().yield();
		}
	}

	
	
	public static void selfTest2() {

		KThread communicator1, communicator2, communicator3, communicator4, communicator5,
		        communicator6, communicator7, communicator8;
		/*
		 * Ŀ�´������� ��ü�� �����Ѵ�. �� Ŀ�´������ʹ� ID(������)�� �̸��� ������.
		 * ¦�� ID�� ���� Ŀ�´������ʹ� speaker�� �ǰ� Ȧ�� ID�� ���� Ŀ�´������ʹ� listener�� �ȴ�.
		 * speaker�� �ڽ��� ID�� listener���� ���ϰ�, listener�� �װ��� ��´�.
		 */
		
		communicator1 = new KThread(new CommunicatorTester(0, "Speaker1 "));
		communicator2 = new KThread(new CommunicatorTester(2, "Speaker2 "));
		communicator3 = new KThread(new CommunicatorTester(4, "Speaker3 "));

		communicator5 = new KThread(new CommunicatorTester(1, "Listener1"));
		communicator6 = new KThread(new CommunicatorTester(3, "Listener2"));
		communicator7 = new KThread(new CommunicatorTester(5, "Listener3"));

		/*
		 * ��ü fork
		 */
		communicator1.fork();
		communicator2.fork();
		communicator3.fork();


		communicator5.fork();
		communicator6.fork();
		communicator7.fork();



		for (int i = 0; i < 20; i++) { 
			KThread.currentThread().yield();
		}
	}

	public static Lock lock = new Lock(); // �� ���� �������� ���� lock ����
	public static Condition2 SpeakerRoom = new Condition2(lock); // ��������� ������ speaker�� �� ���Ǻ���
	public static Condition2 ListenerRoom = new Condition2(lock); // ��������� ������ listener�� �� ���Ǻ���
}