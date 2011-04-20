package edu.cmu.cs.lti.ark.util.ds.graph;

public interface HasWeightedEdges<N,W> {
	W getWeight(N targetNode);
}
