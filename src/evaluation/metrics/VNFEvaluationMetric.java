package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;


public abstract class VNFEvaluationMetric {
	
	public abstract Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult);
	
	
	public static double[] ci(double[] all) {
        // compute sample mean
        double sumx = 0.0;
        for (int i = 0; i < all.length; ++i) {
            sumx  += all[i];
        }
        double mean = sumx / all.length;

        // compute sample variance
        double xxbar = 0.0;
        for (int i = 0; i < all.length; i++) {
            xxbar += (all[i] - mean) * (all[i] - mean);
        }
        double variance = xxbar / (all.length - 1);
        double stddev = Math.sqrt(variance);
        
        double lo = mean - 1.96 * stddev;
        double hi = mean + 1.96 * stddev;
        
        return new double[] {mean, lo, hi};
	}
	
}
