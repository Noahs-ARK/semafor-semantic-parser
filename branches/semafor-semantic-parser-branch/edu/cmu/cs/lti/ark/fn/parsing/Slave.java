package edu.cmu.cs.lti.ark.fn.parsing;

public interface Slave {
	public double[] makeZUpdate(double rho, double[] us, double[] lambdas, double[] zs);
	public void cache(double[] as, double[] zs);
	public boolean checkEquals(double[] as);
}
