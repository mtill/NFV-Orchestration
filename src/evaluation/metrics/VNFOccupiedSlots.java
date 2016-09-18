package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.resources.FreeSlotsResource;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class VNFOccupiedSlots extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		double result = 0d;
		
		for (SubstrateNode s : sNet.getVertices()) {
			FreeSlotsResource c = (FreeSlotsResource) s.get(FreeSlotsResource.class);
			if (c != null)
				result += c.getOccupiedSlots();
		}
		
		return result;
	}

}
