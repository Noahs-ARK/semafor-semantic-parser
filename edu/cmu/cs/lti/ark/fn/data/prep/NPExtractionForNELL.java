package edu.cmu.cs.lti.ark.fn.data.prep;

import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;


public class NPExtractionForNELL {
	public static void main(String[] args) {
		String parseFile = args[0];
		String feFile = args[1];
		String outFile = args[2];
		ArrayList<String> parses = 
			ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = 
			ParsePreparation.readSentencesFromFile(feFile);
		Set<String> relations = new THashSet<String>();
		for (String feLine: feLines) {
			feLine = feLine.trim();
			String[] toks = feLine.split("\t");
			String frame = toks[1];
			int sentNum = new Integer(toks[5]);
			String parseLine = parses.get(sentNum);
			StringTokenizer st = new StringTokenizer(parseLine,"\t");
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
			DependencyParse parse = DependencyParse.processFN(data, 0.0);
			DependencyParse[] sortedNodes = DependencyParse.getIndexSortedListOfNodes(parse);
			ArrayList<Integer> selected = new ArrayList<Integer>();
			ArrayList<int[]> tokArray = new ArrayList<int[]>();
			for (int i = 6; i < toks.length; i = i + 2) {
				String[] toks1 = toks[i+1].split(":");
				int[] span = new int[2];
				if (toks1.length == 1) {
					span[0] = new Integer(toks1[0]);
					span[1] = new Integer(toks1[0]);
				} else {
					span[0] = new Integer(toks1[0]);
					span[1] = new Integer(toks1[1]);
				}
				int[] mTokenNums = new int[span[1] - span[0] + 1];
				for (int j = 0; j < mTokenNums.length; j++) {
					mTokenNums[j] = span[0] + j;
				}
				DependencyParse node = DependencyParse.getHeuristicHead(sortedNodes, mTokenNums);
				String pos = node.getPOS();
				if (pos.startsWith("N")) {
					selected.add(i);
					tokArray.add(mTokenNums);
				}
			}
			for (int i = 1; i < selected.size(); i++) {
				for (int j = 0; j < i; j++) {
					String fe1 = toks[selected.get(i)];
					String fe2 = toks[selected.get(j)];
					int[] toks1 = tokArray.get(i);
					int[] toks2 = tokArray.get(j);
					String np1 = "";
					String np2 = "";
					for (int k = 0; k < toks1.length; k++) {
						np1 = np1 + data[0][toks1[k]] + " ";
					}
					np1 = np1.trim();
					for (int k = 0; k < toks2.length; k++) {
						np2 = np2 + data[0][toks2[k]] + " ";
					}
					np2 = np2.trim();
					String pair = "";
					if (fe1.compareTo(fe2) > 0) {
						pair = np2 + " " + fe2 + "__" + frame + "__" + fe1 + " " + np1;
					} else {
						pair = np1 + " " + fe1 + "__" + frame + "__" + fe2 + " " + np2;
					}
					relations.add(pair);
				}
			}
		}
		int totalSize = relations.size();
		String[] arr = new String[totalSize];
		arr = relations.toArray(arr);
		Arrays.sort(arr);
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(arr));
		ParsePreparation.writeSentencesToTempFile(outFile, list);
	}
}