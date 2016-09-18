package networks;


import java.util.HashMap;
import java.util.LinkedList;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class VNFChain extends VirtualNetwork {
	private static final long serialVersionUID = 1L;
	
	public VNFChainNode initialVertex = null;
	static int counter = 0;
	
	public final HashMap<VNF, LinkedList<VNFChainNode>> VNFInstances;
	
	public VNFChain(int layer) {
		super(layer, false);
		setName("VNFChain" + counter++ + "(layer:" + layer + ")");
		this.VNFInstances = new HashMap<VNF, LinkedList<VNFChainNode>>();
	}

	@Override
	public boolean addVertex(VirtualNode vertex) {
		throw new AssertionError();
	}
	
	public boolean addVertex(VirtualNode vertex, VNF VNF) {
		if (!(vertex instanceof VNFChainNode))
			throw new AssertionError();
		
		VNFChainNode v = (VNFChainNode) vertex;
		
		boolean result = super.addVertex(v);
		if (result) {
			if (this.getVertexCount() == 1)
				this.initialVertex = v;
			
			if (VNF != null) {
				LinkedList<VNFChainNode> instances = VNFInstances.get(VNF);
				if (instances == null) {
					instances = new LinkedList<VNFChainNode>();
					VNFInstances.put(VNF, instances);
				}
				instances.add(v);
			}
		}
		return result;
	}
	
	@Override
	public boolean removeVertex(VirtualNode v) {
		if (super.removeVertex(v)) {
			for (LinkedList<VNFChainNode> list : VNFInstances.values())
				list.remove(v);
			return true;
		}
		
		return false;
	}

	@Override
	public boolean addEdge(VirtualLink edge, VirtualNode v, VirtualNode w) {
		if (w == initialVertex)
//			throw new AssertionError(v.getId() + "->" + w.getId() + "\n" + this);
			throw new AssertionError(((VNFChainNode) v).getId() + "->" + ((VNFChainNode) w).getId() + "\n" + this);
		boolean result = super.addEdge(edge, v, w);
//		if (this.getInEdges(w).size() > 1)
//			throw new AssertionError(this.getInEdges(w).size());
		return result;
	}
	
	public VNFChain getCopy(boolean deepCopy) {
		VNFChain result = new VNFChain(this.getLayer());
		
		HashMap<VNFChainNode, VNFChainNode> map = new HashMap<VNFChainNode, VNFChainNode>();
		if (this.initialVertex != null) {
			if (deepCopy) {
				VNFChainNode copy = this.initialVertex.getCopy(true);
				result.addVertex(copy, this.initialVertex.getVNF());
				map.put(this.initialVertex, copy);
			} else {
				result.addVertex(this.initialVertex, this.initialVertex.getVNF());
			}
		}
		
		for (VirtualNode vnode : getVertices()) {
			VNFChainNode v = (VNFChainNode) vnode;
			if (v == this.initialVertex)
				continue;
			
			if (deepCopy) {
				VNFChainNode copy = v.getCopy(deepCopy);
				result.addVertex(copy, v.getVNF());
				map.put(v, copy);
			} else {
				result.addVertex(v, v.getVNF());
			}
		}
		
		for (VirtualLink vlink : getEdges()) {
			VNFChainNode source = (VNFChainNode) getSource(vlink);
			VNFChainNode dest = (VNFChainNode) getDest(vlink);
			if (deepCopy) {
				VirtualLink vlinkcopy = vlink.getCopy(true);
				result.addEdge(vlinkcopy, map.get(source), map.get(dest));
			} else {
				result.addEdge(vlink, source, dest);
			}
		}
		
		return result;
	}
	
	public boolean containsAllInputInterfacesOf(VNFChain c) {
		outer: for (VirtualLink l : c.getEdges()) {
			for (VirtualLink l2 : this.getEdges()) {
				if (l.VNFInputInterface == l2.VNFInputInterface) {
					continue outer;
				}
			}
			return false;
		}
	
		outer: for (VirtualNode n : c.getVertices()) {
			VNFChainNode nn = (VNFChainNode) n;
			for (VirtualNode n2 : this.getVertices()) {
				VNFChainNode n2n = (VNFChainNode) n2;
				if (nn.VNFid == n2n.VNFid) {
					continue outer;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		String result = "\nVNFChain " + getName() + ": (init: " + (this.initialVertex == null ? "null" : this.initialVertex.getId()) + ")\n\nNODES:\n";
		for (VirtualNode n : getVertices()) {
			result += n.toString() + "\n";
		}

		result += "\nEDGES:\n";
		for (VirtualLink l : getEdges()) {
			result += "id:" + l.getId() + " name:" + l.getName() + "  (" + getSource(l).getId() + "-->"
					+ getDest(l).getId() + ")\n";
			for (AbstractDemand d : l.get()) {
				result += "  " + d.toString() + "\n";
			}
		}

		return result;
	}
	
	public LinkedList<VirtualNode> getUnmappedNodes(VNFChain vNet) {
		LinkedList<VirtualNode> result = new LinkedList<VirtualNode>(this.getVertices());
		
		for (VirtualLink l : this.getEdges()) {
			VNFChainNode s = (VNFChainNode) this.getSource(l);
			VNFChainNode d = (VNFChainNode) this.getDest(l);

			for (VirtualLink l2 : vNet.getEdges()) {
				VNFChainNode s2 = (VNFChainNode) vNet.getSource(l2);
				VNFChainNode d2 = (VNFChainNode) vNet.getDest(l2);				
				
				if (s.VNFid == s2.VNFid && d.VNFid == d2.VNFid) {
					result.remove(s);
					result.remove(d);
					break;
				}
			}
		}
		
		return result;
	}

	public boolean containsNet(VNFChain vNet) {
		outer: for (VirtualNode n : vNet.getVertices()) {
			for (VirtualNode n2 : this.getVertices()) {
				if (((VNFChainNode) n).VNFid == ((VNFChainNode) n2).VNFid)
					continue outer;
			}
			
			return false;
		}
		
		for (VirtualLink l : vNet.getEdges()) {
			VNFChainNode s = (VNFChainNode) vNet.getSource(l);
			VNFChainNode d = (VNFChainNode) vNet.getDest(l);
			
			boolean same = false;
			for (VirtualLink l2 : this.getEdges()) {
				if (l.VNFInputInterface == l2.VNFInputInterface) {
					VNFChainNode s2 = (VNFChainNode) this.getSource(l2);
					VNFChainNode d2 = (VNFChainNode) this.getDest(l2);
					
					if (s.VNFid == s2.VNFid && d.VNFid == d2.VNFid) {
						same = true;
						break;
					}
				}
			}
			if (!same)
				return false;
		}
		
		return true;
	}

}
