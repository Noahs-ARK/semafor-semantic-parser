package edu.cmu.cs.lti.ark.fn.optimization;

import java.util.Map;

import edu.cmu.cs.lti.ark.fn.parsing.FrameFeatures;
import edu.cmu.cs.lti.ark.fn.parsing.SpanAndCorrespondingFeatures;

public class Parameters {
	private double SCORE = 0.0;
	public double[] parameters;
	public double[] total;

	public Parameters(int size) { 	
		parameters = new double[size];
		total = new double[size];
		for(int i = 0; i < parameters.length; i++) {
			parameters[i] = 0.0;
			total[i] = 0.0;
		}
	}

	public void averageParams(double avVal) {
		for(int j = 0; j < total.length; j++)
			total[j] *= 1.0/((double)avVal);		
		parameters = total;
	}

	public double updateParamsMIRA(FrameFeatures actF,
			FeatureVector instFv,
			Map<String, String>[] pred,
			FeatureVector[] bestFv,
			double upd) {
		FeatureVector actFV = instFv;
		int K = 0;
		for(int i = 0; i < bestFv.length && bestFv[i] != null; i++) {
			K = i+1;
		}
		double[] b = new double[K];
		double[] lam_dist = new double[K];
		FeatureVector[] dist = new FeatureVector[K];
		double totalError = 0.0;
		for(int k = 0; k < K; k++) {
			lam_dist[k] = getScore(actFV) - getScore(bestFv[k]);
			b[k] = (double) numErrorsPrecision(actF, pred[k]);
			totalError += b[k];
			b[k] -= lam_dist[k];
			dist[k] = actFV.getDistVector(bestFv[k]);
		}
		double[] alpha;
		alpha = hildreth(dist,b);
		FeatureVector fv  = null;
		for(int k = 0; k < K; k++) {
			fv = dist[k];
			fv.update(parameters, total, alpha[k], upd);	    
		}
		return totalError;
	}

	private double numErrors(FrameFeatures actF, 
			Map<String, String> pred) {
		int actSize = actF.fElements.size();
		if (actSize != pred.size()) {
			System.out.println("Problem. The sizes do not match. Exiting");
			System.exit(-1);
		}
		double totalErrors = 0.0;
		for (int i = 0; i < actSize; i++) {
			String fe = actF.fElements.get(i);
			SpanAndCorrespondingFeatures[] spans = 
				actF.fElementSpansAndFeatures.get(i);
			String actSpan = spans[actF.fGoldSpans.get(i)].span[0] + "_" + spans[actF.fGoldSpans.get(i)].span[1];
			if (!pred.containsKey(fe)) {
				System.out.println("Fe: " + fe + " not in map. Exiting.");
				System.exit(-1);
			}
			if (!actSpan.equals(pred.get(fe))) {
				totalErrors += 1.0;
			}
		}
		return totalErrors;
	}
	
	private double numErrorsFMeasure(FrameFeatures actF, 
			Map<String, String> pred) {
		int actSize = actF.fElements.size();
		if (actSize != pred.size()) {
			System.out.println("Problem. The sizes do not match. Exiting");
			System.exit(-1);
		}
		double matches = 0.0;
		double totalGold = 0.0;
		double totalPred = 0.0;
		for (int i = 0; i < actSize; i++) {
			String fe = actF.fElements.get(i);
			SpanAndCorrespondingFeatures[] spans = 
				actF.fElementSpansAndFeatures.get(i);
			String actSpan = spans[actF.fGoldSpans.get(i)].span[0] + "_" + spans[actF.fGoldSpans.get(i)].span[1];
			if (!pred.containsKey(fe)) {
				System.out.println("Fe: " + fe + " not in map. Exiting.");
				System.exit(-1);
			}
			if (!pred.get(fe).equals("-1_-1")) {
				totalPred += 1.0;
				if (pred.get(fe).equals(actSpan)) {
					matches += 1.0;
				}
			}
			if (!actSpan.equals("-1_-1")) {
				totalGold += 1.0;
			}
		}
		double p = matches / totalPred;
		double e = matches / totalGold;
		double f;
		if (matches == 0) {
			f = 0.0;
		} else {
			f = 2 * p * e / (p + e);
		}
		return (1 - f);
	}
	
	private double numErrorsPrecision(FrameFeatures actF, 
			Map<String, String> pred) {
		int actSize = actF.fElements.size();
		if (actSize != pred.size()) {
			System.out.println("Problem. The sizes do not match. Exiting");
			System.exit(-1);
		}
		double matches = 0.0;
		double totalGold = 0.0;
		double totalPred = 0.0;
		for (int i = 0; i < actSize; i++) {
			String fe = actF.fElements.get(i);
			SpanAndCorrespondingFeatures[] spans = 
				actF.fElementSpansAndFeatures.get(i);
			String actSpan = spans[actF.fGoldSpans.get(i)].span[0] + "_" + spans[actF.fGoldSpans.get(i)].span[1];
			if (!pred.containsKey(fe)) {
				System.out.println("Fe: " + fe + " not in map. Exiting.");
				System.exit(-1);
			}
			if (!pred.get(fe).equals("-1_-1")) {
				totalPred += 1.0;
				if (pred.get(fe).equals(actSpan)) {
					matches += 1.0;
				}
			}
			if (!actSpan.equals("-1_-1")) {
				totalGold += 1.0;
			}
		}
		double p = matches / totalPred;
		if (totalPred != 0) {
			return (1 - p);
		} else {
			return 0;
		}
	}

	public double getScore(FeatureVector fv) {
		return fv.getScore(parameters);
	}

	private double[] hildreth(FeatureVector[] a, double[] b) {

		int i;
		int max_iter = 10000;
		double eps = 0.00000001;
		double zero = 0.000000000001;

		double[] alpha = new double[b.length];

		double[] F = new double[b.length];
		double[] kkt = new double[b.length];
		double max_kkt = Double.NEGATIVE_INFINITY;

		int K = a.length;

		double[][] A = new double[K][K];
		boolean[] is_computed = new boolean[K];
		for(i = 0; i < K; i++) {
			A[i][i] = a[i].dotProduct(a[i]);
			is_computed[i] = false;
		}

		int max_kkt_i = -1;


		for(i = 0; i < F.length; i++) {
			F[i] = b[i];
			kkt[i] = F[i];
			if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }
		}

		int iter = 0;
		double diff_alpha;
		double try_alpha;
		double add_alpha;

		while(max_kkt >= eps && iter < max_iter) {

			diff_alpha = A[max_kkt_i][max_kkt_i] <= zero ? 0.0 : F[max_kkt_i]/A[max_kkt_i][max_kkt_i];
			try_alpha = alpha[max_kkt_i] + diff_alpha;
			add_alpha = 0.0;

			if(try_alpha < 0.0)
				add_alpha = -1.0 * alpha[max_kkt_i];
			else
				add_alpha = diff_alpha;

			alpha[max_kkt_i] = alpha[max_kkt_i] + add_alpha;

			if (!is_computed[max_kkt_i]) {
				for(i = 0; i < K; i++) {
					A[i][max_kkt_i] = a[i].dotProduct(a[max_kkt_i]); // for version 1
					is_computed[max_kkt_i] = true;
				}
			}

			for(i = 0; i < F.length; i++) {
				F[i] -= add_alpha * A[i][max_kkt_i];
				kkt[i] = F[i];
				if(alpha[i] > zero)
					kkt[i] = Math.abs(F[i]);
			}

			max_kkt = Double.NEGATIVE_INFINITY;
			max_kkt_i = -1;
			for(i = 0; i < F.length; i++)
				if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }

			iter++;
		}

		return alpha;
	}

}
