package edu.cmu.cs.lti.ark.fn.identification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;



public class ExpandedSupModel {
	public static void main(String[] args) {
		String supmodel = args[0];
		String supalphabet = args[1];
		String ssalphabet = args[2];
		String outmodel = args[3];
		
		TIntObjectHashMap<String> supAlphabet = 
			readAlphabet(supalphabet);
		TObjectIntHashMap<String> rSupAlphabet = getReverseMap(supAlphabet);
		TIntObjectHashMap<String> ssAlphabet = 
			readAlphabet(ssalphabet);
		int supsize = supAlphabet.size();
		int sssize = ssAlphabet.size();
		double[] supmodelarr = readDoubleArray(supmodel, supsize+1);
		double[] ssmodelarr = new double[sssize+1];
		ssmodelarr[0] = supmodelarr[0];
		for (int i = 1; i < sssize+1; i++) {
			String feat = ssAlphabet.get(i);
			if (rSupAlphabet.contains(feat)) {
				int index = rSupAlphabet.get(feat);
				ssmodelarr[i] = supmodelarr[index];
			} else {
				ssmodelarr[i] = 1.0;
			}
		}
		writeArray(ssmodelarr, outmodel);
	}
	
	public static void writeArray(double[] arr, String file) {
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < arr.length; i ++) {
				bWriter.write(arr[i] + "\n");
			}
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static TObjectIntHashMap<String> getReverseMap(TIntObjectHashMap<String> map) {
		TObjectIntHashMap<String> rMap = new TObjectIntHashMap<String>();
		int[] keys = map.keys();
		for (int i = 0; i < keys.length; i++) {
			rMap.put(map.get(keys[i]), keys[i]);
		}
		return rMap;
	}
	
	public static double[] readDoubleArray(String file, int size) {
		double[] arr = new double[size];
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			String line = null;
			int count = 0;
			while ((line = bReader.readLine()) != null) {
				line = line.trim();
				arr[count] = new Double(line);
				count++;
			}
			bReader.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return arr;
	}
	
	
	public static TIntObjectHashMap<String> readAlphabet(String alphabetFile) {
		TIntObjectHashMap<String> map = new TIntObjectHashMap<String>();
		try
		{
			String line = null;
			int count = 0;
			BufferedReader bReader = new BufferedReader(new FileReader(alphabetFile));
			while((line=bReader.readLine())!=null)
			{
				if(count==0)
				{
					count++;
					continue;
				}
				String[] toks = line.trim().split("\t");
				map.put(new Integer(toks[1]),toks[0]);
			}
			bReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return map;
	}
	
}