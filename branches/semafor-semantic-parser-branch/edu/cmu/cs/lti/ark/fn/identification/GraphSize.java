package edu.cmu.cs.lti.ark.fn.identification;

import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.ark.util.SerializedObjects;

public class GraphSize {
	public static void main(String[] args) {
		String graphFile = args[0];
		SmoothedGraph sg = 
			(SmoothedGraph)SerializedObjects.readSerializedObject(graphFile);
		Map<String, Set<String>> map = sg.getFineMap();
		Set<String> keys = map.keySet();
		int totalNonZero = 0;
		for (String key: keys) {
			Set<String> set = map.get(key);
			totalNonZero += set.size();
		}
		System.out.println("Total number of non-zero components: " + totalNonZero);
	}
}