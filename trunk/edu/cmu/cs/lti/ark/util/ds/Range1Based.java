/**
 * 
 */
package edu.cmu.cs.lti.ark.util.ds;

/**
 * A range of values whose smallest legal index is 1. Setting the start or end position to 0 is an error. 
 * (Negative values may be used for "non-normal" indices.) 
 * This is to distinguish 1-based ranges from 0-based ranges when both are used.
 * @author Nathan Schneider (nschneid)
 * @since 2009-06-25
 */
public class Range1Based extends Range {

	/**
	 * Converts a 0-based range to a 1-based range by adding 1 to the start and end indices
	 * @param r A 0-based range
	 */
	public Range1Based(Range0Based r) {
		this(r.getStart()+1, r.getEnd()+1, r.isEndInclusive());
	}
	
	/**
	 * Creates a new range with start and end indices computed relative to those of an existing range.
	 * @param r Range serving as a point of reference
	 * @param deltaStart Amount to add to the start position in the provided range
	 * @param deltaEnd Amount to add to the end position in the provided range
	 */
	public Range1Based(Range r, int deltaStart, int deltaEnd) {
		this(r.getStart()+deltaStart, r.getEnd()+deltaEnd, r.isEndInclusive());
	}
	
	/**
	 * @param startPosition
	 * @param endPosition
	 */
	public Range1Based(int startPosition, int endPosition) {
		super(1, startPosition, endPosition);
	}

	/**
	 * @param startPosition
	 * @param endPosition
	 * @param isEndInclusive
	 */
	public Range1Based(int startPosition, int endPosition, boolean isEndInclusive) {
		super(1, startPosition, endPosition, isEndInclusive);
	}
	
	/**
	 * Creates and returns a range immediately following the current range and having the specified length.
	 * @param newLength
	 */
	public Range1Based successor(int newLength) {
		return new Range1Based(this.start+this.length(), this.start+this.length()+newLength + ((this.endInclusive) ? -1 : 0), this.endInclusive);
	}

	public Range1Based clone() {
		return new Range1Based(this.start, this.end, this.endInclusive);
	}
}
