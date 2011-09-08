package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;


public class InterpolatedDecodingWithHeads extends Decoding {
	private float[][] mHeadDist;
	private String[] mSortedUniqueHeads;
	private String[] mSortedFEs;
	private double mIWeight;
	private DependencyParse[][] mParses;
	private double avg;
	
	public InterpolatedDecodingWithHeads() {
		
	}
	
	public void init(String modelFile, 
			 		 String alphabetFile,
			 		 String predictionFile,
			 		 ArrayList<FrameFeatures> list,
			 		 ArrayList<String> frameLines,
			 		 String headsSerFile,
			 		 String headsFile,
			 		 String feFile,
			 		 double interpolationWeight) {
		super.init(modelFile, 
				  alphabetFile, 
				  predictionFile, 
				  list, 
				  frameLines);
		System.out.println("Reading sorted heads...");
		mSortedUniqueHeads = CoarseDistributions.getSortedUniqueHeads(headsFile);
		System.out.println("Reading distributions over heads " + headsSerFile);
		mHeadDist = (float[][]) SerializedObjects.readSerializedObject(headsSerFile);
		System.out.println("Reading sorted FEs");
		readFEFile(feFile); 
		modifyHeadDist();
		mIWeight = interpolationWeight;
	}
	
	public void modifyHeadDist() {
		int len = mHeadDist.length;
		avg = 0.0;
		for (int i = 0; i < len; i++) {
			double min = Double.MAX_VALUE;
			double max = - Double.MAX_VALUE;
			boolean flag1 = false;
			boolean flag2 = false;
			for (int j = 0; j < mSortedFEs.length; j++) {
				if (mHeadDist[i][j] > max) {
					max = mHeadDist[i][j];
					flag1 = true;
				}
				if (mHeadDist[i][j] < min) {
					min = mHeadDist[i][j];
					flag2 = true;
				}
			}
			System.out.println("Max: " + max);
			System.out.println("Min: " + min);
			if (!flag1 && !flag2) {
				System.out.println("Problem with distribution.");
				for (int j = 0; j < mSortedFEs.length; j++) {
					System.out.println(mHeadDist[i][j]);
				}
				System.exit(-1);
			}
			for (int j = 0; j < mSortedFEs.length; j++) {
				if (max != min) {
					 mHeadDist[i][j] = mHeadDist[i][j] / (float)(max - min);
				}
			}
			avg += ((max - min) / 2.0);
			System.out.println("Average: " + avg);
		}
		avg = avg / (double) len;
		System.out.println("Average component weight: " + avg);
		if (true) {
			System.exit(-1);
		}
	}
	
	public void readFEFile(String feFile) {
		int count = 0;
		System.out.println("Reading fe file...");
		String line = null;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(feFile));
			while ((line = bReader.readLine()) != null) {
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			System.out.println("Could not read file: " + feFile);
			System.exit(-1);
		}
		System.out.println("Total number of fes: " + count);
		mSortedFEs = new String[count];
		count = 0;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(feFile));
			while ((line = bReader.readLine()) != null) {
				line = line.trim();
				mSortedFEs[count] = line;
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			System.out.println("Could not read file: " + feFile);
			System.exit(-1);
		}
		System.out.println("Stored FEs.");
	}
	
	public void setParses(DependencyParse[][] parses) {
		mParses = parses;
	}
	
	public String getHead(String frameLine, int istart, int iend) {
		String[] frameToks = frameLine.split("\t");
		int sentNum = new Integer(frameToks[5]);
		int[] tokNums = new int[iend - istart + 1];
		for (int m = istart; m <= iend; m++) {
			tokNums[m-istart] = m;
		}
		DependencyParse head = DependencyParse.getHeuristicHead(mParses[sentNum], tokNums);
		String hw = head.getWord().toLowerCase();
		hw = ScanPotentialSpans.replaceNumbersWithAt(hw);
		return hw;
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
				double weightSum=getWeightSum(feats);
				double expVal = Math.exp(weightSum);
				Z += expVal;
			}
			String fe = frameElements.get(i);
			int feIndex = Arrays.binarySearch(mSortedFEs, fe);
			for(int j = 0; j < featArrLen; j ++)
			{
				int[] feats = featureArray[j].features;
				double weightSum=getWeightSum(feats);
				double expVal = Math.exp(weightSum);
				double prob = (1 - mIWeight) * expVal / Z;
				int[] span = featureArray[j].span;
				if (span[0] == span[1] && span[0] == -1) {
					// prob += mIWeight * (1.0 / mSortedFEs.length);
					prob += mIWeight * avg;
				} else {
					String head = getHead(frameLine, span[0], span[1]);
					int headIndex = Arrays.binarySearch(mSortedUniqueHeads, head); 
					if (headIndex >= 0) {
						prob += mIWeight * (mHeadDist[headIndex][feIndex]);
					} else {
						// prob += mIWeight * (1.0 / mSortedFEs.length);
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
}