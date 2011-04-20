package edu.cmu.cs.lti.ark.fn.identification;

import java.util.Set;

import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;


public class CountNumberOfRoles {
	public static void main(String[] args) {
		// String file = "/usr2/dipanjan/experiments/FramenetParsing/fndata-1.5/ACLSplits/5/framenet.frame.element.map";
		String file = "/usr2/dipanjan/experiments/FramenetParsing/FrameStructureExtraction/lrdata/framenet.original.frame.elements.map";
		THashMap<String, THashSet<String>> map = 
			(THashMap<String,THashSet<String>>)SerializedObjects.readSerializedObject(file);
		Set<String> frames = map.keySet();
		System.out.println("Number of frames:" + frames.size());
		THashSet<String> roleSet = new THashSet<String>();
		for (String frame: frames) {
			System.out.println(frame + "\t" + map.get(frame));
			THashSet<String> roles = map.get(frame);
			for (String r: roles) 
				roleSet.add(r);
		}
		System.out.println(roleSet);
		System.out.println("Number of unique roles:" + roleSet.size());
	}
}