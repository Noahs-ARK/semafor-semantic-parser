package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.parsing.ScanPotentialSpans;

public class GraphCoverage {

	public static final String DATA_DIR = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012";

	public static void main(String[] args) {
		checkSpanCoverage();
	}

	public static void checkSpanCoverage() {
		String devFEFile = DATA_DIR + "/cv.dev.sentences.frame.elements";
		String devParseFile = DATA_DIR + "/cv.dev.sentences.all.lemma.tags";
		String graphSpansFile = DATA_DIR + "/all.spans.sorted";
		String[] spanArr = readSpansFile(graphSpansFile);
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(devParseFile);
		ArrayList<String> feLines = ParsePreparation.readSentencesFromFile(devFEFile);
		float total = 0;
		float found = 0;
		for (String fe: feLines) {
			String[] feToks = fe.trim().split("\t");
			int sentNum = new Integer(feToks[5]);
			StringTokenizer st = new StringTokenizer(parses.get(sentNum),"\t");
			int tokensInFirstSent = new Integer(st.nextToken());
			String[][] data = new String[5][tokensInFirstSent];
			for(int k = 0; k < 5; k ++) {
				data[k]=new String[tokensInFirstSent];
				for(int l = 0; l < tokensInFirstSent; l++) {
					String tok = st.nextToken().trim();
					if (k == 0) {
						if (tok.equals("-LRB-")) { tok = "("; }
						if (tok.equals("-RRB-")) { tok = ")"; }
						if (tok.equals("-RSB-")) { tok = "]"; }
						if (tok.equals("-LSB-")) { tok = "["; }
						if (tok.equals("-LCB-")) { tok = "{"; }
						if (tok.equals("-RCB-")) { tok = "}"; }
					}
					data[k][l]=""+tok;
				}
			}	
			for(int k = 6; k < feToks.length; k = k + 2) {
				String[] spanS = feToks[k+1].split(":");
				int start = -1;
				int end = -1;
				if(spanS.length==1) {
					start=new Integer(spanS[0]);
					end=new Integer(spanS[0]);
				}
				else {
					start=new Integer(spanS[0]);
					end=new Integer(spanS[1]);
				}
				String span = "";
				for (int m = start; m <= end; m++) {
					span += data[0][m] + " ";
				}
				span = span.toLowerCase();
				span = ScanPotentialSpans.replaceNumbersWithAt(span.trim());
				if (Arrays.binarySearch(spanArr, span) >= 0) {
					found++;
				}
				total++;
			}
		}
		System.out.println("Total number of gold spans: " + total);
		System.out.println("Total number of matched spans: " + found);
	}	

	public static String[] readSpansFile(String spansFile) {
		String[] sortedSpans;
		int count = 0;
		System.out.println("Reading spans file...");
		String line = null;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(spansFile));
			while ((line = bReader.readLine()) != null) {
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			System.out.println("Could not read file: " + spansFile);
			System.exit(-1);
		}
		System.out.println("Total number of spans: " + count);
		sortedSpans = new String[count];
		count = 0;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(spansFile));
			while ((line = bReader.readLine()) != null) {
				line = line.trim();
				sortedSpans[count] = line;
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			System.out.println("Could not read file: " + spansFile);
			System.exit(-1);
		}
		System.out.println("Stored spans.");
		return sortedSpans;
	}
}