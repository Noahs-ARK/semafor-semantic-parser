package edu.cmu.cs.lti.ark.util;

import java.util.Iterator;

/**
 * Utilities for working with strings.
 * @author Nathan Schneider (nschneid)
 * @since 2009-09-24
 */
public class StringUtil {
	
	/**
	 * Source: http://snippets.dzone.com/posts/show/91
	 * @param <T>
	 * @param objs
	 * @param delimiter
	 * @return String representations of the Iterable object's elements joined together with the given delimiter
	 */
	public static <T> String join(final Iterable<T> objs, final String delimiter) {
	    Iterator<T> iter = objs.iterator();
	    if (!iter.hasNext())
	        return "";
	    StringBuffer buffer = new StringBuffer(String.valueOf(iter.next()));
	    while (iter.hasNext())
	        buffer.append(delimiter).append(String.valueOf(iter.next()));
	    return buffer.toString();
	}

	public static String join(Object[] items, String delimiter) {
		StringBuffer buffer = new StringBuffer("");
		boolean first = true;
		for (Object item : items) {
			if (!first)
				buffer.append(delimiter);
			buffer.append(String.valueOf(item));
			first = false;
		}
		return buffer.toString();
	}
}
