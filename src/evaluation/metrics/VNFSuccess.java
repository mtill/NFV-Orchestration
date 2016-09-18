package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class VNFSuccess extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		LinkedList<Integer> layers = new LinkedList<Integer>();
		
		for (SubstrateNode n : sNet.getVertices()) {
			for (AbstractResource r : n.get()) {
				for (Mapping m : r.getMappings()) {
					if (m.getDemand().getOwner() != null) {
						int layer = m.getDemand().getOwner().getLayer();
						if (!layers.contains(layer)) {
							layers.add(layer);
						}
					}
				}
			}
		}
		
		return ((double) layers.size());
	}

}
