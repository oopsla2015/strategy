
class LogisticMapConfig {
	protected static int numTerms = 25_000;
	protected static int numSeries = 10;
	protected static double startRate = 3.46;
	protected static double increment = 0.0025;
	protected static boolean debug = false;

	protected static void parseArgs(final String[] args) {

	}

	protected static void printArgs() {

	}

	protected static double computeNextTerm(final double curTerm, final double rate) {
		return rate * curTerm * (1 - curTerm);
	}

	protected static final class StartMessage {
		protected static StartMessage ONLY = new StartMessage();
	}

	protected static final class StopMessage {
		protected static StopMessage ONLY = new StopMessage();
	}

	protected static final class NextTermMessage {
		protected static NextTermMessage ONLY = new NextTermMessage();
	}

	protected static final class GetTermMessage {
		protected static GetTermMessage ONLY = new GetTermMessage();
	}

	protected static class ComputeMessage {
		public final int senderId;
		public final double term;
		public ComputeMessage(final int senderId, final double term) {
			this.senderId = senderId;
			this.term = term;
		}
	}

	protected static class ResultMessage {
		public final double term;
		public ResultMessage(final double term) {
			this.term = term;
		}
	}
}

capsule Master(RateComputer computers[], SeriesWorker workers[]) {
	int numComputers = LogisticMapConfig.numSeries;
	int numWorkers = LogisticMapConfig.numSeries;
	int numWorkRequested = 0;
	int numWorkReceived = 0;
	double termsSum = 0;

	void begin(LogisticMapConfig.StartMessage sm) {
		int i = 0;
		while (i < LogisticMapConfig.numTerms) {
			for (SeriesWorker worker : workers) {
				worker.nextTerm();
			}
			i += 1;
		}
		for (SeriesWorker worker : workers) {
			worker.getTerm();
			numWorkRequested += 1;
		}
	}

	void process(LogisticMapConfig.ResultMessage rm) {
		termsSum += rm.term;
		numWorkReceived += 1;

		if (numWorkRequested == numWorkReceived) {
			System.out.println("Terms sum: " + termsSum);
			for (SeriesWorker worker : workers) {
				worker.done();
			}
			for (RateComputer computer : computers) {
				computer.done();
			}
		}
	}
}

capsule SeriesWorker(int id, Master master, RateComputer computer) {
	double startTerm = 0;
	double curTerm = 0;

	void setStartTerm(double st) {
		startTerm = st;
		curTerm = startTerm;
	}

	void nextTerm() {
		int senderId = id;
		computer.compute(new LogisticMapConfig.ComputeMessage(senderId, curTerm));
	}

	void result(LogisticMapConfig.ResultMessage resultMessage) {
		curTerm = resultMessage.term;
	}

	void getTerm() {
		master.process(new LogisticMapConfig.ResultMessage(curTerm));
	}

	void done() {
		((PaniniCapsule)master).shutdown();
	}
}

capsule RateComputer(SeriesWorker worker) {
	double rate = 0.0;
	void setRate(double r) {
		rate = r;
	}

	void compute(LogisticMapConfig.ComputeMessage computeMessage) {
		double result = LogisticMapConfig.computeNextTerm(computeMessage.term, rate);
		int senderId = computeMessage.senderId;
		//workers[senderId].result(new LogisticMapConfig.ResultMessage(result));
		worker.result(new LogisticMapConfig.ResultMessage(result));
	}

	void done() {
		((PaniniCapsule)worker).shutdown();
	}
}

@Parallelism(2)
capsule logmap {
	design {
		Master master;
		SeriesWorker workers[10];
		RateComputer computers[10];
		computers[0](workers[0]);
		computers[1](workers[1]);
		computers[2](workers[2]);
		computers[3](workers[3]);
		computers[4](workers[4]);
		computers[5](workers[5]);
		computers[6](workers[6]);
		computers[7](workers[7]);
		computers[8](workers[8]);
		computers[9](workers[9]);
		workers[0](0, master, computers[0]);
		workers[1](1, master, computers[1]);
		workers[2](2, master, computers[2]);
		workers[3](3, master, computers[3]);
		workers[4](4, master, computers[4]);
		workers[5](5, master, computers[5]);
		workers[6](6, master, computers[6]);
		workers[7](7, master, computers[7]);
		workers[8](8, master, computers[8]);
		workers[9](9, master, computers[9]);
		master(computers, workers);
	}

	void run() {
		for (int i = 0; i < computers.length; i++) {
			double rate = LogisticMapConfig.startRate + (i * LogisticMapConfig.increment);
			computers[i].setRate(rate);
		}
		for (int i = 0; i < workers.length; i++) {
			double startTerm = i * LogisticMapConfig.increment;
			workers[i].setStartTerm(startTerm);
		}
		master.begin(LogisticMapConfig.StartMessage.ONLY);
	}
}
