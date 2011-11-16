package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.ark.util.ds.Pair;

public interface JDecoding {
	public void setMaps(Map<String, Set<Pair<String, String>>> excludesMap, 
			Map<String, Set<Pair<String, String>>> requiresMap);
	public Map<String, Pair<String, Double>> decode(Map<String, Pair<int[], Double>[]> scoreMap, 
			  String frame,
			  boolean costAugmented,
			  FrameFeatures goldFF);
	public void end();
	public void setNumThreads(int nt);
}
