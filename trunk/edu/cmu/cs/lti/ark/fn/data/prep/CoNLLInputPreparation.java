package edu.cmu.cs.lti.ark.fn.data.prep;

public class CoNLLInputPreparation
{
	public static void main(String[] args)
	{
		String posFile = args[0];
		String outFile = args[1];
		ParsePreparation.printCoNLLTypeInput(posFile, outFile);
	}	
	
}