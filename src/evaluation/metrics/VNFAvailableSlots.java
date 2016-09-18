package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.FreeSlotsResource;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class VNFAvailableSlots extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		double result = 0d;
		
		for (SubstrateNode s : sNet.getVertices()) {
			for (AbstractResource res : s.get()) {
				if (res instanceof FreeSlotsResource) {
					result += ((FreeSlotsResource) res).getAvailableSlots();
				}
			}
		}
		
		return result;
	}

}
