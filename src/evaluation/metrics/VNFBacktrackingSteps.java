package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFBacktrackingSteps extends VNFEvaluationMetric {
	
	public Double backtrackingSteps;
	
	public VNFBacktrackingSteps() {
		this.backtrackingSteps = 0.0d;
	}
	
	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		return backtrackingSteps;
	}

}
