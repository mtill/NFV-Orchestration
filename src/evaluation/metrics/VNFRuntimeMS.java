package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFRuntimeMS extends VNFEvaluationMetric {
	
	double elapsedTimeMS;
	
	public VNFRuntimeMS(double elapsedTimeMS) {
		this.elapsedTimeMS = elapsedTimeMS;
	}

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		return elapsedTimeMS;
	}

}
