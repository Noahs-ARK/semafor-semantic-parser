/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * JointFNIDAndParsing.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.identification.FastFrameIdentifier;
import edu.cmu.cs.lti.ark.fn.identification.RequiredDataForFrameIdentification;
import edu.cmu.cs.lti.ark.fn.identification.SmoothedGraph;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.optimization.LDouble;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectDoubleHashMap;

public class JointFNIDAndParsing {
	public static void main(String[] args)
	{
		FNModelOptions options = new FNModelOptions(args);
		ArrayList<String> parses = new ArrayList<String>();
		int start = options.startIndex.get();
		int end = options.endIndex.get();
		WordNetRelations wnr = 
			new WordNetRelations(options.stopWordsFile.get(), options.wnConfigFile.get());
		int count = 0;
		ArrayList<String> tokenNums = new ArrayList<String>();
		ArrayList<String> orgSentenceLines = new ArrayList<String>();
		ArrayList<String> originalIndices = new ArrayList<String>();
		try {
			BufferedReader inParses = new BufferedReader(new FileReader(options.testParseFile.get()));
			BufferedReader inOrgSentences = new BufferedReader(new FileReader(options.testTokenizedFile.get()));
			String line = null;
			int dummy = 0;
			while((line=inParses.readLine())!=null) {
				String line2 = inOrgSentences.readLine().trim();
				if(count<start)	{
					count++;
					continue;
				}
				parses.add(line.trim());
				orgSentenceLines.add(line2);
				tokenNums.add(""+dummy);	// ?? I think 'dummy' is just the offset of the sentence relative to options.startIndex
				originalIndices.add(""+count);
				if(count==(end-1))	// skip sentences after the specified range
					break;
				count++;
				dummy++;
			}				
			inParses.close();
			inOrgSentences.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
		RequiredDataForFrameIdentification r = 
			(RequiredDataForFrameIdentification)SerializedObjects.readSerializedObject(options.fnIdReqDataFile.get());
		THashSet<String> allRelatedWords = r.getAllRelatedWords();
		Map<String, Set<String>> relatedWordsForWord = r.getRelatedWordsForWord();
		THashMap<String,THashSet<String>> frameMap = r.getFrameMap();
		THashMap<String,THashSet<String>> cMap = r.getcMap();
		Map<String, Map<String, Set<String>>> revisedRelationsMap = 
			r.getRevisedRelMap();
		ArrayList<String> segs = ParsePreparation.readSentencesFromFile(options.testFrameFile.get());
		ArrayList<String> inputForFrameId = getGoldSeg(segs,start,end);	// Null\tTargetTokenNum(s)\tSentenceOffset
		ArrayList<String> idResult = new ArrayList<String>();
		TObjectDoubleHashMap<String> paramList = parseParamFile(options.idParamFile.get());
		Map<String, String> hvLemmas = r.getHvLemmaCache();
		FastFrameIdentifier idModel = new FastFrameIdentifier(
				paramList, 
				"reg", 
				0.0, 
				frameMap, 
				null, 
				cMap,
				relatedWordsForWord,
				revisedRelationsMap,
				hvLemmas);
		System.out.println("Size of originalSentences list:"+originalIndices.size());
		boolean usegraph = !options.useGraph.get().equals("null");
		SmoothedGraph sg = null;
		if (usegraph) {
			sg = (SmoothedGraph)SerializedObjects.readSerializedObject(options.useGraph.get());
		}
		// initializing argument identification
		System.out.println("Initializing alphabet for argument identification..");
		CreateAlphabet.setDataFileNames(options.alphabetFile.get(), 
										options.frameNetElementsMapFile.get(),
										options.eventsFile.get(),
										options.spansFile.get());
		JointDecoding decoding = new JointDecoding("ilp");
		decoding.init(options.modelFile.get(), 
					  options.alphabetFile.get());
		decoding.setMaps(options.requiresMap.get(), options.excludesMap.get());		
		
		System.out.println("Start Time:"+(new Date()));
		ArrayList<String> argResults = new ArrayList<String>();
		for(String input: inputForFrameId) {
			String[] toks = input.split("\t");
			int sentNum = new Integer(toks[2]);	// offset of the sentence within the loaded data (relative to options.startIndex)
			String[] frames = null;
			if (sg == null) {
				frames = idModel.getBestFrame(input, parses.get(sentNum), true, 1000);
			} else {
				frames = idModel.getBestFrame(input,parses.get(sentNum), sg, true);
			}
			double[] scores = new double[frames.length];
			String[] frames1 = new String[frames.length];
			for (int j = 0; j < frames.length; j++) {
				String[] tokFrames = frames[j].split("\t");
				scores[j] = new Double(tokFrames[1]);
				frames1[j] = tokFrames[0];
			}			
			String tokenRepresentation = 
				getTokenRepresentation(toks[1], parses.get(sentNum));  
			String[] split = tokenRepresentation.trim().split("\t");
			String sentCount = originalIndices.get(sentNum);
			idResult.clear();
			for (int j = 0; j < frames.length; j++) {
				idResult.add(1+"\t"+frames1[j]+"\t"+split[0]+"\t"+toks[1]+"\t"+split[1]+"\t"+sentCount);
				System.out.println("1"+"\t"+frames1[j]+"\t"+split[0]+"\t"+toks[1]+"\t"+split[1]+"\t"+sentCount+"\t"+scores[j]);
			}
			// 3. argument identification
			CreateAlphabet.run(false, parses, idResult, wnr, false, options.posConstraintsFile.get());
			LocalFeatureReading lfr = 
					new LocalFeatureReading(options.eventsFile.get(),
											options.spansFile.get(),
											idResult);
			try
			{
				lfr.readLocalFeatures(false, null);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.err.println("Could not read local features. Exiting.");
				System.exit(-1);
			}
			ArrayList<FrameFeatures> frList = lfr.getMFrameFeaturesList();
			decoding.setData(null, frList, idResult);
			ArrayList<String> argResult = decoding.decodeAll("overlapcheck", 0, true);	
			System.out.println("Arg results:");
			double max = -Double.MAX_VALUE;
			String maxLine = null;
			for (int j = 0; j < argResult.size(); j++) {
				String res = argResult.get(j);
				System.out.println(res);
				String[] toksRes = res.split("\t");
				double score = new Double(toksRes[toksRes.length-1]);
				double totalScore = Math.log(scores[j]) + score;
				if (totalScore > max) {
					maxLine = res;
					max = totalScore;
				}
			}
			String argLine = "";
			String[] toksRes = maxLine.split("\t");
			for (int j = 0; j < toksRes.length - 1; j++) {
				argLine = argLine + toksRes[j] + "\t";
			}
			argLine = argLine.trim();
			argResults.add(argLine);
		}
		System.out.println("End Time:"+(new Date()));
		String feFile = options.frameElementsOutputFile.get();
		ParsePreparation.writeSentencesToTempFile(feFile, argResults);
	}	
	
	public static TObjectDoubleHashMap<String> parseParamFile(String paramsFile)
	{
		TObjectDoubleHashMap<String> startParamList = new TObjectDoubleHashMap<String>(); 
		try {
			BufferedReader fis = new BufferedReader(new FileReader(paramsFile));
			String pattern = null;
			int count = 0;
			while ((pattern = fis.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(pattern.trim(),"\t");
				String paramName = st.nextToken().trim();
				String rest = st.nextToken().trim();
				String[] arr = rest.split(",");
				double value = new Double(arr[0].trim());
				boolean sign = new Boolean(arr[1].trim());
				LDouble val = new LDouble(value,sign);
				startParamList.put(paramName, val.exponentiate());
				if(count%100000==0)
					System.out.println("Processed param number:"+count);
				count++;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return startParamList;
	}
	
	public static ArrayList<String> getGoldSeg(ArrayList<String> segs,int start, int end)
	{
		ArrayList<String> result = new ArrayList<String>();
		int count = 0;
		for(String seg:segs)
		{
			String[] toks = seg.split("\t");
			int sentNum = new Integer(toks[5]);
			if(sentNum<start)
			{
				count++;
				continue;
			}
			if(sentNum>=end)
				break;
			String span = toks[3];
			String line = "Null\t"+span+"\t"+(sentNum-start);
			result.add(line);
			count++;
		}
		return result;
	}
	
	public static String getTokenRepresentation(String tokNum, String parse)
	{
		StringTokenizer st = new StringTokenizer(parse,"\t");
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
		String[] tokNums = tokNum.split("_");
		int[] intTokNums = new int[tokNums.length];
		for(int j = 0; j < tokNums.length; j ++)
			intTokNums[j] = new Integer(tokNums[j]);
		Arrays.sort(intTokNums);
		
		String actualTokens = "";
		String firstTok = "";
		for(int i = 0; i < intTokNums.length; i ++)
		{
			String lexUnit = data[0][intTokNums[i]];
			String pos = data[1][intTokNums[i]];	
			actualTokens+=lexUnit+" ";
			if(i==0)
				firstTok =  lexUnit.toLowerCase()+"."+pos.substring(0,1).toLowerCase();
		}
		actualTokens=actualTokens.trim();
		firstTok=firstTok.trim();
		
		return firstTok+"\t"+actualTokens;
	}
}
