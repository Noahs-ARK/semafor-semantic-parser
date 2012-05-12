package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class JointDecodingUsingLPFiles implements JDecoding {
	private Map<String, Set<Pair<String, String>>> excludesMap;
	private Map<String, Set<Pair<String, String>>> requiresMap;
	public static final double TAU = 1.5;
	public static final double RHO_START = 0.03;
	private int mNumThreads = 1;
	private double[][] zs;
	private String mFactorFile;
	private static final boolean WRITE_FACTORS_TO_FILE = false;
	private BufferedReader bReader = null;
	private String decoderFlag = null; 
	private static final String admm = "admm";
	private static final String admmilp = "admmilp";
	private static final String cplexlp = "lp";
	private static final String cplexilp = "ilp";
	
	public JointDecodingUsingLPFiles() {
	}
	
	public void setMaps(Map<String, Set<Pair<String, String>>> excludesMap, 
						Map<String, Set<Pair<String, String>>> requiresMap) {
		this.excludesMap = excludesMap;
		this.requiresMap = requiresMap;
	}
	
	public void setFlag(String flag) {
		decoderFlag = flag;
	}
	 
	public Map<String, Pair<String, Double>> decode(Map<String, Pair<int[], Double>[]> scoreMap, 
									  String frame,
									  boolean costAugmented,
									  FrameFeatures goldFF) {
		Map<String, Pair<String, Double>> res = new THashMap<String, Pair<String, Double>>();
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
		if (requiresMap.containsKey(frame)) {
			Set<Pair<String, String>> set = requiresMap.get(frame);
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
				System.out.println("Found two FEs with a requires relationship: " + one + "\t" + two);
				
				int nullIndex1 = -1;
				int nullIndex2 = -1;
				count = 0;
				TIntHashSet rSet1 = new TIntHashSet();
				TIntHashSet rSet2 = new TIntHashSet();
				Pair<int[], Double>[] arr1 = scoreMap.get(one);
				Pair<int[], Double>[] arr2 = scoreMap.get(two);
				for (int j = 0; j < scoreMap.get(one).length; j++) {
					if (arr1[j].getFirst()[0] == -1 && arr1[j].getFirst()[1] == -1) {
						nullIndex1 = mappedIndices[oneIndex][j];
						continue;
					}
					rSet1.add(mappedIndices[oneIndex][j]);
					count++;
				}
				rSet1.add(totalCount);				
				count = 0;
				for (int j = 0; j < scoreMap.get(two).length; j++) {
					if (arr2[j].getFirst()[0] == -1 && arr2[j].getFirst()[1] == -1) {
						nullIndex2 = mappedIndices[twoIndex][j];
						continue;
					}
					rSet2.add(mappedIndices[twoIndex][j]);
					count++;
				}
				rSet2.add(totalCount);
				
				int[] a1 = new int[2];
				a1[0] = nullIndex1;
				a1[1] = nullIndex2;
				requiredSets.add(a1);				
			}
		}
		
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
		
		if (bReader == null) {
			try {
				bReader = new BufferedReader(new FileReader(mFactorFile));
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		double[] u = new double[objVals.length];
		for (int i = 0; i < objVals.length; i++) {
			String line = null;
			try {
				line = bReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			String[] toks = line.trim().split("\\s");
			if (decoderFlag.equals(admm)) {
				u[i] = new Double(toks[0]);
			} else if (decoderFlag.equals(admmilp)) {
				u[i] = new Double(toks[1]);
			} else if (decoderFlag.equals(cplexlp)) {
				u[i] = new Double(toks[2]);	
			} else {
				u[i] = new Double(toks[3]);
			}
		}		
		
		try {
			String line = bReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/** end of optimization procedure **/
		count = 0;
		double totalScore = 0.0;
		for (int i = 0; i < keys.length; i++) {
			Pair<int[], Double>[] arr = scoreMap.get(keys[i]);
			double maxVal = -Double.MAX_VALUE;
			int maxIndex = -1;
			// System.out.println(keys[i]);
			double score = 0.0;
			for (int j = 0; j < arr.length; j++) {
				// String span = arr[j].getFirst()[0] + "_" + arr[j].getFirst()[1];
				// System.out.println(span + " " + u[count]);
				if (u[count] > maxVal) {
					maxVal = u[count];
					maxIndex = j;
					score = objVals[count];
				}
				count++;
			}
			// System.out.println();
			if (maxIndex != -1 && maxVal > 0) {
				totalScore += score;
				Pair<String, Double> p = 
					new Pair<String, Double>(arr[maxIndex].getFirst()[0] + "_" + arr[maxIndex].getFirst()[1], score);
				res.put(keys[i], p);
			}  			
		}
		System.out.println("Solution value: " + totalScore);
		return res;
	}

	public Runnable createTask(final double rho, 
									  final double[] us, 
									  final double[][] lambdas, 
									  final double[][] z,
									  final Slave[] slave,
									  final int i,
									  final int slavelen,
									  final Integer[] sarray)                                                                                                     
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
					zs[sarray[s]] = slave[sarray[s]].makeZUpdate(rho, us, lambdas[sarray[s]], z[sarray[s]]);
				}
				// System.out.println("Task " + s + " : end");                                                                                                             
			}
		};
	}

	
	@Override
	public void end() {
		// TODO Auto-generated method stub
		if (bReader != null) {
			try { 
				bReader.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Could not close file.");
				System.exit(-1);
			}
		}
	}

	@Override
	public void setNumThreads(int nt) {
		// TODO Auto-generated method stub
		mNumThreads = nt;
	}

	@Override
	public void setFactorFile(String factorFile) {
		// TODO Auto-generated method stub
		mFactorFile = factorFile;
	}
}