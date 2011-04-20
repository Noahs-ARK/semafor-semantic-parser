package edu.cmu.cs.lti.ark.util.ds.graph;

import java.util.Comparator;





public class NodeComparator implements Comparator<RootedDAGNode<?>>
{
	public int compare(RootedDAGNode<?> arg0, RootedDAGNode<?> arg1) {
		if(arg0.getDepth()>arg1.getDepth())
			return -1;
		else if(arg0.getDepth()==arg1.getDepth())
			return 0;
		else
			return 1;
	}
}