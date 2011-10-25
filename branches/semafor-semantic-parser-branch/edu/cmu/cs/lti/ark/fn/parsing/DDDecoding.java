package edu.cmu.cs.lti.ark.fn.parsing;

import ilog.concert.IloNumExpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.ark.fn.utils.ThreadPool;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;

public class DDDecoding implements JDecoding {
	private Map<String, Set<Pair<String, String>>> excludesMap;
	private Map<String, Set<Pair<String, String>>> requiresMap;
	public static final double TAU = 1.5;
	public static final double RHO_START = 0.03;
	private int mNumThreads = 1;
	private double[][] zs;
	
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
		for (int i = 0; i < max+1; i++) {
			overlapArray[i] = new TIntHashSet();
		}
		
		// counting number of required slaves
		// doing this to add slack variables for required slaves
		ArrayList<int[]> requiredSets = new ArrayList<int[]>();
//		if (requiresMap.containsKey(frame)) {
//			Set<Pair<String, String>> set = requiresMap.get(frame);
//			for (Pair<String, String> p: set) {
//				String one = p.getFirst();
//				String two = p.getSecond();
//				int oneIndex = Arrays.binarySearch(keys, one);
//				if (oneIndex < 0) {
//					continue;
//				}
//				int twoIndex = Arrays.binarySearch(keys, two);
//				if (twoIndex < 0) {
//					continue;
//				}
//				System.out.println("Found two FEs with a requires relationship: " + one + "\t" + two);
//				
//				int nullIndex1 = -1;
//				int nullIndex2 = -1;
//				count = 0;
//				TIntHashSet rSet1 = new TIntHashSet();
//				TIntHashSet rSet2 = new TIntHashSet();
//				Pair<int[], Double>[] arr1 = scoreMap.get(one);
//				Pair<int[], Double>[] arr2 = scoreMap.get(two);
//				for (int j = 0; j < scoreMap.get(one).length; j++) {
//					if (arr1[j].getFirst()[0] == -1 && arr1[j].getFirst()[1] == -1) {
//						nullIndex1 = mappedIndices[oneIndex][j];
//						continue;
//					}
//					rSet1.add(mappedIndices[oneIndex][j]);
//					count++;
//				}
//				rSet1.add(totalCount);
//				requiredSets.add(rSet1.toArray());				
//				count = 0;
//				for (int j = 0; j < scoreMap.get(two).length; j++) {
//					if (arr2[j].getFirst()[0] == -1 && arr2[j].getFirst()[1] == -1) {
//						nullIndex2 = mappedIndices[twoIndex][j];
//						continue;
//					}
//					rSet2.add(mappedIndices[twoIndex][j]);
//					count++;
//				}
//				rSet2.add(totalCount);
//				totalCount++;				
//				requiredSets.add(rSet2.toArray());
//				
//				int[] a1 = new int[2];
//				a1[0] = nullIndex1;
//				a1[1] = totalCount;
//				requiredSets.add(a1);
//				
//				
//				int[] a2 = new int[2];
//				a2[0] = nullIndex2;
//				a2[1] = totalCount;
//				totalCount++;
//				requiredSets.add(a2);
//			}
//		}
		
		double[] objVals = new double[totalCount];
		double[] costs = new double[totalCount];
		
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
		
		for (int i = count; i < objVals.length; i++) {
			objVals[i] = 0.0;
		}
		
		// counting number of exclusion slaves
		ArrayList<int[]> exclusionSets = new ArrayList<int[]>();
		if (excludesMap.containsKey(frame)) {
			Set<Pair<String, String>> set = excludesMap.get(frame);	
			for (Pair<String, String> p: set) {
				String one = p.getFirst();
				String two = p.getSecond();
				int oneIndex = Arrays.binarySearch(keys, one);
				if (oneIndex < 0) {
					continue;
				}
				int twoIndex = Arrays.binarySearch(keys, two);
				if (twoIndex < 0) {
					continue;
				}
				System.out.println("Found two mutually exclusive FEs: " + one + "\t" + two);
				TIntHashSet eSet = new TIntHashSet();
				count = 0;
				Pair<int[], Double>[] arr1 = scoreMap.get(one);
				Pair<int[], Double>[] arr2 = scoreMap.get(two);
				for (int j = 0; j < scoreMap.get(one).length; j++) {
					if (arr1[j].getFirst()[0] == -1 && arr1[j].getFirst()[1] == -1) {
						continue;
					}
					eSet.add(mappedIndices[oneIndex][j]);
					count++;
				}
				for (int j = 0; j < scoreMap.get(two).length; j++) {
					if (arr2[j].getFirst()[0] == -1 && arr2[j].getFirst()[1] == -1) {
						continue;
					}
					eSet.add(mappedIndices[twoIndex][j]);
					count++;
				}
				int[] arr = eSet.toArray();
				exclusionSets.add(arr);
			}
		}	
		
		
		
		// finished adding costs
		int len = objVals.length;
		int[] deltaarray = new int[len];
		int numExclusionSlaves = exclusionSets.size();
		int numRequiredSlaves = requiredSets.size();
		int slavelen = keys.length + max + 1 + numExclusionSlaves + numRequiredSlaves;
		int[][] slaveparts = new int[slavelen][];
		int[][] partslaves = new int[len][];
		Arrays.fill(deltaarray, 0);
				
		// creating deltaarray
		// for every unique span slave
		for (int i = 0; i < keys.length; i++) {
			for (int j = 0; j < mappedIndices[i].length; j++) {
				deltaarray[mappedIndices[i][j]] += 1;
			}
		}		
		// for the overlap slaves
		for (int i = keys.length; i < keys.length + max + 1; i++) {
			int[] vars = overlapArray[i-keys.length].toArray();
			for (int v: vars) {
				deltaarray[v] += 1;
			}
		}
		// for the exclusion slaves
		for (int[] vars: exclusionSets) {
			for (int v: vars) {
				deltaarray[v] += 1;
			}
		}
		// for the required slaves
		for (int[] vars: requiredSets) {
			for (int v: vars) {
				deltaarray[v] += 1;
			}
		}		
		// end of creation of deltaarray
		
		
		double[] thetas = new double[objVals.length];
		for (int i = 0; i < len; i++) {
			thetas[i] = objVals[i] / (double)deltaarray[i];
		}
		
		// creating slaves
		Slave[] slaves = new Slave[slavelen];
		for (int i = 0; i < keys.length; i++) {
			slaves[i] = new UniqueSpanSlave(thetas, 
					   						mappedIndices[i][0], 
					   						mappedIndices[i][mappedIndices[i].length-1] + 1);
			slaveparts[i] = new int[mappedIndices[i].length];
			for (int j = 0; j < mappedIndices[i].length; j++) {
				slaveparts[i][j] = mappedIndices[i][j];
			}
		}
		
		for (int i = keys.length; i < keys.length + max + 1; i++) {
			int[] vars = overlapArray[i-keys.length].toArray();
			slaves[i] = new OverlapSlave(thetas, vars);
			slaveparts[i] = Arrays.copyOf(vars, vars.length);
		}
		
		for (int i = keys.length + max + 1; 
				 i < keys.length + max + 1 + numExclusionSlaves; 
				 i++) {
			int[] vars = exclusionSets.get(i - (keys.length + max + 1));
			slaves[i] = new ExclusionSlave(thetas, vars);
			slaveparts[i] = Arrays.copyOf(vars, vars.length);
		}		
		
		for (int i = keys.length + max + 1 + numExclusionSlaves;
			     i < keys.length + max + 1 + numExclusionSlaves + numRequiredSlaves;
			     i++) {
			int[] vars = requiredSets.get(i - (keys.length + max + 1 + numExclusionSlaves));
			slaves[i] = new RequiredSlave(thetas, vars);
			slaveparts[i] = Arrays.copyOf(vars, vars.length);
		}
		
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
		zs = new double[slavelen][len]; 
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
		
		
		List<Slave> slist = Arrays.asList(slaves);
		Collections.shuffle(slist);
		slist.toArray(slaves);
		
		while (true) {
			// System.out.println("Rho: " + rho);
			double eta = TAU * rho;
			// System.out.println("Eta: " + eta);
			// making z-update
			if (this.mNumThreads == 1) {
				for (int s = 0; s < slavelen; s++) {
					zs[s] = slaves[s].makeZUpdate(rho, u, lambdas[s], zs[s]);
				}
			} else {
				ThreadPool threadPool = new ThreadPool(mNumThreads);
				for (int i = 0; i < mNumThreads; i++) {
					threadPool.runTask(createTask(rho, 
							  					  u, 
							  					  lambdas, 
							  					  zs,
							  					  slaves,
							  					  i,
							  					  slavelen));
				}
				threadPool.join();
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
			
			//System.out.println(itr + ": Primal residual: " + pr);
			//System.out.println(itr + ": Dual residual: " + dr);
			if (pr > dr) {
				double rat;
				if (dr == 0.0) {
					rat = 20.0;
				} else {
					rat = pr / dr;
				}
				if (rat > 10.0) {
					rho = rho * 2.0;
				}
			} else {
				double rat = 0;
				if (pr == 0) {
					if (dr != 0) {
						rat = 20.0;
					}
				} else {
					rat = dr / pr;
				}
				if (rat > 10.0) {
					rho = rho / 2.0;
				}
			}			
			itr++;
			if (itr >= 100) {
				System.out.println("Optimization did not converge in 100 iterations: " + pr + " " + dr);
				break;
			}
			if (pr < 0.000001 && dr < 0.000001) {
				System.out.println("Optimization converged: " + pr + " " + dr);
				break;
			}
		}		
		/** end of optimization procedure **/
		count = 0;
		for (int i = 0; i < keys.length; i++) {
			Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
			double maxVal = -Double.MAX_VALUE;
			int maxIndex = -1;
			// System.out.println(keys[i]);
			for (int j = 0; j < arr.length; j++) {
				String span = arr[j].getFirst()[0] + "_" + arr[j].getFirst()[1];
				// System.out.println(span + " " + u[count]);
				if (u[count] > maxVal) {
					maxVal = u[count];
					maxIndex = j;
				}
				count++;
			}
			// System.out.println();
			if (maxIndex != -1 && maxVal > 0) {
				res.put(keys[i], arr[maxIndex].getFirst()[0] + "_" + arr[maxIndex].getFirst()[1]);
			}  			
		}
		return res;
	}

	public Runnable createTask(final double rho, 
									  final double[] us, 
									  final double[][] lambdas, 
									  final double[][] z,
									  final Slave[] slave,
									  final int i,
									  final int slavelen)                                                                                                     
	{                                                                                                                                                                           
		return new Runnable() {                                                                                                                                             
			public void run() {                                                                                                                                           
				// System.out.println("Task " + s + " : start");
				int batchSize = (int)(Math.ceil((double) slavelen / (double) mNumThreads));
				int start = i * batchSize;
				int end = start + batchSize;
				if (end > slavelen) {
					end = slavelen;
				}
				for (int s = start; s < end; s++) {
					zs[s] = slave[s].makeZUpdate(rho, us, lambdas[s], z[s]);
				}
				// System.out.println("Task " + s + " : end");                                                                                                             
			}
		};
	}

	
	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNumThreads(int nt) {
		// TODO Auto-generated method stub
		mNumThreads = nt;
	}
}