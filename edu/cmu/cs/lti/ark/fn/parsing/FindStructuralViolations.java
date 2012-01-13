package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.ds.Pair;

public class FindStructuralViolations {
	public static void main(String[] args) {
		String inputFile = args[0];
		String requiresMapFile = args[1];
		String excludesMapFile = args[2];
		Map<String, Set<Pair<String, String>>> exclusionMap = 
			(Map<String, Set<Pair<String, String>>>) SerializedObjects.readSerializedObject(excludesMapFile);
		Map<String, Set<Pair<String, String>>> requiresMap = 
			(Map<String, Set<Pair<String, String>>>) SerializedObjects.readSerializedObject(requiresMapFile);
		
		ArrayList<String> lines = ParsePreparation.readSentencesFromFile(inputFile);
		int overlapViolations = 0;
		int excludesViolations = 0;
		int requiresViolations = 0;
		for (String line: lines) {
			String[] toks = line.trim().split("\t");
			String frame = toks[5];
			ArrayList<String> fes = new ArrayList<String>();
			ArrayList<int[]> spans = new ArrayList<int[]>();
			for (int i = 7; i < toks.length; i = i + 2) {
				fes.add(toks[i]);
				String[] toks1 = toks[i+1].split(":");
				int[] span = new int[2];
				if (toks1.length == 1) {
					span[0] = new Integer(toks1[0]);
					span[1] = new Integer(toks1[0]);
				} else {
					span[0] = new Integer(toks1[0]);
					span[1] = new Integer(toks1[1]);
				}
			}
			// overlap
			int totalfes = fes.size();
			if (totalfes > 1) {
				for (int i = 1; i < totalfes; i++) {
					for (int j = 0; j < i; j++) {
						int[] span1 = spans.get(i);
						int[] span2 = spans.get(j);
						if (checkOverlap(span1, span2)) {
							overlapViolations++;
						}					
					}
				}
			}
			// excludes
			if (exclusionMap.containsKey(frame)) {
				Set<Pair<String, String>> set = exclusionMap.get(frame);
				for (Pair<String, String> p: set) {
					String one = p.getFirst();
					String two = p.getSecond();
					if (fes.contains(one) && fes.contains(two)) {
						excludesViolations++;
					}
				}
			}
			
			// requires
			if (requiresMap.containsKey(frame)) {
				Set<Pair<String, String>> set = requiresMap.get(frame);
				for (Pair<String, String> p: set) {
					String one = p.getFirst();
					String two = p.getSecond();
					if (fes.contains(one) && !fes.contains(two)) {
						requiresViolations++;
					} else if (fes.contains(two) && !fes.contains(one)) {
						requiresViolations++;
					}					
				}
			}
		}
		System.out.println("Total number of violations: " + (requiresViolations + overlapViolations + excludesViolations));
		System.out.println("Excludes violations: " + excludesViolations);
		System.out.println("Requires violations: " + requiresViolations);
		System.out.println("Overlap violations: " + overlapViolations);
	}
	
	public static boolean checkOverlap(int[] one, int[] two) {
		int oneStart = one[0];
		int oneEnd = one[1];
		
		int twoStart = two[0];
		int twoEnd = two[1];
		
		if(oneStart<twoStart)
		{
			if(oneEnd<twoStart)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		else
		{
			if(twoEnd<oneStart)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}
	
	
}