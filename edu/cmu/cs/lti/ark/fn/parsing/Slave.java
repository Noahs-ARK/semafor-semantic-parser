package edu.cmu.cs.lti.ark.fn.parsing;

public interface Slave {
	public double[] makeZUpdate(double rho, double[] us, double[] lambdas, double[] zs);
}
