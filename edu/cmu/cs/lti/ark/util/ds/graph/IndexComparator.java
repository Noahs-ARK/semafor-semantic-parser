package edu.cmu.cs.lti.ark.util.ds.graph;


import java.util.Comparator;


public class IndexComparator implements Comparator<Node<?>>
{

	public int compare(Node<?> arg0, Node<?> arg1) {
		if(arg0.getIndex()<arg1.getIndex())
		{
			return -1;
		}
		else if(arg0.getIndex()==arg1.getIndex())
		{
			return 0;
		}
		else return 1;
	}
	
} 