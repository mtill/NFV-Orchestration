package networks;

import java.util.LinkedList;

import networks.VNF.VNFInputInterface;

public class VNFFlowInfo {
	
	public LinkedList<VNF> flowNodes = new LinkedList<VNF>();
	public LinkedList<VNFLink> flowLinks = new LinkedList<VNFLink>();
	public LinkedList<VNFInputInterface> flowInputInterfaces = new LinkedList<VNFInputInterface>();
	public VNFFlowInfo getCopy() {
		VNFFlowInfo r = new VNFFlowInfo();
		r.flowNodes = new LinkedList<VNF>(this.flowNodes);
		r.flowLinks = new LinkedList<VNFLink>(this.flowLinks);
		r.flowInputInterfaces = new LinkedList<VNFInputInterface>(this.flowInputInterfaces);
		return r;
	}
	
	public void add(VNFFlowInfo i) {
		if (i == null)
			return;
		
		for (VNF v : i.flowNodes)
			if (!this.flowNodes.contains(v))
				this.flowNodes.add(v);
		
		for (VNFLink v : i.flowLinks)
			if (!this.flowLinks.contains(v))
				this.flowLinks.add(v);
		
		for (VNFInputInterface v : i.flowInputInterfaces)
			if (!this.flowInputInterfaces.contains(v))
				this.flowInputInterfaces.add(v);
	}

}
