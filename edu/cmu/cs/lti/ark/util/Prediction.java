package edu.cmu.cs.lti.ark.util;

/**
 * Wrapper for a predicted value or object and an associated score.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2009-11-17
 *
 * @param <T> Type of the predicted object
 */
public class Prediction<T> {
	T _item;
	double _score;
	
	public Prediction(T item, double score) {
		_item = item;
		_score = score;
	}
	public T getItem() { return _item; }
	public double getScore() { return _score; }
}
