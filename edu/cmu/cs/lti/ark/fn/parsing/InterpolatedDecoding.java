package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;
import java.util.Arrays;

import edu.cmu.cs.lti.ark.util.SerializedObjects;


public class InterpolatedDecoding extends Decoding {
	private GraphSpans mGS;
	private double mIWeight;
	private String[][] mToks;
	
	public InterpolatedDecoding() {
		
	}
	
	public void init(String modelFile, 
			 		 String alphabetFile,
			 		 String predictionFile,
			 		 ArrayList<FrameFeatures> list,
			 		 ArrayList<String> frameLines,
			 		 String graphSpansFile,
			 		 double interpolationWeight) {
		super.init(modelFile, 
				  alphabetFile, 
				  predictionFile, 
				  list, 
				  frameLines);
		System.out.println("Reading graph spans file from: " + graphSpansFile);
		mGS = (GraphSpans) SerializedObjects.readSerializedObject(graphSpansFile);
		System.out.println("Finished reading graph spans file.");
		mIWeight = interpolationWeight;
	}
	
	public void setSentences(String[][] toks) {
		mToks = toks;
	}
	
	public String getSpan(String frameLine, int istart, int iend) {
		String[] frameToks = frameLine.split("\t");
		int sentNum = new Integer(frameToks[5]);
		String span = "";
		for (int i = istart; i <= iend; i++) {
			String tok = mToks[sentNum][i];
			if (tok.equals("-LRB-")) { tok = "("; }
			if (tok.equals("-RRB-")) { tok = ")"; }
			if (tok.equals("-RSB-")) { tok = "]"; }
			if (tok.equals("-LSB-")) { tok = "["; }
			if (tok.equals("-LCB-")) { tok = "{"; }
			if (tok.equals("-RCB-")) { tok = "}"; }
			span += tok + " ";
		}
		span = span.trim().toLowerCase();
		span = ScanPotentialSpans.replaceNumbersWithAt(span.trim());
		return span;
	}
	
	public String getDecision(FrameFeatures mFF, 
							  String frameLine, 
							  int offset)
	{
		String frameName = mFF.frameName;
		System.out.println("Frame:"+frameName);
		String decisionLine=getInitialDecisionLine(frameLine, offset);
		if(mFF.fElements.size()==0)
		{
			decisionLine="0\t1"+"\t"+decisionLine.trim();
			return decisionLine;
		}
		int count = 1;
		ArrayList<SpanAndCorrespondingFeatures[]> featsList = mFF.fElementSpansAndFeatures;
		ArrayList<String> frameElements = mFF.fElements;
		int listSize = featsList.size();
		for(int i = 0; i < listSize; i ++)
		{
			SpanAndCorrespondingFeatures[] featureArray = featsList.get(i);
			int featArrLen = featureArray.length;
			int maxIndex = -1;
			double maxProb = -Double.MAX_VALUE;
			double Z1 = 0.0;
			String fe = frameElements.get(i);
			int feIndex = Arrays.binarySearch(mGS.sortedFEs, fe);
			double Z2 = 0.0;
			for(int j = 0; j < featArrLen; j ++) {
				int[] feats = featureArray[j].features;
				double weightSum1 =getWeightSum(feats);
				double expVal1 = Math.exp(weightSum1);
				Z1 += expVal1;
				int[] span = featureArray[j].span;
				double weightSum2 = 0.0;
				if (span[0] == span[1] && span[0] == -1) {
					weightSum2 = 1.0 / mGS.sortedFEs.length;
				} else {
					String stringSpan = getSpan(frameLine, span[0], span[1]);
					int spanIndex = Arrays.binarySearch(mGS.sortedSpans, stringSpan); 
					if (spanIndex >= 0) {
						weightSum2 = mGS.smoothedGraph[spanIndex][feIndex];
					} else {
						weightSum2 = 1.0 / mGS.sortedFEs.length;
					}
				}
				double expVal2 = Math.exp(weightSum2);
				Z2 += expVal2;
			}
			for(int j = 0; j < featArrLen; j ++) {
				int[] feats = featureArray[j].features;
				double weightSum1 =getWeightSum(feats);
				double expVal1 = Math.exp(weightSum1);
				double prob1 = expVal1 / Z1;
				int[] span = featureArray[j].span;
				double weightSum2 = 0.0;
				if (span[0] == span[1] && span[0] == -1) {
					weightSum2 = 1.0 / mGS.sortedFEs.length;
				} else {
					String stringSpan = getSpan(frameLine, span[0], span[1]);
					int spanIndex = Arrays.binarySearch(mGS.sortedSpans, stringSpan); 
					if (spanIndex >= 0) {
						weightSum2 = mGS.smoothedGraph[spanIndex][feIndex];
					} else {
						weightSum2 = 1.0 / mGS.sortedFEs.length;
					}
				}
				double expVal2 = Math.exp(weightSum2);
				double prob2 = expVal2 / Z2;
				double prob = (1 - mIWeight) * prob1 + mIWeight * prob2;
				if (prob > maxProb) {
					maxProb = prob;
					maxIndex = j;
				}
			}			
			String maxSpan=featureArray[maxIndex].span[0]+"_"+featureArray[maxIndex].span[1];		
			System.out.println("Frame element:"+fe+" Found span:"+maxSpan);
			if(maxSpan.equals("-1_-1"))
				continue;
			count++;
			String[] ocToks = maxSpan.split("_");
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
		return decisionLine;
	}	
}