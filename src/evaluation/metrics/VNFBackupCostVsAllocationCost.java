package evaluation.metrics;



import java.util.HashMap;
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

public class VNFBackupCostVsAllocationCost extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		double nodeCost = 0;
		double nodeBackupCost = 0;
		
		double linkCost = 0;
		double linkBackupCost = 0;
		
		
		for (SubstrateLink currSLink : sNet.getEdges()) {
			for (AbstractResource res : currSLink.get()) {
				if (res instanceof CommonResource) {
					
					for (Mapping m : res.getMappings()) {
						CommonDemand bwdem = (CommonDemand) ((CommonDemand) m.getDemand());
						if (bwdem.isBackup) {
							linkBackupCost += bwdem.getDemandedCapacity();
						} else {
							linkCost += bwdem.getDemandedCapacity();
						}
					}
				}
			}
		}
		for (SubstrateNode tmps : sNet.getVertices()) {
			for (AbstractResource res : tmps.get()) {
				if (res instanceof CommonResource) {
//					nodeCost += ((CommonResource) res).getOccupiedCapacity();
					
					for (Mapping m : res.getMappings()) {
						CommonDemand capdem = (CommonDemand) m.getDemand();
						if (capdem.isBackup) {
							nodeBackupCost += capdem.getDemandedCapacity();
						} else {
							nodeCost += capdem.getDemandedCapacity();
						}
					}
				}
			}
		}
		
		double backupCost = (nodeBackupCost + linkBackupCost);
		double cost = (nodeCost + linkCost);
		
		if (cost == 0.0d)
			return 0.0d;
		
		return (backupCost / cost);
	}

}
