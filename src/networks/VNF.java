package networks;

import java.util.Collection;
import java.util.LinkedList;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.FreeSlotsDemand;
import vnreal.constraints.demands.IdDemand;

public class VNF {
	
	public final long id;
	public String name, type;
	
	public double capacityPer100MBits = 0d;
	public FreeSlotsDemand FreeSlotsDemand = null;
	public IdDemand IdDemand = null;
	
	public final LinkedList<VNFInputInterface> inputInterfaces;
	
	public final LinkedList<VNFLink> outLinks;
	
	
	public final boolean staticModel;
	
	public boolean needsBackup;
	
	public VNF(long id, String type, String name, boolean staticModel) {
		this.needsBackup = false;
		this.type = type;
		
		this.name = (name == null ? ("VNF" + id) : name);
		
		this.id = id;
		
		this.inputInterfaces = new LinkedList<VNFInputInterface>();
//		this.inputInterfaces.add(new VNFInputInterface(this));
		
		this.outLinks = new LinkedList<VNFLink>();
		
		this.staticModel = staticModel;
	}
	
	public VNF(String type, String name, boolean staticModel) {
		this(VNFIDSource.getID(), type, name, staticModel);
	}
	
	public VNF(long id, boolean staticModel) {
		this(id, null, null, staticModel);
	}
	
	public VNF(boolean staticModel) {
		this(VNFIDSource.getID(), staticModel);
	}
	
	public String toString() {
//		return toString("");
		return this.id + "(" + this.name + "): inputInterfaces:[" + inputInterfaces + "] outLinks:[" + outLinks + "]";
	}
	
	public String toString(String prefix) {
//		String links = "";
//		for (VNFLink l : outLinks)
//			links += "\n  " + prefix + (l == null ? "" : l);
//		return name + " capacityPer100MBits:" + capacityPer100MBits + " FreeSlotsDemand:" + FreeSlotsDemand +
//					" IdDemand:" + IdDemand + " " + links + " inputInterfaces:" + inputInterfaces +
//				    " outLinks:" + links;
		return Utils.toString(prefix, this);
	}
	
	public static class VNFInputInterface {
		
		public final VNF VNF;
		public final Collection<VNF> requiredFlowVNFs;
		public final Collection<VNFLink> requiredFlowVNFLinks;
		
		public VNFInputInterface(VNF VNF) {
			this.VNF = VNF;
			this.requiredFlowVNFs = new LinkedList<VNF>();
			this.requiredFlowVNFLinks = new LinkedList<VNFLink>();
		}
		
		public String toString() {
			String requiredFlowVNFsS = "";
			for (VNF v : requiredFlowVNFs)
				requiredFlowVNFsS += v.id + ";";
			String requiredFlowVNFLinksS = "";
			int i = 0;
			for (VNFLink v : requiredFlowVNFLinks) {
				requiredFlowVNFLinksS += v.source.id+"->"+i+";";
				i++;
			}
			return "requiredFlowVNFs:" + requiredFlowVNFsS + " requiredFlowVNFLinks:" + requiredFlowVNFLinksS;
		}
		
	}
	
//	public static class VNFInstance {
//		public final VNF VNF;
//		public VNFInstance(VNF VNF) {
//			this.VNF = VNF;
//		}
//	}
	
}
