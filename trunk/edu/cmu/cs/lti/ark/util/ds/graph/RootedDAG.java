package edu.cmu.cs.lti.ark.util.ds.graph;

import java.util.List;


public class RootedDAG<N extends RootedDAGNode<N>> {

	protected boolean allowParallelEdges = false;
	protected N _root;

	public RootedDAG(N root) {
		_root = root;
	}

	public N getRoot() {
		return _root;
	}

	public List<N> topologicalSort() throws GraphException {
		return topologicalSort(this);
	}
	
	public static <N extends RootedDAGNode<N>> List<N> topologicalSort(RootedDAG<N> dag) throws GraphException {
		return RootedDAGNode.topologicalSort(dag.getRoot());
	}
}