package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.CommonResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;

public class VNFAllPathLength extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {

		HashMap<VirtualLink, Integer> lengths = new HashMap<VirtualLink, Integer>();

		for (SubstrateLink sl : sNet.getEdges()) {
			CommonResource bw = (CommonResource) sl.get(CommonResource.class);

			for (Mapping m : bw.getMappings()) {
				AbstractDemand d = m.getDemand();
				NetworkEntity<? extends AbstractConstraint> e = d
						.getOwner();
				if (e instanceof VirtualLink) {
					VirtualLink vl = (VirtualLink) e;
					Integer i = lengths.get(vl);
					if (i == null) {
						i = 1;
					} else {
						i++;
					}
					lengths.put(vl, i);
				}
			}
		}

		double sum = 0;
		for (Integer i : lengths.values()) {
			sum += i;
		}

		double size = (double) lengths.keySet().size();
		return (lengths.keySet().size() == 0 ? 0.0 : (sum / size));
	}

}
