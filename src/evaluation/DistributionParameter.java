package evaluation;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;

public class DistributionParameter {

	public final int numEvents;
	public final double lambda;
	public final double mu_L;

	public DistributionParameter(int numEvents, double lambda, double mu_L) {
		this.numEvents = numEvents;
		this.lambda = lambda;
		this.mu_L = mu_L;
	}
	
	public String getSuffix(String prefix) {
		return Utils.toString(this);	}

	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", " = ", true);
	}

}
