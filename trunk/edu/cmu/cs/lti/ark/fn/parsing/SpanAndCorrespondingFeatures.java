package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public class SpanAndCorrespondingFeatures implements Serializable, Comparator<SpanAndCorrespondingFeatures>
{
	private static final long serialVersionUID = 5754166746392268274L;
	public int[] span;
	public int[] features;
		
	public int compare(SpanAndCorrespondingFeatures arg0, SpanAndCorrespondingFeatures arg1)
	{
		String span1 = arg0.span[0]+":"+arg0.span[1];
		String span2 = arg1.span[0]+":"+arg1.span[1];
		if(span1.compareTo(span2)<0)
			return -1;
		else if(span1.compareTo(span2)==0)
			return 0;
		else
			return 1;	
	}
	
	public static void sort(SpanAndCorrespondingFeatures[] arr)
	{
		Arrays.sort(arr,new SpanAndCorrespondingFeatures());
	}
	
	public static int search(SpanAndCorrespondingFeatures[] arr, SpanAndCorrespondingFeatures s)
	{
		return Arrays.binarySearch(arr,s,new SpanAndCorrespondingFeatures());
	}
	
	/*
	 * span with _
	 */
	public static int search(SpanAndCorrespondingFeatures[] arr, String span)
	{
		String[] toks = span.split("_");
		SpanAndCorrespondingFeatures scf = new SpanAndCorrespondingFeatures();
		scf.span=new int[2];
		scf.span[0]=new Integer(toks[0]);
		scf.span[1]=new Integer(toks[1]);
		return search(arr,scf);
	}
}