package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFAllRevenueCost extends VNFEvaluationMetric {

	COORDVNF_STRATEGY strategy;
	
	public VNFAllRevenueCost(COORDVNF_STRATEGY strategy) {
		this.strategy = strategy;
	}
	
	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		VNFRevenue rev = new VNFRevenue(strategy, false);
		VNFCost cost = new VNFCost();
		
		double costvalue = cost.calculate(sNet, mappingResult);
		if (costvalue == 0.0d)
			return 0.0d;
		
		double revvalue = rev.calculate(sNet, mappingResult);
		
		double result = revvalue / costvalue;
		return result;
	}
}
