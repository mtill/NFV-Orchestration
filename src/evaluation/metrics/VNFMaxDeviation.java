package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFMaxDeviation extends VNFEvaluationMetric {
	
	public Double deviation;
	
	public VNFMaxDeviation() {
		this.deviation = 0d;
	}
	
	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		return deviation;
	}

}
