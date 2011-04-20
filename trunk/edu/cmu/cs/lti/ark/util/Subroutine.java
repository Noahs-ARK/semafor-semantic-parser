package edu.cmu.cs.lti.ark.util;

/**
 * A routine with no return value.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2010-11-01
 * @see {@link Function}
 */

public interface Subroutine {
	void $(Object ...args);
}
