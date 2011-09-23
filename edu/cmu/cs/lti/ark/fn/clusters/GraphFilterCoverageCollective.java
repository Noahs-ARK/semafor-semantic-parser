package edu.cmu.cs.lti.ark.fn.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.parsing.CoarseDistributions;
import edu.cmu.cs.lti.ark.fn.parsing.GraphSpans;
import edu.cmu.cs.lti.ark.fn.parsing.LabeledHeadWordsWithSemTypes;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class GraphFilterCoverageCollective extends GraphFilterCoverage {
	public static final String GRAPH_DIR = 
		"/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012/smoothed";
	public static final String DATA_DIR = 
		"/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";
	public static final String INFIX = "train";
		
	public static void main (String[] args) {
		// headsCollective();
		semTypesCollective();
	}
	
	public static void semTypesCollective() {
		String semTypesMapFile = DATA_DIR + "/headsToSemTypes.map";
		THashMap<String, THashSet<String>> headsToSemTypesMap = 
				(THashMap<String, THashSet<String>>) SerializedObjects.readSerializedObject(semTypesMapFile);
		String feToSemTypesMapFile = DATA_DIR + "/fe2semTypes.map";
		THashMap<String, THashSet<String>> fe2semTypesMap = 
				(THashMap<String, THashSet<String>>) SerializedObjects.readSerializedObject(feToSemTypesMapFile);
		checkCoverageWithSemTypesCollective(headsToSemTypesMap, fe2semTypesMap);
	}
	
	public static void headsCollective() {
		String graphFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.graph.spans.jobj";
		System.out.println("Reading graph spans file from: " + graphFile);
		GraphSpans gs = (GraphSpans) SerializedObjects.readSerializedObject(graphFile);		
		String headsFile = DATA_DIR + "/all.spans.heads";
		String[] mSortedUniqueHeads = CoarseDistributions.getSortedUniqueHeads(headsFile);
		String headsSerFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.heads.jobj";
		System.out.println("Reading distributions over heads " + headsSerFile);
		float[][] mHeadDist = (float[][]) SerializedObjects.readSerializedObject(headsSerFile);
		modifyHeadDist(mHeadDist);
		System.out.println("Reading sorted FEs");
		String feFile = DATA_DIR + "/fes.sorted";
		String[] mSortedFEs = readFEFile(feFile);
		for (int i = 1; i <= 6; i++) {
			System.out.println("K = " + i);
			checkCoverageWithHeadsCollective(gs, mSortedUniqueHeads, mSortedFEs, mHeadDist, i);
			System.out.println("\n\n");
		}
	}
	
	public static void checkCoverageWithSemTypesCollective(
			THashMap<String, THashSet<String>> headsToSemTypesMap,
			THashMap<String, THashSet<String>> fe2SemTypesMap) {
		String parseFile = DATA_DIR + "/cv."+INFIX+".sentences.all.lemma.tags";
		String feFile = DATA_DIR + "/cv."+INFIX+".sentences.frame.elements";
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
		double total = 0.0;
		double match = 0.0;
		double totalNaiveSpans = 0.0;
		double totalFilteredSpans = 0.0;
		for(String feLine:feLines)
		{
			String[] toks = feLine.trim().split("\t");
			int sentNum = new Integer(toks[5]);
			StringTokenizer st = new StringTokenizer(parses.get(sentNum),"\t");
			int tokensInFirstSent = new Integer(st.nextToken());
			String[][] data = new String[6][tokensInFirstSent];
			for(int k = 0; k < 6; k ++)
			{
				data[k]=new String[tokensInFirstSent];
				for(int j = 0; j < tokensInFirstSent; j ++)
				{
					data[k][j]=""+st.nextToken().trim();
				}
			}	
			DependencyParse parseS = DependencyParse.processFN(data, 0.0);
			DependencyParse[] sortedNodes = DependencyParse.getIndexSortedListOfNodes(parseS);
			boolean[][] spanMat = new boolean[sortedNodes.length][sortedNodes.length];
			int[][] heads = new int[sortedNodes.length][sortedNodes.length];
			ScrapTest.findSpans(spanMat, heads, sortedNodes);
			Set<String> fes = new THashSet<String>();
			for(int k = 6; k < toks.length; k = k + 2) {
				String fe = toks[k];
				fes.add(fe);
			}
			String[] fesArray = new String[fes.size()];
			fes.toArray(fesArray);
			Arrays.sort(fesArray);
			THashSet<String> autoSpans = new THashSet<String>();
			for (int i = 0; i < sortedNodes.length; i++) {
				for (int j = 0; j < sortedNodes.length; j++) {
					if (spanMat[i][j]) {
						String span = i + "_" + j;
						autoSpans.add(span);
					}
				}
			}
			Set<String>[] filteredSpanArray = new Set[fes.size()];
			for (int i = 0; i < fes.size(); i++) {
				filteredSpanArray[i] = new THashSet<String>();
			}
			for (String autoSpan: autoSpans) {
				String[] toks1 = autoSpan.split("_");
				int start = new Integer(toks1[0]);
				int end = new Integer(toks1[1]);
				String head = LabeledHeadWordsWithSemTypes.getHeadWithPOS(sortedNodes, data[5], start, end);
				if (!headsToSemTypesMap.containsKey(head)) {
					for (int i = 0; i < fes.size(); i++) {
						filteredSpanArray[i].add(autoSpan);
					}
				} else {
					Set<String> semTypes = headsToSemTypesMap.get(head);
					for (int i = 0; i < fes.size(); i++) {
						if (fe2SemTypesMap.contains(fesArray[i])) {
							Set<String> feSemTypes = fe2SemTypesMap.get(fesArray[i]);
							boolean flag = false;
							for (String stype: feSemTypes) {
								if (semTypes.contains(stype)) {
									flag = true;
									break;
								}
							}
							if (flag) {
								filteredSpanArray[i].add(autoSpan);
							}
						} else {
							filteredSpanArray[i].add(autoSpan);
						}
					}
				}
			}			
			for(int k = 6; k < toks.length; k = k + 2) {
				String fe = toks[k];
				int index = Arrays.binarySearch(fesArray, fe);
				Set<String> filteredSpans = filteredSpanArray[index];
				totalNaiveSpans += autoSpans.size();
				totalFilteredSpans += filteredSpans.size();
				String[] spans = toks[k+1].split(":");
				String span;
				if (spans.length == 1) {
					span = spans[0]+"_"+spans[0];
				} else {
					span = spans[0]+"_"+spans[1];
				}
				total++;
				if (filteredSpans.contains(span)) {
					match++;
				}
			}
		}
		System.out.println("Recall: " + (match / total));
		System.out.println("Total naive spans: " + totalNaiveSpans);
		System.out.println("Total filtered spans: " + totalFilteredSpans);
	}
	
	public static void checkCoverageWithHeadsCollective(
			GraphSpans gs,
			String[] sortedHeads, 
			String[] sortedFEs, 
			float[][] headDist,
			int K) {
		Comparator<Pair<Integer, Double>> comp = new Comparator<Pair<Integer, Double>>() {
			public int compare(Pair<Integer, Double> o1,
					Pair<Integer, Double> o2) {
				if (o1.getSecond() > o2.getSecond()) {
					return -1;
				} else if (o1.getSecond() == o2.getSecond()) {
					return 0;
				} else {
					return 1;
				}
			}
		};
		String parseFile = DATA_DIR + "/cv."+INFIX+".sentences.all.lemma.tags";
		String feFile = DATA_DIR + "/cv."+INFIX+".sentences.frame.elements";
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
		double total = 0.0;
		double match = 0.0;
		double totalNaiveSpans = 0.0;
		double totalFilteredSpans = 0.0;
		for(String feLine:feLines)
		{
			String[] toks = feLine.trim().split("\t");
			int sentNum = new Integer(toks[5]);
			StringTokenizer st = new StringTokenizer(parses.get(sentNum),"\t");
			int tokensInFirstSent = new Integer(st.nextToken());
			String[][] data = new String[5][tokensInFirstSent];
			for(int k = 0; k < 5; k ++)
			{
				data[k]=new String[tokensInFirstSent];
				for(int j = 0; j < tokensInFirstSent; j ++)
				{
					data[k][j]=""+st.nextToken().trim();
				}
			}	
			DependencyParse parseS = DependencyParse.processFN(data, 0.0);
			DependencyParse[] sortedNodes = DependencyParse.getIndexSortedListOfNodes(parseS);
			boolean[][] spanMat = new boolean[sortedNodes.length][sortedNodes.length];
			int[][] heads = new int[sortedNodes.length][sortedNodes.length];
			ScrapTest.findSpans(spanMat, heads, sortedNodes);
			Set<String> fes = new THashSet<String>();
			for(int k = 6; k < toks.length; k = k + 2) {
				String fe = toks[k];
				fes.add(fe);
			}
			String[] fesArray = new String[fes.size()];
			fes.toArray(fesArray);
			Arrays.sort(fesArray);
			THashSet<String> autoSpans = new THashSet<String>();
			for (int i = 0; i < sortedNodes.length; i++) {
				for (int j = 0; j < sortedNodes.length; j++) {
					if (spanMat[i][j]) {
						String span = i + "_" + j;
						autoSpans.add(span);
					}
				}
			}
			Set<String>[] filteredSpanArray = new Set[fes.size()];
			for (int i = 0; i < fes.size(); i++) {
				filteredSpanArray[i] = new THashSet<String>();
			}
			for (String autoSpan: autoSpans) {
				String[] toks1 = autoSpan.split("_");
				int start = new Integer(toks1[0]);
				int end = new Integer(toks1[1]);
				String phrase = getSpan(sortedNodes, start, end);
				String head = getHead(sortedNodes, start, end);
				int phraseIndex = Arrays.binarySearch(gs.sortedSpans, phrase);
				int headIndex = Arrays.binarySearch(sortedHeads, head);
				if (phraseIndex > 0) {
					Pair<Integer, Double>[] parr = new Pair[fes.size()];
					for (int i = 0; i < fes.size(); i++) {
						int feIndex = Arrays.binarySearch(sortedFEs, fesArray[i]);
						double val = gs.smoothedGraph[phraseIndex][feIndex];
						parr[i] = new Pair<Integer, Double>(i, val);
					}
					Arrays.sort(parr, comp);
					int endIndex = K;
					if (parr.length < K) {
						endIndex = parr.length;
					}
					for (int i = 0; i < endIndex; i++) {
						filteredSpanArray[parr[i].getFirst()].add(autoSpan);
					}
				} else if (headIndex < 0) {
					for (int i = 0; i < fes.size(); i++) {
						filteredSpanArray[i].add(autoSpan);
					}
				} else {
					Pair<Integer, Double>[] parr = new Pair[fes.size()];
					for (int i = 0; i < fes.size(); i++) {
						int feIndex = Arrays.binarySearch(sortedFEs, fesArray[i]);
						double val = headDist[headIndex][feIndex];
						parr[i] = new Pair<Integer, Double>(i, val);
					}
					Arrays.sort(parr, comp);
					int endIndex = K;
					if (parr.length < K) {
						endIndex = parr.length;
					}
					for (int i = 0; i < endIndex; i++) {
						filteredSpanArray[parr[i].getFirst()].add(autoSpan);
					}
				}
			}			
			for(int k = 6; k < toks.length; k = k + 2) {
				String fe = toks[k];
				int index = Arrays.binarySearch(fesArray, fe);
				Set<String> filteredSpans = filteredSpanArray[index];
				totalNaiveSpans += autoSpans.size();
				totalFilteredSpans += filteredSpans.size();
				String[] spans = toks[k+1].split(":");
				String span;
				if (spans.length == 1) {
					span = spans[0]+"_"+spans[0];
				} else {
					span = spans[0]+"_"+spans[1];
				}
				total++;
				if (filteredSpans.contains(span)) {
					match++;
				}
			}
		}
		System.out.println("Recall: " + (match / total));
		System.out.println("Total naive spans: " + totalNaiveSpans);
		System.out.println("Total filtered spans: " + totalFilteredSpans);
	}
}