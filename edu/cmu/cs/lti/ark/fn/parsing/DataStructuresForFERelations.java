package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.FilenameFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.cmu.cs.lti.ark.util.XmlUtils;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class DataStructuresForFERelations {
	public static void main(String[] args) {
		String dir = "/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/frame";
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".xml");
			}
		};
		File f = new File(dir);
		String[] files = f.list(filter);
		Map<String, Set<Pair<String, String>>> exclusionMap = 
			new THashMap<String, Set<Pair<String, String>>>();
		for (String file: files) {
			int index = file.indexOf(".xml");
			String frame = file.substring(0, index);
			System.out.println("Frame: " + frame);
			Document d = XmlUtils.parseXmlFile(f.getAbsolutePath() + "/" + file, false);
			Element[] e = XmlUtils.applyXPath(d, "/frame/FE/excludesFE");
			if (e == null || e.length == 0) {
				System.out.println("Done with: " + file);
				continue;
			}
			System.out.println("Total number of exlude FEs found: " + e.length);
		}
	}
}