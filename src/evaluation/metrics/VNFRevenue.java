package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import algorithm.VNFUtils;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.demands.CommonDemand;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNode;

public class VNFRevenue extends VNFEvaluationMetric {

	final boolean successfulOnly;
	final COORDVNF_STRATEGY strategy;

	public VNFRevenue(COORDVNF_STRATEGY strategy, boolean successfulOnly) {
		this.successfulOnly = successfulOnly;
		this.strategy = strategy;
	}

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {

		Double result = null;

		for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> entry : mappingResult.entrySet()) {
			if (successfulOnly && entry.getValue() == null)
				continue;

			LinkedList<VNFChain> chains = entry.getKey().getAllVNFChainTopologies(-1, -1, strategy);

			for (VNFChain chain : chains) {
				double thisresult = 0.0d;
				double cpusum = 0.0d;
				double bwsum = 0.0d;
				for (VirtualNode n : chain.getVertices()) {
					CommonDemand cpudemand = (CommonDemand) VNFUtils.getDemand(n.get(), CommonDemand.class);
					if (cpudemand != null)
						cpusum += cpudemand.getDemandedCapacity();
				}
				for (VirtualLink l : chain.getEdges()) {
					CommonDemand bwdemand = VNFUtils.getBandwidthDemand(l);
					bwsum += bwdemand.getDemandedCapacity();
				}
				thisresult += (cpusum + bwsum);
				
				if (result == null || result > thisresult)
					result = thisresult;
			}

		}

		return (result == null ? 0.0d : result);
	}

}
