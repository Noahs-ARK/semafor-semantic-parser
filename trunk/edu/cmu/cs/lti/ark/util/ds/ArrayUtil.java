package edu.cmu.cs.lti.ark.util.ds;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2010-10-30
 */

public class ArrayUtil {
	/** 
	 * Convert a List<Integer> object to a primitive array of type int[].
	 * From http://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
	 */
	public static int[] toIntArray(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = integers.get(i).intValue();
	    }
	    return ret;
	}

	public static List<Integer> toArrayList(int[] ii) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i : ii)
			list.add(i);
		return list;
	}

	public static List<String> toArrayList(String[] ss) {
		List<String> list = new ArrayList<String>();
		for (String s : ss)
			list.add(s);
		return list;
	}
}
