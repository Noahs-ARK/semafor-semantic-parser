package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.util.SerializedObjects;


public class FrameFeaturesCache
{
	public static void main(String[] args)
	{
		FNModelOptions opts = new FNModelOptions(args);
		String eventsFile = opts.eventsFile.get();
		String spanFile = opts.spansFile.get();
		String frFile = opts.trainFrameFile.get();
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
		SerializedObjects.writeSerializedObject(list, opts.frameFeaturesCacheFile.get());
	}	
}