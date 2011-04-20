package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;


public class FilterNewLines
{
	public static void main(String[] args) throws Exception
	{
		String root = "/usr2/dipanjan/experiments/FramenetParsing/FrameStructureExtraction/mstscripts/data";
		String[] infiles = {"semeval.fulltrain.berkeley.parsed", "semeval.fulldev.berkeley.parsed", "semeval.fulltest.berkeley.parsed"};
		String[] outfiles = {"semeval.fulltrain.berkeley.parsed.trimmed", "semeval.fulldev.berkeley.parsed.trimmed", "semeval.fulltest.berkeley.parsed.trimmed"};
		
		for(int i = 0; i < 3; i ++)
		{
			BufferedReader bReader = new BufferedReader(new FileReader(root+"/"+infiles[i]));
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(root+"/"+outfiles[i]));
			String line = null;
			int count=0;
			while((line=bReader.readLine())!=null)
			{
				line=line.trim();
				if(!line.equals(""))
					bWriter.write(line+"\n");
				if(count%1000==0)
					System.out.print(count+" ");
				if(count%10000==0)
					System.out.println();
				count++;
			}
			bReader.close();
			bWriter.close();
		}
		
	}
}