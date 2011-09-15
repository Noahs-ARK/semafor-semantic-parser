package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.Arrays;
import java.util.Map;

import edu.cmu.cs.lti.ark.util.ds.Pair;
import gnu.trove.THashMap;

import ilog.concert.*; 
import ilog.cplex.*;

public class ILPDecoding {
	private IloCplex cplex = null;
	public ILPDecoding() {
		try {
			cplex = new IloCplex(); 
		} catch (IloException e) { 
			System.err.println("Concert exception caught: " + e);
			System.exit(-1);
		}
	}

	public Map<String, String> decode(Map<String, Pair<int[], Double>[]> scoreMap) {
		String[] keys = new String[scoreMap.size()];
		scoreMap.keySet().toArray(keys);
		Arrays.sort(keys);
		int totalCount = 0;
		for (int i = 0; i < keys.length; i++) {
			Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
			totalCount += arr.length; 
		}
		int[] lb = new int[totalCount];
		int[] ub = new int[totalCount];
		double[] objVals = new double[totalCount];
		Map<String, String> res = new THashMap<String, String>();
		System.out.println("Size of keys: " + keys.length);
		System.out.println("Totalcount: " + totalCount);
		try {
			int count = 0;
			for (int i = 0; i < keys.length; i++) {
				Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
				for (int j = 0; j < arr.length; j++) {
					lb[count] = 0; ub[count] = 1;
					objVals[count] = arr[j].getSecond();
					count++;
				}
			}
			IloIntVar[] x = cplex.intVarArray(totalCount, lb, ub);
			cplex.addMaximize(cplex.scalProd(x, objVals));
			count = 0;
			for (int i = 0; i < keys.length; i++) {
				Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
				IloNumExpr[] prods = new IloNumExpr[arr.length];
				for (int j = 0; j < arr.length; j++) {
					lb[count] = 0; ub[count] = 1;
					objVals[count] = arr[j].getSecond();
					prods[j] = cplex.prod(1.0, x[j]);
					count++;
				}
				cplex.addEq(cplex.sum(prods), 1.0);
			}			
			if (cplex.solve()) { 
				cplex.output().println("Solution status = " + cplex.getStatus()); 
				cplex.output().println("Solution value  = " + cplex.getObjValue());
				double[] val = cplex.getValues(x); 
				int ncols = cplex.getNcols(); 
				for (int j = 0; j < ncols; ++j) 
					cplex.output().println("Column: " + j + " Value = " + val[j]); 
			}
			cplex.clearModel();
		} catch (IloException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}

	public void decodeTrivial() {
		try { 
			int[] lb = {0, 0, 0}; 
			int[] ub = {40, Integer.MAX_VALUE, Integer.MAX_VALUE};
			IloIntVar[] x  = cplex.intVarArray(3, lb, ub);
			int[] objvals = {1, 2, 3};
			cplex.addMaximize(cplex.scalProd(x, objvals)); 
			cplex.addLe(cplex.sum(cplex.prod(-1.0, x[0]), 
					cplex.prod( 1.0, x[1]), 
					cplex.prod( 1.0, x[2])), 20.0); 
			cplex.addLe(cplex.sum(cplex.prod( 1.0, x[0]), 
					cplex.prod(-3.0, x[1]), 
					cplex.prod( 1.0, x[2])), 30.0);
			if (cplex.solve()) { 
				cplex.output().println("Solution status = " + cplex.getStatus()); 
				cplex.output().println("Solution value  = " + cplex.getObjValue());
				double[] val = cplex.getValues(x); 
				int ncols = cplex.getNcols(); 
				for (int j = 0; j < ncols; ++j) 
					cplex.output().println("Column: " + j + " Value = " + val[j]); 
			}
			cplex.clearModel();
		} catch (IloException e) { 
			System.err.println("Concert exception caught: " + e); 
		}
	}

	public void end() {
		cplex.end();
	}

	public static void main(String[] args) {
		System.out.println("Solving ILP:");
		ILPDecoding ilp = new ILPDecoding();
		ilp.decodeTrivial();
		ilp.decodeTrivial();
		ilp.end();
	}
}