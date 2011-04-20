package edu.cmu.cs.lti.ark.fn.evaluation;

import java.util.ArrayList;
import java.util.StringTokenizer;



public class ParseUtils
{
	/**
	 * 
	 * @param segs Lines from a .seg.data file ??
	 * @return
	 */
	public static ArrayList<String> getRightInputForFrameIdentification(ArrayList<String> segs)
	{
		ArrayList<String> result = new ArrayList<String>();
		int size = segs.size();
		for(int i = 0; i < size; i ++)
		{
			String line = segs.get(i).trim();
			StringTokenizer st = new StringTokenizer(line,"\t");
			while(st.hasMoreTokens())
			{
				String tok = st.nextToken();
				int lastInd = tok.lastIndexOf("#");
				String rest = tok.substring(lastInd+1);
				if(rest.equals("true"))	// token(s) comprise a gold target
				{
					String ind = tok.substring(0,lastInd);	// the token number(s) for the potential target
					result.add("Null\t"+ind+"\t"+i);
				}
			}
		}		
		return result;
	}	
	
	public void buildIdentificationXML(ArrayList<String> ids, ArrayList<String> originalSentences, String outFile)
	{
				
		
	}
	
	
}