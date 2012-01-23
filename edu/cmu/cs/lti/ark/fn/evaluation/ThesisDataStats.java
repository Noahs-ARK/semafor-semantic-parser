package edu.cmu.cs.lti.ark.fn.evaluation;

import java.util.Set;

import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;


public class ThesisDataStats {
	public static void main(String[] args) {
		// frameMapStats();
		feMapStats();
	}
	
	public static void feMapStats() {
		String framenetOriginalMap = "/home/dipanjan/work/fall2011/ThesisFNStats/framenet.original.frame.elements.map";
		String semEvalTrainingMap = "/home/dipanjan/work/fall2011/ThesisFNStats/semeval.fulltrain.frame.elements.map";
		THashMap<String,THashSet<String>> fedict = 
			 (THashMap<String,THashSet<String>>) SerializedObjects.readSerializedObject(framenetOriginalMap);
		 THashMap<String,THashSet<String>> tempdict = 
			 (THashMap<String,THashSet<String>>)SerializedObjects.readSerializedObject(semEvalTrainingMap);
		for(String key : tempdict.keySet()){
			THashSet newval=tempdict.get(key);
			if(fedict.contains(key)){
				THashSet val=fedict.get(key);
				val.addAll(newval);
			}
			else{
				fedict.put(key, newval);
			}
		}
		fedict.putAll(tempdict);
		THashSet<String> setOfRoles = new THashSet<String>();
		Set<String> frames = fedict.keySet();
		for (String frame: frames) {
			Set<String> fes = fedict.get(frame);
			setOfRoles.addAll(fes);
		}
		System.out.println("Total number of unique fe labels:" + setOfRoles.size());
	}
	
	public static void frameMapStats() {
		String framenetOriginalMap = "/home/dipanjan/work/fall2011/ThesisFNStats/framenet.original.map";
		String semEvalTrainingMap = "/home/dipanjan/work/fall2011/ThesisFNStats/semeval.fulltrain.map";
		
		 THashMap<String,THashSet<String>> fedict = 
			 (THashMap<String,THashSet<String>>) SerializedObjects.readSerializedObject(framenetOriginalMap);
		 THashMap<String,THashSet<String>> tempdict = 
			 (THashMap<String,THashSet<String>>)SerializedObjects.readSerializedObject(semEvalTrainingMap);
		for(String key : tempdict.keySet()){
			THashSet newval=tempdict.get(key);
			if(fedict.contains(key)){
				THashSet val=fedict.get(key);
				val.addAll(newval);
			}
			else{
				fedict.put(key, newval);
			}
		}
		fedict.putAll(tempdict);
		System.out.println("Total number of frames: " + fedict.size());
	}	
}