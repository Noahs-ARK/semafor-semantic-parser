package edu.cmu.cs.lti.ark.util.ds.graph;

/**
 * Exception indicating a structural problem in a graph.
 * @see TreeNode
 * @author Nathan Schneider (nschneid)
 * @since 2009-04-04
*/
public class GraphException extends Exception {

	private static final long serialVersionUID = 2291743821032526660L;

	public GraphException(String msg) {
		super(msg);
	}
}
