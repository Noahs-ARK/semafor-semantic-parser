package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import edu.cmu.cs.lti.ark.fn.utils.BitOps;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;

public class UniqueSpanSlave implements Slave {
	public double[] mObjVals;
	public int mStart;
	public int mEnd;
	public int mTotLen;
	public double[] oldAs;
	public double[] oldZs;
	
	public UniqueSpanSlave(double[] objVals, 
						   int start, 
						   int end) {
		mObjVals = new double[end-start];
		for (int i = start; i < end; i++) {
			mObjVals[i-start] = objVals[i];
		}
		mStart = start;
		mEnd = end;
		mTotLen = objVals.length;
		oldAs = null;
		oldZs = null;
	}
	
	@Override
	public void setObjVals(double[] objVals) {
		for (int i = mStart; i < mEnd; i++) {
			mObjVals[i-mStart] = objVals[i];
		}
	}
	
	@Override
	public double computeDual(double[] objVals, double rho, double[] us, double[] lambdas,
			double[] zs) {
		setObjVals(objVals);
		double value = 0.0;
		for (int i = mStart; i < mEnd; i++) {
			value += zs[i] * (mObjVals[i-mStart] + lambdas[i]);
			value -= lambdas[i] * us[i];
			value -= (zs[i] - us[i]) * (zs[i] - us[i]) * rho / 2.0;
		}	
		return value;
	}
	
	@Override
	// XOR factor
	public double[] makeZUpdate(double[] objVals, double rho, 
						   double[] us, 
						   double[] lambdas,
						   double[] zs) {
		setObjVals(objVals);
		double[] as = new double[mEnd - mStart];
		for (int i = mStart; i < mEnd; i++) {
			double a = us[i] + (1.0 / rho) * (mObjVals[i-mStart] + lambdas[i]);
			as[i-mStart] = a;
		}
		if (checkEquals(as)) {
			return oldZs;
		}
		Double[] bs = new Double[as.length];
		for (int i = 0; i < bs.length; i++) {
			bs[i] = as[i];
		}
		Arrays.sort(bs, Collections.reverseOrder());
		double[] sums = new double[bs.length];
		Arrays.fill(sums, 0);
		sums[0] = bs[0];
		for (int i = 1; i < as.length; i++) {
			sums[i] = sums[i-1] + bs[i];
		}
		int tempRho = -1;
		for (int i = 0; i < as.length; i++) {
			double temp = bs[i] - (1.0 / (double)(i+1)) * (sums[i] - 1.0);
			if (temp > 0) {
				if (i > tempRho) {
					tempRho = i;
				}
			}	
		}
		if (tempRho == -1) {
			System.out.println("Problem. tempRho is -1");
			System.exit(-1);
		}
		double tau = (1.0 / (double)(tempRho+1)) * (sums[tempRho] - 1.0);
		double[] updZs = new double[mTotLen];
		Arrays.fill(updZs, 0);
		for (int i = mStart; i < mEnd; i++) {
			updZs[i] = Math.max(as[i-mStart] - tau, 0);
		}
		cache(as, updZs);
		return updZs;
	}
	
	@Override
	public void cache(double[] as, double[] zs) {
		oldAs = Arrays.copyOf(as, as.length);
		oldZs = Arrays.copyOf(zs, zs.length);
	}

	@Override
	public boolean checkEquals(double[] as) {
		// TODO Auto-generated method stub
		if (oldAs == null) {
			return false;
		}
		return BitOps.nearlyEquals(as, oldAs, FNModelOptions.TOL);
	}
}