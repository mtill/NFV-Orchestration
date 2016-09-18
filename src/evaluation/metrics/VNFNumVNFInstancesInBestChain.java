package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFNumVNFInstancesInBestChain extends VNFEvaluationMetric {

	private COORDVNF_STRATEGY strategy;

	public VNFNumVNFInstancesInBestChain(COORDVNF_STRATEGY strategy) {
		this.strategy = strategy;
	}

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		double result = 0d;
		
		for (VNFFG c : mappingResult.keySet()) {
			Double thisresult = null;
			LinkedList<VNFChain> chains = c.getAllVNFChainTopologies(-1, -1, strategy);
			for (VNFChain chain : chains) {
				if (thisresult == null || thisresult > chain.getVertexCount())
					thisresult = (double) chain.getVertexCount();
			}
			
			if (thisresult != null)
				result += thisresult;
		}
		
		return result;
	}

}
