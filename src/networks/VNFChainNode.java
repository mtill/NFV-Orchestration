package networks;

import java.util.LinkedList;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.network.virtual.VirtualNode;

public class VNFChainNode extends VirtualNode {
	
	public final long VNFid;
	private final VNF VNF;
	public LinkedList<AbstractDemand> backupDemands = null;
	
	public VNF getVNF() {
		return VNF;
	}
	
	public VNFChainNode(VNF VNF, int layer) {
		super(layer);
		this.VNFid = VNF.id;
		this.VNF = VNF;
	}
	
	@Override
	public String toString() {
		String demString = "";
		for (AbstractDemand d : get())
			demString += "  " + d.toString() + "\n";
		return "VNFChainNode " + getName() + "(id:" + getId() + ")\n" + demString;
	}
	
	@Override
	public VNFChainNode getCopy(boolean deepCopy) {
		VNFChainNode clone = new VNFChainNode(VNF, getLayer());
		clone.setName(getName());

		for (AbstractDemand r : this) {
			if (deepCopy)
				clone.add(r.getCopy(clone));
			else
				clone.add(r);
		}

		return clone;
	}
	
}
