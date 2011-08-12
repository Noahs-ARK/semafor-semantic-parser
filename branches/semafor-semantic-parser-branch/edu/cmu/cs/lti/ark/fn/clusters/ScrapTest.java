/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * ScrapTest.java is part of SEMAFOR 2.0.
 * 
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along
 * with SEMAFOR 2.0.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParses;
import gnu.trove.THashMap;
import gnu.trove.THashSet;


public class ScrapTest {
	public static void main(String[] args)
	{	
		// testCoverageWithUnlabeledData(getSpans());
		// testCoverage();
		testMultiwordCoverage();
		// compareTotalNumberOfSpansInThreeSettings();
		// checkCoverageOfSpansUsingKBestLists();
	}
	
	public static void listSpanPOSs() {
		
	}
		
	public static void checkCoverageOfSpansUsingKBestLists()
	{
		String prefix="dev";
		String parseDirectory = "/usr2/dipanjan/experiments/FramenetParsing/FrameStructureExtraction/mstscripts/data/depparses";
		
		//String parseFile = "lrdata/semeval.fulltrain.sentences.lemma.tags";
		//THashSet<String> spanSet = getSpans();
		
		String jobjFileDirectory = parseDirectory+"/"+prefix;
		String feFile = "lrdata/semeval.fulldev.sentences.frame.elements";
		
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
		int match=0;
		int total=0;
		int index = -1;
		DependencyParses dp = null;

		int K = 10000;
		
		for(String feLine:feLines)
		{
			System.out.println(feLine);
			String[] toks = feLine.trim().split("\t");
			int sentNum = new Integer(toks[5]);
			DependencyParses parses = null;
			if(sentNum==index)
			{
				parses=dp;
			}
			else
			{
				parses = (DependencyParses)SerializedObjects.readSerializedObject(jobjFileDirectory+"/parse_"+sentNum+".jobj");
				index=sentNum;
				dp=parses;
			}
			DependencyParse parseS=parses.get(0);	
			DependencyParse[] sortedNodes = DependencyParse.getIndexSortedListOfNodes(parseS);
			boolean[][] spanMat = new boolean[sortedNodes.length][sortedNodes.length];
			int[][] heads = new int[sortedNodes.length][sortedNodes.length];
			for(int j = 0; j < sortedNodes.length; j ++)
			{
				for(int k = 0; k < sortedNodes.length; k ++)
				{
					spanMat[j][k]=false;
					heads[j][k]=-1;
				}
			}
			int pLen = parses.size();
			System.out.println("pLen="+pLen+" K="+K);
			for(int m = 0; m < K; m++)
			{
				if(m>=pLen)
					continue;
				parseS=parses.get(m);	
				sortedNodes = DependencyParse.getIndexSortedListOfNodes(parseS);
				findSpans(spanMat,heads,sortedNodes);				
			}
			for(int k = 6; k < toks.length; k = k + 2)
			{
				String[] spans = toks[k+1].split(":");
				int start = -1;
				int end = -1;
				if(spans.length==1)
				{
					start=new Integer(spans[0]);
					end=new Integer(spans[0]);
				}
				else
				{
					start=new Integer(spans[0]);
					end=new Integer(spans[1]);
				}
				if(spanMat[start][end])
					match++;
				total++;
			}
		}
		double recall = (double)match/total;
		System.out.println("Recall:"+recall);
	}
	
	
	
	public static void compareTotalNumberOfSpansInThreeSettings()
	{
		THashSet<String> spanSet = getSpans();
		String parseFile = "lrdata/semeval.fulltrain.sentences.lemma.tags";
		ArrayList<String> allParses = ParsePreparation.readSentencesFromFile(parseFile);
		int bruteForce = 0;
		int subtreesOnly = 0;
		int subtreesPlusUnlabeled = 0;
		for(String parsedSent:allParses)
		{
			StringTokenizer st = new StringTokenizer(parsedSent,"\t");
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
			findSpans(spanMat,heads,sortedNodes);
			for(int j = 0; j < tokensInFirstSent; j ++)
			{
				for(int k = 0; k < tokensInFirstSent; k ++)
				{
					if(k<j)
						continue;
					bruteForce++;
					if(j==k)
						continue;
					if(spanMat[j][k])
						subtreesOnly++;
					String span = "";
					for(int l = j; l <= k; l ++)
						span+= data[0][l]+" ";
					span=span.trim().toLowerCase();
					if(spanSet.contains(span))
					{
						if(!spanMat[j][k])
							subtreesPlusUnlabeled++;
						spanMat[j][k]=true;
					}
				}
			}
		}
		
		System.out.println("Brute force:"+bruteForce);
		System.out.println("Subtrees only:"+subtreesOnly);
		System.out.println("Subtrees plus unlabeled data:"+subtreesPlusUnlabeled);
	}
	
	
	public static THashSet<String> getSpans()
	{
		THashSet<String> spans = new THashSet<String>();
		try
		{
			String line = null;
			BufferedReader bReader = new BufferedReader(new FileReader("lrdata/matched_spans"));
			while((line=bReader.readLine())!=null)
			{
				String[] toks = line.trim().split("\t");
				char first = toks[0].charAt(0);
				if((first>='a'&&first<='z')||(first>='A'&&first<='Z')||(first>='0'&&first<='9'))
				{
					spans.add(toks[0].trim());
				}
			}
			bReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return spans;
	}
	
	public static THashSet<String> getSpans(String file)
	{
		THashSet<String> spans = new THashSet<String>();
		try
		{
			String line = null;
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			while((line=bReader.readLine())!=null)
			{
				String[] toks = line.trim().split("\t");
				char first = toks[0].charAt(0);
				if((first>='a'&&first<='z')||(first>='A'&&first<='Z')||(first>='0'&&first<='9'))
				{
					spans.add(toks[0].trim());
				}
			}
			bReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return spans;
	}
	
	public static THashMap<String,Integer> getSpansWithHeads(String file)
	{
		THashMap<String, Integer> spans = new THashMap<String, Integer>();
		THashMap<String, Integer> countMap = new THashMap<String,Integer>();
		try
		{
			String line = null;
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			while((line=bReader.readLine())!=null)
			{
				String[] toks = line.trim().split("\t");
				char first = toks[0].charAt(0);
				if((first>='a'&&first<='z')||(first>='A'&&first<='Z')||(first>='0'&&first<='9'))
				{
					String word = toks[0].trim();
					int count = new Integer(toks[2].trim());
					if(countMap.contains(word))
					{
						if(countMap.get(word)<count)
						{
							countMap.put(word, count);
							spans.put(word, new Integer(toks[1].trim()));
						}
					}
					else
					{
						countMap.put(word, count);
						spans.put(word, new Integer(toks[1].trim()));
					}
				}
			}
			bReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Size of spans:"+spans.size());
		return spans;
	}
	
	
	public static void testCoverageWithUnlabeledData(THashSet<String> spanSet)
	{
		String parseFile = "lrdata/semeval.fulltrain.sentences.lemma.tags";
		String feFile = "lrdata/semeval.fulltrain.sentences.frame.elements";
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
				
		int match=0;
		int total=0;
		for(String feLine:feLines)
		{
			System.out.println(feLine);
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
			
			findSpans(spanMat,heads,sortedNodes);
						
			for(int j = 0; j < tokensInFirstSent; j ++)
			{
				for(int k = 0; k < tokensInFirstSent; k ++)
				{
					if(j==k)
						continue;
					if(k<j)
						continue;
					String span = "";
					for(int l = j; l <= k; l ++)
						span+= data[0][l]+" ";
					span=span.trim().toLowerCase();
					if(spanSet.contains(span))
						spanMat[j][k]=true;
				}
			}
			
			
			for(int k = 6; k < toks.length; k = k + 2)
			{
				String[] spans = toks[k+1].split(":");
				int start = -1;
				int end = -1;
				if(spans.length==1)
				{
					start=new Integer(spans[0]);
					end=new Integer(spans[0]);
				}
				else
				{
					start=new Integer(spans[0]);
					end=new Integer(spans[1]);
				}
				if(spanMat[start][end])
					match++;
				total++;
			}
		}
		double recall = (double)match/total;
		System.out.println("Recall:"+recall);
		
	}
	
	public static void testMultiwordCoverage() {
		String parseFile = "/usr0/dipanjan/work/summer2011/FN/data/cv.train.sentences.all.lemma.tags";
		String feFile = "/usr0/dipanjan/work/summer2011/FN/data/cv.train.sentences.frame.elements";
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
		int match=0;
		int total=0;
		THashMap<Integer, THashSet<String>> goldSpans = new THashMap<Integer, THashSet<String>>();
		THashMap<Integer, THashSet<String>> autoSpans = new THashMap<Integer, THashSet<String>>();		
		
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
			findSpansAlternative2(spanMat,heads,sortedNodes);
			if (!autoSpans.contains(sentNum)) {
				THashSet<String> spans = new THashSet<String>();
				for (int i = 0; i < sortedNodes.length; i++) {
					for (int j = 0; j < sortedNodes.length; j++) {
						if (i == j) {
							continue;
						}
						if (spanMat[i][j]) {
							String span = i + ":" + j;
							spans.add(span);
						}
					}
				}
				autoSpans.put(sentNum, spans);
			}
			
			for(int k = 6; k < toks.length; k = k + 2) {
				String[] spans = toks[k+1].split(":");
				if(spans.length!=1)
				{
					if (goldSpans.contains(sentNum)) {
						THashSet<String> spanSet = goldSpans.get(sentNum);
						spanSet.add(toks[k+1]);
						goldSpans.put(sentNum, spanSet);
					} else {
						THashSet<String> spanSet = new THashSet<String>();
						spanSet.add(toks[k+1]);
						goldSpans.put(sentNum, spanSet);
					}
				}
			}
		}
		
		Set<Integer> goldKeys = goldSpans.keySet();
		Integer[] keys = new Integer[goldKeys.size()];
		goldKeys.toArray(keys);
		Arrays.sort(keys);
		int numAutoSpans = 0;
		for (int key: keys) {
			Set<String> g = goldSpans.get(key);
			Set<String> a = autoSpans.get(key);
			numAutoSpans += a.size();
			System.out.println("Problems with sentence: " + key);
			for (String s: g) {
				if (a.contains(s)) {
					match++;
				} else {
					System.out.println("Span " + s + " does not match.");
					printWords(s, parses.get(key));
				}
				total++;
			}
			System.out.println("\n\n");
		}		
		double recall = (double)match/total;
		System.out.println("Recall:"+recall);
		System.out.println("Number of auto spans: " + numAutoSpans);
	}
	
	public static void printWords(String s, String parse) {
		String[] spans = s.split(":");
		int start = new Integer(spans[0]);
		int end = new Integer(spans[1]);
		StringTokenizer st = new StringTokenizer(parse,"\t");
		int tokensInFirstSent = new Integer(st.nextToken());
		String span = "";
		for(int j = 0; j < tokensInFirstSent; j ++)
		{
			String tok = st.nextToken().trim();
			if (j >= start && j <= end) {
				span += tok + " ";
			}
		}		
		System.out.println(span.trim());
	}
	
	public static void testCoverage()
	{
		String parseFile = "/usr0/dipanjan/work/summer2011/FN/data/cv.train.sentences.all.lemma.tags";
		String feFile = "/usr0/dipanjan/work/summer2011/FN/data/cv.train.sentences.frame.elements";
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(feFile);
		int match=0;
		int total=0;
		
		for(String feLine:feLines)
		{
			System.out.println(feLine);
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
			findSpansAlternative(spanMat,heads,sortedNodes);
						
			for(int k = 6; k < toks.length; k = k + 2)
			{
				String[] spans = toks[k+1].split(":");
				int start = -1;
				int end = -1;
				if(spans.length==1)
				{
					start=new Integer(spans[0]);
					end=new Integer(spans[0]);
				}
				else
				{
					start=new Integer(spans[0]);
					end=new Integer(spans[1]);
				}
				if(spanMat[start][end]) {
					match++;
				}
				total++;
			}
		}
		double recall = (double)match/total;
		System.out.println("Recall:"+recall);
	}
	
	public static void findSpansAlternative(boolean[][] spanMat, int[][] heads, DependencyParse[] nodes) {
		int[] parent = new int[nodes.length - 1];
		for (int i = 0; i < parent.length; i++) {
			parent[i] = (nodes[i + 1].getParentIndex() - 1);
		}
		// single words
		for (int i = 0; i < parent.length; i++) {
			spanMat[i][i] = true;
		}
		// multiple words
		for (int j = 1; j < parent.length; j++) {
			for (int i = 0; i < j; i++) {
				if (i == j) continue;
				int totalHeads = 0;
				for (int k = i; k <= j; k++) {
					if (parent[k] < i || parent[k] > j) {
						totalHeads++;
					}
				}
				if (totalHeads <= 1) {
					spanMat[i][j] = true;
				}
			}
		}		
	}
	
	public static void findSpansAlternative2(boolean[][] spanMat, int[][] heads, DependencyParse[] nodes) {
		int[] parent = new int[nodes.length - 1];
		for (int i = 0; i < parent.length; i++) {
			parent[i] = (nodes[i + 1].getParentIndex() - 1);
		}
		// single words
		for (int i = 0; i < parent.length; i++) {
			spanMat[i][i] = true;
		}
		// multiple words
		for (int j = 1; j < parent.length; j++) {
			for (int i = 0; i < j; i++) {
				if (i == j) continue;
				int totalHeads = 0;
//				for (int k = i; k <= j; k++) {
//					if (parent[k] < i || parent[k] > j) {
//						totalHeads++;
//					}
//				}
				if (totalHeads <= 1) {
					spanMat[i][j] = true;
				}
			}
		}		
	}
	
	public static void findSpans(boolean[][] spanMat, int[][] heads, DependencyParse[] nodes) {
		int[] parent = new int[nodes.length - 1];
		int left[] = new int[parent.length];
		int right[] = new int[parent.length];
		for (int i = 0; i < parent.length; i++) {
			parent[i] = (nodes[i + 1].getParentIndex() - 1);
			left[i] = i;
			right[i] = i;
		}
		for (int i = 0; i < parent.length; i++) {
			int index = parent[i];
			while (index >= 0) {
				if (left[index] > i) {
					left[index] = i;
				}
				if (right[index] < i) {
					right[index] = i;
				}
				index = parent[index];
			}
		}
		for (int i = 0; i < parent.length; i++)
		{
			spanMat[left[i]][right[i]] = true;
			heads[left[i]][right[i]] = i;
		}
		
		// single words
		for (int i = 0; i < parent.length; i++) {
			spanMat[i][i] = true;
			heads[i][i]=i;
		}
		
		for (int i = 0; i < parent.length; i++)
		{
			if(!(left[i]<i&&right[i]>i))
				continue;
			//left
			int justLeft=i-1;
			if(i-1>=0)
			{
				if(spanMat[left[i]][justLeft])
				{
					if(justLeft-left[i]==0&&nodes[justLeft+1].getPOS().equals("DT"))
					{
						;
					}
					else if(justLeft-left[i]==0&&nodes[justLeft+1].getPOS().equals("JJ"))
					{
						;
					}
					else
					{	
						spanMat[i][right[i]]=true;
						heads[i][right[i]]=i;
					}
				}
			}
			
			//right
			int justRight=i+1;
			if(justRight<=parent.length-1)
			{
				if(spanMat[justRight][right[i]])
				{
					spanMat[left[i]][i]=true;
					heads[left[i]][i]=i;
				}
			}
		}
		
	}	
}
