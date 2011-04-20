package edu.cmu.cs.lti.ark.util;

/**
 * Interface for classes that store indices to some other class of objects.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2010-11-04
 */
public interface Indexer<I,T,U> {
	/** Given a target object, apply the stored index and return the object referred to by that index. */
	U apply(T target);
	I indices();
}
