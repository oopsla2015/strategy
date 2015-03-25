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

/**
 * A benchmark for many-to-one message passing that spawns one receiver and multiple senders
 * that flood the receiver with messages. 
 * The benchmark is parameterized by the number of senders to spawn and the number of messages 
 * that each sender sends to the receiver. 
 * ported from http://release.softlab.ntua.gr/bencherl/benchmarks.html
 * @author Ganesha Upadhyaya
 */

capsule Sender (Receiver receiver) {
	void send(String msg) {
		for (int i=0; i<440; i++) { // send M messages 
			receiver.receive(msg);
		}
	}
}

capsule Receiver {
	//java.util.List<String> output = new java.util.ArrayList<String>();
	String[] output = new String[500];
	int count = 0;
	void receive(String msg) {
		output[count++] = msg;
		//output.add(msg);
		System.out.println("MSG Recv!"+msg);
	}
}

capsule bang(String[] args) {
	design {
		// int S = ; //short: 16 * cores , intermediate: 55 * cores, long: 79 * cores
		// int S = 32; int S = 110; int S = 158; // 2-cores, sizes = (S,I,L)
		// int S = 64; int S = 220; int S = 316; // 4-cores, sizes = (S,I,L)
		// int S = 128; int S = 440; int S = 632; // 8-cores, sizes = (S,I,L)
		// int S = 192; int S = 660; int S = 948; // 12-cores, sizes = (S,I,L)
		// int S = 256; int S = 880; int S = 1264; // 16-cores, sizes = (S,I,L)
		// int S = 384; int S = 1320; int S = 1896; // 24-cores, sizes = (S,I,L)
		int S = 440; // senders
		int M = 440; // messages per sender
		 Sender senders[440];
		 Receiver receiver;
		wireall(senders, receiver);
	}
	
	void run() {
		for (Sender sender : senders) {
			sender.send("Bang!");
		}
	}
}
