package edu.cmu.cs.lti.ark.fn.identification;

import gnu.trove.TObjectIntHashMap;

import java.io.Serializable;
import java.util.ArrayList;

public class Graph implements Serializable {
	private static final long serialVersionUID = -1255114597229118205L;
	public String[] predicates;
	public int[][] frames;
	public TObjectIntHashMap<String> f2Idx;
	public ArrayList<String> idx2F;
}