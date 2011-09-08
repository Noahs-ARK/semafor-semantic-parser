package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.util.SerializedObjects;

import gnu.trove.THashSet;

public class CoarseDistributions {
	public static void main(String[] args) {
		String graphSpansFile = args[0];
		String headFile = args[1];
		String smoothedHeadsFile = args[2];
		String[] corrHeads = getCorrespondingHeads(headFile);
		String[] sortedUniqueHeads = getSortedUniqueHeads(headFile);
		System.out.println("Total number of unique heads: " + sortedUniqueHeads.length);
		GraphSpans gs = (GraphSpans) SerializedObjects.readSerializedObject(graphSpansFile);
		findHeadDistributions(corrHeads, sortedUniqueHeads, gs, smoothedHeadsFile);
	}
	
	public static void test() {
		String datadir = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";
		String spansFile = 
			datadir + "/all.spans.sorted";
		String feFile = 
			datadir + "/fes.sorted";
		String smoothedFile =
			datadir + "/smoothed/lp.mu.0.5.nu.0.1.10";
		String headFile = 
			datadir + "/all.spans.heads";
		String smoothedHeadsFile = 
			datadir + "/smoothed/lp.mu.0.5.nu.0.1.10.heads.jobj";
		
		String[] corrHeads = getCorrespondingHeads(headFile);
		String[] sortedUniqueHeads = getSortedUniqueHeads(headFile);
		System.out.println("Total number of unique heads: " + sortedUniqueHeads.length);
		GraphSpans gs = new GraphSpans(spansFile, feFile, smoothedFile);
		findHeadDistributions(corrHeads, sortedUniqueHeads, gs, smoothedHeadsFile);
	}
	
	public static void findHeadDistributions(String[] corrHeads, 
											 String[] sortedUniqueHeads,
											 GraphSpans gs,
											 String smoothedHeadsFile) {
		float[][] graph = gs.smoothedGraph;
		String[] sortedSpans = gs.sortedSpans;
		String[] sortedFEs = gs.sortedFEs;
		int numUniqueHeads = sortedUniqueHeads.length;
		int numFEs = sortedFEs.length;
		float[][] headDist = new float[numUniqueHeads][];
		for (int i = 0; i < numUniqueHeads; i++) {
			headDist[i] = null;
		}
		System.out.println("COarsening smoothed distributions...");
		for (int i = 0; i < sortedSpans.length; i++) {
			String corrHead = corrHeads[i];
			int index = Arrays.binarySearch(sortedUniqueHeads, corrHead);
			if (headDist[index] == null) {
				headDist[index] = new float[numFEs];
				for (int j = 0; j < numFEs; j++) {
					headDist[index][j] = graph[i][j];
				}
			} else {
				for (int j = 0; j < numFEs; j++) {
					headDist[index][j] += graph[i][j];
				}
			}
			if (i % 100000 == 0) {
				System.out.println(i);
			}
		}
		System.out.println("Normalizing head distributions...");
		for (int i = 0; i < numUniqueHeads; i++) {
			float sum = 0;
			for (int j = 0; j < numFEs; j++) {
				sum += headDist[i][j];
			}
			for (int j = 0; j < numFEs; j++) {
				headDist[i][j] /= sum;
			}
			if (sortedUniqueHeads[i].contains("lfm")) {
				System.out.println("Found: " + sortedUniqueHeads[i]);
				for (int j = 0; j < numFEs; j++) {
					System.out.println(headDist[i][j]);
				}
				System.out.println(sum);
			}
		}
		SerializedObjects.writeSerializedObject(headDist, smoothedHeadsFile);
		System.out.println("Written head distributions...");
	}
	
	public static String[] getCorrespondingHeads(String headFile) {
		ArrayList<String> headList = ParsePreparation.readSentencesFromFile(headFile);
		String[] arr = new String[headList.size()];
		headList.toArray(arr);
		return arr;
	}
	
	public static String[] getSortedUniqueHeads(String headFile) {
		Set<String> headSet = new THashSet<String>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(headFile));
			String line = null;
			while ((line = bReader.readLine()) != null) {
				line = line.trim();
				headSet.add(line);
			}
			bReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not read head file");
		}
		String[] arr = new String[headSet.size()];
		headSet.toArray(arr);
		Arrays.sort(arr);
		return arr;
	}
}