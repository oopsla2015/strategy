/*
 * This file is part of the Panini project at Iowa State University.
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * For more details and the latest version of this code please see
 * http://paninij.org
 * 
 * Contributor(s): Ganesha Upadhyaya
 */

import java.io.*;
import java.util.Random;
import java.lang.Math;

class Bool {
	private boolean v;
	public Bool(boolean v) { this.v = v; }
	public boolean value() { return v; }
}

class Result {
	final int min;
	final int max;
	final String minFile;
	final String maxFile;
	
	public Result(int min, int max, String minFile, String maxFile) {
		this.min = min;
		this.max = max;
		this.minFile = minFile;
		this.maxFile = maxFile;
	}
}
capsule LocCounter(Accumulator whoToTell) {
	boolean running = true;
	int workCount = 0;
	void process(String filename) {
		//yield(1500); // to simulate linecount work
		int lines = 0;
		workCount++;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while (reader.readLine() != null) lines++;
			reader.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		//System.out.println(filename+": "+lines);
		whoToTell.report(filename, lines);
		running = false;
	}
	Bool running() {
		System.out.println("WorkCount : "+workCount);
		whoToTell.done();
		return new Bool(running);
	}
}

capsule Accumulator(ResultAccumulator whoToTell) {
	int min = 100000; int max = 0;
	String minFile; String maxFile;
	int doneWorkers = 0;
	int workCount = 0;
	void report(String filename, int loc) {
		workCount++;
		if (loc < min) {
			min = loc;
			minFile = filename;
		}
		if (loc > max) {
			max = loc;
			maxFile = filename;
		}
	}
	
	void done() {
		doneWorkers++;
		if (doneWorkers == 2) {
			System.out.println("Acc workCount : "+workCount);
			whoToTell.report(new Result(min, max, minFile, maxFile));
		}
	}
}

capsule ResultAccumulator() {
	int min = 100000; int max = 0;
	String minFile; String maxFile;
	int doneWorkers = 0;
	void report(Result result) {
		if (result.min < min) {
			min = result.min;
			minFile = result.minFile;
		}
		if (result.max > max) {
			max = result.max;
			maxFile = result.maxFile;
		}
		doneWorkers++;
		if (doneWorkers == 5)
			end();
	}
	private void end() {
		System.out.println("Min: "+ min + ", Max: "+ max);
		System.out.println("MinFile: "+minFile+", MaxFile: "+maxFile);
		long memUsed = Runtime.getRuntime().totalMemory() - 
				Runtime.getRuntime().freeMemory();
		System.out.println("Memory used : "+memUsed);
	}
}

capsule LocAnalyser(LocCounter workerPool[]) {
	//Random rnd = new Random();
	int workCount = 0;
	
	void process(String filename) {
		workCount++;
		//System.out.println("Adding a new worker for " + filename);
		int id = (workCount-1)%10;//Math.abs(rnd.nextInt() % 100);
		workerPool[id].process(filename);
	}
	
	void end() {
		System.out.println("Winding up, total work: "+workCount);
		for(LocCounter worker : workerPool)
			worker.running().value();
				//System.out.println("worker done"); // blocks until all the worker are done
		//}
		/*accumulator.end();
		((PaniniCapsule)walker).panini$disconnect();
		long memUsed = Runtime.getRuntime().totalMemory() - 
				Runtime.getRuntime().freeMemory();
		System.out.println("Memory used : "+memUsed);*/
	}
}


capsule FilesystemWalker(LocAnalyser analyser) {
	void walk(String path) {
		walkAndNotify(path);
		analyser.end();
	}
	
	private void walkAndNotify(String path) {
		File file = new File(path);
		if (file.isFile()) {
			analyser.process(path);
		} else {
			for(File child : file.listFiles()) {
				walkAndNotify(child.getAbsolutePath());
			}
		}
	}
}

capsule ScratchPad(String[] args) {
	design {
		FilesystemWalker walker; 
		LocAnalyser analyser;
		LocCounter workerPool[10];
		Accumulator accumulators[5];
		ResultAccumulator resultacc;

		walker(analyser);
		analyser(workerPool);
		workerPool[0](accumulators[0]); workerPool[1](accumulators[0]);
		workerPool[2](accumulators[1]); workerPool[3](accumulators[1]);
		workerPool[4](accumulators[2]); workerPool[5](accumulators[2]);
		workerPool[6](accumulators[3]); workerPool[7](accumulators[3]);
		workerPool[8](accumulators[4]); workerPool[9](accumulators[4]);
		wireall(accumulators, resultacc);
	}
	void run() {
		File file = new File(args[0]);
		if (file.exists())
			walker.walk(args[0]);
		else 
			System.out.println("[Error] File does not exists");
	}
}
