package edu.cmu.cs.lti.ark.util.ds.graph;

import java.io.Serializable;

/**  
 * Undirected graph node.
 *
 * @author Nathan Schneider (nschneid)
 * @since 2009-07-23
 */
public abstract class UGNode<T extends UGNode<T>> implements Serializable
{

	private static final long serialVersionUID = -7364265721666876050L;
	
	/**
	 * node label, e.g. phrase structure nonterminal category or parent edge label (for labeled dependency parses)
	 */
	protected String labelType;
	/**
	 * the node's index in the tree; the only guarantee is that the root node will have index 0
	 */
	protected int index;
	/**
	 * log probability associated with the node
	 */
	protected double mLogProb;

	
	public UGNode() {
		super();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int i) {
		index = i;
	}

	public String getLabelType() {
		return labelType;
	}

	public void setLabelType(String l) {
		labelType = l;
	}

	public double getLogProb() {
		return mLogProb;
	}

	public void setLogProb(double logProb) {
		mLogProb = logProb;
	}
	
}
