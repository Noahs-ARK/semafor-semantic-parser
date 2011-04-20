package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class FrameFeatures implements Serializable
{
	private static final long serialVersionUID = 1628884148325532841L;
	public String frameName;
	public int start;
	public int end;
	String goldFrameElement;
	
	public ArrayList<String> fElements;
	public ArrayList<SpanAndCorrespondingFeatures[]> fElementSpansAndFeatures;
	public ArrayList<Integer> fGoldSpans;
		
	public FrameFeatures(String fn, int fs, int fe)
	{
		frameName=""+fn;
		fs = start;
		fe = end;
		fElements = new ArrayList<String>();
		fElementSpansAndFeatures = new ArrayList<SpanAndCorrespondingFeatures[]>();
		fGoldSpans = new ArrayList<Integer>();
	}	
}

