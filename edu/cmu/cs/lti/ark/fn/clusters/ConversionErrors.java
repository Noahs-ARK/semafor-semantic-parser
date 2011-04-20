package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.*;
import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;

public class ConversionErrors
{
	public static ArrayList<String> seenFiles = new ArrayList<String>();
	static
	{
		seenFiles.add("54.out");
	}
	
	public static void main(String[] args)
	{
		String rootDir = "/mal2/dipanjan/experiments/FramenetParsing/FrameStructureExtraction/mstscripts";
		String outFolder = rootDir+"/out";
		String errFolder = rootDir+"/error";
		
		File errDir = new File(errFolder);
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith(".out");
			}
		};
		String[] files = errDir.list(filter);
		for(String file:files)
		{
			String filename = errFolder+"/"+file;
			ArrayList<String> lines = ParsePreparation.readSentencesFromFile(filename);
			boolean errFlag = false;
			for(String line:lines)
			{
				if(line.contains("java"))
				{
					errFlag=true;
					System.out.println(line);
				}
				if(errFlag)
				{
					String outFile=outFolder+"/"+file;
					ArrayList<String> outLines = ParsePreparation.readSentencesFromFile(outFile);
					for(String ol:outLines)
						System.out.println(ol);
					System.out.println();
					//System.exit(0);
				}
			}
		}
		
	}
	
	
}