package edu.cmu.cs.lti.ark.util.ds.graph;

import java.util.List;

public interface HasLabeledEdges<N,L> {
	List<L> getLabels(N targetNode);
}
