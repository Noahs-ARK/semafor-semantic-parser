package edu.cmu.cs.lti.ark.fn.parsing;

import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class FEDict {
	//public static String dirname="frame";
	public static String outdictFilename="data/fedict.train";

	private THashMap<String,THashSet<String>>fedict;
	public FEDict(String dictFilename){
		fedict=(THashMap<String,THashSet<String>>)SerializedObjects.readSerializedObject(dictFilename);
	}
	public String [] lookupFes(String frame){
		THashSet<String> feSet=fedict.get(frame);
		if(feSet==null)return new String[0];
		String [] ret=new String[ feSet.size()];
		int count=0;
		for(String fe:feSet){
			ret[count]=fe;
			count++;
		}
		
		return ret;
	}
	public void merge(String filename){
		THashMap<String,THashSet<String>> tempdict=(THashMap<String,THashSet<String>>)SerializedObjects.readSerializedObject(filename);
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
	}
}
