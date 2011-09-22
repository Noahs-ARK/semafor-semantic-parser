package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.parsing.CoarseDistributions;
import edu.cmu.cs.lti.ark.fn.parsing.GraphSpans;
import edu.cmu.cs.lti.ark.fn.parsing.ScanPotentialSpans;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import gnu.trove.THashSet;

public class GraphFilterCoverage {
	
	public static final String GRAPH_DIR = 
		"/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012/smoothed";
	public static final String DATA_DIR = 
		"/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";
	public static final String INFIX = "train";
	
	public static void main(String[] args) {
		heads();
	}
	
	public static void graph() {
		String graphFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.graph.spans.jobj";
		System.out.println("Reading graph spans file from: " + graphFile);
		GraphSpans gs = (GraphSpans) SerializedObjects.readSerializedObject(graphFile);		
		modifyHeadDist(gs);
		System.out.println("Finished normalizing potentials");
//		for (int K = 200; K <= 1000; K = K + 100)  {
//			int[][] topKFEs = getTopKFEs(gs, K);
//			String sertopKFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.top.k."+K+".jobj";
//			SerializedObjects.writeSerializedObject(topKFEs, sertopKFile);
//			System.out.println("Finished with K: " + sertopKFile);
//		}
		for (int K = 200; K <= 700; K = K + 100) {
			String sertopKFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.top.k."+K+".jobj";
			int[][] topKFEs = (int[][]) SerializedObjects.readSerializedObject(sertopKFile);
			System.out.println("Got top K FEs for K="+K);
			checkCoverage(gs, topKFEs);
			System.out.println("\n\n");
		}
	}
	
	public static void heads() {
		String headsFile = DATA_DIR + "/all.spans.heads";
		String[] mSortedUniqueHeads = CoarseDistributions.getSortedUniqueHeads(headsFile);
		String headsSerFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.heads.jobj";
		System.out.println("Reading distributions over heads " + headsSerFile);
		float[][] mHeadDist = (float[][]) SerializedObjects.readSerializedObject(headsSerFile);
		modifyHeadDist(mHeadDist);
		System.out.println("Reading sorted FEs");
		String feFile = DATA_DIR + "/fes.sorted";
		String[] mSortedFEs = readFEFile(feFile);
		for (int K = 100; K <= 700; K++) {
			int[][] topKFEs = getTopKFEsHeads(mHeadDist, K);
			String sertopKFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.top.k."+K+".heads.jobj";
			SerializedObjects.writeSerializedObject(topKFEs, sertopKFile);
			System.out.println("Finished with K: " + sertopKFile);
		}
	}
	
	public static String[] readFEFile(String feFile) {
		int count = 0;
		System.out.println("Reading fe file...");
		String line = null;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(feFile));
			while ((line = bReader.readLine()) != null) {
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			System.out.println("Could not read file: " + feFile);
			System.exit(-1);
		}
		System.out.println("Total number of fes: " + count);
		String[] mSortedFEs = new String[count];
		count = 0;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(feFile));
			while ((line = bReader.readLine()) != null) {
				line = line.trim();
				mSortedFEs[count] = line;
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			System.out.println("Could not read file: " + feFile);
			System.exit(-1);
		}
		System.out.println("Stored FEs.");
		return mSortedFEs;
	}
	
	public static int[][] getTopKFEs(GraphSpans gs, int K) {
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
		int[][] arr = new int[gs.sortedSpans.length][];
		for (int i = 0; i < gs.sortedSpans.length; i++) {
			arr[i] = new int[K];
			Pair<Integer, Double>[] parr = new Pair[gs.smoothedGraph[i].length];
			for (int j = 0; j < gs.smoothedGraph[i].length; j++) {
				parr[j] = new Pair<Integer, Double>(j, (double)gs.smoothedGraph[i][j]);
			}
			Arrays.sort(parr, comp);
			for (int j = 0; j < K; j++) {
				arr[i][j] = parr[j].getFirst();
			}
			Arrays.sort(arr[i]);
			if (i % 10000 == 0) {
				System.out.print(i + " ");
			}
		}
		System.out.println();
		return arr;
	}
	
	public static int[][] getTopKFEsHeads(float[][] headDist, int K) {
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
		int[][] arr = new int[headDist.length][];
		for (int i = 0; i < headDist.length; i++) {
			arr[i] = new int[K];
			Pair<Integer, Double>[] parr = new Pair[headDist.length];
			for (int j = 0; j < headDist[i].length; j++) {
				parr[j] = new Pair<Integer, Double>(j, (double)headDist[i][j]);
			}
			Arrays.sort(parr, comp);
			for (int j = 0; j < K; j++) {
				arr[i][j] = parr[j].getFirst();
			}
			Arrays.sort(arr[i]);
			if (i % 10000 == 0) {
				System.out.print(i + " ");
			}
		}
		System.out.println();
		return arr;
	}
	
	public static void checkCoverage(GraphSpans gs, int[][] topKFEs) {
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
			THashSet<String> autoSpans = new THashSet<String>();
			for (int i = 0; i < sortedNodes.length; i++) {
				for (int j = 0; j < sortedNodes.length; j++) {
					if (spanMat[i][j]) {
						String span = i + "_" + j;
						autoSpans.add(span);
					}
				}
			}
			for(int k = 6; k < toks.length; k = k + 2) {
				String fe = toks[k];
				Set<String> filteredSpans = filterSpans(fe, autoSpans, sortedNodes, gs, topKFEs);
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
	
	public static String getSpan(DependencyParse[] nodes, 
						  int istart, int iend) {
		String span = "";
		for (int i = istart; i <= iend; i++) {
			String tok = nodes[i+1].getWord();
			if (tok.equals("-LRB-")) { tok = "("; }
			if (tok.equals("-RRB-")) { tok = ")"; }
			if (tok.equals("-RSB-")) { tok = "]"; }
			if (tok.equals("-LSB-")) { tok = "["; }
			if (tok.equals("-LCB-")) { tok = "{"; }
			if (tok.equals("-RCB-")) { tok = "}"; }
			span += tok + " ";
		}
		span = span.trim().toLowerCase();
		span = ScanPotentialSpans.replaceNumbersWithAt(span.trim());
		return span;
	}
	
	public static Set<String> filterSpans(String fe, 
										  THashSet<String> autoSpans,
										  DependencyParse[] sortedNodes,
										  GraphSpans gs,
										  int[][] topKFEs) {
		Set<String> filteredSpans = new THashSet<String>();
		
		for (String span: autoSpans) {
			String[] toks = span.split("_");
			int start = new Integer(toks[0]);
			int end = new Integer(toks[1]);
			String phrase = getSpan(sortedNodes, start, end);
			int foundIndex = Arrays.binarySearch(gs.sortedSpans, phrase);
			if (foundIndex < 0) {
				filteredSpans.add(span);
			} else {
				int[] topK = topKFEs[foundIndex];
				int feIndex = Arrays.binarySearch(gs.sortedFEs, fe);
				if (feIndex < 0) {
					System.out.println("Problem. feIndex for fe:" + fe + " is negative. Exiting.");
					System.exit(-1);
				}
				if (Arrays.binarySearch(topK, feIndex) >= 0) {
					filteredSpans.add(span);
				}
			}
		}
		return filteredSpans;
	}
										  
	public static void modifyHeadDist(GraphSpans gs) {
		int len = gs.smoothedGraph.length;
		int flen = gs.smoothedGraph[0].length;
		double avg = 0.0;
		for (int i = 0; i < len; i++) {
			double min = Double.MAX_VALUE;
			double max = - Double.MAX_VALUE;
			for (int j = 0; j < flen; j++) {
				if (gs.smoothedGraph[i][j] > max) {
					max = gs.smoothedGraph[i][j];
				}
				if (gs.smoothedGraph[i][j] < min) {
					min = gs.smoothedGraph[i][j];
				}
			}
			for (int j = 0; j < flen; j++) {
				if (max != min) {
					gs.smoothedGraph[i][j] = gs.smoothedGraph[i][j] / (float)(max - min);
				}
				avg += gs.smoothedGraph[i][j] / (float) flen;
			}
		}
		avg = avg / (double) len;
		System.out.println("Average component weight: " + avg);
	}
	
	public static void modifyHeadDist(float[][] mHeadDist) {
		int len = mHeadDist.length;
		double avg = 0.0;
		for (int i = 0; i < len; i++) {
			double min = Double.MAX_VALUE;
			double max = - Double.MAX_VALUE;
			boolean flag1 = false;
			boolean flag2 = false;
			for (int j = 0; j < mHeadDist[i].length; j++) {
				if (mHeadDist[i][j] > max) {
					max = mHeadDist[i][j];
					flag1 = true;
				}
				if (mHeadDist[i][j] < min) {
					min = mHeadDist[i][j];
					flag2 = true;
				}
			}
			if (!flag1 && !flag2) {
				System.out.println("Problem with distribution.");
				for (int j = 0; j < mHeadDist[i].length; j++) {
					System.out.println(mHeadDist[i][j]);
				}
				System.exit(-1);
			}
			for (int j = 0; j < mHeadDist[i].length; j++) {
				if (max != min) {
					 mHeadDist[i][j] = mHeadDist[i][j] / (float)(max - min);
				}
				avg += mHeadDist[i][j] / (float) mHeadDist[i].length;
			}
		}
		avg = avg / (double) len;
		System.out.println("Average component weight: " + avg);
	}
}
