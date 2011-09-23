package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class LabeledHeadWordsWithSemTypes {

	public static final String DATA_DIR = 
			"/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";

	public static void main(String[] args) {
		headWordsAndSemTypes();
	}	

	public static void headWordsAndSemTypes() {
		String semTypesMapFile = DATA_DIR + "/fe2semTypes.map";
		THashMap<String, THashSet<String>> semTypesMap = 
				(THashMap<String, THashSet<String>>) SerializedObjects.readSerializedObject(semTypesMapFile);
		String[] labeledProcessedFiles = 
			{DATA_DIR + "/framenet.original.sentences.all.lemma.tags"};
		String[] labeledFEFiles = {DATA_DIR + "/framenet.original.sentences.frame.elements"};
		for (int i = 0; i < labeledProcessedFiles.length; i++) {
			String file = labeledProcessedFiles[i];
			ArrayList<String> parses = ParsePreparation.readSentencesFromFile(file);
			ArrayList<String> fes = ParsePreparation.readSentencesFromFile(labeledFEFiles[i]);
			for (String fe: fes) {
				String[] feToks = fe.trim().split("\t");
				int sentNum = new Integer(feToks[5]);
				StringTokenizer st = new StringTokenizer(parses.get(sentNum),"\t");
				int tokensInFirstSent = new Integer(st.nextToken());
				String[][] data = new String[6][tokensInFirstSent];
				for(int k = 0; k < 6; k ++) {
					data[k]=new String[tokensInFirstSent];
					for(int l = 0; l < tokensInFirstSent; l ++) {
						String tok = st.nextToken().trim();
						if (k == 0) {
							if (tok.equals("-LRB-")) {
								tok = "(";
							}
							if (tok.equals("-RRB-")) {
								tok = ")";
							}
							if (tok.equals("-RSB-")) {
								tok = "]";
							}
							if (tok.equals("-LSB-")) {
								tok = "[";
							}
							if (tok.equals("-LCB-")) {
								tok = "{";
							}
							if (tok.equals("-RCB-")) {
								tok = "}";
							}
						}
						data[k][l]=""+tok;
					}
				}
				DependencyParse dp = DependencyParse.processFN(data, 0);
				DependencyParse[] dpnodes = DependencyParse.getIndexSortedListOfNodes(dp);
				for(int k = 6; k < feToks.length; k = k + 2) {
					String[] spanS = feToks[k+1].split(":");
					int start = -1;
					int end = -1;
					if(spanS.length==1) {
						start=new Integer(spanS[0]);
						end=new Integer(spanS[0]);
					} else {
						start=new Integer(spanS[0]);
						end=new Integer(spanS[1]);
					}
					String head = getHeadWithPOS(dpnodes, data[5], start, end);
					String frel = feToks[k];
					if (!semTypesMap.containsKey(frel)) {
						continue;
					}
					if (head == null) {
						continue;
					}
					System.out.println("Head: " + head);
				}
			}
		}	
	}
	
	public static String getHeadWithPOS(DependencyParse[] nodes, String[] lemmas, int istart, int iend) {
		int[] tokNums = new int[iend - istart + 1];
		for (int m = istart; m <= iend; m++) {
			tokNums[m-istart] = m;
		}
		DependencyParse head = DependencyParse.getHeuristicHead(nodes, tokNums);
		String pos = head.getPOS();
		if (pos.startsWith("N")) {
			pos = "n";
		} else if (pos.startsWith("V")) {
			pos = "v";
		} else if (pos.startsWith("J")) {
			pos = "a";
		} else if (pos.startsWith("RB")) {
			pos = "adv";
		} else if (pos.startsWith("I") || pos.startsWith("TO")) {
			pos = "prep";
		} else {
			pos = null;
		}
		int index = head.getIndex();
		String lemma = lemmas[index-1].toLowerCase();
		System.out.println(head + "\t" + lemma);
		String hw = ScanPotentialSpans.replaceNumbersWithAt(lemma);
		if (pos != null) {
			return hw + "." + pos;
		} else {
			return null;
		}
	}
}
