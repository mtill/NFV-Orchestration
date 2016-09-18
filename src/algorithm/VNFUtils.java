package algorithm;


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import networks.VNF;
import networks.VNF.VNFInputInterface;
import networks.VNFChain;
import networks.VNFFG;
import networks.VNFLink;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.demands.FreeSlotsDemand;
import vnreal.constraints.demands.IdDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CommonResource;
import vnreal.constraints.resources.FreeSlotsResource;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNode;

public class VNFUtils {
	
	public static SubstrateNode getRandomNode(Random random, SubstrateNetwork sNet) {
		LinkedList<SubstrateNode> rndNodes = new LinkedList<SubstrateNode>(sNet.getVertices());
		Collections.shuffle(rndNodes, random);
		return rndNodes.getFirst();
	}

	//	private static boolean alreadyMapped(
	//			List<AbstractDemand> n1,
	//			List<AbstractResource> n2) {
	//		
	//		boolean alreadyMappedHere = false;
	//		for (AbstractDemand d : n1) {
	//			boolean lastOneFound = false;
	//			resLoop: for (AbstractResource r : n2) {
	//				for (Mapping m : r.getMappings()) {
	//					if (m.getDemand() == d) {
	//						// this demand has previously been mapped.
	//						alreadyMappedHere = true;
	//						lastOneFound = true;
	//						break resLoop;
	//					}
	//				}
	//			}
	//			
	//			if (alreadyMappedHere && !lastOneFound)
	//				throw new AssertionError();
	//		}
	//		
	//		return alreadyMappedHere;
	//	}

	public static AbstractDemand addDemand(NetworkEntity<AbstractDemand> e, AbstractDemand c, boolean merge) {
		AbstractDemand old = null;
		if (e != null)
			old = (AbstractDemand) e.get(c.getClass());
		if (old == null) {
			if (e != null)
				e.add(c);
			return c;
		} else {
			if (merge) {
				if (!c.getMappings().isEmpty()) {
					throw new AssertionError();
				}
				
				if (c instanceof FreeSlotsDemand) {
					FreeSlotsDemand d = (FreeSlotsDemand) c;
					FreeSlotsDemand oldd = (FreeSlotsDemand) old;
					if (!d.demandedLabel.equals(oldd.demandedLabel) || !d.VNFType.equals(oldd.VNFType))
						throw new AssertionError();
				} else if (c instanceof CommonDemand) {
					CommonDemand d = (CommonDemand) c;
					CommonDemand oldd = (CommonDemand) old;
					oldd.setDemandedCapacity(oldd.getDemandedCapacity() + d.getDemandedCapacity());
				} else if (c instanceof CommonDemand) {
					CommonDemand d = (CommonDemand) c;
					CommonDemand oldd = (CommonDemand) old;
					oldd.setDemandedCapacity(oldd.getDemandedCapacity() + d.getDemandedCapacity());
				} else if (c instanceof IdDemand) {
					IdDemand d = (IdDemand) c;
					IdDemand oldd = (IdDemand) old;
					if (!d.getDemandedId().equals(oldd.getDemandedId()))
						throw new AssertionError();
				} else {
					throw new AssertionError(c);
				}
				
				if (old.getName() == null)
					old.setName(c.getName());
				else if (c.getName() != null && !old.getName().equals(c.getName()))
					old.setName(old.getName() + " " + c.getName());
				
				return old;
			} else {
				throw new AssertionError();
			}
		}
	}
	
	public static AbstractResource getResource(Collection<AbstractResource> resources, Class<? extends AbstractResource> res) {
		for (AbstractResource r : resources)
			if (r.getClass().isAssignableFrom(res)) {
				return r;
			}

		return null;
	}
	
	public static double getCapacityDemand(VNFChain chain) {
		double cpusum = 0.0d;
		for (VirtualNode n : chain.getVertices()) {
			CommonDemand cpudemand = (CommonDemand) VNFUtils.getDemand(n.get(), CommonDemand.class);
			if (cpudemand != null)
				cpusum += cpudemand.getDemandedCapacity();
		}
		return cpusum;
	}
	
	public static HashMap<String, Double> getAvailCapacityResources(SubstrateNetwork sNet) {
		HashMap<String, Double> result = new HashMap<String, Double>();
		
		for (SubstrateNode n : sNet.getVertices()) {
			CommonResource cpures = (CommonResource) n.get(CommonResource.class);
			if (cpures != null) {
				FreeSlotsResource slots = (FreeSlotsResource) n.get(FreeSlotsResource.class);
				String slotLabel = slots == null ? "null" : slots.label;
				
				Double value = result.get(slotLabel);
				if (value == null)
					value = 0.0d;
				value += cpures.getAvailableCapacity();
				
				result.put(slotLabel, value);
			}
		}
		
		return result;
	}
	
	public static AbstractDemand getDemand(Collection<AbstractDemand> demands, Class<? extends AbstractDemand> dem) {
		for (AbstractDemand d : demands)
			if (d.getClass().isAssignableFrom(dem)) {
				return d;
			}

		return null;
	}

	public static CommonDemand getBandwidthDemand(NetworkEntity<? extends AbstractDemand> n) {
		for (AbstractDemand dem : n.get()) {
			if (dem instanceof CommonDemand) {
				return ((CommonDemand) dem);
			}
		}
		return null;
	}

	
//	public static LinkedList<VirtualLink> getRandomEdges(VirtualNetwork tree, int num) {
//		LinkedList<VirtualLink> result = new LinkedList<VirtualLink>();
//		
//		LinkedList<VirtualLink> edges = new LinkedList<VirtualLink>(tree.getEdges());
//		Collections.shuffle(edges);
//		
//		int i = 0;
//		for (VirtualLink l : edges) {
//			if (i >= num)
//				break;
//			
//			result.add(l);
//			i++;
//		}
//		
//		return result;
//	}

	public static void sortVNFCandidates(LinkedList<Pair<VNF, VNFInputInterface>> VNFCandidatesPairs, COORDVNF_STRATEGY strategy, VNFFG VNFFG) {
		
//		Collections.shuffle(VNFCandidatesPairs, random);
		
		Collections.sort(VNFCandidatesPairs, new Comparator<Pair<VNF, VNFInputInterface>>() {
			@Override
			public int compare(Pair<VNF, VNFInputInterface> o1, Pair<VNF, VNFInputInterface> o2) {
				
				if (strategy == COORDVNF_STRATEGY.BANDWIDTH) {
					double o1sum = getTotalTrafficRate(o1.getFirst());
					double o2sum = getTotalTrafficRate(o2.getFirst());
					if (o1sum > o2sum)
						return +1;
					if (o1sum < o2sum)
						return -1;
				}
				
//				if (strategy == COORDVNF_STRATEGY.VNF_INSTANCES) {
//				if (o1.getFirst().outLinks.size() > o2.getFirst().outLinks.size())
//					return +1;
//				if (o1.getFirst().outLinks.size() < o2.getFirst().outLinks.size())
//					return -1;
				// }

				
//				int indexo1 = VNFCandidatesPairs.indexOf(o1);
//				int indexo2 = VNFCandidatesPairs.indexOf(o2);
//				if (indexo1 < indexo2)
//					return -1;
//				if (indexo1 > indexo2)
//					return 1;
				
				return 0;
			}
		});
	}

	public static double getTotalTrafficRate(VNF VNF) {
		double result = 0.0d;

		if (VNF.outLinks.isEmpty())
			result += 1.0d;
		for (VNFLink l : VNF.outLinks)
			result += l.dataratepercentage;

		return result;
	}
	
	
	public static void printTree(SubstrateNetwork backtrackingGraph, SubstrateNode root) {
		printTree(backtrackingGraph, root, "");
	}
	
	private static void printTree(SubstrateNetwork backtrackingGraph, SubstrateNode current, String prefix) {
		System.out.println(prefix + current.getName());
		for (SubstrateLink l : backtrackingGraph.getOutEdges(current)) {
			printTree(backtrackingGraph, backtrackingGraph.getOpposite(current, l), prefix + "  ");
		}
	}
	
	
	public static SubstrateNode addBacktrackingNode(SubstrateNetwork backtrackingGraph,
			String name,
			SubstrateNode backtrackingParentNode,
			String backtrackingPrefix) {
		
		SubstrateNode nodeID = null;
		
		if (backtrackingGraph != null) {
			nodeID = new SubstrateNode();
			nodeID.setName(name);
			backtrackingGraph.addVertex(nodeID);

			if (backtrackingParentNode != null) {
				SubstrateLink edgeID = new SubstrateLink();
				edgeID.setName(null);
				backtrackingGraph.addEdge(edgeID, backtrackingParentNode, nodeID);
			}
		}
		
//		System.out.println(backtrackingPrefix + name);
		System.out.println(backtrackingGraph.getVertexCount() + " backtracking steps");
		
		return nodeID;
	}
	

	public static double getNR(SubstrateNetwork G, SubstrateNode y, int depth, double capacityWeight) {
		double result = 0.0d;
		
		LinkedList<SubstrateNode> visited = new LinkedList<SubstrateNode>();
		LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>> queue = new LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>>();
		queue.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(y, new LinkedList<SubstrateLink>()));
		visited.add(y);
		CommonResource r = (CommonResource) y.get(CommonResource.class);
		result += r.getAvailableCapacity();

		while (!queue.isEmpty()) {
			Tuple<SubstrateNode, LinkedList<SubstrateLink>> entry = queue.poll();
			if (depth == -1 || entry.y.size() > depth)
				continue;
			
			LinkedList<SubstrateLink> outEdges = new LinkedList<SubstrateLink>(G.getOutEdges(entry.x));
			for (SubstrateLink out : outEdges) {

				CommonResource bw = (CommonResource) out.get(CommonResource.class);
				result += bw.getAvailableCapacity();

				SubstrateNode opp = G.getOpposite(entry.x, out);
				if (!visited.contains(opp)) {
					visited.add(opp);

					LinkedList<SubstrateLink> path = new LinkedList<SubstrateLink>(entry.y);
					path.add(out);

					CommonResource r2 = (CommonResource) opp.get(CommonResource.class);
					result += r2.getAvailableCapacity();

					queue.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(opp, path));
				}
			}
		}
		
//		for (SubstrateLink l : G.getOutEdges(y)) {
//			BandwidthResource bw = (BandwidthResource) l.get(BandwidthResource.class);
//			result += bw.getAvailableBandwidth();
//			
//			SubstrateNode y2 = G.getDest(l);
//			CapacityResource r2 = (CapacityResource) y2.get(CapacityResource.class);
//			result += r2.getAvailableCapacity();
//		}
		
//		for (SubstrateLink l : G.getInEdges(y)) {
//			BandwidthResource bw = (BandwidthResource) l.get(BandwidthResource.class);
//			result += bw.getAvailableBandwidth();
//			
//			SubstrateNode y2 = G.getSource(l);
//			CapacityResource r2 = (CapacityResource) y2.get(CapacityResource.class);
//			result += r2.getAvailableCapacity();
//		}
		
		return result;
	}

}
