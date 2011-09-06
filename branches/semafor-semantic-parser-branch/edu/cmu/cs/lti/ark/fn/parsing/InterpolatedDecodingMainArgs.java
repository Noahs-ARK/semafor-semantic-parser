/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * DecodingMainArgs.java is part of SEMAFOR 2.0.
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

import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;


public class InterpolatedDecodingMainArgs
{
	public static void main(String[] args)
	{
		InterpolatedDecoding bpd = new InterpolatedDecoding();
		String modelFile = args[0];
		String alphabetFile = args[1];
		String eventsFile = args[2];
		String spanFile = args[3];
		String predictionFile = args[4];
		String frFile = args[5];
		String overlap = args[6];
		String parseFile = args[7];
		double interpolationWeight = new Double(args[8]);
		String graphSpansFile = args[9];
		String[][] toks = getToks(parseFile);
		LocalFeatureReading lfr = new LocalFeatureReading(eventsFile, spanFile, frFile);
		try
		{
			lfr.readLocalFeatures(false, null);
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
				ParsePreparation.readSentencesFromFile(frFile),
				graphSpansFile,
				interpolationWeight
				);
		bpd.setSentences(toks);
		bpd.decodeAll(overlap, 0);		
	}
	
	public static String[][] getToks(String parseFile) {
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		int num = parses.size();
		String[][] toks = new String[num][];
		for (int i = 0; i < num; i++) {
			String[] tokens = parses.get(i).split("\t");
			int numToks = new Integer(tokens[0]);
			toks[i] = new String[numToks];
			for (int j = 1; j < numToks + 1; j++) {
				toks[i][j-1] = tokens[j];  
			}
		}
		return toks;
	}
}