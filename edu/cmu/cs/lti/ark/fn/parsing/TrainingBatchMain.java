package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.util.SerializedObjects;

public class TrainingBatchMain
{
	public static void main(String[] args)
	{
		FNModelOptions opts = new FNModelOptions(args);
		String modelFile = opts.modelFile.get();
		String alphabetFile = opts.alphabetFile.get();
		String frameFeaturesCacheFile = opts.frameFeaturesCacheFile.get();
		String frFile = opts.trainFrameFile.get();
		String reg = opts.reg.get();
		double lambda = opts.lambda.get();
		int numThreads = opts.numThreads.get();
		String binaryFactorPresent = opts.binaryOverlapConstraint.get();
		ArrayList<FrameFeatures> list = (ArrayList<FrameFeatures>)SerializedObjects.readSerializedObject(frameFeaturesCacheFile);
		Training bpt = new Training();
		bpt.init(modelFile, alphabetFile, list, frFile, binaryFactorPresent, reg, lambda, numThreads);
		bpt.trainBatch();
	}	
}