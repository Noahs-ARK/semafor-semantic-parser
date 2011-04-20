package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import gnu.trove.THashMap;


public class ParseFixer
{
	public static void main(String[] args)
	{
		String dirname = "/usr2/dipanjan/experiments/FramenetParsing/FrameStructureExtraction/mstscripts/data/splits";
		File dirFile = new File(dirname);
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String name) {
				if(name.startsWith("train")||name.startsWith("dev")||name.startsWith("test"))
					return true;
				else
					return false;
			}
		};
		
		String[] files = dirFile.list(filter);
		
		String outdirname = "/usr2/dipanjan/experiments/FramenetParsing/FrameStructureExtraction/mstscripts/data/revisedsplits";
		for(String file:files)
		{
			String inFile = dirname+"/"+file;
			System.out.println(file);
			ArrayList<String> sentences = ParsePreparation.readSentencesFromFile(inFile);
			ArrayList<String> outSentences = new ArrayList<String>();
			for(String sentence:sentences)
			{
				for(int i = 0; i < POS_TAGS.length; i ++)
				{
					String tag = POS_TAGS[i];					
					sentence=sentence.replace("("+tag+" ))", "("+tag+" -RRB-)");
					sentence=sentence.replace("("+tag+" ()", "("+tag+" -LRB-)");
				}				
				outSentences.add(sentence);
			}
			ParsePreparation.writeSentencesToTempFile(outdirname+"/"+file, outSentences);
		}
		
		
	}
	
	public static final String[] POS_TAGS={
		"#",
		"$",
		"''",
		",",
		"SYM",
		".",
		":",
		"CC",
		"CD",
		"DT",
		"EX",
		"FW",
		"IN",
		"JJ",
		"JJR",
		"JJS",
		"LS",
		"MD",
		"NN",
		"NNP",
		"NNPS",
		"NNS",
		"PDT",
		"POS",
		"PRP",
		"PRP$",
		"RB",
		"RBR",
		"RBS",
		"RP",
		"TO",
		"UH",
		"VB",
		"VBD",
		"VBG",
		"VBN",
		"VBP",
		"VBZ",
		"WDT",
		"WP",
		"WP$",
		"WRB",
		"``",
	};
	
}