package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.util.SerializedObjects;

public class FeatureASCIIConversion {
	public static void main(String[] args) throws IOException {
		FNModelOptions opts = new FNModelOptions(args);
		String frameFeaturesCacheFile = opts.frameFeaturesCacheFile.get();
		String eventsFilePrefix = opts.eventsFile.get();	
		boolean separateFiles = opts.separateEventFiles.get();
		int size;
		ArrayList<FrameFeatures> list = new ArrayList<FrameFeatures>();
		if (!separateFiles) {
			list = (ArrayList<FrameFeatures>)SerializedObjects.readSerializedObject(frameFeaturesCacheFile);
			size = list.size();
		} else {
			size = opts.dataSize.get();
		}
		for (int i = 0; i < size; i++) {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(eventsFilePrefix + "." + i));
			FrameFeatures f = null;
			if (!separateFiles) {
				f = list.get(i);
			} else {
				f = (FrameFeatures)SerializedObjects.readSerializedObject(frameFeaturesCacheFile + "." + i);
			}
			ArrayList<SpanAndCorrespondingFeatures[]> featsList = f.fElementSpansAndFeatures;
			ArrayList<Integer> goldSpans = f.fGoldSpans;
			int fsize = featsList.size();
			bWriter.write(fsize+"\n");
			for(int j = 0; j < fsize; j++)
			{
				SpanAndCorrespondingFeatures[] featureArray = featsList.get(j);
				int goldSpan = goldSpans.get(j);
				bWriter.write(goldSpan+"\n");
				int featArrLen = featureArray.length;
				bWriter.write(featArrLen+"\n");
				for(int k = 0; k < featArrLen; k++) {
					String outLine = "";
					int[] feats = featureArray[k].features;
					bWriter.write(feats.length + "\n");
					for (int l = 0; l < feats.length; l++) {
						outLine += feats[l] + " ";
					}
					outLine = outLine.trim();
					bWriter.write(outLine+"\n");
				}
			}
			bWriter.close();
			System.out.println("Done with: " + i);
		}		
	}		
}
