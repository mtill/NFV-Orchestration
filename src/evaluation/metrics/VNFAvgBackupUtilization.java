package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.resources.CommonResource;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class VNFAvgBackupUtilization extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		double num = 0;
		double length = 0;
		for (SubstrateNode sn : sNet.getVertices()) {
			CommonResource r = (CommonResource) sn.get(CommonResource.class);
			if (r != null) {
				boolean found = false;
				for (Mapping m : r.getMappings()) {
					if (((CommonDemand) m.getDemand()).isBackup) {
						found = true;
						num ++;
					}
				}
				
				if (found) {
					length++;
				}
			}
		}
		
		for (SubstrateLink sl : sNet.getEdges()) {
			CommonResource r = (CommonResource) sl.get(CommonResource.class);
			if (r != null) {
				
				boolean found = false;
				for (Mapping m : r.getMappings()) {
					if (((CommonDemand) m.getDemand()).isBackup) {
						found = true;
						num++;
					}
				}
				
				if (found) {
					length++;
				}
			}
		}
		
		if (length == 0)
			return Double.NaN;
		
		return (num / length);
	}

}
