package edu.cmu.cs.lti.ark.fn.parsing;

import java.util.HashMap;

public class CustomOptions {
	private HashMap<String ,String>opts;
	public CustomOptions(String args[]){
		opts=new HashMap<String, String>();
		for(String opt:args){
			String toks[]=opt.split(":");
			if(toks.length>1){
				opts.put(toks[0], toks[1]);
			}
			else if(toks.length>0){
				opts.put(toks[0], "\t");
			}
		}
	}
	public String get(String optionName){
		if(!opts.containsKey(optionName)){
			System.err.println("option not present:"+optionName);
			return "";
		}
		return opts.get(optionName);
	}
	public boolean isPresent(String optionName){
		return opts.containsKey(optionName);
	}
}
