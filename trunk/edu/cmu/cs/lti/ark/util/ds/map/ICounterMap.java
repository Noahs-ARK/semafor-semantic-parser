package edu.cmu.cs.lti.ark.util.ds.map;

import java.util.Map;

public interface ICounterMap<K, L, V extends Number, C extends AbstractCounter<L,V>> extends Map<K,C> {
	public C getCounter(K key);
	
	public V getCount(K key1, L key2);
	
	public V increment(K key1, L key2);
	public V incrementBy(K key1, L key2, V delta);
}
