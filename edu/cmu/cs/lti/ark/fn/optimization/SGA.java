package edu.cmu.cs.lti.ark.fn.optimization;


public class SGA
{
	public static final double LEARNING_RATE = 0.001;
	
	public static double[] updateGradient(double[] params, double[] derivatives)
	{
		int length = params.length;
		for(int i = 0; i < length; i ++)
		{
			params[i] = params[i] - LEARNING_RATE*derivatives[i];
		}
		return params;
	}	
	
	public static double[] updateGradient(double[] params, double[] derivatives, double learningRate)
	{
		int length = params.length;
		for(int i = 0; i < length; i ++)
		{
			params[i] = params[i] - learningRate*derivatives[i];
		}
		return params;
	}
}


