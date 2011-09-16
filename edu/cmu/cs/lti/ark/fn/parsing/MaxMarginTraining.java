/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * Training.java is part of SEMAFOR 2.0.
 * 
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along
 * with SEMAFOR 2.0.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Date;

import riso.numerical.LBFGS;


import de.saar.coli.salsa.reiter.framenet.FrameNet;
import edu.cmu.cs.lti.ark.fn.constants.FNConstants;
import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.optimization.SGA;
import edu.cmu.cs.lti.ark.fn.utils.ThreadPool;
import edu.cmu.cs.lti.ark.util.FileUtil;
import edu.cmu.cs.lti.ark.util.ds.Pair;

public class MaxMarginTraining
{
	private String mModelFile;
	private String mAlphabetFile;
	private ArrayList<FrameFeatures> mFrameList; 
	private FrameNet mFNInstance;
	private int numFeatures;
	private ArrayList<String> mFrameLines;
	private int numDataPoints;
	private int mNumThreads;
	private double[][] tGradients;
	private double[] tValues;
	private Parameters params;
	private JointDecoding mJd;
	
	public MaxMarginTraining()
	{

	}
	
	public void init(String modelFile, 
			String alphabetFile, 
			ArrayList<FrameFeatures> list, 
			String lexiconFile,
			String frFile,
			String binaryFactorPresent)
	{
		mModelFile = modelFile;
		mAlphabetFile = alphabetFile;
		initModel();
		mFrameList = list;
		mFrameLines = ParsePreparation.readSentencesFromFile(frFile);
		numDataPoints = mFrameList.size();
		mNumThreads = 1;
		mJd = new JointDecoding();
	}

	public void init(String modelFile, 
			String alphabetFile, 
			ArrayList<FrameFeatures> list, 
			String frFile,
			String binaryFactorPresent,
			int numThreads)
	{
		mModelFile = modelFile;
		mAlphabetFile = alphabetFile;
		initModel();
		mFrameList = list;
		mFrameLines = ParsePreparation.readSentencesFromFile(frFile);
		numDataPoints = mFrameList.size();
		mNumThreads = numThreads;
		mJd = new JointDecoding();
	}
	
	private void initModel()
	{
		Scanner localsc = FileUtil.openInFile(mAlphabetFile);
		numFeatures = localsc.nextInt() + 1;
		localsc.close();
		params = new Parameters(numFeatures);
	}	
	
	public void trainingIter(int iter) {
		for (int i = 0; i < mFrameList.size(); i++) {
			if((i+1) % 200 == 0) {
                System.out.print((i+1)+",");
			}
			FrameFeatures ff = mFrameList.get(i);
			Map<String, String> map = 
				mJd.getNonOverlappingDecision(ff, mFrameLines.get(i), 0, params.parameters, true);
						
		}
	}
	
	public void train(int numIters) {
		for (int i = 0; i < numIters; i++) {
			System.out.print(" Iteration "+i);
            System.out.print("[");
            long start = System.currentTimeMillis();
            trainingIter(i+1);
            long end = System.currentTimeMillis();
            System.out.println("|Time:"+(end-start)+"]");
		}
		params.averageParams(numIters*mFrameList.size());
		writeModel(mModelFile);
	}
	
	public void writeModel(String modelFile) {
		PrintStream ps = FileUtil.openOutFile(modelFile);
		// ps.println(w[0]);
		System.out.println("Writing Model... ...");
		// for (String key : paramIndex.keySet()) {\
		for (int i = 0; i < params.parameters.length; i++) {
			// ps.println(key + "\t" + w[paramIndex.get(key)]);
			ps.println(params.parameters[i]);
		}
		System.out.println("Finished Writing Model");
		ps.close();
	}
	
	public void writeModel() {
		PrintStream ps = FileUtil.openOutFile(mModelFile);
		// ps.println(w[0]);
		System.out.println("Writing Model... ...");
		// for (String key : paramIndex.keySet()) {\
		for (int i = 0; i < params.parameters.length; i++) {
			// ps.println(key + "\t" + w[paramIndex.get(key)]);
			ps.println(params.parameters[i]);
		}
		System.out.println("Finished Writing Model");
		ps.close();
	}
}
