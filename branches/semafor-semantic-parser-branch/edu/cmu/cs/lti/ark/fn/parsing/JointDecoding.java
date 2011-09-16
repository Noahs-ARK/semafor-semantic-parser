package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.Map;

import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import gnu.trove.THashMap;

public class JointDecoding extends Decoding {
	
	private boolean mIgnoreNullSpansWhileJointDecoding;
	private ILPDecoding ilpd = null;
	
	public JointDecoding()
	{
		ilpd = new ILPDecoding();
	}
	
	public void wrapUp() {
		ilpd.end();
	}
	
	public void init(String modelFile, 
					 String alphabetFile,
					 String predictionFile,
					 ArrayList<FrameFeatures> list,
					 ArrayList<String> frameLines)
	{
		super.init(modelFile, alphabetFile, predictionFile, list, frameLines);
		mIgnoreNullSpansWhileJointDecoding = true;
	}
	
	public void init(String modelFile, 
			 String alphabetFile,
			 String predictionFile,
			 ArrayList<FrameFeatures> list,
			 ArrayList<String> frameLines,
			 boolean ignoreNullSpansWhileJointDecoding)
	{
		super.init(modelFile, alphabetFile, predictionFile, list, frameLines);
		mIgnoreNullSpansWhileJointDecoding = ignoreNullSpansWhileJointDecoding;
	}	
	
	public String getNonOverlappingDecision(FrameFeatures mFF, 
			String frameLine, 
			int offset) {
		return getNonOverlappingDecision(
				mFF, 
				frameLine, 
				offset, 
				localW);
	}
	
	public String getNonOverlappingDecision(FrameFeatures mFF, 
											String frameLine, 
											int offset, double[] w) {
		String frameName = mFF.frameName;
		System.out.println("Frame:"+frameName);
		String decisionLine=getInitialDecisionLine(frameLine, offset);
		if(mFF.fElements.size()==0) {
			decisionLine="0\t1"+"\t"+decisionLine.trim();
			return decisionLine;
		}
		if(mFF.fElements.size()==0) {
			decisionLine="0\t1"+"\t"+decisionLine.trim();
			return decisionLine;
		}
		WeightComparator wc = new WeightComparator();
		ArrayList<SpanAndCorrespondingFeatures[]> featsList = mFF.fElementSpansAndFeatures;
		ArrayList<String> frameElements = mFF.fElements;
		int listSize = featsList.size();
		// maps each FE to a list of spans and their corresponding scores
		Map<String,Pair<int[], Double>[]> vs = 
			new THashMap<String,Pair<int[], Double>[]>();
		for(int i = 0; i < listSize; i ++) {
			SpanAndCorrespondingFeatures[] featureArray = featsList.get(i);
			int featArrLen = featureArray.length;
			Pair<int[], Double>[] arr = new Pair[featArrLen];
			double maxProb = -Double.MAX_VALUE;
			String outcome = null;
			for(int j = 0; j < featArrLen; j ++) {
				int[] feats = featureArray[j].features;
				double weightFeatSum = getWeightSum(feats, w);
				arr[j] = new Pair<int[], Double>(featureArray[j].span, weightFeatSum);
				if (weightFeatSum > maxProb) {
					maxProb = weightFeatSum;
					outcome = featureArray[j].span[0]+"_"+featureArray[j].span[1];
				}
			}			
			// null span is the best span
			if (outcome.equals("-1_-1")) {
				if (!mIgnoreNullSpansWhileJointDecoding) {
					vs.put(frameElements.get(i), arr);
				}
			} else {
				vs.put(frameElements.get(i), arr);
			}
			System.out.println("Frame element:"+frameElements.get(i)+" Found span:"+outcome);
		}
		// vs is the set of FEs on which joint decoding has to be done
		Map<String, String> feMap = ilpd.decode(vs, frameName);
		Set<String> keySet = feMap.keySet();
		int count = 1;
		for(String fe:keySet)
		{
			String outcome = feMap.get(fe);
			if(outcome.equals("-1_-1"))
				continue;
			count++;
			String[] ocToks = outcome.split("_");
			String modToks;
			if(ocToks[0].equals(ocToks[1]))
			{
				modToks=ocToks[0];
			}
			else
			{
				modToks=ocToks[0]+":"+ocToks[1];
			}
			decisionLine+=fe+"\t"+modToks+"\t";
		}		
		decisionLine="0\t"+count+"\t"+decisionLine.trim();
		System.out.println(decisionLine);
		return decisionLine;
	}

	public void setMaps(String requiresMap, String excludesMap) {
		Map<String, Set<Pair<String, String>>> exclusionMap = 
			(Map<String, Set<Pair<String, String>>>) SerializedObjects.readSerializedObject(excludesMap);
		Map<String, Set<Pair<String, String>>> requiresMapObj = 
			(Map<String, Set<Pair<String, String>>>) SerializedObjects.readSerializedObject(requiresMap);
		ilpd.setMaps(exclusionMap, requiresMapObj);
	}
}


class WeightComparator implements Comparator<Pair<int[], Double>> {
	public int compare(Pair<int[], Double> o1, Pair<int[], Double> o2) {
		if (o1.getSecond() > o2.getSecond()) {
			return -1;
		} else if (o1.getSecond() == o1.getSecond()) {
			return 0;
		} else {
			return 1;
		}
	}
}