package edu.cmu.cs.lti.ark.fn.wordnet;



import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;


import danbikel.wordnet.Morphy;
import danbikel.wordnet.WordNet;



public class WordnetInteraction
{
	WordNet wn=null;
	Morphy morphy=null;
	
	public WordnetInteraction(String wnFilePath)
	{
		wn = new WordNet(wnFilePath);
		morphy=new Morphy(wn);
	}	
	
	
	public String returnRoot(String word, String POS)
	{
		String wnPOS;
		if(POS.startsWith("V"))
		{
			wnPOS = WordNet.verbPos;
		}
		else if(POS.startsWith("J"))
		{
			wnPOS = WordNet.adjPos;
		}
		else if(POS.startsWith("R"))
		{
			wnPOS = WordNet.advPos;
		}
		else
			wnPOS = WordNet.nounPos;
		
		String[] words=morphy.morphStr(word, wnPOS);
		
		if(words.length==0)
		{
			return word;
		}
		else
		{
			if(words.length>1)
			{
				System.out.println("Verb:"+word+" has more than 1 root.");
			}
			return words[0];
		}
	}
	
}
