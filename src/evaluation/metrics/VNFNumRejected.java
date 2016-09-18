package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFNumRejected extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		double result = 0.0d;
		
		for (Tuple<VNFChain, LinkedList<EntityMapping>> c : mappingResult.values())
			if (c == null)
				result++;
		
		return result;
	}

}
