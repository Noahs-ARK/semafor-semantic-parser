package edu.cmu.cs.lti.ark.util.optimization;


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
	
	/** Uses the DCA algorithm as described in a Martins et al. paper draft.
	 * 
	 * From Eq. 15 (for CRFs):
	 *     θ_{t+1} = θ_t − η_t * (E_{θ_t}[φ(x_t, Y_t)] − φ(x_t, y_t))
	 * =??           θ_t − η_t * (gradient)
	 * with m training examples and learning rate η_t = 
	 *     min { 1/(λm), − log(P_{θ_t}(y_t | x_t)) / ||E_{θ_t}[φ(x_t, Y_t)] − φ(x_t, y_t)||^2
	 * =?? min { 1/(λm), − (log-value of the training data under the current model) / (squared L2 norm of the gradient) }
	 * where λ is the regularization parameter.
	 */
	public static double[] updateGradientDCA(double[] params, double[] derivatives, 
			double value, double numTrainingExamples, double regularizationParameter) {
		double learningRate = Math.min(1.0/(regularizationParameter*numTrainingExamples),
									   -value/sqsum(derivatives));
		return updateGradient(params, derivatives, learningRate);
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
	
	/** Computes the sum of the squares of values in the vector. */
	public static double sqsum(double[] vec) {
		double sqsum = 0.0;
		for (double v : vec) {
			sqsum += v*v;
		}
		return sqsum;
	}
}


