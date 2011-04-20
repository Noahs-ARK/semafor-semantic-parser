package edu.cmu.cs.lti.ark.util.ds.path;

import java.util.List;

import edu.cmu.cs.lti.ark.util.Indexer;

/**
 * {@link Path} subclass in which elements are integers that index a list of objects.
 * The {@link #apply(List)} method retrieves the indexed objects from a list.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2010-10-31
 *
 * @param <N> Type of objects in the indexed list
 */

public class IndexedPath<N> extends Path<Integer> implements Indexer<List<Integer>,List<N>,Path<N>> {
	private static final long serialVersionUID = -7986750107457067782L;

	public IndexedPath() { }
	public IndexedPath(IndexedPath<N> path) { super(path); }
	public IndexedPath(List<Integer> list) {
		super(list);
	}
	public IndexedPath(List<Integer> list, boolean overrideCycleCheck) {
		super(list, overrideCycleCheck);
	}
	public IndexedPath(List<Integer> list, boolean includeSource, boolean allowCycles, boolean allowSelfLoops) {
		super(list);
		this._includeSource = includeSource;
	}
	@Override
	public Path<N> apply(List<N> nodes) {
		Path<N> path = new Path<N>();
		path._includeSource = this._includeSource;
		for (Integer item : this) {
			path.add(nodes.get(item));
		}
		return path;
	}
	@Override
	public List<Integer> indices() {
		return this;
	}
	
	@Override
	public IndexedPath<N> getSuffix(int maxLength) {
		assert maxLength>0;
		return new IndexedPath<N>(this.subList(Math.max(0, this.size()-maxLength), this.size()), this._includeSource, this._allowCycles, this._allowSelfLoops);
	}
	@Override
	public IndexedPath<N> getPrefix(int maxLength) {
		assert maxLength>0;
		return new IndexedPath<N>(this.subList(0, Math.min(this.size(), this.size()+maxLength)), this._includeSource, this._allowCycles, this._allowSelfLoops);
	}
	
	@Override
	public Object clone() {
		return new IndexedPath<N>(this);
	}
}
