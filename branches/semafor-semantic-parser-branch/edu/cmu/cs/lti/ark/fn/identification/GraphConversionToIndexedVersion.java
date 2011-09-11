package edu.cmu.cs.lti.ark.fn.identification;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import gnu.trove.THashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class GraphConversionToIndexedVersion {
	public static void main(String[] args) {
		String inputGraphPath = args[0];
		String outputGraphPath = args[1];
		File inputDir = new File(inputGraphPath);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith("sym.graph.a.");
			}			
		};
		String[] files = inputDir.list(filter);
		for (int i = 0; i < files.length; i++) {
			writeConvertedFile(inputGraphPath, outputGraphPath, files[i]);
			System.out.println("Done with: " + files[i]);
		}			
	}
	
	public static void writeConvertedFile(String inputDir,
										  String outputDir, 
										  String fileName) {
		String[] sortedTypes = getSortedListOfTypes(inputDir + "/" + fileName);
		writeSortedTypes(outputDir + "/sorted.types." + fileName, sortedTypes);
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(inputDir + "/" + fileName));
			String line = null;
			String[] lines = new String[sortedTypes.length];
			for (int i = 0; i < lines.length; i++) {
				lines[i] = null;
			}
			while ((line = bReader.readLine()) != null) {
				String[] toks = line.trim().split("\t");
				int index = Arrays.binarySearch(sortedTypes, toks[0]);
				if (index < 0) {
					System.out.println("0. Problem with type: " + toks[0]);
					System.exit(-1);
				}
				lines[index] = index + "";
				for (int i = 1; i < toks.length; i = i + 2) {
					int index1 = Arrays.binarySearch(sortedTypes, toks[i]);
					if (index1 < 0) {
						System.out.println("1. Problem with type: " + toks[i]);
						System.exit(-1);
					}
					double val = new Double(toks[i+1]);
					lines[index] += "\t" + index1 + "\t" + val;
				}
				lines[index] = lines[index].trim();
			}
			bReader.close();
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(outputDir + "/" + fileName));
			for (int i = 0; i < sortedTypes.length; i++) {
				if (lines[i] == null) {
					lines[i] = i + "";
				}
				bWriter.write(lines[i] + "\n");
			}
			bWriter.close();
			ArrayList<String> frames = ParsePreparation.readSentencesFromFile(inputDir + "/sorted.frames");
			String[] frameArr = new String[frames.size()];
			for (int i = 0; i < frames.size(); i++) {
				frameArr[i] = frames.get(i).trim();
			}
			Arrays.sort(frameArr);
			System.out.println("Number of frames: " + frameArr.length);
			String labeledPredicatesFile = "labeled.predicates";
			bReader =  new BufferedReader(new FileReader(inputDir + "/" + labeledPredicatesFile));
			bWriter = new BufferedWriter(new FileWriter(outputDir + "/" + labeledPredicatesFile + "." + fileName));
			line = null;
			while ((line = bReader.readLine()) != null) {
				String[] toks = line.trim().split("\t");
				String type = toks[0];
				if (Arrays.binarySearch(sortedTypes, type) >= 0) {
					int index = Arrays.binarySearch(sortedTypes, type);
					double[] dist = new double[frameArr.length];
					String outLine = index + "";
					for (int i = 1; i < toks.length; i = i + 2) {
						String frame = toks[i];
						// System.out.println(toks[i] + "\t" + toks[i+1]);
						int fIndex = Arrays.binarySearch(frameArr, frame);
						if (fIndex < 0) {
							System.out.println("Problem with frame: " + frame +". Not in list.");
							System.exit(-1);
						}
						dist[fIndex] = new Double(toks[i+1]);
					}
					for (int i = 0; i < frameArr.length; i++) {
						outLine += "\t" + dist[i];
					}
					outLine = outLine.trim();
					bWriter.write(outLine + "\n");
				}
			}
			bReader.close();
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void writeSortedTypes(String filePath, String[] sortedTypes) {
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(filePath));
			for (String string: sortedTypes) {
				bWriter.write(string + "\n");
			}
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static String[] getSortedListOfTypes(String file) {
		Set<String> set = new THashSet<String>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = bReader.readLine()) != null) {
				String[] toks = line.trim().split("\t");
				set.add(toks[0]);
				for (int i = 1; i < toks.length; i = i + 2) {
					set.add(toks[i]);
				}
			}
			bReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		String[] arr = new String[set.size()];
		set.toArray(arr);
		Arrays.sort(arr);
		return arr;
	}
}