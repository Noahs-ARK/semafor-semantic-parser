package edu.cmu.cs.lti.ark.fn.segmentation;

import java.util.ArrayList;
import java.util.Arrays;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.identification.ScanAdverbsAndAdjectives;
import gnu.trove.THashSet;

public class CheckCoverage {
	public static void main(String[] args) {
		newCoverage(args);
	}
	
	public static void newCoverage(String[] args) {
		String predicatesFile = args[0];
		String frameElementsFile = args[1];
		String parseFile = args[2];
		ArrayList<String> preds = ParsePreparation.readSentencesFromFile(predicatesFile);
		THashSet<String> predUnits = new THashSet<String>();
		for (String p: preds) {
			String[] toks = p.trim().split("\t");
			int li = toks[0].lastIndexOf(".");
			String u = toks[0].substring(0, li);
			predUnits.add(u);
		}
		int count = 0;
		int found = 0;
		ArrayList<String> fes = ParsePreparation.readSentencesFromFile(frameElementsFile);
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(parseFile);
		for (String fe: fes) {
			String[] toks = fe.trim().split("\t");
			String tokNums = toks[3];
			String unit = "";
			int sentNum = new Integer(toks[5]);
			String parse = parses.get(sentNum);
			String[] pToks = parse.split("\t");
			int numTokens = new Integer(pToks[0]);
			if (tokNums.contains("_")) {
				String[] ns = tokNums.split("_");
				for (int i = 0; i < ns.length; i++)
				{
					int n = new Integer(ns[i]);
					unit += pToks[1 + n].toLowerCase() + " ";
				}
				unit = unit.trim();
			} else {
				unit = pToks[1 + 5*numTokens + (new Integer(tokNums))];
			}
			unit = ScanAdverbsAndAdjectives.getCanonicalForm(unit);
			if (predUnits.contains(unit)) {
				found++;
			} else {
				System.out.println(unit);
			}
			count++;
		}
		double cov = (double)found/(double)count;
		System.out.println("Coverage:" + cov);
	}
	
	public static void oldCoverage(String[] args) {
		String predicatesFile = args[0];
		String frameElementsFile = args[1];
		ArrayList<String> preds = ParsePreparation.readSentencesFromFile(predicatesFile);
		String[] arr = new String[preds.size()];
		preds.toArray(arr);
		Arrays.sort(arr);
		int count = 0;
		int found = 0;
		ArrayList<String> fes = ParsePreparation.readSentencesFromFile(frameElementsFile);
		for (String fe: fes) {
			String[] toks = fe.trim().split("\t");
			String tokNums = toks[3];
			if (tokNums.contains("_")) {
				continue;
			}
			String word = toks[4].toLowerCase();
			if (Arrays.binarySearch(arr, word) >= 0) {
				found++;
			} else {
				System.out.println(word);
			}
			count++;
		}
		double cov = (double)found/(double)count;
		System.out.println("Coverage:" + cov);
	}
}