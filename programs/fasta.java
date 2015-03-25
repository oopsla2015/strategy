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

import java.io.IOException;
import java.io.OutputStream;


/**
 * fasta benchmark : Generate and write random DNA sequences
 * http://benchmarksgame.alioth.debian.org/u32/performance.php?test=fasta#about
 * @author Ganesha Upadhyaya
 */
capsule FloatProbFreq(int id) {
	int IM = 139968;
	int IA = 3877;
	int IC = 29573;
	
	byte[] iub = new byte[]{
            'a',  'c',  'g',  't',
            'B',  'D',  'H',  'K',
            'M',  'N',  'R',  'S',
            'V',  'W',  'Y'};
	byte[] sapiens = new byte[]{
            'a',
            'c',
            'g',
            't'};
	double[] iub_probs = new double[]{
            0.27, 0.12, 0.12, 0.27,
            0.02, 0.02, 0.02, 0.02,
            0.02, 0.02, 0.02, 0.02,
            0.02, 0.02, 0.02,
            };
	double[] sapiens_probs = new double[]{
            0.3029549426680d,
            0.1979883004921d,
            0.1975473066391d,
            0.3015094502008d};
	
	int last = 42;
	byte[] chars;
	float[] probs;
	=>{
		byte[] dchars;
		double[] dprobs;
		if (id == 0) {
			dchars = iub;
			dprobs = iub_probs;
			
		} else {
			dchars = sapiens;
			dprobs = sapiens_probs;
		}
		chars = dchars;
		probs = new float[dprobs.length];
        for (int i = 0; i < dprobs.length; i++) {
           this.probs[i] = (float)dprobs[i];
        }
		makeCumulative();
	};
	
	private void makeCumulative() {
		double cp = 0.0;
        for (int i = 0; i < probs.length; i++) {
            cp += probs[i];
            probs[i] = (float)cp;
        }
	}
	
	int selectRandomIntoBuffer(byte[] buffer, int bufferIndex, final int nRandom) {
		byte[] lchars = chars;
		float[] lprobs = probs;
		int len = lprobs.length;
		outer:
			for (int rIndex = 0; rIndex < nRandom; rIndex++) {
				final float r = random(1.0f);
				for (int i = 0; i < len; i++) {
					if (r < lprobs[i]) {
						buffer[bufferIndex++] = lchars[i];
						continue outer;
					}
				}
				buffer[bufferIndex++] = chars[len-1];
			}
		return bufferIndex;
	}
	
	// pseudo-random number generator
	private float random(final float max) {
		final float oneOverIM = (1.0f / IM);
		last = (last * IA + IC) % IM;
		return max * last * oneOverIM;
	}
}

capsule RandomFasta(FloatProbFreq fpf, Writer writer) {
	int LINE_LENGTH = 60;
	int BUFFER_SIZE = (LINE_LENGTH + 1)*1024;
	
	void makeRandomFasta(String id, String desc, int nChars, boolean finish) {
		byte[] buffer = new byte[BUFFER_SIZE];

		if (buffer.length % (LINE_LENGTH + 1) != 0) {
			throw new IllegalStateException(
					"buffer size must be a multiple of "
							+ "line length (including line break)");
		}

		String descStr = ">" + id + " " + desc + '\n';
		writer.write(descStr.getBytes());

		int bufferIndex = 0;
		while (nChars > 0) {
			int chunkSize;
			if (nChars >= LINE_LENGTH) {
				chunkSize = LINE_LENGTH;
			} else {
				chunkSize = nChars;
			}

			if (bufferIndex == BUFFER_SIZE) {
				writer.write(buffer, 0, bufferIndex);
				bufferIndex = 0;
			}

			bufferIndex = fpf.selectRandomIntoBuffer(buffer, bufferIndex,
					chunkSize);
			buffer[bufferIndex++] = '\n';

			nChars -= chunkSize;
		}

		writer.write(buffer, 0, bufferIndex);
		if (finish)
			writer.close();
	}
}

capsule RepeatFasta(Writer writer) {
	int LINE_LENGTH = 60;
	int BUFFER_SIZE = (LINE_LENGTH + 1)*1024;
	String alu =
            "GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGG"
          + "GAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGA"
          + "CCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAAT"
          + "ACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA"
          + "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG"
          + "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC"
          + "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA";

	void makeRepeatFasta(String id, String desc, int nChars) {
		final byte[] aluBytes = alu.getBytes();
		int aluIndex = 0;
		byte[] buffer = new byte[BUFFER_SIZE];

		if (buffer.length % (LINE_LENGTH + 1) != 0) {
			throw new IllegalStateException("buffer size must be a multiple "
					+ "of line length (including line break)");
		}

		String descStr = ">" + id + " " + desc + '\n';
		writer.write(descStr.getBytes());

		int bufferIndex = 0;
		while (nChars > 0) {
			final int chunkSize;
			if (nChars >= LINE_LENGTH) {
				chunkSize = LINE_LENGTH;
			} else {
				chunkSize = nChars;
			}

			if (bufferIndex == BUFFER_SIZE) {
				writer.write(buffer, 0, bufferIndex);
				bufferIndex = 0;
			}

			for (int i = 0; i < chunkSize; i++) {
				if (aluIndex == aluBytes.length) {
					aluIndex = 0;
				}

				buffer[bufferIndex++] = aluBytes[aluIndex++];
			}
			buffer[bufferIndex++] = '\n';

			nChars -= chunkSize;
		} 

		writer.write(buffer, 0, bufferIndex);
	}
}

capsule Writer {
	OutputStream writer = System.out;
	void write(byte[] buffer) {
		try {
			writer.write(buffer);
		} catch (Exception e) {
			
		}
	}
	
	void write(byte[] buffer, int size, int index) {
		try {
			writer.write(buffer, size, index);
		} catch (Exception e) {
			
		}
		
	}
	
	void close() {
		try {
			writer.close();
		} catch (Exception e) {
			
		}
	}
}

capsule fasta {
	int n;
	design {
		n = 1000000;
		//n = 5000000;
		//n = 500000;
		//n = 25000000;
		  FloatProbFreq IUB;
		  FloatProbFreq HOMO_SAPIENS;
		  RandomFasta random1;
		  RandomFasta random2;
		 RepeatFasta repeat;
		  Writer writer;
		
		IUB(0);
		HOMO_SAPIENS(1);
		
		repeat(writer);
		random1(IUB, writer);
		random2(HOMO_SAPIENS, writer);
	}
	
	void run() {
		try {
			repeat.makeRepeatFasta("ONE", "Homo sapiens alu", n * 2);
			random1.makeRandomFasta("TWO", "IUB ambiguity codes", n * 3, false);
			random2.makeRandomFasta("THREE", "Homo sapiens frequency", n * 5, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
