

/**
 * A benchmark about message proxying through a dispatcher.
 * The benchmark spawns a certain number of receivers, one dispatcher, and a certain number of generators. 
 * The dispatcher forwards the messages that it receives from generators to the appropriate receiver.
 * Each generator sends a number of messages to a specific receiver. 
 * The parameters of the benchmark are the number of receivers, the number of messages and the message length.
 * ported from http://release.softlab.ntua.gr/bencherl/benchmarks.html
 * @author Ganesha Upadhyaya 
 */

capsule Receiver () {
	void process(String msg) {
		System.out.println(msg);
	}
}

capsule Dispatcher (Receiver recvs[]) {
	void dispatch(int recv_id, String msg) {
		recvs[recv_id].process(msg);
	}
}

capsule Generator (int id, Dispatcher disp) {
	void generate() {
		for(int i=0; i<1024*2; i++) {
			String msg = "Generate msg for recv: "+id+", msg# "+i;
			disp.dispatch(id, msg);
		}
	}
}

capsule serialmsg (String args[]) {
	design {
		// short -> [2, 16, 32], intermediate -> [2, 32, 40], long -> [5, 16, 32]
		// 2-cores, [4,32,64], [4,64,80], [10,32,64]
		// 4-cores, [8,64,128], [8,128,160], [20,64,128]
		// 8-cores, [16,128,256], [16,256,320], [40,128,256]
		// 12-cores, [24,192,384], [24,384,480], [60,192,384]
		// 16-cores, [32,256,512], [32,512,640], [80,256,512]
		// 24-cores, [48,384,768], [48,768,960], [120,384,768]
		int P = 120;
		int N = 384;
		int L = 768;
		
		Receiver recvs[120];
		Dispatcher disp;
		Generator gens[120]; // for each receiver
		
		disp(recvs);
		/*gens[0](0,disp);
		gens[1](1,disp);
		gens[2](2,disp);
		gens[3](3,disp);
		gens[4](4,disp);
		gens[5](5,disp);
		gens[6](6,disp);
		gens[7](7,disp);*/
		gens[0](0,disp);gens[1](1,disp);gens[2](2,disp);gens[3](3,disp);gens[4](4,disp);gens[5](5,disp);gens[6](6,disp);gens[7](7,disp);gens[8](8,disp);gens[9](9,disp);gens[10](10,disp);gens[11](11,disp);gens[12](12,disp);gens[13](13,disp);gens[14](14,disp);gens[15](15,disp);gens[16](16,disp);gens[17](17,disp);gens[18](18,disp);gens[19](19,disp);gens[20](20,disp);gens[21](21,disp);gens[22](22,disp);gens[23](23,disp);gens[24](24,disp);gens[25](25,disp);gens[26](26,disp);gens[27](27,disp);gens[28](28,disp);gens[29](29,disp);gens[30](30,disp);gens[31](31,disp);gens[32](32,disp);gens[33](33,disp);gens[34](34,disp);gens[35](35,disp);gens[36](36,disp);gens[37](37,disp);gens[38](38,disp);gens[39](39,disp);gens[40](40,disp);gens[41](41,disp);gens[42](42,disp);gens[43](43,disp);gens[44](44,disp);gens[45](45,disp);gens[46](46,disp);gens[47](47,disp);gens[48](48,disp);gens[49](49,disp);gens[50](50,disp);gens[51](51,disp);gens[52](52,disp);gens[53](53,disp);gens[54](54,disp);gens[55](55,disp);gens[56](56,disp);gens[57](57,disp);gens[58](58,disp);gens[59](59,disp);gens[60](60,disp);gens[61](61,disp);gens[62](62,disp);gens[63](63,disp);gens[64](64,disp);gens[65](65,disp);gens[66](66,disp);gens[67](67,disp);gens[68](68,disp);gens[69](69,disp);gens[70](70,disp);gens[71](71,disp);gens[72](72,disp);gens[73](73,disp);gens[74](74,disp);gens[75](75,disp);gens[76](76,disp);gens[77](77,disp);gens[78](78,disp);gens[79](79,disp);gens[80](80,disp);gens[81](81,disp);gens[82](82,disp);gens[83](83,disp);gens[84](84,disp);gens[85](85,disp);gens[86](86,disp);gens[87](87,disp);gens[88](88,disp);gens[89](89,disp);gens[90](90,disp);gens[91](91,disp);gens[92](92,disp);gens[93](93,disp);gens[94](94,disp);gens[95](95,disp);gens[96](96,disp);gens[97](97,disp);gens[98](98,disp);gens[99](99,disp);gens[100](100,disp);gens[101](101,disp);gens[102](102,disp);gens[103](103,disp);gens[104](104,disp);gens[105](105,disp);gens[106](106,disp);gens[107](107,disp);gens[108](108,disp);gens[109](109,disp);gens[110](110,disp);gens[111](111,disp);gens[112](112,disp);gens[113](113,disp);gens[114](114,disp);gens[115](115,disp);gens[116](116,disp);gens[117](117,disp);gens[118](118,disp);gens[119](119,disp);

	}
	
	void run() {
		for (Generator gen : gens) {
			gen.generate();
		}
	}
}
