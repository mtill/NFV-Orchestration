package networks;

import java.util.LinkedList;

import algorithm.Tuples.Tuple;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.demands.FreeSlotsDemand;
import vnreal.network.virtual.VirtualLink;

public class VNFChainEntityCreator {

	private final VNFFG VNFFG;
	private LinkedList<VNF> createdTerminatingNodes = new LinkedList<VNF>();

	public VNFChainEntityCreator(VNFFG VNFFG) {
		this.VNFFG = VNFFG;
	}
	
	public Tuple<VNFChainNode, LinkedList<AbstractDemand>> createVNode(VNF VNF, Double datarateMBits, boolean registerDemands) {
		VNFChainNode result = new VNFChainNode(VNF, this.VNFFG.layer);
		result.setName(VNF.name);
		
		return new Tuple<VNFChainNode, LinkedList<AbstractDemand>>(result, createVNodeDemands(result, VNF, datarateMBits, registerDemands));
	}
	
	public Tuple<VirtualLink, CommonDemand> createVLink(CommonDemand linkDemand, Double flowDataRate, boolean registerDemands) {
		VirtualLink vLink = new VirtualLink(VNFFG.layer);
		
		return new Tuple<VirtualLink, CommonDemand>(vLink, createVLinkDemand(vLink, linkDemand, flowDataRate, registerDemands));
	}

	private LinkedList<AbstractDemand> createVNodeDemands(VNFChainNode result, VNF VNF, Double datarateMBits, boolean registerDemands) {
		LinkedList<AbstractDemand> demands = new LinkedList<AbstractDemand>();
		
		//result.setName("(name:" + VNF.type + "; VNFid:" + VNF.id + "; id: " + result.getId() + ")");
		
		if (VNF.IdDemand != null) {
			AbstractDemand d = VNF.IdDemand.getCopy(result);
			demands.add(d);
			if (registerDemands)
				result.add(d);
		}
		
		if (VNF.FreeSlotsDemand != null) {
			FreeSlotsDemand copy = VNF.FreeSlotsDemand.getCopy(result);
//			copy.setOriginalOwnerDemands(result.get()); //NB!
			demands.add(copy);
			if (registerDemands)
				result.add(copy);
		}
		
		double capacity = 0.0d;
		if (VNF.staticModel) {
			capacity = VNF.capacityPer100MBits;
			
			if (this.VNFFG.terminatingNodes.contains(VNF)) {  // is terminating node?
				if (!this.createdTerminatingNodes.contains(VNF)) {  // instance already created?
					capacity = 0.0d;  // only demand capacity for the first instance
					this.createdTerminatingNodes.add(VNF);
				}
			}
		} else if (datarateMBits != null) {
			capacity = Math.floor(VNF.capacityPer100MBits * (datarateMBits / 100.0d));
		}
		
		if (capacity > 0.0d) {
			CommonDemand d = new CommonDemand(capacity, result);
			demands.add(d);
			if (registerDemands)
				result.add(d);
		}
		
		return demands;
	}

	private CommonDemand createVLinkDemand(VirtualLink vLink, CommonDemand linkDemand, Double flowDataRate, boolean registerDemands) {
//		LinkedList<AbstractDemand> demands = new LinkedList<AbstractDemand>();
		
		if (flowDataRate != null) {
			CommonDemand bwdemand = new CommonDemand(flowDataRate, vLink);
//			demands.add(bwdemand);
			if (registerDemands)
				vLink.add(bwdemand);
			
			return bwdemand;
		}
		
		return null;

//		if (linkDemands != null) {
//			for (AbstractDemand d : linkDemands) {
//				AbstractDemand copy = d.getCopy(vLink);
//				demands.add(copy);
//				if (registerDemands)
//					vLink.add(copy);
//			}
//		}
//		
//		return demands;
	}
}
