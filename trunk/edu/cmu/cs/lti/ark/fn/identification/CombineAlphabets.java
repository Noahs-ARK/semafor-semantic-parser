package edu.cmu.cs.lti.ark.fn.identification;

import edu.cmu.cs.lti.ark.util.BasicFileIO;
import gnu.trove.THashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.Set;

public class CombineAlphabets {
	public static void main(String[] args) {
		String dir = args[0];
		String outFile = args[1];
		File f = new File(dir);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith("alphabet.dat_");
			}
		};
		String[] files = f.list(filter);
		Map<String, Integer> alphabet = new THashMap<String, Integer>();
		for (String file: files) {
			String path = dir + "/" + file;
			Map<String, Integer> temp = 
				readAlphabetFile(path);
			Set<String> keys = temp.keySet();
			for (String key: keys) {
				if (!alphabet.containsKey(key)) {
					alphabet.put(key, alphabet.size() + 1);
				}
			}
		}
		writeAlphabetFile(outFile, alphabet);
	}
	
	private static void writeAlphabetFile(String file, Map<String, Integer> alphabet)
	{
		try
		{
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
			bWriter.write(alphabet.size()+"\n");
			Set<String> set = alphabet.keySet();
			for(String key:set)
			{
				bWriter.write(key+"\t"+alphabet.get(key)+"\n");
			}
			bWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	private static Map<String, Integer> readAlphabetFile(String file)
	{
		Map<String, Integer> alphabet = new THashMap<String, Integer>();
		BufferedReader bReader = BasicFileIO.openFileToRead(file);
		alphabet = new THashMap<String,Integer>();
		int num = new Integer(BasicFileIO.getLine(bReader));
		for (int i = 0; i < num; i ++) {
			String line = BasicFileIO.getLine(bReader);
			line = line.trim();
			String[] toks = line.split("\t");
			alphabet.put(toks[0], new Integer(toks[1]));
		}
		BasicFileIO.closeFileAlreadyRead(bReader);
		return alphabet;
	}
}

