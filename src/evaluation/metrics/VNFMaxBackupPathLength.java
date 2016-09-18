package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.resources.CommonResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;

public class VNFMaxBackupPathLength extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {

		HashMap<VirtualLink, Integer> lengths = new HashMap<VirtualLink, Integer>();

		for (SubstrateLink sl : sNet.getEdges()) {
			CommonResource bw = (CommonResource) sl.get(CommonResource.class);
			if (bw == null)
				continue;

			for (Mapping m : bw.getMappings()) {
				CommonDemand d = (CommonDemand) m.getDemand();
				if (!d.isBackup)
					continue;
				
				NetworkEntity<? extends AbstractConstraint> e = d.getOwner();
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

		Entry<VirtualLink, Integer> max = null;
		for (Entry<VirtualLink, Integer> e : lengths.entrySet()) {
			if (max == null || e.getValue() > max.getValue())
				max = e;
		}
		
		return (max == null ? 0.0d : max.getValue());
	}

}
