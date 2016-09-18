package algorithm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithmParameters.VNFAlgorithmParameter;
import evaluation.metrics.AcceptedVnrRatio;
import evaluation.metrics.VNFAllPathLength;
import evaluation.metrics.VNFAvailableSlots;
import evaluation.metrics.VNFAvgBWPerLink;
import evaluation.metrics.VNFAvgDelayMS;
import evaluation.metrics.VNFCost;
import evaluation.metrics.VNFCostPerVNFR;
import evaluation.metrics.VNFCostPerVNFRIgnoreRejected;
import evaluation.metrics.VNFEvaluationMetric;
import evaluation.metrics.VNFMaxDelayMS;
import evaluation.metrics.VNFMaxPathLength;
import evaluation.metrics.VNFMaxVLinkBandwidthDemand;
import evaluation.metrics.VNFMeanRemainingBandwidth;
import evaluation.metrics.VNFNumRejected;
import evaluation.metrics.VNFNumVNFInstances;
import evaluation.metrics.VNFNumVNFInstancesInBestChain;
import evaluation.metrics.VNFNumVirtualLinks;
import evaluation.metrics.VNFOccupiedSlots;
import evaluation.metrics.VNFRunningNodes;
import evaluation.metrics.VNFRuntimeMS;
import evaluation.metrics.VNFSuccess;
import evaluation.metrics.VNFTotalRemainingBandwidth;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public abstract class VNFAlgorithm {

	public final String name;
	public Integer timeoutSNSize = null;
	public Integer timeoutMaxPathLength = null;
	boolean isCanceled = false;
	SubstrateNetwork G = null;
	
	public VNFAlgorithm() {
		this.name = this.getClass().getSimpleName();
		this.timeoutSNSize = -1;
	}
	
	public VNFAlgorithm(String name) {
		this.name = name;
		this.timeoutSNSize = -1;
	}
	
	public void init(SubstrateNetwork G, VNFAlgorithmParameter param) {
		this.G = G;
	}
	
	public abstract HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mapVNFFGs(Random random, VNFAlgorithmParameter param, LinkedList<VNFFG> VNFFGs);

	public abstract VNFAlgorithmParameters getAlgorithmParams();
	
	public void unmap(SubstrateNetwork sNet, VirtualNetwork vNet) {
		Utils.clearVnrMappings(sNet, vNet.getLayer());
	}
	
	public void setCanceled() {
		this.isCanceled = true;
	}
	
	public void reset() {
	}
	
	public LinkedList<VNFEvaluationMetric> getMetrics(VNFAlgorithmParameter param, double elapsedTimeMS) {
		LinkedList<VNFEvaluationMetric> result = new LinkedList<VNFEvaluationMetric>();
		
		result.add(new AcceptedVnrRatio());
		result.add(new VNFSuccess());
		result.add(new VNFNumRejected());
		
//		result.add(new VNFAssignedResources());
//		result.add(new VNFAssignedFreeSlotsResources());
//		result.add(new VNFAssignedCapacityResources());
//		result.add(new VNFAssignedBandwidthResources());
		
		result.add(new VNFCost());
//		result.add(new VNFCostBW());
//		result.add(new VNFCostCapacity());
		result.add(new VNFCostPerVNFR());
		result.add(new VNFCostPerVNFRIgnoreRejected());
//		result.add(new VNFBWCostPerVNFRIgnoreRejected());
		
//		result.add(new VNFRevenue(param.strategy, false));
//		result.add(new VNFAllRevenueCost(param.strategy));
//		result.add(new VNFAllRevenueCostBW(param.strategy));
//		result.add(new VNFAllRevenueCostCapacity(param.strategy));
//		result.add(new VNFSuccRevenueCost(param.strategy));
//		result.add(new VNFSuccRevenueCostBW(param.strategy));
//		result.add(new VNFSuccRevenueCostCapacity(param.strategy));

		
		result.add(new VNFAllPathLength());
		result.add(new VNFMaxPathLength());
		result.add(new VNFAvgBWPerLink());
		result.add(new VNFMeanRemainingBandwidth());
		result.add(new VNFTotalRemainingBandwidth());
		result.add(new VNFMaxVLinkBandwidthDemand());
		
		result.add(new VNFAvgDelayMS());
		result.add(new VNFMaxDelayMS());
		
		result.add(new VNFNumVNFInstancesInBestChain(param.strategy));
		result.add(new VNFNumVNFInstances());
		result.add(new VNFNumVirtualLinks());
		result.add(new VNFRunningNodes());
		result.add(new VNFAvailableSlots());
		result.add(new VNFOccupiedSlots());
		
		result.add(new VNFRuntimeMS(elapsedTimeMS));
		
		return result;
	}

}
