package edu.cmu.cs.lti.ark.util;

/**
 * A function with a return value.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2010-11-01
 *
 * @param <R> Return type
 * @see {@link Subroutine}
 */

public interface Function<R> {
	R $(Object ...args);
}
