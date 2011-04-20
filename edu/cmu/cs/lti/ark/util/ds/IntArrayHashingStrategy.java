package edu.cmu.cs.lti.ark.util.ds;

import java.util.Arrays;

import gnu.trove.TObjectHashingStrategy;

/**
 * Hashing strategy for int[] objects based on their contents. 
 * Necessary because the last expression of:
 * <pre>
 * {@code
 * int[] x = new int[1];
 * x[0] = 1;
 * int[] y = new int[1];
 * y[0] = 1;
 * x.equals(y);
 * }</pre>
 * will be {@code false}.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2010-10-29
 * @see {@link gnu.trove.THashMap#THashMap(TObjectHashingStrategy)}
 */
public class IntArrayHashingStrategy implements TObjectHashingStrategy<int[]> {
	private static final long serialVersionUID = -5635392346633048334L;

	public int computeHashCode(int[] o) {
		return Arrays.hashCode(o);
	}

	public boolean equals(int[] o1, int[] o2) {
		return Arrays.equals(o1, o2);
	}
}
