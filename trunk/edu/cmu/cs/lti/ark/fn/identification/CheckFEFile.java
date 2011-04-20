package edu.cmu.cs.lti.ark.fn.identification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CheckFEFile {
	public static void main(String[] args) {
		String file = "/mal2/dipanjan/experiments/FramenetParsing/fndata-1.5/uRData/0/ss.frame.elements";
		int prev = -1;
		try {
			BufferedReader bReader = 
				new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = bReader.readLine()) != null) {
				line = line.trim();
				String[] toks = line.split("\t");
				int sentNum = new Integer(toks[5]);
				if (sentNum < prev) {
					System.out.println("Problem: " + line);
					System.exit(-1);
				}
				prev = sentNum;
				System.out.println(line);
			}
			bReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}