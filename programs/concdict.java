import java.util.*;

class DictionaryConfig {
	protected static int NUM_ENTITIES = 20;
	protected static int NUM_MSGS_PER_WORKER = 10_000;
	protected static int WRITE_PERCENTAGE = 10;

	protected static int DATA_LIMIT = Integer.MAX_VALUE / 4_096;
	protected static Map<Integer, Integer> DATA_MAP = new HashMap<>(DATA_LIMIT);

	protected static boolean debug = false;

	protected static void parseArgs(final String[] args) {

	}

	protected static void printArgs() {

	}

	protected static class WriteMessage {
		final int senderId;
		final int key;
		final int value;
		protected WriteMessage(final int senderId, final int key, final int value) {
			this.senderId = senderId;
			this.key = key;
			this.value = value;
		}
	}

	protected static class ReadMessage {
		final int senderId;
		final int key;
		protected ReadMessage(final int senderId, int key) {
			this.senderId = senderId;
			this.key = key;
		}
	}

	protected static class ResultMessage {

	}

	protected static class DoWorkMessage {

	}

	protected static class EndWorkMessage {

	}
}

capsule Master(int numWorkers, int numMessagesPerWorker, 
					Dictionary dictionary) {
	int numWorkersTerminated = 0;

	void done() {
		numWorkersTerminated += 1;
		if (numWorkersTerminated == numWorkers) {
			dictionary.done();
		}
	}
}

capsule Worker(Master master, Dictionary dictionary, int id, int numMessagesPerWorker) {
	int writePercent;
	int messageCount = 0;
	Random random;// = new Random(id + numMessagesPerWorker + writePercent);
	=>{
		writePercent = DictionaryConfig.WRITE_PERCENTAGE;
		random = new Random(id + numMessagesPerWorker + writePercent);
	};
	void process() {
		messageCount += 1;
		if (messageCount <= numMessagesPerWorker) {
			int anInt = random.nextInt(100);
			if (anInt < writePercent)  {
				dictionary.write(new DictionaryConfig.WriteMessage(id, random.nextInt(), random.nextInt()));
			} else {
				dictionary.read(new DictionaryConfig.ReadMessage(id, random.nextInt()));
			}
		} else {
			master.done();
		}
	}
}

capsule Dictionary(Map<Integer, Integer> initialState, Worker workers[]) {
	Map<Integer,Integer> dataMap;// = new HashMap<Integer,Integer>(initialState);
	=>{
		dataMap = new HashMap<Integer,Integer>(initialState);
	};
	void write(DictionaryConfig.WriteMessage writeMessage) {
		int key = writeMessage.key;
		int value = writeMessage.value;
		dataMap.put(key, value);
		int senderId = writeMessage.senderId;
		workers[senderId].process();
	}

	void read(DictionaryConfig.ReadMessage readMessage) {
		int value = 0;
		if(dataMap.get(readMessage.key) != null)
			value = dataMap.get(readMessage.key);
		int senderId = readMessage.senderId;
		workers[senderId].process();
	}

	void done() {
		System.out.println("Dictionary Size: " + dataMap.size());
		for (Worker worker : workers) {
			((PaniniCapsule)worker).shutdown();
		}
	}
}

capsule concdict {
	design {
		int numWorkers = DictionaryConfig.NUM_ENTITIES;
		int numMessagesPerWorker = DictionaryConfig.NUM_MSGS_PER_WORKER;

		Master master;
		Worker workers[20];
		Dictionary dictionary;
		dictionary(DictionaryConfig.DATA_MAP, workers);
		//workers[0](master, dictionary, 0, numMessagesPerWorker); // do it for others
		//workers[0](master, dictionary, 0, numMessagesPerWorker);workers[1](master, dictionary, 1, numMessagesPerWorker);workers[2](master, dictionary, 2, numMessagesPerWorker);workers[3](master, dictionary, 3, numMessagesPerWorker);workers[4](master, dictionary, 4, numMessagesPerWorker);workers[5](master, dictionary, 5, numMessagesPerWorker);workers[6](master, dictionary, 6, numMessagesPerWorker);workers[7](master, dictionary, 7, numMessagesPerWorker);workers[8](master, dictionary, 8, numMessagesPerWorker);workers[9](master, dictionary, 9, numMessagesPerWorker);workers[10](master, dictionary, 10, numMessagesPerWorker);workers[11](master, dictionary, 11, numMessagesPerWorker);workers[12](master, dictionary, 12, numMessagesPerWorker);workers[13](master, dictionary, 13, numMessagesPerWorker);workers[14](master, dictionary, 14, numMessagesPerWorker);workers[15](master, dictionary, 15, numMessagesPerWorker);workers[16](master, dictionary, 16, numMessagesPerWorker);workers[17](master, dictionary, 17, numMessagesPerWorker);workers[18](master, dictionary, 18, numMessagesPerWorker);workers[19](master, dictionary, 19, numMessagesPerWorker);workers[20](master, dictionary, 20, numMessagesPerWorker);workers[21](master, dictionary, 21, numMessagesPerWorker);workers[22](master, dictionary, 22, numMessagesPerWorker);workers[23](master, dictionary, 23, numMessagesPerWorker);workers[24](master, dictionary, 24, numMessagesPerWorker);workers[25](master, dictionary, 25, numMessagesPerWorker);workers[26](master, dictionary, 26, numMessagesPerWorker);workers[27](master, dictionary, 27, numMessagesPerWorker);workers[28](master, dictionary, 28, numMessagesPerWorker);workers[29](master, dictionary, 29, numMessagesPerWorker);workers[30](master, dictionary, 30, numMessagesPerWorker);workers[31](master, dictionary, 31, numMessagesPerWorker);workers[32](master, dictionary, 32, numMessagesPerWorker);workers[33](master, dictionary, 33, numMessagesPerWorker);workers[34](master, dictionary, 34, numMessagesPerWorker);workers[35](master, dictionary, 35, numMessagesPerWorker);workers[36](master, dictionary, 36, numMessagesPerWorker);workers[37](master, dictionary, 37, numMessagesPerWorker);workers[38](master, dictionary, 38, numMessagesPerWorker);workers[39](master, dictionary, 39, numMessagesPerWorker);
		workers[0](master, dictionary, 0, numMessagesPerWorker);workers[1](master, dictionary, 1, numMessagesPerWorker);workers[2](master, dictionary, 2, numMessagesPerWorker);workers[3](master, dictionary, 3, numMessagesPerWorker);workers[4](master, dictionary, 4, numMessagesPerWorker);workers[5](master, dictionary, 5, numMessagesPerWorker);workers[6](master, dictionary, 6, numMessagesPerWorker);workers[7](master, dictionary, 7, numMessagesPerWorker);workers[8](master, dictionary, 8, numMessagesPerWorker);workers[9](master, dictionary, 9, numMessagesPerWorker);workers[10](master, dictionary, 10, numMessagesPerWorker);workers[11](master, dictionary, 11, numMessagesPerWorker);workers[12](master, dictionary, 12, numMessagesPerWorker);workers[13](master, dictionary, 13, numMessagesPerWorker);workers[14](master, dictionary, 14, numMessagesPerWorker);workers[15](master, dictionary, 15, numMessagesPerWorker);workers[16](master, dictionary, 16, numMessagesPerWorker);workers[17](master, dictionary, 17, numMessagesPerWorker);workers[18](master, dictionary, 18, numMessagesPerWorker);workers[19](master, dictionary, 19, numMessagesPerWorker);
		master(numWorkers, numMessagesPerWorker, dictionary);

	}

	void run() {
		for (Worker worker : workers) {
			worker.process();
		}
	}
}