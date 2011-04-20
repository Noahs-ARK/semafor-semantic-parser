package edu.cmu.cs.lti.ark.fn.clusters;


import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import edu.cmu.cs.lti.ark.util.ds.Range1Based;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParses;


public class TestDependencyParses
{
	public static void main(String[] args)
	{
		DependencyParses parses = (DependencyParses)SerializedObjects.readSerializedObject("../sampleParses/parse_981.jobj");
		System.out.println("Size of parses:"+parses.size());
		DependencyParse best = parses.get(0);
		DependencyParse[] arr = DependencyParse.getIndexSortedListOfNodes(best);
		for(int i = 1; i < arr.length; i ++)
		{
			for(int j = i; j < arr.length; j ++)
			{
				if(i==j)
					continue;
				String string = "";
				for(int k = i; k <= j; k ++)
					string+=arr[k].getWord()+" ";
				string=string.trim();
				Range1Based span = new Range1Based(i,j);
				Pair<Integer, DependencyParse> p = parses.matchesSomeSubtree(span);
				if(p!=null)
				{
					System.out.println(p+"\t\t\t\t"+string+"\t\theadIndex:"+p.getSecond().getIndex());
				}
			}
		}
		
//		
//		best.processSentence();
//		System.out.println(best.getSentence());
//		Range1Based span = new Range1Based(9,11);
//		Pair<Integer, DependencyParse> p = parses.matchesSomeSubtree(span);
//		System.out.println(p);
	}	
}