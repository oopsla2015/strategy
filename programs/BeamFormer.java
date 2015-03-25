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
 * StreamIt BeamFormer benchmark
 * src: http://groups.csail.mit.edu/cag/streamit/shtml/benchmarks.shtml
 *
 */

signature Stage {
	void consume (int nIter, int iterations, float[] input);
}

capsule AnonFilter_a1(Stage[] anonFilter_a0) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		for (int i = 0; (i < anonFilter_a0.length); i++) {
			anonFilter_a0[i].consume(nIter, iterations, input);
		}
	}
}

capsule AnonFilter_a0(int n, Stage inputGen) implements Stage {
	void consume(int nIter, int iterations, float[] input) {
		inputGen.consume(nIter, iterations, input);
	}
}

capsule InputGenerate(int myChannel, Stage coarseBeamFirFilter,
		int numberOfSamples, int targetBeam, int targetSample, float thresh) implements Stage {
    int curSample;
    boolean holdsTarget;
    =>{
    	curSample = 0;
    	holdsTarget = (targetBeam == myChannel);
    }
    
	void consume(int nIter, int iterations, float[] input) {
		float[] output = new float[4];
		int oI = 0;
		for (int i = 2;(0 < i);i--) {
			float val = (float)Math.sqrt(curSample*myChannel);
			if ((holdsTarget && (curSample == targetSample))) {
				output[oI++] = val;
				output[oI++] = val+1;
			} else {
				output[oI++] = -val;
				output[oI++] = -val+1;
			}
			curSample++;
	
			if (curSample >= numberOfSamples) {
			    curSample = 0;
			}
		}
		coarseBeamFirFilter.consume(nIter, iterations, output);
	}
}

capsule CoarseBeamFirFilter(Stage beamFirFilter,
		int numTaps, int inputLength, int decimationRatio) implements Stage {
	float[] real_weight;
	float[] imag_weight;
	float[] realBuffer;
	float[] imagBuffer;
	int count = 0;
	int pos;
	int numTapsMinusOne;
	=>{
		real_weight = new float[numTaps];
		imag_weight = new float[numTaps];
		realBuffer = new float[numTaps];
		imagBuffer = new float[numTaps];
		numTapsMinusOne = (numTaps - 1);
		pos = 0;
		for (int j = 0; (j < numTaps); j++) {
			int idx;
			idx = (j + 1);
			real_weight[j] = ((float)Math.sin(idx) / ((float)(idx)));
			imag_weight[j] = ((float)Math.cos(idx) / ((float)(idx)));
		}
	}

	void consume(int nIter, int iterations, float[] input) {
		float[] output = new float[4];
		int oI = 0;
		int iI = 0;
		int min;
		for (int c = 2;(0 < c);c--) {
			float real_curr = 0;
			float imag_curr = 0;
			int i;
			int modPos;
			realBuffer[(numTapsMinusOne - pos)] = input[iI++];
			imagBuffer[(numTapsMinusOne - pos)] = input[iI++];
			modPos = (numTapsMinusOne - pos);
			for (i = 0; (i < numTaps); i++) {
				real_curr += ((realBuffer[modPos] * real_weight[i]) + (imagBuffer[modPos] * imag_weight[i]));
				imag_curr += ((imagBuffer[modPos] * real_weight[i]) + (realBuffer[modPos] * imag_weight[i]));
				modPos = ((modPos + 1) & numTapsMinusOne);
			}
			pos = ((pos + 1) & numTapsMinusOne);
			output[oI++] = real_curr;
			output[oI++] = imag_curr;
			count += decimationRatio;
			if ((count == inputLength)) {
				count = 0;
				pos = 0;
				for (i = 0; (i < numTaps); i++) {
					realBuffer[i] = 0;
					imagBuffer[i] = 0;
				}
			}
		}
		beamFirFilter.consume(nIter, iterations, output);
	}
}

capsule BeamFirFilter(Stage anonFilter_a3,
		int numTaps, int inputLength, int decimationRatio) implements Stage {
	float[] real_weight;
	float[] imag_weight;
	float[] realBuffer;
	float[] imagBuffer;
	int count = 0;
	int pos;
	int numTapsMinusOne;
	=>{
		real_weight = new float[numTaps];
		imag_weight = new float[numTaps];
		realBuffer = new float[numTaps];
		imagBuffer = new float[numTaps];
		int i;
		numTapsMinusOne = (numTaps - 1);
		pos = 0;
		for (int j = 0; (j < numTaps); j++) {
			int idx;
			idx = (j + 1);
			real_weight[j] = ((float)Math.sin(idx) / ((float)(idx)));
			imag_weight[j] = ((float)Math.cos(idx) / ((float)(idx)));
		}
	}

	void consume(int nIter, int iterations, float[] input) {
		float[] output = new float[2];
		float real_curr = 0;
		float imag_curr = 0;
		int i;
		int modPos;
		realBuffer[(numTapsMinusOne - pos)] = input[0];
		imagBuffer[(numTapsMinusOne - pos)] = input[1];
		modPos = (numTapsMinusOne - pos);
		for (i = 0; (i < numTaps); i++) {
			real_curr += ((realBuffer[modPos] * real_weight[i]) + (imagBuffer[modPos] * imag_weight[i]));
			imag_curr += ((imagBuffer[modPos] * real_weight[i]) + (realBuffer[modPos] * imag_weight[i]));
			modPos = ((modPos + 1) & numTapsMinusOne);
		}
		pos = ((pos + 1) & numTapsMinusOne);
		output[0] = real_curr;
		output[1] = imag_curr;

		count += decimationRatio;
		if ((count == inputLength)) {
			count = 0;
			pos = 0;
			for (i = 0; (i < numTaps); i++) {
				realBuffer[i] = 0;
				imagBuffer[i] = 0;
			}
		}
		anonFilter_a3.consume(nIter, iterations, output);
		
	}
}

capsule AnonFilter_a3() implements Stage {
	float[][] output = null;
	int[] oI = null;

	void consume(int nIter, int iterations, float[] input) {
		if (output == null) {
			output = new float[iterations][24];
			oI = new int[iterations];
		}
		output[nIter][oI[nIter]++] = input[0];
		output[nIter][oI[nIter]++] = input[1];
		if (oI[nIter] == 24) {
			for (int i = 0; i < 24; i++) {
				System.out.println(output[nIter][i]);
			}
		}
	}
}

@Parallelism(2)
capsule BeamFormer(String[] args) {
	int iterations;
	=>{
		iterations = Integer.parseInt(args[0]);
	}
	design {
		int numChannels           = 12; 
		int numSamples            = 256; 
		int numBeams              = 4; 
		int numCoarseFilterTaps   = 64; 
		int numFineFilterTaps     = 64; 
		
		int coarseDecimationRatio = 1;
		int fineDecimationRatio   = 2;
		int numSegments           = 1;
		int numPostDec1           = numSamples/coarseDecimationRatio;
		int numPostDec2           = numPostDec1/fineDecimationRatio;
		int mfSize                = numSegments*numPostDec2;
		int pulseSize             = numPostDec2/2;
		int predecPulseSize       = pulseSize*coarseDecimationRatio*fineDecimationRatio;
		int targetBeam            = numBeams/4;
		int targetSample          = numSamples/4;
		     
		int targetSamplePostDec   = targetSample/coarseDecimationRatio/fineDecimationRatio;
		float dOverLambda         = (float)0.5;
		float cfarThreshold       = (float)(0.95 * dOverLambda * numChannels * (0.5 * pulseSize));
		    
		 AnonFilter_a1 anonFilter_a1;
		 AnonFilter_a0 anonFilter_a0[12];
		 InputGenerate inputGen[12];
		 CoarseBeamFirFilter coarseBeamFirFilter[12];
		 BeamFirFilter beamFirFilter1[12];
		 AnonFilter_a3 anonFilter_a3;
		
		anonFilter_a1(anonFilter_a0);
		anonFilter_a0[0](0,inputGen[0]);
		anonFilter_a0[1](1,inputGen[1]);
		anonFilter_a0[2](2,inputGen[2]);
		anonFilter_a0[3](3,inputGen[3]);
		anonFilter_a0[4](4,inputGen[4]);
		anonFilter_a0[5](5,inputGen[5]);
		anonFilter_a0[6](6,inputGen[6]);
		anonFilter_a0[7](7,inputGen[7]);
		anonFilter_a0[8](8,inputGen[8]);
		anonFilter_a0[9](9,inputGen[9]);
		anonFilter_a0[10](10,inputGen[10]);
		anonFilter_a0[11](11,inputGen[11]);
		inputGen[0](0,coarseBeamFirFilter[0], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[1](1,coarseBeamFirFilter[1], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[2](2,coarseBeamFirFilter[2], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[3](3,coarseBeamFirFilter[3], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[4](4,coarseBeamFirFilter[4], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[5](5,coarseBeamFirFilter[5], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[6](6,coarseBeamFirFilter[6], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[7](7,coarseBeamFirFilter[7], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[8](8,coarseBeamFirFilter[8], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[9](9,coarseBeamFirFilter[9], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[10](10,coarseBeamFirFilter[10], numSamples, targetBeam, targetSample, cfarThreshold);
		inputGen[11](11,coarseBeamFirFilter[11], numSamples, targetBeam, targetSample, cfarThreshold);
		coarseBeamFirFilter[0](beamFirFilter1[0], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[1](beamFirFilter1[1], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[2](beamFirFilter1[2], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[3](beamFirFilter1[3], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[4](beamFirFilter1[4], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[5](beamFirFilter1[5], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[6](beamFirFilter1[6], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[7](beamFirFilter1[7], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[8](beamFirFilter1[8], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[9](beamFirFilter1[9], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[10](beamFirFilter1[10], numFineFilterTaps, numSamples, fineDecimationRatio);
		coarseBeamFirFilter[11](beamFirFilter1[11], numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[0](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[1](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[2](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[3](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[4](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[5](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[6](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[7](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[8](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[9](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[10](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
		beamFirFilter1[11](anonFilter_a3, numFineFilterTaps, numSamples, fineDecimationRatio);
	}
	
	void run() {
		for (int n = 0; n < iterations; n++) {
			anonFilter_a1.consume(n, iterations, null);
		}
	}
}