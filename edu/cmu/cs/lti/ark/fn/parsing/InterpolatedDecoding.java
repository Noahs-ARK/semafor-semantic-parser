package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.ArrayList;


public class InterpolatedDecoding extends Decoding {
	
	private GraphSpans mGS;
	
	public InterpolatedDecoding() {
		
	}
	
	public void init(String modelFile, 
			 		 String alphabetFile,
			 		 String predictionFile,
			 		 ArrayList<FrameFeatures> list,
			 		 ArrayList<String> frameLines) {
		super.init(modelFile, 
				  alphabetFile, 
				  predictionFile, 
				  list, 
				  frameLines);
	}
}