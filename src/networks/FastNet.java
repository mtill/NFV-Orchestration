package networks;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import algorithm.Tuples.Triple;
import networks.VNF.VNFInputInterface;
import vnreal.network.virtual.VirtualLink;

public class FastNet {
	
	final VNFFG VNFFG;
	// stores incoming links!
	HashMap<Node, LinkedList<Triple<Node, VNFInputInterface, Double>>> nodes;
	public final HashMap<Node, Double> nodeDemands;
	public final HashMap<VNF, LinkedList<Node>> VNFInstances;
	Node initialNode = null;
	
	public FastNet(VNFFG VNFFG) {
		this.VNFFG = VNFFG;
		this.nodes = new HashMap<Node, LinkedList<Triple<Node, VNFInputInterface, Double>>>();
		this.nodeDemands = new HashMap<Node, Double>();
		this.VNFInstances = new HashMap<VNF, LinkedList<Node>>();
	}
	
	public FastNet getCopy() {
		FastNet copy = new FastNet(this.VNFFG);
		
		for (Node e : this.nodes.keySet()) {
			copy.addNode(e, this.nodeDemands.get(e));
		}
		copy.initialNode = this.initialNode;
		
		for (Entry<Node, LinkedList<Triple<Node, VNFInputInterface, Double>>> e : this.nodes.entrySet()) {
			if (e.getValue() != null) {
				for (Triple<Node, VNFInputInterface, Double> list : e.getValue()) {
					copy.addLink(list.x, e.getKey(), list.y, list.z);
				}
			}
		}
		
		return copy;
	}
	
	public int getNodeCount() {
		return this.nodes.size();
	}
	
	public void addNode(Node node, double demand) {
		this.nodes.put(node, null);
		
		if (this.initialNode == null)
			this.initialNode = node;
		
		if (node.VNF != null) {
			LinkedList<Node> instances = VNFInstances.get(node.VNF);
			if (instances == null) {
				instances = new LinkedList<Node>();
				VNFInstances.put(node.VNF, instances);
			}
			instances.add(node);
		}
		
		this.nodeDemands.put(node,  demand);
	}
	
	public void addLink(Node from, Node to, VNFInputInterface VNFInputInterface, double demand) {
		
//		if (!this.nodes.containsKey(from))
//			throw new AssertionError(from);
//		if (!this.nodes.containsKey(to))
//			throw new AssertionError(to);
		
		
		LinkedList<Triple<Node, VNFInputInterface, Double>> incomingLinks = this.nodes.get(to);
		if (incomingLinks == null) {
			incomingLinks = new LinkedList<Triple<Node, VNFInputInterface, Double>>();
			this.nodes.put(to, incomingLinks);
		}
		
		incomingLinks.add(new Triple<Node, VNFInputInterface, Double>(from, VNFInputInterface, demand));
	}
	
	public LinkedList<Triple<Node, VNFInputInterface, Double>> getInLinks(Node node) {
		LinkedList<Triple<Node, VNFInputInterface, Double>> inedges = this.nodes.get(node);
		return inedges == null ? new LinkedList<Triple<Node, VNFInputInterface, Double>>() : inedges;
	}
	
	public VNFChain convert(int layer) {
		VNFChainEntityCreator creator = new VNFChainEntityCreator(this.VNFFG);
		VNFChain result = new VNFChain(layer);
		HashMap<Node, VNFChainNode> mapping = new HashMap<Node, VNFChainNode>();
		
		VNFChainNode initNode = creator.createVNode(this.initialNode.VNF, this.nodeDemands.get(this.initialNode), true).x;
		mapping.put(this.initialNode, initNode);
		result.addVertex(initNode, this.initialNode.VNF);
		
		for (Node n : this.nodes.keySet()) {
			if (n == this.initialNode)
				continue;
			
			VNFChainNode newNode = creator.createVNode(n.VNF, this.nodeDemands.get(n), true).x;
			mapping.put(n, newNode);
			result.addVertex(newNode, newNode.getVNF());
		}
		for (Entry<Node, LinkedList<Triple<Node, VNFInputInterface, Double>>> e : this.nodes.entrySet()) {
			if (e.getValue() != null) {
				VNFChainNode to = mapping.get(e.getKey());
				for (Triple<Node, VNFInputInterface, Double> f : e.getValue()) {
					VNFChainNode from = mapping.get(f.x);
					VirtualLink vlink = creator.createVLink(null, f.z, true).x;
					vlink.VNFInputInterface = f.y;
					result.addEdge(vlink, from, to);
				}
			}
		}
		
		return result;
	}
	
	public static class Node {
		VNF VNF;
		
		public Node(VNF VNF) {
			this.VNF = VNF;
		}
	}
	
}
