package edu.cmu.cs.lti.ark.util.ds.graph;

import edu.cmu.cs.lti.ark.util.ds.path.Path;

public interface HasWeightedHyperEdges<N,W> {
	W getWeight(Path<?> path);
}
