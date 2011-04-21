package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;


public class DecodingMainArgs
{
	public static void main(String[] args)
	{
		Decoding bpd = new Decoding();
		String modelFile = args[0];
		String alphabetFile = args[1];
		String eventsFile = args[2];
		String spanFile = args[3];
		String predictionFile = args[4];
		String frFile = args[5];
		String overlap = args[6];
		LocalFeatureReading lfr = new LocalFeatureReading(eventsFile, spanFile, frFile);
		try
		{
			lfr.readLocalFeatures();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		ArrayList<FrameFeatures> list = lfr.getMFrameFeaturesList();
		bpd.init(modelFile, 
				alphabetFile, 
				predictionFile,
				list,
				ParsePreparation.readSentencesFromFile(frFile)
				);
		bpd.decodeAll(overlap, 0);		
	}		
}
