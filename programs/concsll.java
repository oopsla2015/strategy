import java.util.*;

class SortedListConfig {
	protected static int NUM_ENTITIES = 20;
	protected static int NUM_MSGS_PER_WORKER = 8_000;
	protected static int WRITE_PERCENTAGE = 10;
	protected static int SIZE_PERCENTAGE = 1;

	protected static boolean debug = false;

	protected static void parseArgs(final String[] args) {

	}

	protected static void printArgs() {

	}

	protected static class WriteMessage {
		protected final int senderId;
		protected final int value;

		protected WriteMessage(final int senderId, final int value) {
			this.senderId = senderId;
			this.value = value;
		}
	}

	protected static class ContainsMessage {
		protected final int senderId;
		protected final int value;

		protected ContainsMessage(final int senderId, final int value) {
			this.senderId = senderId;
			this.value = value;
		}
	}

	protected static class SizeMessage {
		protected int senderId;

		protected SizeMessage(final int senderId) {
			this.senderId = senderId;
		}
	}

	protected static class ResultMessage {
		protected final int senderId;
		protected final int value;

		protected ResultMessage(final int senderId, final int value) {
			this.senderId = senderId;
			this.value = value;
		}
	}

	protected static class DoWorkMessage {

	}

	protected static class EndWorkMessage {

	}
}

class SortedLinkedList<T extends Comparable<T>> {
	private static class Node<T extends Comparable<T>> {
		public T item;
		public Node<T> next;

		public Node(final T i) {
			item = i;
			next = null;
		}
	}

	private Node<T> head;
	private Node<T> iterator;

	protected SortedLinkedList() {
		head = null;
		iterator = null;
	}

	public boolean isEmpty() {
		return (head == null);
	}

	public void add(final T item) {
		final Node<T> newNode = new Node<>(item);
		System.out.println("head: "+head+", item: "+item);
		if (head == null) {
			head = newNode;
		} else if (item.compareTo(head.item) < 0){
			newNode.next = head;
			head = newNode;
		} else {
			Node<T> after = head.next;
			Node<T> before = head;
			while (after != null) {
				if (item.compareTo(after.item) < 0) {
					break;
				}
				before = after;
				after = after.next;
				System.out.println("Come out");
			}
			newNode.next = before.next;
			before.next = newNode;
		}
	}

	public boolean contains(final T item) {
		Node<T> n = head;
		while (n != null) {
			if (item.compareTo(n.item) == 0) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		final StringBuilder s = new StringBuilder(100);
		Node<T> n = head;
		while (n != null) {
			s.append(n.item.toString());
			n = n.next;
		}
		return s.toString();
	}

	public Comparable<T> next() {
		if (iterator != null) {
			final Node<T> n = iterator;
			iterator = iterator.next;
			return n.item;
		} else {
			return null;
		}
	}

	public void reset() {
		iterator = head;
	}

	public int size() {
		int r = 0;
		Node<T> n = head;
		while (n != null) {
			r++;
			n = n.next;
		}
		return r;
	}
}

capsule Master(int numWorkers, int numMessagesPerWorker, 
					SortedList sortedList) {
	int numWorkersTerminated = 0;

	void done() {
		numWorkersTerminated += 1;
		//System.out.println("Worker done, reported to master");
		if (numWorkersTerminated == numWorkers) {
			System.out.println("master done!");
			sortedList.done();
		}
	}
}

capsule Worker(Master master, SortedList sortedList, int id, int numMessagesPerWorker) {
	int writePercent;
	int sizePercent;
	int messageCount;
	Random random;
	=>{
		messageCount = 0;
		writePercent = SortedListConfig.WRITE_PERCENTAGE;
		sizePercent = SortedListConfig.SIZE_PERCENTAGE;
		random = new Random(id + numMessagesPerWorker + writePercent + sizePercent);
	};
	void process() {
		messageCount += 1;
		if (messageCount <= numMessagesPerWorker) {
			int anInt = random.nextInt(100);
			//System.out.println("anInt: "+anInt+", messageCount: "+messageCount);
			if (anInt < sizePercent) {
				sortedList.size(new SortedListConfig.SizeMessage(id));
			} else if (anInt < (sizePercent + writePercent)) {
				sortedList.write(new SortedListConfig.WriteMessage(id, random.nextInt()));
			} else {
				sortedList.contains(new SortedListConfig.ContainsMessage(id, random.nextInt()));
			}
		} else {
			master.done();
		}
	}
}

capsule SortedList(Worker workers[]) {
	SortedLinkedList dataList;
	=>{
		dataList = new SortedLinkedList<Integer>();
	};
	void size(SortedListConfig.SizeMessage sizeMessage) {
		int value = dataList.size();
		int senderId = sizeMessage.senderId;
		workers[senderId].process();
	}

	void write(SortedListConfig.WriteMessage writeMessage) {
		int value = writeMessage.value;
		//System.out.println("Value: "+value);
		//dataList.add(value);
		//System.out.println("Added: "+value);
		int senderId = writeMessage.senderId;
		workers[senderId].process();
	}

	void contains(SortedListConfig.ContainsMessage containsMessage) {
		int value = containsMessage.value;
		int result = 0; 
		if (dataList.contains(value)) 
			result = 1;
		int senderId = containsMessage.senderId;
		workers[senderId].process();
	}

	void done() {
		System.out.println("List Size" + dataList.size());
		for (Worker worker : workers) {
			((PaniniCapsule)worker).shutdown();
		}
	}
}

capsule concsll {
	design {
		int numWorkers = SortedListConfig.NUM_ENTITIES;
		int numMessagesPerWorker = SortedListConfig.NUM_MSGS_PER_WORKER;

		Master master;
		Worker workers[20];
		SortedList sortedList;
		sortedList(workers);
		//workers[0](master, sortedList, 0, numMessagesPerWorker); // do it for others
		workers[0](master, sortedList, 0, numMessagesPerWorker);workers[1](master, sortedList, 1, numMessagesPerWorker);workers[2](master, sortedList, 2, numMessagesPerWorker);workers[3](master, sortedList, 3, numMessagesPerWorker);workers[4](master, sortedList, 4, numMessagesPerWorker);workers[5](master, sortedList, 5, numMessagesPerWorker);workers[6](master, sortedList, 6, numMessagesPerWorker);workers[7](master, sortedList, 7, numMessagesPerWorker);workers[8](master, sortedList, 8, numMessagesPerWorker);workers[9](master, sortedList, 9, numMessagesPerWorker);workers[10](master, sortedList, 10, numMessagesPerWorker);workers[11](master, sortedList, 11, numMessagesPerWorker);workers[12](master, sortedList, 12, numMessagesPerWorker);workers[13](master, sortedList, 13, numMessagesPerWorker);workers[14](master, sortedList, 14, numMessagesPerWorker);workers[15](master, sortedList, 15, numMessagesPerWorker);workers[16](master, sortedList, 16, numMessagesPerWorker);workers[17](master, sortedList, 17, numMessagesPerWorker);workers[18](master, sortedList, 18, numMessagesPerWorker);workers[19](master, sortedList, 19, numMessagesPerWorker);
		master(numWorkers, numMessagesPerWorker, sortedList);
	}

	void run() {
		for (Worker worker : workers) {
			worker.process();
		}
	}
}
