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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;


/**
 * StreamIt DCT benchmark
 * Description: The dct_ieee package contains functions that implement Discrete Cosine 
 * Transforms and Inverse Discrete Cosine Transforms in accordance with IEEE specifications 
 * for such operations. The IEEE specified DCT is used in both the MPEG and JPEG standards.
 * A definition of what makes a DCT or inverse DCT implementation conform to the 
 * IEEE specification can be found in Appendix A of the MPEG-2 specification (ISO/IEC 13818-2) 
 * on P. 125.
 * src: http://groups.csail.mit.edu/cag/streamit/shtml/benchmarks.shtml
 */

signature Stage {
	void consume (int nIter, int iterations, float[] input);
}

capsule FileReader(String inputfile, Stage dct_reference) implements Stage {
	DataInputStream inputStream = null;
	private void init() {
		try{
			File inputFile = new File(inputfile);
			FileInputStream localFileInputStream = new FileInputStream(inputFile);
			inputStream = new DataInputStream(new BufferedInputStream(localFileInputStream));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private int endianFlip(int paramInt)
	{
	    int i = paramInt >> 24 & 0xFF;
	    int j = paramInt >> 16 & 0xFF;
	    int k = paramInt >> 8 & 0xFF;
	    int m = paramInt >> 0 & 0xFF;

	    return i | j << 8 | k << 16 | m << 24;
	}

	private short endianFlip(short paramShort)
	{
	    int i = paramShort >> 8 & 0xFF;
	    int j = paramShort >> 0 & 0xFF;

	    return (short)(i | j << 8);
	}
	
	void consume(int nIter, int iterations, float[] input) {
		int n = 256;
		float[] output = new float[n];
		init();
		try {
			for (int index=0; index < n; index++) {
				output[index] = /*Float.intBitsToFloat(endianFlip(*/inputStream.readInt();//);//);
			}
			inputStream.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		dct_reference.consume(nIter, iterations, output);
	}
}

capsule iDCT8x8_ieee(Stage reference2D) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		reference2D.consume(nIter, iterations, input);
	}
}

capsule iDCT_2D_reference_fine(Stage anonFilter_a0) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		anonFilter_a0.consume(nIter, iterations, input);
	}
}

capsule AnonFilter_a0(Stage reference1DY) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		reference1DY.consume(nIter, iterations, input);
	}
}

capsule iDCT_1D_Y_reference_fine(Stage ySplitter) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		ySplitter.consume(nIter, iterations, input);
	}
}

capsule YSplitter(int x, Stage[] reference1Ds) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		for (int i = 0; (i < x); i++) {
			float[] in = new float[x];
			for (int j = 0; j < x; j++) {
				in[j] = input[i*x+j];
			}
			reference1Ds[i].consume(nIter, iterations, in);
		}
	}
}

capsule iDCT_1D_reference_fine(int size, Stage dct_reference_fine) implements Stage {
	float[][] coeff = null;

	void consume(int nIter, int iterations, float[] input) {
		coeff = new float[size][size];
		for (int x = 0; (x < size); x++) {
			for (int u = 0; (u < size); u++) {
				float Cu;
				Cu = 1;
				if ((u == 0)) { 
					Cu = (1 / (float)Math.sqrt(2)); 
				}
				coeff[x][u] = ((0.5f * Cu) * (float)Math.cos((((u * 3.141592653589793f) * 
						((2.0f * x) + 1)) / (2.0f * size))));
			}
		}
		//int n = 16;
		float[] output = new float[size];
		//for (int index=0; index < n; index++) {
			for (int x = 0; (x < size); x++) {
				float tempsum = 0;
				for (int u = 0; (u < size); u++) { 
					tempsum += (coeff[x][u] * input[u]);
				}
				output[x] = tempsum;
			}
			for (int i = 0; (i < size); i++) {
				// pop input array values
			}
		//}
		dct_reference_fine.consume(nIter, iterations, output);
	}
}

capsule iDCT_1D_X_reference_fine(int size, Stage writer) implements Stage {
	float[][] output = null;
	int[] oI = null;
	
	void consume(int nIter, int iterations, float[] input) {
		if (output == null) {
			output = new float[iterations][size*size];
			oI = new int[iterations];
		}
		for(int i = 0; i < size; i++) {
			output[nIter][oI[nIter]*size+i] = input[i];
		}
		oI[nIter]++;
		if(oI[nIter] == size) {
			float[] out = new float[size*size];
			for(int i=0; i<size*size; i++) {
				out[i] = output[nIter][i];
			}
			writer.consume(nIter, iterations, out);
		}
	}
}

/*capsule XSplitter(int x, Stage[] reference1Ds) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		for (int i = 0; (i < x); i++) {
			float[] in = new float[x];
			for (int j = 0; j < x; j++) {
				in[j] = input[i*x+j];
			}
			reference1Ds[i].consume(nIter, iterations, in);
		}
	}
}

capsule AnonFilter_a1(int size, Stage writer) implements Stage {
	float[][] output = null;
	int[] oI = null;

	void consume(int nIter, int iterations, float[] input) {
		if (output == null) {
			output = new float[iterations][size*size];
			oI = new int[iterations];
		}
		for(int i = 0; i < size; i++) {
			output[nIter][oI[nIter]*size+i] = input[i];
		}
		oI[nIter]++;
		if(oI[nIter] == size) {
			float[] out = new float[size*size];
			for(int i=0; i<size*size; i++) {
				out[i] = output[nIter][i];
			}
			writer.consume(nIter, iterations, out);
		}
	}
}*/

capsule FileWriter(String outputfile) implements Stage {
	DataOutputStream outputStream;
	=>{
		try{
			File outputFile = new File(outputfile);
			FileOutputStream localFileOutputStream = new FileOutputStream(outputFile);
			outputStream = new DataOutputStream(new BufferedOutputStream(localFileOutputStream));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private int endianFlip(int paramInt)
	{
	    int i = paramInt >> 24 & 0xFF;
	    int j = paramInt >> 16 & 0xFF;
	    int k = paramInt >> 8 & 0xFF;
	    int m = paramInt >> 0 & 0xFF;

	    return i | j << 8 | k << 16 | m << 24;
	}

	private short endianFlip(short paramShort)
	{
	    int i = paramShort >> 8 & 0xFF;
	    int j = paramShort >> 0 & 0xFF;

	    return (short)(i | j << 8);
	}
	  
	void consume(int nIter, int iterations, float[] input) {
		int n = 256;
		for (int index=0; index < n; index++) {
			try{
				//System.out.println(input[index]);
				outputStream.writeInt(/*endianFlip(*/Float.floatToIntBits(input[index]));//)));
				outputStream.flush();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
@Parallelism(2)
capsule DCT(String[] args) {
	int iterations;
	=>{
		iterations = Integer.parseInt(args[0]);
	}
	design {
		int x = 16;
		String inputFileName = "idct-input-small.bin";//args[1];
		String outputFileName = "idct-output-small.bin";//args[2];
		
		FileReader reader;
		 iDCT8x8_ieee dct_reference;
		 iDCT_2D_reference_fine reference2D;
		 AnonFilter_a0 anonFilter_a0;
		 iDCT_1D_Y_reference_fine reference1DY;
		 YSplitter ySplitter;
		 iDCT_1D_reference_fine reference1Ds[16];
		 iDCT_1D_X_reference_fine reference1DX;
		/*task XSplitter xSplitter;
		iDCT_1D_reference_fine reference1Ds2[16];
		AnonFilter_a1 anonFilter_a1;*/
		FileWriter writer;
		
		reader(inputFileName, dct_reference);
		dct_reference(reference2D);
		reference2D(anonFilter_a0);
		anonFilter_a0(reference1DY);
		reference1DY(ySplitter);
		ySplitter(x,reference1Ds);
		reference1Ds[0](x,reference1DX);
		reference1Ds[1](x,reference1DX);
		reference1Ds[2](x,reference1DX);
		reference1Ds[3](x,reference1DX);
		reference1Ds[4](x,reference1DX);
		reference1Ds[5](x,reference1DX);
		reference1Ds[6](x,reference1DX);
		reference1Ds[7](x,reference1DX);
		reference1Ds[8](x,reference1DX);
		reference1Ds[9](x,reference1DX);
		reference1Ds[10](x,reference1DX);
		reference1Ds[11](x,reference1DX);
		reference1Ds[12](x,reference1DX);
		reference1Ds[13](x,reference1DX);
		reference1Ds[14](x,reference1DX);
		reference1Ds[15](x,reference1DX);
		reference1DX(x,writer);
		/*xSplitter(x,reference1Ds2);
		reference1Ds2[0](x,anonFilter_a1);
		reference1Ds2[1](x,anonFilter_a1);
		reference1Ds2[2](x,anonFilter_a1);
		reference1Ds2[3](x,anonFilter_a1);
		reference1Ds2[4](x,anonFilter_a1);
		reference1Ds2[5](x,anonFilter_a1);
		reference1Ds2[6](x,anonFilter_a1);
		reference1Ds2[7](x,anonFilter_a1);
		reference1Ds2[8](x,anonFilter_a1);
		reference1Ds2[9](x,anonFilter_a1);
		reference1Ds2[10](x,anonFilter_a1);
		reference1Ds2[11](x,anonFilter_a1);
		reference1Ds2[12](x,anonFilter_a1);
		reference1Ds2[13](x,anonFilter_a1);
		reference1Ds2[14](x,anonFilter_a1);
		reference1Ds2[15](x,anonFilter_a1);
		anonFilter_a1(x,writer);*/
		writer(outputFileName);
	}
	void run() {
		// Steady phase
		for (int n = 0; n < iterations; n++) {
			reader.consume(n, iterations, null);
		}
	}
}
