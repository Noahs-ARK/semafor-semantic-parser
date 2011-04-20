package edu.cmu.cs.lti.ark.fn.evaluation;

import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashSet;

public class PrintAllRelatedWords {
	public static void main(String[] args) {
		int split = 1;
		String serFile = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/CVSplits/" + split +"/allrelatedwords.ser";
		String txtFile = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/CVSplits/" + split +"/allrelatedwords.txt";
		THashSet<String> set = (THashSet<String>) SerializedObjects.readSerializedObject(serFile);
		ArrayList<String> list = new ArrayList<String>(set);
		ParsePreparation.writeSentencesToTempFile(txtFile, list);
	} 
}