package edu.cmu.cs.lti.ark.fn.identification;

import gnu.trove.THashSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class GraphStats {
	public static void main(String[] args) {
		String graphFile = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/ACLSplits/5/" 
				+ "smoothed.graph.a.0.2.k.10.mu.1.0.nu.0.000001";
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(graphFile));
			String line = null;
			int count = 0;
			double avgratio = 0.0;
			while ((line = bReader.readLine()) != null) {
				String[] toks = line.trim().split("\t");
				String pred = toks[0];
				int li = pred.lastIndexOf(".");
				String coarsepred = pred.substring(0, li);
				String[] frames = toks[1].split(" ");
				double sum = 0.0;
				double fcount = 0;
				double sum1 = 0.0;
				for (int i = 0 ; i < frames.length; i = i + 2) {
					double val = new Double(frames[i+1]);
					sum += val;
					fcount++;
					if (i < 2) {
						sum1 += val;
					}
				}
				double avg = sum / fcount;
				double avg1 = sum1 / 2;
				double ratio = avg1 / avg;
				System.out.println(ratio);
				avgratio += ratio;
				count++;
			}
			System.out.println();
			bReader.close();
			System.out.println("Finished reading graph file.");
			System.out.println("Average ratio:" + (avgratio/count));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}	
}