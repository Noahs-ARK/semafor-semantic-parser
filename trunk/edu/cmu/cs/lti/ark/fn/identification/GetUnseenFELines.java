package edu.cmu.cs.lti.ark.fn.identification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class GetUnseenFELines {
	public static void main(String[] args)	{
		FNModelOptions options = new FNModelOptions(args);
		ArrayList<String> parses = new ArrayList<String>();
		int count = 0;
		ArrayList<String> orgSentenceLines = new ArrayList<String>();
		try
		{
			BufferedReader inParses = new BufferedReader(new FileReader(options.testParseFile.get()));
			BufferedReader inOrgSentences = new BufferedReader(new FileReader(options.testTokenizedFile.get()));
			String line = null;
			while((line=inParses.readLine())!=null)
			{
				String line2 = inOrgSentences.readLine().trim();
				parses.add(line.trim());
				orgSentenceLines.add(line2);
				count++;
			}				
			inParses.close();
			inOrgSentences.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
		RequiredDataForFrameIdentification r = (RequiredDataForFrameIdentification)SerializedObjects.readSerializedObject(options.fnIdReqDataFile.get());
		THashMap<String,THashSet<String>> cMap = r.getcMap();
		ArrayList<String> segs = ParsePreparation.readSentencesFromFile(options.testFrameFile.get());
		for (String sent: segs) {
			String[] toks = sent.trim().split("\t");
			String[] tokNums = toks[3].split("_");
			int[] intTokNums = new int[tokNums.length];
			for(int j = 0; j < tokNums.length; j ++)
				intTokNums[j] = new Integer(tokNums[j]);
			Arrays.sort(intTokNums);
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
			Set<String> set = checkPresenceOfTokensInMap(intTokNums,data,cMap);
			if (set==null) {
				System.err.println(sent);
			}
		}
	}
	
	public static Set<String> checkPresenceOfTokensInMap(int[] intTokNums, String[][] data, THashMap<String,THashSet<String>> mHvCorrespondenceMap)
	{
		String lemmatizedTokens = "";
		for(int i = 0; i < intTokNums.length; i ++)
		{
			String lexUnit = data[0][intTokNums[i]];
			String pos = data[1][intTokNums[i]];
			//lemmatizedTokens+=mWNR.getLemmaForWord(lexUnit, pos).toLowerCase()+" ";
			lemmatizedTokens+=data[5][intTokNums[i]]+" ";
		}
		lemmatizedTokens=lemmatizedTokens.trim();
		return mHvCorrespondenceMap.get(lemmatizedTokens);
	}
} 