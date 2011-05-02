/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * RequiredDataCreation.java is part of SEMAFOR 2.0.
 * 
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along
 * with SEMAFOR 2.0.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.fn.identification;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.XmlUtils;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import gnu.trove.THashSet;
import gnu.trove.THashMap;

public class RequiredDataCreation {
	
	public static THashMap<String,String> conversionMap = new THashMap<String,String>();
	static
	{
		conversionMap.put("NUM", "CD");
		conversionMap.put("N","NN");
		conversionMap.put("A","JJ");
		conversionMap.put("ADV", "RB");
		conversionMap.put("PREP", "IN");
		conversionMap.put("C", "CC");
		conversionMap.put("V", "VB");
		conversionMap.put("INTJ", "UH");
		conversionMap.put("ART", "DET");
		conversionMap.put("SCON", "IN");
	}
	
	public static void main(String[] args) {
		FNModelOptions options = new FNModelOptions(args);
		Map<String, String> hvLemmas = getHVLemmas(options);
		THashSet<String> relWords = 
								getRelatedWords(options);
		THashMap<String,THashSet<String>> cMap = 
			getHVCorrespondence(options);
		Pair<Map<String, Set<String>>, Map<String, THashMap<String, Set<String>>>>
			pair = buildHVWordNetCache(options);
		Map<String, Map<String, Set<String>>> revisedMap = 
			reviseRelMap(pair.getSecond(), options);		
		String fmFile = options.frameNetMapFile.get();
		THashMap<String, THashSet<String>> frameMap = 
			(THashMap<String, THashSet<String>>)
			SerializedObjects.readSerializedObject(fmFile);
		RequiredDataForFrameIdentification req = 
			new RequiredDataForFrameIdentification(relWords, 
					  pair.getFirst(), 
					  pair.getSecond(), 
					  frameMap, 
					  cMap,revisedMap,
					  hvLemmas);
		SerializedObjects.writeSerializedObject(req, options.fnIdReqDataFile.get());
	}	
	
	public static Map<String, String> getHVLemmas(FNModelOptions options) {
		String fmFile = options.frameNetMapFile.get();
		String wnConfigFile = options.wnConfigFile.get();
		String stopFile = options.stopWordsFile.get();
		THashMap<String, THashSet<String>> frameMap = 
			(THashMap<String, THashSet<String>>)
			SerializedObjects.readSerializedObject(fmFile);
		WordNetRelations wnr = new WordNetRelations(stopFile, wnConfigFile);
		Map<String, String> lemmaMap = new THashMap<String, String>();
		Set<String> keySet = frameMap.keySet();
		for(String frame: keySet)
		{
			THashSet<String> hus = frameMap.get(frame);
			for(String hUnit: hus)
			{
				String[] hiddenToks = hUnit.split(" ");
				String hiddenUnitTokens="";
				String hiddenUnitLemmas="";
				for(int i = 0; i < hiddenToks.length; i ++)
				{
					String[] arr = hiddenToks[i].split("_");
					hiddenUnitTokens+=arr[0]+" ";
					String lowerCaseLemma = wnr.getLemmaForWord(arr[0], arr[1]).toLowerCase();
					lemmaMap.put(arr[0] + "_" + arr[1], lowerCaseLemma);
					hiddenUnitLemmas+=wnr.getLemmaForWord(arr[0], arr[1]).toLowerCase()+" ";
				}
				hiddenUnitTokens=hiddenUnitTokens.trim();
				hiddenUnitLemmas=hiddenUnitLemmas.trim();
				System.out.println("Processed:"+hiddenUnitLemmas);
			}
		}
		SerializedObjects.writeSerializedObject(lemmaMap, options.lemmaCacheFile.get());
		return lemmaMap;
		
	}
	
	public static Map<String, Map<String, Set<String>>> reviseRelMap(
			Map<String, THashMap<String, Set<String>>> relMap, FNModelOptions options) {
		Map<String, Map<String, Set<String>>> revisedMap = 
			new HashMap<String, Map<String, Set<String>>>();
		Set<String> keys = relMap.keySet();
		for (String key: keys) {
			THashMap<String, Set<String>> rels = relMap.get(key);
			Set<String> relations = rels.keySet();
			for (String rel: relations) {
				Set<String> words = rels.get(rel);
				for (String word: words) {
					if (!revisedMap.containsKey(key)) {
						Map<String, Set<String>> map = 
							new THashMap<String, Set<String>>();
						Set<String> rRels = new THashSet<String>();
						rRels.add(rel);
						map.put(word, rRels);
						revisedMap.put(key, map);
					} else {
						Map<String, Set<String>> map = 
							revisedMap.get(key);
						if (map.containsKey(word)) {
							Set<String> rRels = map.get(word);
							rRels.add(rel);
							map.put(word, rRels);
							revisedMap.put(key, map);
						} else {
							Set<String> rRels = new THashSet<String>();
							rRels.add(rel);
							map.put(word, rRels);
							revisedMap.put(key, map);
						}
					}
				}
			}
		}
		String revisedRelFile = options.revisedMapFile.get();
		SerializedObjects.writeSerializedObject(revisedMap, revisedRelFile);
		return revisedMap;
	}
	
	
	public static Pair<Map<String, Set<String>>, Map<String, THashMap<String, Set<String>>>>
		buildHVWordNetCache(FNModelOptions options) {
		String fmFile = options.frameNetMapFile.get();
		String wnConfigFile = options.wnConfigFile.get();
		String stopFile = options.stopWordsFile.get();
		String relFile = options.wnRelatedWordsForWordsFile.get();
		String wnMapFile = options.wnMapFile.get();
		THashMap<String, THashSet<String>> frameMap = 
			(THashMap<String, THashSet<String>>)
			SerializedObjects.readSerializedObject(fmFile);
		Collection<THashSet<String>> values = frameMap.values();		
		WordNetRelations wnr = new WordNetRelations(stopFile, wnConfigFile);
		for(THashSet<String> relWords: values)
		{
			for(String hiddenWord: relWords)
			{
				String[] hiddenToks = hiddenWord.split(" ");
				String hiddenUnitTokens="";
				for(int i = 0; i < hiddenToks.length; i ++)
				{
					String[] arr = hiddenToks[i].split("_");
					hiddenUnitTokens+=arr[0]+" ";
				}
				hiddenUnitTokens=hiddenUnitTokens.trim().toLowerCase();
				wnr.getAllRelationsMap(hiddenUnitTokens);
				System.out.println(hiddenUnitTokens);
			}
		}
		Map<String, Set<String>> relatedWordsForWord = wnr.getRelatedWordsForWord();
		Map<String, THashMap<String, Set<String>>> wordNetMap = wnr.getWordNetMap();
		SerializedObjects.writeSerializedObject(relatedWordsForWord, relFile);
		SerializedObjects.writeSerializedObject(wordNetMap, wnMapFile);
		return new Pair<Map<String, Set<String>>, 
			Map<String, THashMap<String, Set<String>>>>(relatedWordsForWord, wordNetMap);
	}	
	
	public static THashMap<String,THashSet<String>> 
		getHVCorrespondence(FNModelOptions options) {
		String fmFile = options.frameNetMapFile.get();
		String wnConfigFile = options.wnConfigFile.get();
		String stopFile = options.stopWordsFile.get();
		String hvCorrespondenceFile = options.hvCorrespondenceFile.get();
		THashMap<String, THashSet<String>> frameMap = 
			(THashMap<String, THashSet<String>>)
			SerializedObjects.readSerializedObject(fmFile);
		WordNetRelations wnr = new WordNetRelations(stopFile, wnConfigFile);
		THashMap<String,THashSet<String>> cMap = new THashMap<String,THashSet<String>>();
		Set<String> keySet = frameMap.keySet();
		for(String frame: keySet)
		{
			THashSet<String> hus = frameMap.get(frame);
			for(String hUnit: hus)
			{
				String[] hiddenToks = hUnit.split(" ");
				String hiddenUnitTokens="";
				String hiddenUnitLemmas="";
				for(int i = 0; i < hiddenToks.length; i ++)
				{
					String[] arr = hiddenToks[i].split("_");
					hiddenUnitTokens+=arr[0]+" ";
					hiddenUnitLemmas+=wnr.getLemmaForWord(arr[0], arr[1]).toLowerCase()+" ";
				}
				hiddenUnitTokens=hiddenUnitTokens.trim();
				hiddenUnitLemmas=hiddenUnitLemmas.trim();
				THashSet<String> frames = cMap.get(hiddenUnitLemmas);
				if(frames==null)
				{
					frames = new THashSet<String>();
					frames.add(frame);
					cMap.put(hiddenUnitLemmas, frames);
				}
				else
				{
					frames.add(frame);
				}
				System.out.println("Processed:"+hiddenUnitLemmas);
			}
		}
		SerializedObjects.writeSerializedObject(cMap,hvCorrespondenceFile);
		return cMap;
	}
	
	public static THashSet<String> getRelatedWords(FNModelOptions options) {
		String fmFile = options.frameNetMapFile.get();
		String wnConfigFile = options.wnConfigFile.get();
		String stopFile = options.stopWordsFile.get();
		String luXmlDir = options.luXmlDir.get();
		THashMap<String, THashSet<String>> mFrameMap = 
			(THashMap<String, THashSet<String>>)
			SerializedObjects.readSerializedObject(fmFile);
		WordNetRelations wnr = new WordNetRelations(stopFile, wnConfigFile);
		THashSet<String> set = getAllRelatedWords(mFrameMap, 
												wnr);
		THashSet<String> absentExampleLUs = getListOfLUs(luXmlDir, wnr);
		set.addAll(absentExampleLUs);
		String relatedWordsFile = options.allRelatedWordsFile.get();
		SerializedObjects.writeSerializedObject(set, relatedWordsFile);
		return set;
	}
	
	public static THashSet<String> getListOfLUs(String directory, WordNetRelations wnr) {
		File f = new File(directory);
		FilenameFilter filt = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.endsWith(".xml");
			}
		};
		THashSet<String> result = new THashSet<String>();	
		String[] files = f.list(filt);
		int count = 0;
		for(String file: files)
		{
			String fileName = directory+"/"+file;
			Document d = XmlUtils.parseXmlFile(fileName, false);		
			String topPath = "/lexUnit";
			Element top = XmlUtils.getUniqueChildNodeFromXPathTM(d, topPath);
			String name = top.getAttribute("name");
			String pos = top.getAttribute("POS");
			String corpusPath = "/lexUnit/subcorpus";
			Element[] corpora = XmlUtils.applyXPath(d, corpusPath);
			if(corpora==null || corpora.length==0)
			{
				if(name.contains(" ") || name.contains("_") || name.contains("(") || name.contains(")"))
					continue;
				int lastIndex = name.lastIndexOf(".");
				String word = name.substring(0,lastIndex).toLowerCase();
				String lemma = wnr.getLemmaForWord(word,conversionMap.get(pos)); 
				result.add(lemma+"_"+conversionMap.get(pos).substring(0,1));
				count++;
			}	
		}
		System.out.println("Total number of cases with no examples:"+count);
		for(String pos: result)
		{
			System.out.println(pos);
		}
		return result;
	}	
	
	public static THashSet<String> getAllRelatedWords(
			THashMap<String,THashSet<String>> mFrameMap, 
			WordNetRelations mWNR) {
		THashSet<String> result = new THashSet<String>();
		Set<String> set = mFrameMap.keySet();
		int count = 0;
		for(String string:set)
		{
			THashSet<String> hus = mFrameMap.get(string);
			System.out.println(count+"\t"+string);
			count++;
			for(String hu: hus)
			{
				String[] wps = hu.trim().split(" ");
				if(wps.length==1)
				{
					String[] toks = wps[0].split("_");
					String w = toks[0];
					String p = toks[1];
					String cP = toks[1].substring(0,1);
					String l = mWNR.getLemmaForWord(w.toLowerCase(), p);
					if(p.startsWith("N")||/*p.startsWith("JJ")||*/p.startsWith("V"))
					{	
						THashMap<String,Set<String>> rlMap = mWNR.getAllRelationsMap(l);
						Set<String> rl = getSelectedRelatedWords(rlMap);
						rl = getRevisedSet(rl, true);
						for(String r: rl)
						{
							result.add(r.trim()+"_"+cP);
						}
					}
					else
					{
						result.add(l.trim()+"_"+cP);
					}								
				}	
				else
				{
					String lTok = "";
					for(int i = 0; i < wps.length; i ++)
					{
						String[] toks = wps[i].split("_");
						String w = toks[0];
						String p = toks[1];
						String cP = toks[1].substring(0,1);
						String l = mWNR.getLemmaForWord(w.toLowerCase(), p);
						lTok+=l+"_"+cP+" ";
					}
					lTok=lTok.trim();
					//Set<String> rl = mWNR.getAllRelatedWords(lTok);
					//rl = getRevisedSet(rl);
					//result.addAll(rl);
					result.add(lTok);
					if(lTok.equals("there be"))
					{
						result.add("there_E 's_P");
					}					
				}
			}
		}	
		return result;
	}
	
	public static Set<String> getRevisedSet(Set<String> rl, boolean check)
	{
		THashSet<String> result = new THashSet<String>();
		for(String r: rl)
		{
			r=r.toLowerCase();
			if(check)
			{
				StringTokenizer st = new StringTokenizer(r," ");
				if(st.countTokens()>1)
					continue;
			}
			if(r.contains("_"))
			{
				if(check)
					continue;
				String[] toks = r.split("_");
				r = "";
				for(int i = 0; i < toks.length; i ++)
				{
					r+=toks[i]+" ";
				}
				r=r.trim();
			}
			result.add(r);
		}		
		return result;
	}	
	
	public static Set<String> getSelectedRelatedWords(THashMap<String,Set<String>> rlMap)
	{
		Set<String> keys = rlMap.keySet();
		THashSet<String> result = new THashSet<String>();
		for(String key: keys)
		{
			if(key.equals("identity")/*||key.equals("synonym")||key.equals("derived-form")||key.equals("morph")*/)
				result.addAll(rlMap.get(key));
		}
		return result;
	}
}
