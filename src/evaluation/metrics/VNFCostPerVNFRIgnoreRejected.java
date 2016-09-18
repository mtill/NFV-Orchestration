package evaluation.metrics;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CommonResource;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class VNFCostPerVNFRIgnoreRejected extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		double counter = 0.0d;
		for (Tuple<VNFChain, LinkedList<EntityMapping>> c : mappingResult.values())
			if (c != null)
				counter++;
		
		if (counter == 0.0d)
			return null;
		
		double nodeCost = 0;
		double linkCost = 0;
		for (Iterator<SubstrateLink> tmpSLink = sNet.getEdges()
				.iterator(); tmpSLink.hasNext();) {
			SubstrateLink currSLink = tmpSLink.next();
			for (AbstractResource res : currSLink) {
				if (res instanceof CommonResource) {
					for (Mapping f : res.getMappings()) {
						CommonDemand tmpBwDem = (CommonDemand) f.getDemand();
						linkCost += tmpBwDem.getDemandedCapacity();
					}
				}
			}
		}
		for (Iterator<SubstrateNode> tmpNode = sNet
				.getVertices().iterator(); tmpNode.hasNext();) {
			SubstrateNode tmps = tmpNode.next();
			for (AbstractResource res : tmps) {
				if (res instanceof CommonResource) {
					for (Mapping f : res.getMappings()) {
						CommonDemand tmpCpuDem = (CommonDemand) f.getDemand();
						nodeCost += tmpCpuDem.getDemandedCapacity();
					}
				}
			}
		}
		return ((nodeCost + linkCost) / counter);
	}

}
