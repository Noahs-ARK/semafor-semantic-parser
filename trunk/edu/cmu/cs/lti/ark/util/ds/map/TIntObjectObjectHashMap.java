package edu.cmu.cs.lti.ark.util.ds.map;


import gnu.trove.THashMap;
import gnu.trove.TIntObjectHashMap;



public class TIntObjectObjectHashMap<V,K>
{
	TIntObjectHashMap<THashMap<V,K>> map;
	
	public TIntObjectObjectHashMap()
	{
		map = new TIntObjectHashMap<THashMap<V,K>>();
	}
	
	public K get(int one, V two)
	{
		THashMap<V,K> val = map.get(one);
		if(val==null)
			return null;
		return val.get(two);
	}
	
	public K put(int one, V two, K val)
	{
		THashMap<V,K> firstVal = map.get(one);
		if(firstVal==null)
		{
			firstVal = new THashMap<V,K>();
			map.put(one, firstVal);
		}
		return firstVal.put(two, val);
	}
	
	public int[] keys()
	{
		return map.keys();
	}	
	
	public THashMap<V,K> getValueMap(int i)
	{
		return map.get(i);
	}
}