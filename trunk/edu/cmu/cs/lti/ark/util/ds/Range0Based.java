/**
 * 
 */
package edu.cmu.cs.lti.ark.util.ds;

/**
 * A range of values whose smallest normal index is 0. (Negative values may be used for "non-normal" indices.) 
 * This is to distinguish 0-based ranges from 1-based ranges when both are used.
 * @author Nathan Schneider (nschneid)
 * @since 2009-06-25
 */
public class Range0Based extends Range {
	
	/**
	 * Converts a 1-based range to a 0-based range by subtracting 1 from the start and end indices
	 * @param r A 1-based range
	 */
	public Range0Based(Range1Based r) {
		this(r.getStart()-1, r.getEnd()-1, r.isEndInclusive());
	}
	
	/**
	 * Creates a new range with start and end indices computed relative to those of an existing range.
	 * @param r Range serving as a point of reference
	 * @param deltaStart Amount to add to the start position in the provided range
	 * @param deltaEnd Amount to add to the end position in the provided range
	 */
	public Range0Based(Range r, int deltaStart, int deltaEnd) {
		this(r.getStart()+deltaStart, r.getEnd()+deltaEnd, r.isEndInclusive());
	}
	
	/**
	 * @param startPosition
	 * @param endPosition
	 */
	public Range0Based(int startPosition, int endPosition) {
		super(0, startPosition, endPosition);
	}

	/**
	 * @param startPosition
	 * @param endPosition
	 * @param isEndInclusive
	 */
	public Range0Based(int startPosition, int endPosition, boolean isEndInclusive) {
		super(0, startPosition, endPosition, isEndInclusive);
	}
	
	/**
	 * Creates and returns a range immediately following the current range and having the specified length.
	 * @param newLength
	 */
	public Range0Based successor(int newLength) {
		return new Range0Based(this.start+this.length(), this.start+this.length()+newLength + ((this.endInclusive) ? -1 : 0), this.endInclusive);
	}

	public Range0Based clone() {
		return new Range0Based(this.start, this.end, this.endInclusive);
	}
}
