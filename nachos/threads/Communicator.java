package nachos.threads;

public class Communicator {

	public Communicator() {
	}

	public void speak(int word) {
		lock.acquire(); // lock 획득
		while (WatingListener == 0 || SpeakingSpeaker != 0) { //현재 대기중인 listener가 없거나, 이미 speaking중인  speaker가 있을 경우,
			SpeakerRoom.sleep(); // 현재 speaker는 speaking을 할 수 없는 상황이므로 sleep 상태에 빠진다.                               
		}
		SpeakingSpeaker = 1; // 현재 speaker 하나가 speaking중임을 나타낸다. 
		message = word; // id를 message에 넣는다. message는 listener에게 전달된다. 
		ListenerRoom.wake(); //sleep 상태에 있는 나머지 listener을 하나 깨운다.
		lock.release(); // lock 해제
	}

	public int listen() {
		lock.acquire(); //lock 획득
		WatingListener++; // listening하기 위해 대기중인 listener 수를 1 증가시킨다.
		while (SpeakingSpeaker == 0) { // 현재 speaking중인 speaker가 없을 경우,
			SpeakerRoom.wake(); // sleep 상태에 있던 speaker 하나를 깨운 뒤,
			ListenerRoom.sleep(); // 현재 listener은 sleep 상태에 빠진다.
		}
		SpeakingSpeaker--; // 위 while문을 벗어났다는 것은 speaking & listening이 일어났다는 의미이다.
		WatingListener--;  // 그러므로 speaking중인 speaker, 대기중인 listener수를 각각 1씩 감소시킨다. 
		lock.release(); // lock 해제
		return message; // message 리턴
	}

	private static int message; // speaker가 listener에게 전달할 메시지
	private static int SpeakingSpeaker; //현재 speaking중인 speaker 수
	private static int WatingListener;  // 현재 대기중인 listener 수

	private static class CommunicatorTester implements Runnable {
		
		Communicator Comm = new Communicator(); 
		
		CommunicatorTester(int id, String name) {
			this.id = id; //id 초기화. 짝수 id는 speaker, 홀수 id는 listener가 된다.
			this.name = name; // 이름 초기화
		}
	
		public void run() {
			for (int i = 0; i < 3; i++) { // 각각 3번씩 말하고 듣는다.
				if (id % 2 == 0) { // id가 짝수이면 speaker 역할을 한다.
					Comm.speak(id);  
					System.out.println(name + " says    "+ message);
				} else { // id가 홀수이면 listener 역할을 한다.
					Comm.listen(); 
					System.out.println(name + " listens " + message);
				}
			}
		}
		String name;
		private int id;
	}

	/**
	 올바른 Communicator 작동 확인을 위한 selfTest()
	 */
	public static void selfTest1() {

		KThread communicator1, communicator2;

		/*
		 * 커뮤니케이터 객체를 생성한다. 각 커뮤니케이터는 ID(정수형)와 이름을 가진다.
		 * 짝수 ID를 갖는 커뮤니케이터는 speaker가 되고 홀수 ID를 갖는 커뮤니케이터는 listener가 된다.
		 * speaker는 자신의 ID를 listener에게 말하고, listener는 그것을 듣는다.
		 */
		
		communicator1 = new KThread(new CommunicatorTester(0, "Speaker1 "));


		communicator2 = new KThread(new CommunicatorTester(1, "Listener1"));	
		/*
		 * 객체 fork
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
		 * 커뮤니케이터 객체를 생성한다. 각 커뮤니케이터는 ID(정수형)와 이름을 가진다.
		 * 짝수 ID를 갖는 커뮤니케이터는 speaker가 되고 홀수 ID를 갖는 커뮤니케이터는 listener가 된다.
		 * speaker는 자신의 ID를 listener에게 말하고, listener는 그것을 듣는다.
		 */
		
		communicator1 = new KThread(new CommunicatorTester(0, "Speaker1 "));
		communicator2 = new KThread(new CommunicatorTester(2, "Speaker2 "));
		communicator3 = new KThread(new CommunicatorTester(4, "Speaker3 "));

		communicator5 = new KThread(new CommunicatorTester(1, "Listener1"));
		communicator6 = new KThread(new CommunicatorTester(3, "Listener2"));
		communicator7 = new KThread(new CommunicatorTester(5, "Listener3"));

		/*
		 * 객체 fork
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

	public static Lock lock = new Lock(); // 각 조건 변수에서 사용될 lock 생성
	public static Condition2 SpeakerRoom = new Condition2(lock); // 대기조건을 만족한 speaker가 들어갈 조건변수
	public static Condition2 ListenerRoom = new Condition2(lock); // 대기조건을 만족한 listener가 들어갈 조건변수
}