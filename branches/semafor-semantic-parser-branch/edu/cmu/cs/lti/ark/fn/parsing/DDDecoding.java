package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;

import ilog.concert.*; 
import ilog.cplex.*;

public class DDDecoding implements JDecoding {
	private Map<String, Set<Pair<String, String>>> excludesMap;
	private Map<String, Set<Pair<String, String>>> requiresMap;
	public static final double TAU = 1.5;
	public static final double RHO_START = 0.03;
	
	public DDDecoding() {
	}
	
	public void setMaps(Map<String, Set<Pair<String, String>>> excludesMap, 
						Map<String, Set<Pair<String, String>>> requiresMap) {
		this.excludesMap = excludesMap;
		this.requiresMap = requiresMap;
	}
	
	public Map<String, String> decode(Map<String, Pair<int[], Double>[]> scoreMap, 
									  String frame,
									  boolean costAugmented,
									  FrameFeatures goldFF) {
		Map<String, String> res = new THashMap<String, String>();
		if (scoreMap.size() == 0) {
			return res;
		}
		String[] keys = new String[scoreMap.size()];
		scoreMap.keySet().toArray(keys);
		Arrays.sort(keys);	
		int totalCount = 0;
		int max = -Integer.MAX_VALUE;
		
		// counting the total number of z variables needed
		// also mapping the role and span indices to a variable index
		int[][] mappedIndices = new int[keys.length][];
		int count = 0;
		for (int i = 0; i < keys.length; i++) {
			Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
			totalCount += arr.length;
			mappedIndices[i] = new int[arr.length];
			for (int j = 0; j < arr.length; j++) {
				int start = arr[j].getFirst()[0];
				int end = arr[j].getFirst()[1];
				if (start != -1) {
					if (start > max) {
						max = start;
					}
				}
				if (end != -1) {
					if (end > max) {
						max = end;
					}
				}
				mappedIndices[i][j] = count;
				count++;
			}
		}		
		System.out.println("Max index:" + max);
		TIntHashSet[] overlapArray = new TIntHashSet[max+1];
		// double[] objVals = new double[totalCount + max + 1];
		double[] objVals = new double[totalCount];
		double[] costs = new double[totalCount];
		for (int i = 0; i < max+1; i++) {
			overlapArray[i] = new TIntHashSet();
		}
		// adding costs to the objVals for cost augmented decoding
		if (costAugmented) {
			ArrayList<String> fes = goldFF.fElements;
			for (int i = 0; i < fes.size(); i++) {
				String fe = fes.get(i);
				int index = Arrays.binarySearch(keys, fe);
				if (index < 0) {
					System.out.println("Problem. Fe: " + fe + " not found in array. Exiting.");
					System.exit(-1);
				}
				Pair<int[], Double>[] arr = scoreMap.get(keys[index]);
				int[] goldSpan = goldFF.fElementSpansAndFeatures.get(i)[goldFF.fGoldSpans.get(i)].span; 
				for (int j = 0; j < arr.length; j++) {
					if (arr[j].getFirst()[0] == goldSpan[0] && 
					    arr[j].getFirst()[1] == goldSpan[1]) {
						costs[mappedIndices[index][j]] = 0.0;
					} else {
						costs[mappedIndices[index][j]] = 1.0;
					}
				}
			}
		}		
		count = 0;
		for (int i = 0; i < keys.length; i++) {
			Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
			for (int j = 0; j < arr.length; j++) {
				objVals[count] = arr[j].getSecond();
				if (costAugmented) {
					objVals[count] += costs[count];
				}
				int start = arr[j].getFirst()[0];
				int end = arr[j].getFirst()[1];
				if (start != -1 && end != -1) {
					for (int k = start; k <= end; k++) {
						overlapArray[k].add(count);
					}
				}
				count++;
			}
		}
		// for a set of dummy variables for the OverlapSlave
//		for (int i = 0; i < max + 1; i++) {
//			objVals[i + totalCount] = 0.0;
//			overlapArray[i].add(i + totalCount);
//		}	
		// finished adding costs
		
		
		int len = objVals.length;
		int[] deltaarray = new int[len];
		// int slavelen = keys.length + max + 1;
		int slavelen = keys.length;
		int[][] slaveparts = new int[slavelen][];
		int[][] partslaves = new int[len][];
		Arrays.fill(deltaarray, 0);
				
		// creating slaves
		Slave[] slaves = new Slave[slavelen];
		for (int i = 0; i < keys.length; i++) {
			slaves[i] = new UniqueSpanSlave(objVals, 
					   						mappedIndices[i][0], 
					   						mappedIndices[i][mappedIndices[i].length-1] + 1);
			slaveparts[i] = new int[mappedIndices[i].length];
			for (int j = 0; j < mappedIndices[i].length; j++) {
				deltaarray[mappedIndices[i][j]] += 1;
				slaveparts[i][j] = mappedIndices[i][j];
			}
		}
		
		/*for (int i = keys.length; i < keys.length + max + 1; i++) {
			int[] vars = overlapArray[i-keys.length].toArray();
			slaves[i] = new OverlapSlave(objVals, vars);
			for (int v: vars) {
				deltaarray[v] += 1;
			}
			slaveparts[i] = vars;
		}*/
		
		for (int s = 0; s < slaveparts.length; s++) {
			Arrays.sort(slaveparts[s]);
		}
		
		double totalDelta = 0.0;
		TIntHashSet[] partslavessets = new TIntHashSet[len];
		for (int i = 0; i < len; i++) {
			totalDelta += deltaarray[i];
			partslavessets[i] = new TIntHashSet();
			for (int s = 0; s < slavelen; s++) {
				if (Arrays.binarySearch(slaveparts[s], i) >= 0) {
					partslavessets[i].add(s);
				}
			}
			partslaves[i] = partslavessets[i].toArray();
			Arrays.sort(partslaves[i]);
		}
		
		/** starting optimization procedure **/		
		double[] u = new double[len]; 
		double[][] zs = new double[slavelen][len]; 
		double[][] lambdas = new double[slavelen][len];
		
		Arrays.fill(u, 0.5);
		for (int i = 0; i < slavelen; i++) {
			lambdas[i] = new double[len];
			Arrays.fill(lambdas[i], 0.0);
			zs[i] = new double[len];
			Arrays.fill(zs[i], 0.0);
		}		
		double rho = RHO_START;
		int itr = 0;		
		while (true) {
			System.out.println("Rho: " + rho);
			double eta = TAU * rho;
			System.out.println("Eta: " + eta);
			// making z-update
			for (int s = 0; s < slavelen; s++) {
				zs[s] = slaves[s].makeZUpdate(rho, u, lambdas[s], zs[s]);
			}			
			// making u update
			double[] oldus = Arrays.copyOf(u, u.length);
			for (int i = 0; i < len; i++) {
				double sum = 0.0;
				for (int j = 0; j < partslaves[i].length; j++) {
					int s = partslaves[i][j];
					sum += zs[s][i];
				}
				u[i] = sum / deltaarray[i];
			}
			
			// making lambda update
			for (int s = 0; s < slavelen; s++) {
				for (int r = 0; r < slaveparts[s].length; r++) {
					int i = slaveparts[s][r];
					lambdas[s][i] = lambdas[s][i] - eta * (zs[s][i] - u[i]);
				}
			}
			
			// computing the primal residual
			double pr = 0.0;
			for (int s = 0; s < slavelen; s++) {
				for (int p = 0; p < slaveparts[s].length; p++) {
					pr += (zs[s][slaveparts[s][p]] - u[slaveparts[s][p]]) *
						   (zs[s][slaveparts[s][p]] - u[slaveparts[s][p]]);
				}
			}
			pr /= totalDelta;
			
			// computing the dual residual
			double dr = 0.0;
			for (int i = 0; i < len; i++) {
				dr += deltaarray[i] * (u[i] - oldus[i]) * (u[i] - oldus[i]); 
			}
			dr /= totalDelta;
			
			System.out.println(itr + ": Primal residual: " + pr);
			System.out.println(itr + ": Dual residual: " + dr);
			if (pr > dr) {
				double rat = pr / dr;
				if (rat > 10.0) {
					rho = rho * 2.0;
				}
			} else {
				double rat = dr / pr;
				if (rat > 10.0) {
					rho = rho / 2.0;
				}
			}			
			itr++;
			if (itr >= 0) {
				break;
			}
		}		
		/** end of optimization procedure **/
		count = 0;
		for (int i = 0; i < keys.length; i++) {
			Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
			double maxVal = -Double.MAX_VALUE;
			int maxIndex = -1;
			for (int j = 0; j < arr.length; j++) {
				if (u[count] > maxVal) {
					maxVal = u[count];
					maxIndex = j;
				}
				count++;
			}
			if (maxIndex != -1 && maxVal > 0) {
				res.put(keys[i], arr[maxIndex].getFirst()[0] + "_" + arr[maxIndex].getFirst()[1]);
			}  			
		}
		return res;
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}
}