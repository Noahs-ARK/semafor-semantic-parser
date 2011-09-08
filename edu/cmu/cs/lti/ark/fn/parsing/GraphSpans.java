package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import edu.cmu.cs.lti.ark.util.SerializedObjects;

public class GraphSpans implements Serializable {
	
	/**
	 * generated id
	 */
	private static final long serialVersionUID = 2598933666088212309L;
	public String[] sortedSpans = null;
	public String[] sortedFEs = null;
	public float[][] smoothedGraph = null;
	public GraphSpans(String spansFile, 
					  String feFile,
					  String smoothedFile) {
		readSpansFile(spansFile);
		readFEFile(feFile);
		readAndSerializeSmoothedFile(smoothedFile);
		// readSerializedGraphFile(smoothedFile + ".jobj");
	}
	
	public void serialize(String file) {
		SerializedObjects.writeSerializedObject(this, file);
	}
	
	public void readSerializedGraphFile(String graphSerFile) {
		System.out.println("Reading serialized graph file.....");
		smoothedGraph = (float[][])SerializedObjects.readSerializedObject(graphSerFile);
		System.out.println("Finished reading serialized graph file.");
	}
	
	public void readSpansFile(String spansFile) {
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
		sortedFEs = new String[count];
		count = 0;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(feFile));
			while ((line = bReader.readLine()) != null) {
				line = line.trim();
				sortedFEs[count] = line;
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			System.out.println("Could not read file: " + feFile);
			System.exit(-1);
		}
		System.out.println("Stored FEs.");
	}
	
	public void readAndSerializeSmoothedFile(String smoothedFile) {
		smoothedGraph = new float[sortedSpans.length][];
		String line = null;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(smoothedFile));
			int count = 0;
			while ((line = bReader.readLine()) != null) {
				String[] toks = line.trim().split("\t");
				int index = new Integer(toks[0]);
				float sum = 0;
				if (toks.length - 1 != sortedFEs.length) {
					System.out.println("Problem with index: " + index);
					System.out.println("Span: " + sortedSpans[index]);
					System.out.println("Number of toks: " + (toks.length - 1));
					System.exit(-1);
				}
				smoothedGraph[index] = new float[sortedFEs.length];
				for (int i = 1; i < toks.length; i++) {
					smoothedGraph[index][i-1] = new Float(toks[i]);
					sum += smoothedGraph[index][i-1];
				}
				if (sum == 0) {
					System.out.println("Problem with sum. " +
							"index: " + index);
					System.out.println("Span: " + sortedSpans[index]);
					System.out.println("Number of toks: " + (toks.length - 1));
					System.exit(-1);
				}
				count++;
				if (count % 10000 == 0) {
					System.out.print(count + " ");
				}
				if (count % 100000 == 0) {
					System.out.println();
				}
			}
			bReader.close();
			// SerializedObjects.writeSerializedObject(smoothedGraph, smoothedFile + ".jobj");
		} catch (IOException e) {
			System.out.println("Could not read smoothed graph.");
			e.printStackTrace();
			System.exit(-1);
		}
	} 
	
	public static void test() {
		String spansFile = 
			"/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012/all.spans.sorted";
		String feFile = 
			"/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012/fes.sorted";
		String smoothedFile =
			"/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012/smoothed/lp.mu.0.5.nu.0.1.10";
		String graphFile = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/NAACL2012/smoothed/lp.mu.0.5.nu.0.1.10.graph.spans.jobj";
		GraphSpans gs = new GraphSpans(spansFile, feFile, smoothedFile);
		gs.serialize(graphFile);
	}
	
	public static void main(String[] args) {
		String spansFile = args[0];
		String feFile = args[1];
		String smoothedFile = args[2];
		String graphFile = args[3];
		GraphSpans gs = new GraphSpans(spansFile, feFile, smoothedFile);
		gs.serialize(graphFile);
	}
}
