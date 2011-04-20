package edu.cmu.cs.lti.ark.fn.identification;

import java.io.Serializable;

import edu.cmu.cs.lti.ark.util.optimization.Alphabet;
import edu.cmu.cs.lti.ark.util.optimization.LDouble;
import gnu.trove.THashMap;


public class IdentificationModel implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3073796529341340548L;
	private Alphabet A;
	private THashMap<String,LDouble> paramMap;
	
	public IdentificationModel(Alphabet a, THashMap<String,LDouble> map)
	{
		A = a;
		paramMap = map;
	}
	
	public Alphabet getAlphabet()
	{
		return A;
	}
	
	public THashMap<String,LDouble> getParamMap()
	{
		return paramMap;
	}
	
	
}

