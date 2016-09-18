package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;

public class VNFMaxVLinkBandwidthDemand extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {

		HashMap<VirtualLink, Double> values = new HashMap<VirtualLink, Double>();

		for (SubstrateLink sl : sNet.getEdges()) {
			LinkedList<VirtualLink> visited = new LinkedList<VirtualLink>();

			for (AbstractResource r : sl.get()) {
				for (Mapping m : r.getMappings()) {
					AbstractDemand d = m.getDemand();
					NetworkEntity<? extends AbstractConstraint> e = d.getOwner();
					if (e instanceof VirtualLink && d instanceof CommonDemand) {
						VirtualLink vl = (VirtualLink) e;

						if (!visited.contains(vl)) {
							visited.add(vl);

							Double i = values.get(vl);
							if (i == null) {
								i = 0d;
							}
							i+= ((CommonDemand) d).getDemandedCapacity();
							
							values.put(vl, i);

						}
					}
				}
			}
		}

		Entry<VirtualLink, Double> max = null;
		for (Entry<VirtualLink, Double> e : values.entrySet()) {
			if (max == null || e.getValue() > max.getValue())
				max = e;
		}
		
		return (max == null ? 0.0d : max.getValue());
	}

}
