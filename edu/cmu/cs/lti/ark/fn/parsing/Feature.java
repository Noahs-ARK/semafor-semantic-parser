package edu.cmu.cs.lti.ark.fn.parsing;

public class Feature implements Comparable<Feature>{
	public int index;
	public double value;
	public String name;
	//public boolean hasValue;
	/*public Feature(String tok){
		String toks[]=tok.split("==");
		index=Integer.parseInt(toks[0]);
		if(toks.length>1){
			value=Double.parseDouble(toks[1]);
		}
		else{
			value=1;
		}
	}*/
	public Feature (int i , double v ){
		this.index=i ;
		this.value=v ;
		name="UKN";
	}
	public Feature (int i){
		this.index=i ;
		this.value=1 ;
		name="UKN";
	}
	public Feature (int i,String n){
		this.index=i ;
		this.value=1 ;
		this.name=n;
	}
	public Feature (String n){
		this.index=0 ;
		this.value=1 ;
		this.name=n;
	}
	public Feature (String n,double v){
		this.index=0 ;
		this.value=v ;
		this.name=n;
	}
	public Feature (int i,String n,double v){
		this.index=i ;
		this.value=v ;
		this.name=n;
	}
	public int compareTo(Feature a){
		if(a.value>value)return -1;
		if(a.value<value)return 1;
		return 0;
	}
	public boolean equals(Object b){
		if(!(b instanceof Feature))return false;
		Feature f=(Feature)b;
		return f.value==value && f.index==index;
	}
	public String toString(){
		return name+"=="+value;
	}
}
