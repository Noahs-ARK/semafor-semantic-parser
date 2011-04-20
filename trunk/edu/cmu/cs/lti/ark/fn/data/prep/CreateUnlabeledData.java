package edu.cmu.cs.lti.ark.fn.data.prep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CreateUnlabeledData {
	public static void main(String[] args) throws IOException {
		String dir = "/mal2/dipanjan/experiments/FramenetParsing/DistributionalSimilarityFeatures/data/pos";
		File f = new File(dir);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith("apw") && arg1.endsWith("pos.txt");
			}			
		};
		String apText = "/mal2/dipanjan/experiments/FramenetParsing/DistributionalSimilarityFeatures/data/pos/AP.txt";
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(apText));
		String[] files = f.list(filter);
		for (int i = 0; i < files.length; i ++) {
			String file = dir + "/" + files[i];
			ArrayList<String> sents = 
				ParsePreparation.readSentencesFromFile(file);
			for (String sent: sents) {
				sent = sent.trim();
				String[] toks = getTokens(sent);
				if (toks.length > 100)
					continue;
				String outLine = "";
				for (String tok: toks) {
					int li = tok.lastIndexOf("_");
					outLine += tok.subSequence(0, li) + " ";
				}
				outLine = outLine.trim();
				bWriter.write(outLine+"\n");
			}
		}
		bWriter.close();
	}	
	
	public static String[] getTokens(String line) {
		StringTokenizer st = new StringTokenizer(line, " \t", true);
		ArrayList<String> list = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String tok = st.nextToken().trim();
			if (tok.equals(""))
				continue;
			list.add(tok);
		}
 		String[] arr = new String[list.size()];
		list.toArray(arr);
		return arr;
	}
}