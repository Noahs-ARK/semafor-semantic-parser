package edu.cmu.cs.lti.ark.fn.parsing;

import gnu.trove.TIntIterator;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.ark.util.ds.Pair;

public class ILPTest {
	private IloCplex cplex = null;
	private String mFactorFile;
	
	public ILPTest(String factorFile) {
		try {
			cplex = new IloCplex(); 
		} catch (IloException e) { 
			System.err.println("Concert exception caught: " + e);
			System.exit(-1);
		}
		mFactorFile = factorFile;
	}
	
	public void decode() throws IOException, IloException {
		BufferedReader bReader = new BufferedReader(new FileReader(mFactorFile));
		String line = null;
		while ((line = bReader.readLine()) != null) {
			int numVars = new Integer(line.trim());
			line = bReader.readLine();
			int numFactors = new Integer(line.trim());
			int[] lb = new int[numVars];
			int[] ub = new int[numVars];
			double[] objVals = new double[numVars];
			for (int i = 0; i < numVars; i++) {
				line = bReader.readLine();
				objVals[i] = new Double(line.trim());
				lb[i] = 0; ub[i] = 1;
			}
			IloIntVar[] x = cplex.intVarArray(numVars, lb, ub);
			cplex.addMaximize(cplex.scalProd(x, objVals));
			for (int i = 0; i < numFactors; i++) {
				line = bReader.readLine();
				String[] toks = line.split(" ");
				String type = toks[0];
				int numConnectedVars = new Integer(toks[1]);
				if (type.equals("XOR")) {
					IloNumExpr[] prods = new IloNumExpr[numConnectedVars];
					for (int j = 0; j < numConnectedVars; j++) {
						int var = new Integer(toks[2+j]) - 1;
						prods[j] = cplex.prod(1.0, x[var]);
					}
					cplex.addEq(cplex.sum(prods), 1.0);
				} else if (type.equals("XOR1")) {
					IloNumExpr[] prods = new IloNumExpr[numConnectedVars];
					for (int j = 0; j < numConnectedVars; j++) {
						int var = new Integer(toks[2+j]) - 1;
						prods[j] = cplex.prod(1.0, x[var]);
					}
					cplex.addLe(cplex.sum(prods), 1.0);
				}
			}
			if (cplex.solve()) { 
				cplex.output().println("Solution status = " + cplex.getStatus()); 
				cplex.output().println("Solution value  = " + cplex.getObjValue());
			}
			cplex.clearModel();
		}
		bReader.close();
	}
	
	public static void main(String[] args) {
		ILPTest it = new ILPTest(args[0]);
		try {
			it.decode();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
}