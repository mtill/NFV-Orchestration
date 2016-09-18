package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFMeanRemainingBandwidth extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		double sum = 0.0d;
		
		for (SubstrateLink sl : sNet.getEdges())
			sum += Utils.getBandwidthAvailable(sl);
		
		double result = sum / ((double) sNet.getEdgeCount());
		return result;
	}

}
