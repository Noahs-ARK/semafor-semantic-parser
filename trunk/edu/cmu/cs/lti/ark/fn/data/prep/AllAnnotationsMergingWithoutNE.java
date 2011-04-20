package edu.cmu.cs.lti.ark.fn.data.prep;

import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.utils.LemmatizeStuff;

public class AllAnnotationsMergingWithoutNE
{
	public static void main(String[] args)
	{
		ArrayList<String> tokenizedSentences = ParsePreparation.readSentencesFromFile(args[0]);
		ArrayList<String> neSentences = findDummyNESentences(tokenizedSentences);
		ArrayList<ArrayList<String>> parses = OneLineDataCreation.readCoNLLParses(args[1]);
		ArrayList<String> perSentenceParses=OneLineDataCreation.getPerSentenceParses(parses,tokenizedSentences,neSentences);
		ParsePreparation.writeSentencesToTempFile(args[2], perSentenceParses);
		LemmatizeStuff.lemmatize(args[3], args[4], args[2], args[5]);
	}
	
	public static ArrayList<String> findDummyNESentences(ArrayList<String> tokenizedSentences)
	{
		ArrayList<String> res = new ArrayList<String>();
		for(String sent:tokenizedSentences)
		{
			StringTokenizer st = new StringTokenizer(sent.trim());
			String resSent = "";
			while(st.hasMoreTokens())
			{
				resSent+=st.nextToken()+"_O"+" ";
			}
			resSent=resSent.trim();
			res.add(resSent);
		}
		return res;
	}
} 