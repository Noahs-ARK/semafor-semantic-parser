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

public class DDDecoding {
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
		double[] costs = new double[totalCount];
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
		double[] objVals = new double[totalCount];
		System.out.println("Max index:" + max);
		TIntHashSet[] overlapArray = new TIntHashSet[max+1];
		for (int i = 0; i < max+1; i++) {
			overlapArray[i] = new TIntHashSet();
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
		return res;
	}
}