package edu.cmu.cs.lti.ark.fn.evaluation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.clusters.ScrapTest;
import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import gnu.trove.THashSet;

public class ComputeARGIDUpperBound {
	public static final String DATA_DIR = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";
	public static final String INFIX = "dev";
	
	public static void main(String[] args) throws IOException {
		String feFile = DATA_DIR + "/cv.dev.sentences.frame.elements";
		String parseFile = DATA_DIR + "/cv.dev.sentences.all.lemma.tags";
		
		String outFEFile = DATA_DIR + "/ub.dev.sentences.frame.elements";
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
		
		
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(outFEFile));
		for(String feLine:feLines) {
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
						if (i == j) {
							String span = "" + i;
							autoSpans.add(span);
						} else {
							String span = i + ":" + j;
							autoSpans.add(span);
						}
						
					}
				}
			}
			String out = "";
			for (int k = 1; k < 6; k++) {
				out += toks[k] + "\t";
			}
			int count = 1;
			for(int k = 6; k < toks.length; k = k + 2) {
				String FE = toks[k];
				if (autoSpans.contains(toks[k+1])) {
					out += FE + "\t" + toks[k+1] + "\t";
					count++;
				}
			}
			out = count + "\t" + out.trim();
			System.out.println(out);
			bWriter.write(out + "\n");
		}
		bWriter.close();
	}
}
