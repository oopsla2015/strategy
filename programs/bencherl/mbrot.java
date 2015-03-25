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
 * This benchmark extrapolates the coordinates of a 2-D complex plane that,
 * correspond to the pixels of a 2-D image of a specific resolution. 
 * For each one of these points, 
 * the benchmark determines whether the point belongs to the Mandelbrot set or not.
 * The total set of points is divided among a number of workers.
 * The benchmark is parameterized by the dimensions of the image.  
 * ported from http://release.softlab.ntua.gr/bencherl/benchmarks.html
 * @author Ganesha Upadhyaya
 */

capsule Worker(Mandel m) {
	int MAXITER = 255;
	double LIM_SQR = 4.0;
	double RL = 2.0;
	double IL = 2.0;
	
	void work(int N) {
		rows(N,N);
		System.out.println("Done!");
	}
	
	private void rows(double W, double H) {
		rows(W, H, H);
	}
	
	private void rows(double W, double H, double Hi) {
		if (Hi > 0) {
			cols(W, H, Hi);
			rows(W, H, Hi-1);
		}
	}
	
	private void cols(double W, double H, double Hi) {
		cols(W, H, W, Hi);
	}
	
	private void cols(double W, double H, double Wi, double Hi) {
		if (Wi > 0) {
			// transform X and Y pixel to mandelbrot coordinates
			double X = (Wi - 1)/W*(2*RL) - RL;
			double Y = (Hi - 1)/H*(2*IL) - IL;
			// do mandelbrot
			m.mbrot(X, Y);
			cols(W, H, Wi - 1, Hi);
		}
	}
}

capsule Mandel {
	int MAXITER = 255;
	double LIM_SQR = 4.0;
	void mbrot(double X, double Y) {
		mbrot(X,Y,X,Y,0);
	}
	
	private void mbrot(double X0, double Y0, double X1, double Y1, int I) {
		if ((I < MAXITER) && ((X1*X1 + Y1*Y1) <= LIM_SQR)) {
			double X2 = X1*X1 - Y1*Y1 + X0;
			double Y2 = 2*X1*Y1 + Y0;
			mbrot(X0, Y0, X2, Y2, (I+1));
		}
	}
}

capsule mbrot (String args[]) {
	design {
		// short -> [2, 4], intermediate -> [4, 4], long -> [7, 4]
		// 2-core, [N,Np] = [4,8], [N,Np] = [8,8], [N,Np] = [14,8], 
		// 4-core, [N,Np] = [8,16], [N,Np] = [16,16], [N,Np] = [28,16], 
		// 8-core, [N,Np] = [16,32], [N,Np] = [32,32], [N,Np] = [56,32], 
		// 12-core, [N,Np] = [24,48], [N,Np] = [48,48], [N,Np] = [84,48], 
		// 16-core, [N,Np] = [32,64], [N,Np] = [64,64], [N,Np] = [112,64], 
		// 24-core, [N,Np] = [48,96], [N,Np] = [96,96], [N,Np] = [168,96], 
		//int N = 168, Np = 96;
		 Worker workers[8];
		 Mandel m;
		wireall(workers, m);
	}
	
	void run() {
		for(Worker worker : workers) {
			worker.work(168);
		}
	}
}
