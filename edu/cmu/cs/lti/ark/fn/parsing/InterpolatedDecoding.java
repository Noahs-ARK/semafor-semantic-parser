package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import edu.cmu.cs.lti.ark.fn.optimization.LDouble;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;


public class InterpolatedDecoding extends Decoding {
	private GraphSpans mGS;
	private double mIWeight;
	private String[][] mToks;
	private double avg;
	
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
		modifyHeadDist();
		mIWeight = interpolationWeight;
	}
	
	public void modifyHeadDist() {
		int len = mGS.smoothedGraph.length;
		int flen = mGS.smoothedGraph[0].length;
		avg = 0.0;
		for (int i = 0; i < len; i++) {
			double min = Double.MAX_VALUE;
			double max = - Double.MAX_VALUE;
			for (int j = 0; j < flen; j++) {
				if (mGS.smoothedGraph[i][j] > max) {
					max = mGS.smoothedGraph[i][j];
				}
				if (mGS.smoothedGraph[i][j] < min) {
					min = mGS.smoothedGraph[i][j];
				}
			}
			// System.out.println("Max: " + max);
			// System.out.println("Min: " + min);
			for (int j = 0; j < flen; j++) {
				if (max != min) {
					mGS.smoothedGraph[i][j] = mGS.smoothedGraph[i][j] / (float)(max - min);
				}
				avg += mGS.smoothedGraph[i][j] / (float) flen;
			}
			// System.out.println("Average: " + avg);
		}
		avg = avg / (double) len;
		System.out.println("Average component weight: " + avg);
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
			double Z = 0.0;
			for(int j = 0; j < featArrLen; j ++)
			{
				int[] feats = featureArray[j].features;
				double weightSum=getWeightSum(feats, localW);
				double expVal = Math.exp(weightSum);
				Z += expVal;
			}
			String fe = frameElements.get(i);
			int feIndex = Arrays.binarySearch(mGS.sortedFEs, fe);
			for(int j = 0; j < featArrLen; j ++)
			{
				int[] feats = featureArray[j].features;
				double weightSum=getWeightSum(feats, localW);
				double expVal = Math.exp(weightSum);
				double prob = (1 - mIWeight) * expVal / Z;
				int[] span = featureArray[j].span;
				if (span[0] == span[1] && span[0] == -1) {
					// prob += mIWeight * (1.0 / mGS.sortedFEs.length);
					prob += mIWeight * avg;
				} else {
					String stringSpan = getSpan(frameLine, span[0], span[1]);
					int spanIndex = Arrays.binarySearch(mGS.sortedSpans, stringSpan); 
					if (spanIndex >= 0) {
						prob += mIWeight * (mGS.smoothedGraph[spanIndex][feIndex]);
					} else {
						// prob += mIWeight * (1.0 / mGS.sortedFEs.length);
						prob += mIWeight * avg;
					}
				}
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
	
	public String getNonOverlappingDecision(FrameFeatures mFF, String frameLine, int offset)
	{
		String frameName = mFF.frameName;
		System.out.println("Frame:"+frameName);
		String decisionLine=getInitialDecisionLine(frameLine, offset);
		if(mFF.fElements.size()==0)
		{
			decisionLine="0\t1"+"\t"+decisionLine.trim();
			return decisionLine;
		}
		if(mFF.fElements.size()==0)
		{
			decisionLine="0\t1"+"\t"+decisionLine.trim();
			return decisionLine;
		}
		THashMap<String,String> feMap = new THashMap<String,String>();
		ArrayList<SpanAndCorrespondingFeatures[]> featsList = mFF.fElementSpansAndFeatures;
		ArrayList<String> frameElements = mFF.fElements;
		int listSize = featsList.size();
		for(int i = 0; i < listSize; i ++)
		{
			SpanAndCorrespondingFeatures[] featureArray = featsList.get(i);
			int featArrLen = featureArray.length;
			int maxIndex = -1;
			double maxProb = -Double.MAX_VALUE;
			double Z = 0.0;
			for(int j = 0; j < featArrLen; j ++)
			{
				int[] feats = featureArray[j].features;
				double weightSum=getWeightSum(feats, localW);
				double expVal = Math.exp(weightSum);
				Z += expVal;
			}
			String fe = frameElements.get(i);
			int feIndex = Arrays.binarySearch(mGS.sortedFEs, fe);
			for(int j = 0; j < featArrLen; j ++)
			{
				int[] feats = featureArray[j].features;
				double weightSum=getWeightSum(feats, localW);
				double expVal = Math.exp(weightSum);
				double prob = (1 - mIWeight) * expVal / Z;
				int[] span = featureArray[j].span;
				if (span[0] == span[1] && span[0] == -1) {
					// prob += mIWeight * (1.0 / mGS.sortedFEs.length);
					prob += mIWeight * avg;
				} else {
					String stringSpan = getSpan(frameLine, span[0], span[1]);
					int spanIndex = Arrays.binarySearch(mGS.sortedSpans, stringSpan); 
					if (spanIndex >= 0) {
						prob += mIWeight * (mGS.smoothedGraph[spanIndex][feIndex]);
					} else {
						// prob += mIWeight * (1.0 / mGS.sortedFEs.length);
						prob += mIWeight * avg;
					}
				}
				if (prob > maxProb) {
					maxProb = prob;
					maxIndex = j;
				}
			}
			String outcome = featureArray[maxIndex].span[0]+"_"+featureArray[maxIndex].span[1];
			feMap.put(frameElements.get(i), outcome);
			System.out.println("Frame element:"+frameElements.get(i)+" Found span:"+outcome);
		}
		THashMap<String,String> oMap = getOnlyOverlappingFes(feMap);
		if(oMap.size()>0)
		{
			Set<String> tempKeySet = oMap.keySet();
			for(String key:tempKeySet)
			{
				System.out.println(key+"\t"+oMap.get(key));
			}
		}
		THashSet<String> seenSpans = new THashSet<String>();
		Set<String> keySet = feMap.keySet();
		for(String key:keySet)
		{
			String span = feMap.get(key);
			if(span.equals("-1_-1"))
				continue;
			if(!oMap.contains(key))
				seenSpans.add(span);
		}		
		if(seenSpans.size()>0)
			System.out.println("yes");		
		THashMap<String,THashMap<String,LDouble>> vs = new THashMap<String,THashMap<String,LDouble>>();
		for(int i = 0; i < listSize; i ++)
		{
			String fe = frameElements.get(i);
			if(!oMap.contains(fe))
				continue;
			SpanAndCorrespondingFeatures[] featureArray = featsList.get(i);
			THashMap<String,LDouble> valMap = new THashMap<String,LDouble>();
			int featArrLen = featureArray.length;
			double Z = 0.0;
			for(int j = 0; j < featArrLen; j ++)
			{
				int[] feats = featureArray[j].features;
				double weightSum=getWeightSum(feats, localW);
				double expVal = Math.exp(weightSum);
				Z += expVal;
			}
			int feIndex = Arrays.binarySearch(mGS.sortedFEs, fe);
			for(int j = 0; j < featArrLen; j ++)
			{
				int[] feats = featureArray[j].features;
				double weightSum=getWeightSum(feats, localW);
				double expVal = Math.exp(weightSum);
				double prob = (1 - mIWeight) * expVal / Z;
				int[] span = featureArray[j].span;
				if (span[0] == span[1] && span[0] == -1) {
					prob += mIWeight * avg;
				} else {
					String stringSpan = getSpan(frameLine, span[0], span[1]);
					int spanIndex = Arrays.binarySearch(mGS.sortedSpans, stringSpan); 
					if (spanIndex >= 0) {
						prob += mIWeight * (mGS.smoothedGraph[spanIndex][feIndex]);
					} else {
						prob += mIWeight * avg;
					}
				}
				LDouble lVal = LDouble.convertToLogDomain(prob);
				String stringSpan = featureArray[j].span[0]+"_"+featureArray[j].span[1];
				valMap.put(stringSpan, lVal);
			}
			vs.put(frameElements.get(i), valMap);
		}				
		THashMap<String,String> nonOMap = getCubePruningDecoding(oMap, mFF.fElements, vs, 10000, seenSpans);
		keySet = nonOMap.keySet();
		for(String key:keySet) {
			feMap.put(key, nonOMap.get(key));
		}		
		keySet = feMap.keySet();
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
}