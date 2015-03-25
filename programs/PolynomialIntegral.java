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

import java.util.*;
import java.lang.*;

class Interval {
	double from;
	double to;
	public Interval(double f, double t) {
		this.from = f;
		this.to = t;
	}
}

class PolynomialInterval {
	Interval interval;
	List<Double> coefList = new ArrayList<Double>();
	public PolynomialInterval(Interval in, List<Double> cList) {
		this.interval = in;
		this.coefList = cList;
	}
}

class ComputationContext {
	Interval interval;
	List<Double> coefList = new ArrayList<Double>();
	double precision;
	public ComputationContext(Interval in, List<Double> cList, double p) {
		this.interval = in;
		this.coefList = cList;
		this.precision = p;
	}
}

/**
 * Actor that delegates the calculation of a polynomial integral to a remote Actor.
 * It runs on the client.
 */
capsule AreaUnderTheCurveRemoteDelegateActor(AreaUnderTheCurveDispatcherActor server) {
	double compute(ComputationContext context) {
		double val = server.computeAreaUnderTheCurve(context);
		return val;
	}
}

/**
 * Actor that runs on the server.
 * It splits the calculation interval and executes an Actor for each of them.
 * The results of the actors are added and returned to the delegate actor in the client.
 */
capsule AreaUnderTheCurveDispatcherActor(AreaUnderTheCurveComputerActor computeActors[]) {
	double areaSum = 0.0;
	double computeAreaUnderTheCurve(ComputationContext context) {
		int subintervals = (int)((context.interval.to - context.interval.from) / context.precision); 
		int subintervalStep = (int)((context.interval.to - context.interval.from) / subintervals);
		double lastX = context.interval.from;
		List<Double> resObjs = new ArrayList<Double>();
		for (int i=0; i<subintervals; i++) {
			double toX = lastX + subintervalStep;
			double res = computeActors[(i%500)].areaUnderTheCurve(
					new Interval(lastX, toX), context.coefList);
			resObjs.add(res);
			lastX = toX;
		}
		for (Double result : resObjs) {
			areaSum += result.doubleValue();
		}
		return areaSum;
	}
}

/**
 * Calculates an approximation of a polynomial integral using the Simpson formula rule.
 */
capsule AreaUnderTheCurveComputerActor {
	 double areaUnderTheCurve(Interval interval, List<Double> coefList) {
		double x1 = interval.from;
		double x2 = interval.to;
		double fx1 = f(x1, coefList);
		double fx2 = f(x2, coefList);
		double fxm = f((x1 + x2)/2, coefList);
		double val = (x2 + x1) / 6 * ( fx1 + 4 * fxm + fx2 );
		return val;
	}
	
	private double f(double x, List<Double> coefList) {
		double f = 0.0;
		for(int i=0; i<coefList.size(); i++) {
			f += coefList.get(i) * Math.pow(x,i); 
		}
		return f;
	}
}

capsule PolynomialIntegral {
	//Configuration
	// - 1 - 4X + 3X^2 - 2X^3 + X^4 + X^5
	List<Double> coefList = new ArrayList<Double>();
	double precision = 0.001;
	Interval interval = new Interval(-10, 10);
	design {
		AreaUnderTheCurveRemoteDelegateActor client;
		AreaUnderTheCurveDispatcherActor server;
		AreaUnderTheCurveComputerActor workers[500];
		
		client(server);
		server(workers);
	}
	
	void run() {
		double x1 = -1, x2 = -4, x3 = 3, x4 = -2, x5 = 1, x6 = 4;
		coefList.add(x1);
		coefList.add(x2);
		coefList.add(x3);
		coefList.add(x4);
		coefList.add(x5);
		coefList.add(x6);
		ComputationContext computationContext = new 
				ComputationContext(interval, coefList, precision);
		List<Double> integralCoefList = integral(coefList);
		double integralValue = (f(interval.to, integralCoefList) -  
				f(interval.from, integralCoefList));
		Double approxIntegralValue = client.compute(computationContext);
		System.out.println("Polynomial equation is " + polynomialToString(coefList));
		System.out.println("Integral to be calculated in [ " 
				+ interval.from + " , " + interval.to + " ]");
		System.out.println("Approximate integral calculated using steps of " + precision);
		System.out.println("Integral equation is " + polynomialToString(integralCoefList));
		System.out.println("Real integral value = " + integralValue);
		System.out.println("Approximate integral value = " + approxIntegralValue);
	}
	
	private List<Double> integral(List<Double> coefList) {
		List<Double> integralCoefList = new ArrayList<Double>();
		for(int i=0; i<coefList.size(); i++) {
			integralCoefList.add(coefList.get(i) / (i + 1));
		}
		return integralCoefList;
	}
	
	private double f(double x, List<Double> coefList) {
		double f = 0.0;
		for(int i=0; i<coefList.size(); i++) {
			f += coefList.get(i) * Math.pow(x,i); 
		}
		return f;
	}
	
	private String polynomialToString(List<Double> coefList) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0; i<coefList.size(); i++) {
			//Print the coef only if it is not 0
			if(coefList.get(i) != 0) {
				if(coefList.get(i) > 0) {
					if(!first) sb.append(" + ");
				} else {
					sb.append(" - ");
				}
				//if the coefficient is not 1 or if it has grade 0, it is printed
				if(Math.abs(coefList.get(i)) != 1 || i == 0) {
					sb.append(Math.abs(coefList.get(i)));
				}
				if(i > 0) sb.append("X");
				if(i > 1) {
					sb.append("^");
					sb.append(i);
				}
				first = false;
			}
		}
		return sb.toString();
	}
}
