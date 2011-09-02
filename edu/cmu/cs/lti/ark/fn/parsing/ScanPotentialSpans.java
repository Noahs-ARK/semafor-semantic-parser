package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.clusters.ScrapTest;
import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.utils.DataPoint;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

public class ScanPotentialSpans {
	// public static final String DATA_DIR = "/home/dipanjan/work/summer2011/ArgID/data";
	public static final String DATA_DIR = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";

	public static final String INFIX = "train";

	public static final int SPAN_LENGTH_UPPER_BOUND = 10;

	public static void main(String[] args) {
		// generateFEStats();
		// generateSpans();
		// generateSpanLengthStats();
		// generateFEStats();
		// generateLabeledData();
		generateHeadWords();
	}
	
	public static void generateHeadWords() {
		String spanFile = DATA_DIR + "/all.spans.sorted";
		ArrayList<String> spanList = ParsePreparation.readSentencesFromFile(spanFile);
		String[] spanArr = new String[spanList.size()];
		for (int i = 0; i < spanArr.length; i++) {
			spanArr[i] = "" + spanList.get(i);
		}
		spanList.clear();
		String[] labeledProcessedFiles = 
						{
						DATA_DIR + "/framenet.original.sentences.all.lemma.tags"
						};
		String[] labeledFEFiles = {DATA_DIR + "/framenet.original.sentences.frame.elements"};
		String unlabeledProcessedFile = 
			"/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/uData/AP_1m.all.lemma.tags";
		TObjectIntHashMap<String>[] headCountArr = new TObjectIntHashMap[spanArr.length];
		for (int i = 0; i < headCountArr.length; i++) {
			headCountArr[i] = new TObjectIntHashMap<String>();
		}
		for (int i = 0; i < labeledProcessedFiles.length; i++) {
			String file = labeledProcessedFiles[i];
			ArrayList<String> parses = ParsePreparation.readSentencesFromFile(file);
			ArrayList<String> fes = ParsePreparation.readSentencesFromFile(labeledFEFiles[i]);
			for (String fe: fes) {
				String[] feToks = fe.trim().split("\t");
				int sentNum = new Integer(feToks[5]);
				StringTokenizer st = new StringTokenizer(parses.get(sentNum),"\t");
				int tokensInFirstSent = new Integer(st.nextToken());
				String[][] data = new String[5][tokensInFirstSent];
				for(int k = 0; k < 5; k ++) {
					data[k]=new String[tokensInFirstSent];
					for(int l = 0; l < tokensInFirstSent; l++) {
						String tok = st.nextToken().trim();
						if (k == 0) {
							if (tok.equals("-LRB-")) { tok = "("; }
							if (tok.equals("-RRB-")) { tok = ")"; }
							if (tok.equals("-RSB-")) { tok = "]"; }
							if (tok.equals("-LSB-")) { tok = "["; }
							if (tok.equals("-LCB-")) { tok = "{"; }
							if (tok.equals("-RCB-")) { tok = "}"; }
						}
						data[k][l]=""+tok;
					}
				}	
				DependencyParse parseS = DependencyParse.processFN(data, 0.0);
				DependencyParse[] parseNodes = DependencyParse.getIndexSortedListOfNodes(parseS);
				for(int k = 6; k < feToks.length; k = k + 2) {
					String[] spanS = feToks[k+1].split(":");
					int start = -1;
					int end = -1;
					if(spanS.length==1) {
						start=new Integer(spanS[0]);
						end=new Integer(spanS[0]);
					}
					else {
						start=new Integer(spanS[0]);
						end=new Integer(spanS[1]);
					}
					if ((end - start + 1) <= SPAN_LENGTH_UPPER_BOUND) {
						String span = "";
						for (int m = start; m <= end; m++) {
							span += data[0][m] + " ";
						}
						span = span.toLowerCase();
						span = replaceNumbersWithAt(span.trim());
						if (Arrays.binarySearch(spanArr, span) >= 0) {
							int index = Arrays.binarySearch(spanArr, span);
							int[] tokNums = new int[end - start + 1];
							for (int m = start; m <= end; m++) {
								tokNums[m-start] = m;
							}
							DependencyParse head = DependencyParse.getHeuristicHead(parseNodes, tokNums);
							String hw = head.getWord().toLowerCase();
							hw = replaceNumbersWithAt(hw);
							if (headCountArr[index].contains(hw)) {
								int c = headCountArr[index].get(hw);
								headCountArr[index].put(hw, c+1);
							} else {
								headCountArr[index].put(hw, 1);
							}
						}
					}
				}
			}
		}
		System.out.println("Finished scanning labeled data.");
	}
	
	public static void generateLabeledData() {
		String[] labeledProcessedFiles = 
		{DATA_DIR + "/framenet.original.sentences.all.lemma.tags"};
		String[] labeledFEFiles = {DATA_DIR + "/framenet.original.sentences.frame.elements"};		
		ArrayList<String> fes = ParsePreparation.readSentencesFromFile(DATA_DIR + "/fes.sorted");
		String[] arr = new String[fes.size()];
		for (int i = 0; i < fes.size(); i++) {
			arr[i] = ""+fes.get(i);
		}
		ArrayList<String> allSpans = ParsePreparation.readSentencesFromFile(DATA_DIR + "/all.spans.sorted");
		String[] spanArr = new String[allSpans.size()];
		for (int i = 0; i < allSpans.size(); i++) {
			spanArr[i] = ""+allSpans.get(i);
		}
		String outFile = DATA_DIR + "/spans.fes";
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(outFile));
			TIntObjectHashMap<int[]> countMap = new TIntObjectHashMap<int[]>(); 
			for (int i = 0; i < labeledProcessedFiles.length; i++) {
				String file = labeledProcessedFiles[i];
				ArrayList<String> parses = ParsePreparation.readSentencesFromFile(file);
				fes = ParsePreparation.readSentencesFromFile(labeledFEFiles[i]);
				int count = 0;
				for (String fe: fes) {
					String[] feToks = fe.trim().split("\t");
					int sentNum = new Integer(feToks[5]);
					StringTokenizer st = new StringTokenizer(parses.get(sentNum),"\t");
					int tokensInFirstSent = new Integer(st.nextToken());
					String[][] data = new String[5][tokensInFirstSent];
					for(int k = 0; k < 5; k ++)
					{
						data[k]=new String[tokensInFirstSent];
						for(int l = 0; l < tokensInFirstSent; l ++)
						{
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
					for(int k = 6; k < feToks.length; k = k + 2)
					{
						String[] spanS = feToks[k+1].split(":");
						String f = feToks[k];
						int fIndex = Arrays.binarySearch(arr, f);
						if (fIndex < 0) {
							System.out.println("FE: " + f + " not found. Exiting.");
							System.exit(-1);
						}
						int start = -1;
						int end = -1;
						if(spanS.length==1)
						{
							start=new Integer(spanS[0]);
							end=new Integer(spanS[0]);
						}
						else
						{
							start=new Integer(spanS[0]);
							end=new Integer(spanS[1]);
						}
						if ((end - start + 1) <= SPAN_LENGTH_UPPER_BOUND) {
							String span = "";
							for (int m = start; m <= end; m++) {
								span += data[0][m] + " ";
							}
							span = span.toLowerCase();
							span = replaceNumbersWithAt(span.trim());
							int index = Arrays.binarySearch(spanArr, span);
							if (index < 0) {
								System.out.println("Problem. Span: " + span + " not found in arr. Exiting.");
								System.exit(-1);
							}
	//						if (countMap.contains(index)) {
	//							int[] Arr = countMap.get(index);
	//							Arr[fIndex] += 1;
	//							countMap.put(index, Arr);
	//						} else {
	//							int[] Arr = new int[fes.size()];
	//							for (int m = 0; m < fes.size(); m++) {
	//								Arr[m] = 0;
	//							}
	//							Arr[fIndex] += 1;
	//							countMap.put(index, Arr);
	//						}
							bWriter.write(span + "\t" + f + "\n");
						}
					}
					count ++;
					if (count % 1000 == 0) {
						System.out.print(count + " ");
					}
					if (count % 10000 == 0) {
						System.out.println();
					}
				}
			} 
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void generateSpanLengthStats() {
		String feFile = DATA_DIR + "/cv.train.sentences.frame.elements";
		ArrayList<String> fes = ParsePreparation.readSentencesFromFile(feFile);
		TIntIntHashMap map = new TIntIntHashMap();
		int total = 0;
		for (String feLine: fes) {
			String[] toks = feLine.trim().split("\t");
			for(int k = 6; k < toks.length; k = k + 2) {
				String[] spans = toks[k+1].split(":");
				int length = 1;
				if(spans.length != 1) {
					int start = new Integer(spans[0]);
					int end = new Integer(spans[1]);
					length = (end - start) + 1;
				} 
				if (map.contains(length)) {
					int val = map.get(length);
					map.put(length, val+1);
				} else {
					map.put(length, 1);
				}
				total++;
			}
		}
		int[] keys = map.keys();
		Arrays.sort(keys);
		for (int key: keys) {
			System.out.println(key + "\t" + ((double)map.get(key) / total));
		}
	}

	public static void generateFEStats() {
		String feMap = "" +
		"/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/ACLSplits/5/framenet.frame.element.map";
		Set<String> fes = new THashSet<String>();
		THashMap<String,THashSet<String>>fedict = 
			(THashMap<String,THashSet<String>>)SerializedObjects.readSerializedObject(feMap);
		Collection<THashSet<String>> col = fedict.values();
		for (THashSet<String> set: col) {
			fes.addAll(set);
		}
		String foFeFile = DATA_DIR + "/framenet.original.sentences.frame.elements";
		String outFile = DATA_DIR + "/fes.sorted";
		ArrayList<String> fres = ParsePreparation.readSentencesFromFile(foFeFile);
		for (String fe: fres) {
			String[] feToks = fe.split("\t");		
			for(int k = 6; k < feToks.length; k = k + 2) {
				String f = feToks[k];
				if (!fes.contains(f)) {
					System.out.println("Set does not contain: " + f);
				}
				fes.add(f);
			}
		}
		System.out.println("Total number of unique fes: " + fes.size());
		String[] arr = new String[fes.size()];
		fes.toArray(arr);
		Arrays.sort(arr);
		ArrayList<String> list = new ArrayList<String>();
		for (String a: arr) {
			list.add(a);
		}
		ParsePreparation.writeSentencesToTempFile(outFile, list);
	}

	public static String replaceNumbersWithAt(String span) {
		String res = "";
		for (int i = 0; i < span.length(); i++) {
			if (Character.isDigit(span.charAt(i))) {
				res += "@";
			} else {
				res += "" + span.charAt(i);
			}
		}
		return res;
	}	
	
	
	public static void generateSpans() {
		String[] labeledProcessedFiles = 
		{DATA_DIR + "/framenet.original.sentences.all.lemma.tags"};
		String[] labeledFEFiles = {DATA_DIR + "/framenet.original.sentences.frame.elements"};		
		
		String unlabeledProcessedFile = 
			"/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/uData/AP_1m.all.lemma.tags";
		Set<String> spans = new THashSet<String>();
		String spanFile = DATA_DIR + "/all.spans.sorted";
		for (int i = 0; i < labeledProcessedFiles.length; i++) {
			String file = labeledProcessedFiles[i];
			ArrayList<String> parses = ParsePreparation.readSentencesFromFile(file);
			ArrayList<String> fes = ParsePreparation.readSentencesFromFile(labeledFEFiles[i]);
			for (String fe: fes) {
				String[] feToks = fe.trim().split("\t");
				int sentNum = new Integer(feToks[5]);
				StringTokenizer st = new StringTokenizer(parses.get(sentNum),"\t");
				int tokensInFirstSent = new Integer(st.nextToken());
				String[][] data = new String[5][tokensInFirstSent];
				for(int k = 0; k < 5; k ++)
				{
					data[k]=new String[tokensInFirstSent];
					for(int l = 0; l < tokensInFirstSent; l ++)
					{
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
				for(int k = 6; k < feToks.length; k = k + 2)
				{
					String[] spanS = feToks[k+1].split(":");
					int start = -1;
					int end = -1;
					if(spanS.length==1)
					{
						start=new Integer(spanS[0]);
						end=new Integer(spanS[0]);
					}
					else
					{
						start=new Integer(spanS[0]);
						end=new Integer(spanS[1]);
					}
					if ((end - start + 1) <= SPAN_LENGTH_UPPER_BOUND) {
						String span = "";
						for (int m = start; m <= end; m++) {
							span += data[0][m] + " ";
						}
						span = span.toLowerCase();
						span = replaceNumbersWithAt(span.trim());
						spans.add(span);
					}
				}
			}
		}
		System.out.println("Number of unique spans in the lexicon:" + spans.size());
		String uFile = unlabeledProcessedFile;
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(uFile);
		int j = 0;
		for (j = 0; j < parses.size(); j++) {
			StringTokenizer st = new StringTokenizer(parses.get(j),"\t");
			int tokensInFirstSent = new Integer(st.nextToken());
			String[][] data = new String[5][tokensInFirstSent];
			for(int k = 0; k < 5; k ++)
			{
				data[k]=new String[tokensInFirstSent];
				for(int l = 0; l < tokensInFirstSent; l ++)
				{
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
			DependencyParse parseS = DependencyParse.processFN(data, 0.0);
			DependencyParse[] sortedNodes = DependencyParse.getIndexSortedListOfNodes(parseS);
			boolean[][] spanMat = new boolean[sortedNodes.length][sortedNodes.length];
			int[][] heads = new int[sortedNodes.length][sortedNodes.length];
			ScrapTest.findSpans(spanMat,heads,sortedNodes);
			for (int m = 0; m < sortedNodes.length; m++) {
				for (int n = 0; n < sortedNodes.length; n++) {
					if (spanMat[m][n]) {
						if ((m-n) + 1 > SPAN_LENGTH_UPPER_BOUND) {
							continue;
						}
						if ((n-m) + 1 > SPAN_LENGTH_UPPER_BOUND) {
							continue;
						}
						String span = "";
						for (int z = m; z<= n; z++) {
							span += data[0][z].toLowerCase() + " ";
						}
						span = replaceNumbersWithAt(span.trim());
						spans.add(span);
					}
				}
			}
			if (spans.size() >= 500000) {
				break;
			}
			if (j % 100 == 0) {
				System.out.print(j+" ");
			}
			if (j % 10000 == 0) {
				System.out.println();
			}
		}
		System.out.println();
		System.out.println("Number of scanned unlabeled sentences: " + j);
		System.out.println("Number of total spans:" + spans.size());
		String[] spanArray = new String[spans.size()];
		spans.toArray(spanArray);
		Arrays.sort(spanArray);
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(spanFile));
			for (String span: spanArray) {
				bWriter.write(span + "\n");
			}
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}