package edu.cmu.cs.lti.ark.fn.clusters;

import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.parsing.GraphSpans;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import gnu.trove.THashSet;

public class GraphFilterCoverage {
	
	public static final String GRAPH_DIR = 
		"/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012/smoothed";
	public static final String DATA_DIR = 
		"/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";
	public static final String INFIX = "train";
	
	public static void main(String[] args) {
		String graphFile = GRAPH_DIR + "/lp.mu.0.01.nu.0.000001.10.graph.spans.jobj";
		System.out.println("Reading graph spans file from: " + graphFile);
		GraphSpans gs = (GraphSpans) SerializedObjects.readSerializedObject(graphFile);		
		modifyHeadDist(gs);
		System.out.println("Finished normalizing potentials");
		checkCoverage(gs);
	}
	
	public static void checkCoverage(GraphSpans gs) {
		String parseFile = DATA_DIR + "/cv."+INFIX+".sentences.all.lemma.tags";
		String feFile = DATA_DIR + "/cv."+INFIX+".sentences.frame.elements";
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
		double total = 0.0;
		double match = 0.0;
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
					if (i == j) {
						continue;
					}
					if (spanMat[i][j]) {
						String span = i + "_" + j;
						autoSpans.add(span);
					}
				}
			}
			for(int k = 6; k < toks.length; k = k + 2) {
				String fe = toks[k];
				String[] spans = toks[k+1].split(":");
				String span;
				if (spans.length == 1) {
					span = spans[0]+"_"+spans[0];
				} else {
					span = spans[0]+"_"+spans[1];
				}
				total++;
				if (autoSpans.contains(span)) {
					match++;
				}
			}
		}
		System.out.println("Recall: " + (match / total));
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
	
	
}
