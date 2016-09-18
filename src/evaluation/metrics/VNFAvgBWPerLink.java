package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;

public class VNFAvgBWPerLink extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {

		HashMap<VirtualLink, Double> bwInfo = new HashMap<VirtualLink, Double>();

		for (SubstrateLink sl : sNet.getEdges()) {
			LinkedList<VirtualLink> visited = new LinkedList<VirtualLink>();

			for (AbstractResource r : sl.get()) {
				for (Mapping m : r.getMappings()) {
					AbstractDemand d = m.getDemand();
					NetworkEntity<? extends AbstractConstraint> e = d.getOwner();
					if (e instanceof VirtualLink) {
						VirtualLink vl = (VirtualLink) e;

						if (!visited.contains(vl)) {
							visited.add(vl);

							Double i = bwInfo.get(vl);
							if (i == null) {
								i = Utils.getBandwidthDemand(vl);
							} else {
								i += Utils.getBandwidthDemand(vl);
							}
							bwInfo.put(vl, i);

						}
					}
				}
			}
		}

		double sum = 0;
		for (Double i : bwInfo.values()) {
			sum += i;
		}

		return (bwInfo.keySet().size() == 0 ? 0.0d : (sum / (double) bwInfo.keySet().size()));
	}

}
