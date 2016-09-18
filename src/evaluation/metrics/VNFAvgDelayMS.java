package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.StaticDelayResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;

public class VNFAvgDelayMS extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		double sum = 0.0d;
		double counter = 0.0d;
		for (List<SubstrateLink> path : getAllSubstratePathElements(sNet).values()) {
			sum += getPathDelay(path);
			counter++;
		}
		
		if (counter == 0.0d)
			return 0.0d;
		
		double result = (sum / counter);
		return result;
	}

	protected static HashMap<VirtualLink, List<SubstrateLink>> getAllSubstratePathElements(SubstrateNetwork sNet) {
		HashMap<VirtualLink, List<SubstrateLink>> paths = new HashMap<VirtualLink, List<SubstrateLink>>();
		for (SubstrateLink l : sNet.getEdges()) {
			for (AbstractResource r : l.get())
				for (Mapping m : r.getMappings()) {
					NetworkEntity<?> owner = (VirtualLink) m.getDemand().getOwner();
					
					if (owner instanceof VirtualLink) {
						VirtualLink vlowner = (VirtualLink) owner;

						List<SubstrateLink> values = paths.get(vlowner);
						if (values == null) {
							values = new LinkedList<SubstrateLink>();
							paths.put(vlowner, values);
						}
						if (!values.contains(l)) {
							values.add(l);
						}
					}
				}
		}
		
		return paths;
	}

	static double getPathDelay(List<SubstrateLink> value) {
		double sum = 0.0d;
		
		for (SubstrateLink sl : value) {
			for (AbstractResource res : sl.get()) {
				if (res instanceof StaticDelayResource) {
					sum += ((StaticDelayResource) res).delayMS;
				}
			}
		}
		
		return sum;
	}

}
