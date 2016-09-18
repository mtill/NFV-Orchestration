package networks;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Pair;

import algorithm.Tuples.Triple;
import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import algorithm.VNFUtils;
import networks.FastNet.Node;
import networks.VNF.VNFInputInterface;
import vnreal.constraints.demands.CommonDemand;
import vnreal.io.GraphMLExporter;
import vnreal.network.virtual.VirtualLink;

public class VNFFG {

	// initialNode and terminatingNode may not be added to the list of dependencies!
	public final int layer;
	public final LinkedList<VNF> dependencies;
	public Double initialBW = null;
	public final LinkedList<DelayConstraint> delayConstraints;

	protected VNF initialNode = null;
	protected final LinkedList<VNF> terminatingNodes;
	
	public static void main(String[] args) throws IOException {
		VNFFG v = new VNFFG(1);
		v.initialBW = 100d;
		
		VNF init = new VNF(0, false);
		VNFLink o1 = new VNFLink(init, null, true);
		o1.dataratepercentage = 1.0;
		VNFLink o2 = new VNFLink(init, null, true);
		o2.dataratepercentage = 1.0;
		v.setInitialNode(init);
		
//		VNF vnf1 = new VNF(1, false);
//		new VNFLink(vnf1, null, true).dataratepercentage = 1.0;
//		v.dependencies.add(vnf1);
		
		VNF vnf1 = new VNF(1, false);
		v.dependencies.add(vnf1);
		VNFLink o3 = new VNFLink(vnf1, null, true);
		o3.dataratepercentage = 1.0;
		VNFInputInterface i1 = new VNFInputInterface(vnf1);
		vnf1.inputInterfaces.add(i1);
		i1.requiredFlowVNFLinks.add(o1);
		VNFInputInterface i2 = new VNFInputInterface(vnf1);
		vnf1.inputInterfaces.add(i2);
		i2.requiredFlowVNFLinks.add(o2);

		VNF vnf2 = new VNF(2, false);
		v.dependencies.add(vnf2);
		VNFInputInterface i3 = new VNFInputInterface(vnf2);
		vnf2.inputInterfaces.add(i3);
		i3.requiredFlowVNFLinks.add(o2);
		
		
//		VNFFG c = ChainingPaperVNFFGs.req100(1, false);
		
		LinkedList<VNFChain> res = v.getAllVNFChainTopologies(-1, -1, COORDVNF_STRATEGY.BANDWIDTH);
		System.out.println("RESULT:");
		
		try {
			Runtime.getRuntime().exec(new String[] {"/usr/bin/bash", "-c", "/bin/rm /home/user/workspace/papers/VNF-FGE/code/src_DAG/*.graphml"}).waitFor();
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}

		int i = 0;
		for (VNFChain c : res) {
			System.out.println(c);
			GraphMLExporter.export("chain" + i + ".graphml", c);
			i++;
		}
		
		
//		LinkedList<VNFFG> VNFFGs = new LinkedList<VNFFG>();
//		VNFFGs.add(v);
//		
////		VNFAlgorithmEvaluation.DEBUG = true;
////		ResilientCoordVNF.DEBUG = true;
//		
//		
//		final int numLabels = 3;
//		final int numVNFTypesPerLabel = 10;
//		
//		
////		final Integer[] numVNFChainsArray = { 1,2,3,4,5,6,7,8,9,10 };
//		final Integer[] numVNFChainsArray = { 1 };
//		final Integer[] numVNFsPerChainArray = new Integer[] { 5 };
//		final double edgesToRemove = 0.8;
////		final Double[] discreteProbabilities = new Double[] { 1.0 };
//		final Double[] discreteProbabilities = new Double[] { 0.7,0.20,0.1 };
//		final Double[] initialDataRatesArray = new Double[] { 50d };
////		final Double[] bandwidthPercentages = new Double[] {0.6, 0.8, 1.0};
//		final Double minBandwidthPercentages = 0.75;
//		final Double maxBandwidthPercentages = 1.25;
//		final Double[] capacityPer100MBits = new Double[] { 100d };
//		final boolean staticModel = false;
//
//
//		final RandomVNFFGDemandGenerator RandomVNFFGDemandGenerator =
//				new RandomVNFFGDemandGenerator(numLabels, numVNFTypesPerLabel, false, capacityPer100MBits, minBandwidthPercentages, maxBandwidthPercentages);
//		HashMap<String, LinkedList<String>> labelsAndVNFTypes = RandomVNFFGDemandGenerator.generateLabelsAndTypes();
//		
//		final RandomVNFFGGeneratorParameters VNFFGGeneratorParameters =
//				new RandomVNFFGGeneratorParameters(RandomVNFFGDemandGenerator, numVNFsPerChainArray, discreteProbabilities, edgesToRemove, initialDataRatesArray);
//
//		
//		final int Barabasi_sEdgesToAttach = 2;
//		final Integer[] Barabasi_sNumTimeStepsArray = { 30 };
//		
//		final BarabasiAlbertNetworkGeneratorParameters sNetParams = new BarabasiAlbertNetworkGeneratorParameters(Barabasi_sEdgesToAttach, Barabasi_sNumTimeStepsArray);
//		final BarabasiAlbertNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator = BarabasiAlbertNetworkGenerator.createSubstrateNetworkInstance();
//
//		final Integer minFreeSlotsResources = 4;
//		final Integer maxFreeSlotsResources = 4;
//		final Integer minCapacity = 50;
//		final Integer maxCapacity = 100;
//		final Integer minBandwidthResource = 50;
//		final Integer maxBandwidthResource = 100;
//		final Integer minDelayResource = -1;
//		final Integer maxDelayResource = -1;
//
//		final ConstraintsGenerator<SubstrateNetwork> constraintsGenerator = new RandomResourceGenerator(new RandomResourceGeneratorParameter(true, labelsAndVNFTypes, minFreeSlotsResources, maxFreeSlotsResources, -1, -1, minCapacity, maxCapacity, minBandwidthResource, maxBandwidthResource, minDelayResource, maxDelayResource, new LinkedList<String>(labelsAndVNFTypes.keySet()).toArray(new String[]{})));
//
//		
//		final COORDVNF_STRATEGY[] strategies = new COORDVNF_STRATEGY[] {COORDVNF_STRATEGY.BANDWIDTH};
//		final BACKUP_STRATEGY[] backupStrategies = new BACKUP_STRATEGY[] {BACKUP_STRATEGY.NO_BACKUP };
//		final Integer[] backupSharingFactor = new Integer[] {3};
//		final Integer[] maxPathLengthArray = new Integer[] { 10 };
//		final Integer[] maxNumCandidatesArray = new Integer[] { 2 };
//		final Integer[] maxBacktrackingStepsArray = new Integer[] { -1 };
//		
//		SubstrateNetwork G = sNetGenerator.generate(new Random(), constraintsGenerator, sNetParams.getParams().getFirst());
//		RandomVNFFGDemandGenerator.generate(new Random(), v, G, (RandomVNFFGGeneratorParameter) VNFFGGeneratorParameters.getParams().getFirst());
//		
//		SVNFParameters algoParam = new SVNFParameters(strategies, backupStrategies, backupSharingFactor, labelsAndVNFTypes, maxPathLengthArray, maxNumCandidatesArray, maxBacktrackingStepsArray, new Boolean[] {true}, false);
//		SVNF s = new SVNF(algoParam);
//		HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> r = s.mapVNFFGs(algoParam.getAlgorithmParamsAsList().getFirst(), G, VNFFGs);
//		int tt = 0;
//		for (Tuple<VNFChain, LinkedList<EntityMapping>> t : r.values()) {
//			GraphMLExporter.export("mapping"+tt+".graphml", t.x);
//		}
	}

	public VNFFG(int layer) {
		this.layer = layer;
		this.dependencies = new LinkedList<VNF>();
		this.delayConstraints = new LinkedList<DelayConstraint>();

		this.terminatingNodes = new LinkedList<VNF>();
	}
	
	public String toString() {
//		return Utils.toString(this);
		return layer + ": " + "_initBW:"+initialBW+ "\n  initialNode:" + initialNode + "\n  dependencies:[" + dependencies + "]" + "\n  terminating:[" + terminatingNodes + "]";
	}

	public VNF getInitialNode() {
		assert (!this.dependencies.contains(this.initialNode));

		return this.initialNode;
	}

	public void setInitialNode(VNF initialNode) {
		if (initialNode != null && this.initialNode != null)
			throw new AssertionError();
		
		//		if (!VNFUtils.hasDemand(initialNode, IdDemand.class))
		//			throw new AssertionError();

//		if (initialNode.inputInterfaces.isEmpty())
//			initialNode.inputInterfaces.add(new VNFInputInterface(initialNode));
		this.initialNode = initialNode;
	}

	public LinkedList<VNF> getTerminatingNodes() {
		return this.terminatingNodes;
	}

	public void addTerminatingNode(VNF terminatingNode) {
		//		if (!VNFUtils.hasDemand(terminatingNode, IdDemand.class))
		//			throw new AssertionError();
		if (this.dependencies.contains(terminatingNode))
			throw new AssertionError();

		this.terminatingNodes.add(terminatingNode);
	}
	
//	public LinkedList<VNFChain> getBestSubFlow(double flowBW, LinkedList<VNF> flowNodes, LinkedList<VNFLink> flowLinks, COORDVNF_STRATEGY strategy) {
//		LinkedList<VNFChain> result = new LinkedList<VNFChain>();
//		getAllVNFChainTopologies(-1, new VNFChain(layer), result, new VNFChainEntityCreator(this), null, null, flowBW, flowNodes, flowLinks, strategy);
//		return result;
//	}
	
//	public void getBestSubFlow(VNFChain VNFChain, double flowBW, LinkedList<VNF> flowNodes, LinkedList<VNFLink> flowLinks, COORDVNF_STRATEGY strategy) {
//		getBestVNFChain(VNFChain, new VNFChainEntityCreator(this), null, null, flowBW, flowNodes, flowLinks, null, strategy);
//	}

//	public VNFChain getBestVNFChain(COORDVNF_STRATEGY strategy) {
////		System.out.println();
////		DEBUG = true;
//		VNFChain VNFChain = new VNFChain(layer);
//		getBestVNFChain(VNFChain, new VNFChainEntityCreator(this), null, null, initialBW, new LinkedList<VNF>(), new LinkedList<VNFLink>(), null, strategy);
//		return VNFChain;
//	}
	
	public static class Counter {
		public int i = 0;
	}
	
	public LinkedList<VNFChain> getAllVNFChainTopologies(int maxnum, int maxNodes, COORDVNF_STRATEGY strategy) {
		Counter netCounter = new Counter();
		return getAllVNFChainTopologies(maxnum, maxNodes, strategy, netCounter);
	}
	
	public LinkedList<VNFChain> getAllVNFChainTopologies(int maxnum, int maxNodes, COORDVNF_STRATEGY strategy, Counter netCounter) {
//		System.out.println("##########################################");
//		DEBUG = true;
//		System.out.println("start " + maxnum);
		LinkedList<FastNet> result = new LinkedList<FastNet>();
		boolean r = getAllVNFChainTopologies(this.initialBW, new Counter(), netCounter, maxnum, maxNodes, new FastNet(this), result, null, null, new HashMap<FastNet, HashMap<Node, VNFFlowInfo>>(), null, null, null, strategy);
		if (!r) {
//			System.out.println("no options");
			return null;
		}
//		System.out.println("options: " + result.size());
		
//		LinkedList<VNFChain> filteredresult = result;
//		LinkedList<VNFChain> filteredresult = new LinkedList<VNFChain>();
//		for (VNFChain c : result) {
//			if (!this.validate(c)) {
//				throw new AssertionError(c + "\n\n" + this);
//			}
//				
////				boolean contains = false;
////				for (VNFChain f : filteredresult)
////					if (f.getVertexCount() == c.getVertexCount() && f.getEdgeCount() == c.getEdgeCount() && f.containsNet(c)) {
////						contains = true;
////						break;
////					}
////				
////				if (!contains)
//					filteredresult.add(c);
//			}
//		}
		
		LinkedList<VNFChain> convertedresult = new LinkedList<VNFChain>();
		for (FastNet net : result) {
			convertedresult.add(net.convert(this.layer));
		}
		
		return convertedresult;
//		return filteredresult;
	}

//	public void getBestVNFChain(
//			VNFChain result,
//			VNFChainEntityCreator creator,
//			VNFChainNode prevVNode,
//			LinkedList<AbstractDemand> linkDemands,
//			double flowBW,
//			LinkedList<VNF> flowNodes,
//			LinkedList<VNFLink> flowLinks,
//			LinkedList<Pair<VNF, VNFInputInterface>> VNFCandidates,
//			COORDVNF_STRATEGY strategy) {
//
//		if (VNFCandidates == null) {
//			VNFCandidates = getNextVNFs(flowNodes, flowLinks);
//		}
//		VNFUtils.sortVNFCandidates(VNFCandidates, strategy, this, flowNodes, flowLinks, flowBW);
//
//		if (VNFCandidates.isEmpty()) {
//			return;
//		}
//
//		Pair<VNF, VNFInputInterface> VNFCandidatePair = VNFCandidates.getFirst();
////		System.out.println(VNFCandidate.name);
//
//		VNFChainNode VNFCandidateVNode = creator.createVNode(VNFCandidatePair, flowBW, true).x;
//		boolean isNew = true;
//		LinkedList<VNFChainNode> instances = result.VNFInstances.get(VNFCandidatePair.getFirst());
//		if (instances != null) {
//			for (VNFChainNode i : instances) {
//				int ins = VNFCandidatePair.getFirst().inputInterfaces.size();
//				if (ins == 0)
//					ins = 1;
//
//				boolean found = false;
////				for (VirtualLink o : result.getOutEdges(prevVNode)) {
////					if (result.getDest(o) == i) {
////						found = true;
////						break;
////					}
////				}
//
//				int inedges = result.getInEdges(i).size();
//				if (found || this.terminatingNodes.contains(VNFCandidatePair.getFirst()) || inedges < ins) {
//
//					if (!i.get().isEmpty() && !i.get().get(0).getMappings().isEmpty()) {
//						NetworkEntity<?> mappedTo = i.get().get(0).getMappings().get(0).getResource().getOwner();
//
//						String id = ((IdResource) mappedTo.get(IdResource.class)).getId();
//						i.add(new IdDemand(id, i));
//					}
//
//					// copy new demands to old virtual node
//					for (AbstractDemand d : VNFCandidateVNode.get()) {
//						VNFUtils.addDemand(i, d.getCopy(i), true);
//					}
//
//					VNFCandidateVNode = i;
//					isNew = false;
//
//					break;
//				}
//			}
//		}
//
//		if (isNew)
//			result.addVertex(VNFCandidateVNode, VNFCandidatePair.getFirst());
//
//		if (DEBUG)
//			System.out.println("added " + VNFCandidateVNode.getName() + " (" + VNFCandidatePair.getFirst().outLinks.size() + ") to vNet " + result.getName());
//
//		if (prevVNode != null) {
//			VirtualLink vLink = creator.createVLink(linkDemands, flowBW, true).x;
//			boolean found = false;
//			for (VirtualLink o : result.getOutEdges(prevVNode)) {
//				if (result.getDest(o) == VNFCandidateVNode) {
//					for (AbstractDemand d : vLink.get()) {
//						VNFUtils.addDemand(o, d.getCopy(o), true);
//					}
//					vLink = o;
//					found = true;
//					break;
//				}
//			}
//			
//			if (DEBUG)
//				System.out.println("found: " + found);
//
//			if (!found) {
//				result.addEdge(vLink, prevVNode, VNFCandidateVNode);
//			}
//		}
//
//		if (DEBUG) {
//			System.out.println("====");
//			System.out.println(result);
//			System.out.println("====");
//		}
//
//		//			if (!isNew)
//		//				return;
//
//
//		Collection<VirtualLink> ins = result.getInEdges(VNFCandidateVNode);
//		if (VNFCandidatePair.getFirst() == this.getInitialNode() || ins.size() == VNFCandidatePair.getFirst().inputInterfaces.size()) {
//
//			LinkedList<VNFLink> outs = VNFCandidatePair.getFirst().outLinks;
//			if (outs.isEmpty()) {
//				// no other VNF does depend on current VNFCandidate
//				// (but there are possibly some more VNFs to attach to this branch)
//				LinkedList<VNF> flowNodesCopy = new LinkedList<VNF>(flowNodes);
//				flowNodesCopy.add(VNFCandidatePair.getFirst());
//				LinkedList<VNFLink> flowLinksCopy = new LinkedList<VNFLink>(flowLinks);
//
//				getBestVNFChain(result, creator, VNFCandidateVNode, null, flowBW, flowNodesCopy, flowLinksCopy, null, strategy);
//			} else {
//				// there are some other VNFs that do depend on current VNFCandidate
//				if (DEBUG)
//					System.out.println(VNFCandidatePair.getFirst().FreeSlotsDemand + " " + VNFCandidatePair.getFirst().id + " (" + outs.size() + ")");
//
//				int o = 0;
//				for (VNFLink out : outs) {
//					double newFlowDataRate = flowBW * out.dataratepercentage;
//					LinkedList<VNF> flowNodesCopy = new LinkedList<VNF>(flowNodes);
//					flowNodesCopy.add(VNFCandidatePair.getFirst());
//					LinkedList<VNFLink> flowLinksCopy = new LinkedList<VNFLink>(flowLinks);
//					flowLinksCopy.add(out);
//
//					if (DEBUG)
//						System.out.println("rec " + o++ + "/" + outs.size());
//
//					getBestVNFChain(result, creator, VNFCandidateVNode, out.demands, newFlowDataRate, flowNodesCopy, flowLinksCopy, null, strategy);
//
//					if (DEBUG) {
//						System.out.println("return");
//						System.out.println(result);
//					}
//
//				}
//
//			}
//
//		}
//
//	}
	
	public void getAll(Counter numResults) {
		
	}
	
	private boolean getAllVNFChainTopologies(
			double flowDataRate,
			Counter counter,
			Counter netCounter,
			int maxnum, int maxNodes,
			FastNet lastVNFChain,
			LinkedList<FastNet> results,
//			VNFChainEntityCreator creator,
			Node prevVNode,
			CommonDemand bandwidthDemand,
			HashMap<FastNet, HashMap<Node, VNFFlowInfo>> allVNFFlowInfos,
			VNFLink prevOutLink, VNFInputInterface prevInputInterface, VNF lastVNF, COORDVNF_STRATEGY strategy) {
		
		// VirtualNode ist immer ein anderer, wenn deepCopy!! --> Demands getrennt von den Nodes speichern und _hinterher_ hinzufügen!
		HashMap<Node, VNFFlowInfo> lastVNFFlowInfos = allVNFFlowInfos.get(lastVNFChain);
		if (lastVNFFlowInfos == null) {
			lastVNFFlowInfos = new HashMap<Node, VNFFlowInfo>();
			allVNFFlowInfos.put(lastVNFChain, lastVNFFlowInfos);
		}
		LinkedList<Pair<VNF, VNFInputInterface>> VNFCandidates = null;
		LinkedList<VNFInputInterface> thisFlowInputInterfaces = new LinkedList<VNFInputInterface>();
		LinkedList<VNF> thisFlowNodes = new LinkedList<VNF>();
		LinkedList<VNFLink> thisFlowLinks = new LinkedList<VNFLink>();

		if (prevVNode != null) {
			VNFFlowInfo lastVNFFlowInfo = lastVNFFlowInfos.get(prevVNode);
			thisFlowInputInterfaces.addAll(lastVNFFlowInfo.flowInputInterfaces);
			thisFlowNodes.addAll(lastVNFFlowInfo.flowNodes);
			thisFlowLinks.addAll(lastVNFFlowInfo.flowLinks);
		}

		if (prevInputInterface != null)
			thisFlowInputInterfaces.add(prevInputInterface);
		if (lastVNF != null)
			thisFlowNodes.add(lastVNF);
		if (prevOutLink != null)
			thisFlowLinks.add(prevOutLink);

		VNFCandidates = getNextVNFs(thisFlowNodes, thisFlowLinks, thisFlowInputInterfaces);

//		for (Pair<VNF, VNFInputInterface> c : VNFCandidates)
//			System.out.println("Candidate: "+c.getKey().id);

		if (VNFCandidates.isEmpty()) {
//			System.out.println(results.size());
			
			if (maxnum != -1 && counter.i > maxnum) {
				return false;
			}
			counter.i++;
			
			results.add(lastVNFChain);
			return true;
		}

		VNFUtils.sortVNFCandidates(VNFCandidates, strategy, this);
		candidateFor: for (Pair<VNF, VNFInputInterface> VNFCandidatePair : VNFCandidates) {
//			System.out.println(lastVNFChain.getVertexCount() + " " + allResults.size());
			//System.out.println(VNFCandidate.name);
			
			if (maxnum != -1 && netCounter.i > 3000) {
				System.err.println("netCounter.i > 3000");
				return false;
			}
			
			FastNet currentVNFChain = lastVNFChain.getCopy();
			netCounter.i++;
//			System.out.println(netCounter.i + " !!");
			
			
			double capacity;
			if (VNFCandidatePair.getFirst().staticModel) {
				capacity = VNFCandidatePair.getFirst().capacityPer100MBits;
			} else {
				capacity = Math.floor(VNFCandidatePair.getFirst().capacityPer100MBits * (flowDataRate / 100.0d));
			}
			
			Node VNode = new Node(VNFCandidatePair.getFirst());
			boolean isNew = true;
			LinkedList<Node> instances = currentVNFChain.VNFInstances.get(VNFCandidatePair.getFirst());
			if (instances != null) {
				for (Node i : instances) {
					int ins = VNFCandidatePair.getFirst().inputInterfaces.size();
					if (ins == 0)
						ins = 1;

					int inedges = currentVNFChain.getInLinks(i).size();
					if (this.terminatingNodes.contains(VNFCandidatePair.getFirst()) || inedges < ins) {
						
						for (Triple<Node, VNFInputInterface, Double> l : currentVNFChain.getInLinks(i))
							if (l.y == VNFCandidatePair.getSecond())
								continue candidateFor;
//
////						if (!i.get().isEmpty() && !i.get().get(0).getMappings().isEmpty()) {
////							NetworkEntity<?> mappedTo = i.get().get(0).getMappings().get(0).getResource().getOwner();
////
////							String id = ((IdResource) mappedTo.get(IdResource.class)).getId();
////							i.add(new IdDemand(id, i));
////						}
////

						// copy new demands to old virtual node
						Double d = currentVNFChain.nodeDemands.get(VNode);
						if (d == null)
							d = 0.0d;
						currentVNFChain.nodeDemands.put(i, currentVNFChain.nodeDemands.get(i) + d);
						
						VNode = i;
						isNew = false;
						
						break;
					}
				}
			}
			
			if (isNew) {
				if (maxNodes != -1 && maxNodes == currentVNFChain.getNodeCount()) {
					return false;
				}
				
				currentVNFChain.addNode(VNode, capacity);
			}
			
//			if (VNFCandidatePair.getSecond() != null) {
//				for (VirtualLink l : currentVNFChain.getInEdges(VNode))
//					if (l.VNFInputInterface == VNFCandidatePair.getSecond()) {
//						System.err.println("SKIPPING!!!!!!");
//						continue candidatesFor;
//					}
//			}

			HashMap<Node, VNFFlowInfo> currentVNFFlowInfos = new HashMap<Node, VNFFlowInfo>();
			allVNFFlowInfos.put(currentVNFChain, currentVNFFlowInfos);
			for (Entry<Node, VNFFlowInfo> e : lastVNFFlowInfos.entrySet())
				currentVNFFlowInfos.put(e.getKey(), e.getValue().getCopy());
			
			VNFFlowInfo i = currentVNFFlowInfos.get(VNode);
			if (i == null) {
				i = new VNFFlowInfo();
				currentVNFFlowInfos.put(VNode, i);
			}
			
			i.flowInputInterfaces.addAll(thisFlowInputInterfaces);
			i.flowLinks.addAll(thisFlowLinks);
			i.flowNodes.addAll(thisFlowNodes);
			
//			results.add(currentVNFChain);

			
//				System.out.println("added " + VNFCandidateVNode.x.getName() + " (" + VNFCandidatePair.getFirst().outLinks.size() + ") to vNet " + currentVNFChain.getName());

			if (prevVNode != null) {
				currentVNFChain.addLink(prevVNode, VNode, VNFCandidatePair.getSecond(), flowDataRate);
//				if (VNFCandidatePair.getSecond() != null) {
////					vLink.x.setName("OUT:" + prevOutLink.source.name + "." + prevOutLink.source.outLinks.indexOf(prevOutLink) + " IN:" + VNFCandidatePair.getFirst().name + "." + VNFCandidatePair.getFirst().inputInterfaces.indexOf(VNFCandidatePair.getSecond())+"");
//					vLink.x.VNFInputInterface = VNFCandidatePair.getSecond();
//				}
			}

//				System.out.println("====");
//				System.out.println(currentVNFChain);
//				System.out.println("====");

			//			if (!isNew)
			//				return;

			
			LinkedList<Triple<Node, VNFInputInterface, Double>> inedges = currentVNFChain.getInLinks(VNode);
			
//			System.err.println(VNFCandidateVNode.x);
			int inint = VNFCandidatePair.getFirst().inputInterfaces.size();
			if (VNFCandidatePair.getFirst() == this.initialNode || inedges.size() == inint) {
//				System.out.println("input links complete");
				
				LinkedList<VNFLink> outs = VNFCandidatePair.getFirst().outLinks;
				if (outs.isEmpty()) {
					outs = new LinkedList<VNFLink>();
					outs.add(new VNFLink(VNFCandidatePair.getFirst(), null, 1.0d, false));
				}

				// there are some other VNFs that do depend on current VNFCandidate
				// System.out.println(VNFCandidatePair.getFirst().FreeSlotsDemand + " " + VNFCandidatePair.getFirst().id + " (" + outs.size() + ")");
				
				double sumFlowRates = 0.0d;
				if (VNFCandidatePair.getFirst() == this.initialNode) {
					sumFlowRates = this.initialBW;
				} else {
					for (Triple<Node, VNFInputInterface, Double> iii : inedges) {
						sumFlowRates += iii.z;
					}
				}
				
				// int o = 0;
				LinkedList<FastNet> lastResults = new LinkedList<FastNet>();
				lastResults.add(currentVNFChain);
				for (VNFLink out : outs) {
					
					double nextFlowDataRate = Math.floor(sumFlowRates * out.dataratepercentage);

					// nextResults gets all the finished branches
					// we take these branches and add the other branches
					// in order to derive complete virtual networks
					LinkedList<FastNet> nextResults = new LinkedList<FastNet>();
					while (!lastResults.isEmpty()) {
						if (!getAllVNFChainTopologies(nextFlowDataRate, counter, netCounter, maxnum, maxNodes, lastResults.pop(), nextResults, VNode, out.bandwidthDemand, allVNFFlowInfos, out, VNFCandidatePair.getSecond(), VNFCandidatePair.getFirst(), strategy))
							return false;
					}
					lastResults = nextResults;
				}

//				if (maxnum != -1 && maxnum < results.size() + lastResults.size())
//					return false;
				results.addAll(lastResults);
//				System.out.println(results.size());

			} else {
//				System.out.println("input links incomplete");
//				System.out.println(ins.size() +" "+ VNFCandidatePair.getFirst().inputInterfaces.size());
				
//				if (maxnum != -1 && results.size() >= maxnum) {
//					return false;
//				}
				results.add(currentVNFChain);
//				System.out.println(results.size());
			}
		}
		
		return true;
	}


	/**
	 * @return list of nodes where all requirements are met
	 * (i.e., where all required predecessors have been already embedded),
	 * but which were not embedded so far
	 */
	public LinkedList<Pair<VNF, VNFInputInterface>> getNextVNFs(LinkedList<VNF> flowNodes, LinkedList<VNFLink> flowLinks, LinkedList<VNFInputInterface> inputInterfaces) {
		LinkedList<Pair<VNF, VNFInputInterface>> result = new LinkedList<Pair<VNF, VNFInputInterface>>();

		if (initialNode != null && (flowNodes == null || !flowNodes.contains(initialNode))) {
			result.add(new Pair<VNF, VNFInputInterface>(initialNode, null));
			return result;
		}

		for (VNF current : dependencies) {
			if (!flowNodes.contains(current)) {
//				boolean first = true;
				
				if (current.inputInterfaces.isEmpty()) {
					result.add(new Pair<VNF, VNFInputInterface>(current, null));
//					throw new AssertionError();
				} else {
					for (VNFInputInterface i : current.inputInterfaces) {
						if (!inputInterfaces.contains(i) && flowNodes.containsAll(i.requiredFlowVNFs) && flowLinks.containsAll(i.requiredFlowVNFLinks)) {
//							if (!first)
//								throw new AssertionError();
							result.add(new Pair<VNF, VNFInputInterface>(current, i));
//							first = false;
//							break; //max. one of each VNF
						}
					}
				}
			}
		}

		if (result.isEmpty() && !this.terminatingNodes.isEmpty() && !this.terminatingNodes.contains(flowNodes.getLast())) {
			boolean found = false;
			LinkedList<VNF> term = new LinkedList<VNF>(this.terminatingNodes);
//			Collections.shuffle(term);
			for (VNF v : term) {
//				boolean first = true;
				
				if (v.inputInterfaces.isEmpty()) {
					result.add(new Pair<VNF, VNFInputInterface>(v, null));
					found = true;
//					throw new AssertionError();
				} else {
					for (VNFInputInterface i : v.inputInterfaces) {
						if (!inputInterfaces.contains(i) && flowNodes.containsAll(i.requiredFlowVNFs) && flowLinks.containsAll(i.requiredFlowVNFLinks)) {
//							if (!first)
//								throw new AssertionError();
							result.add(new Pair<VNF, VNFInputInterface>(v, i));
							found = true;
//							first = false;
//							break; //max. one of each VNF
						}
					}
				}
			}

//			System.out.println("ADDED " + result + " " + flowNodes.getLast().id);
//			System.out.println();
			if (!found)
				throw new AssertionError(term);
		}

		return result;
	}

//	public boolean validate() {
//		for (VNF v1 : dependencies)
//			for (VNF v2 : dependencies)
//				if (v1 != v2)
//					if (requires(v1, v2, true) && requires(v2, v1, true))
//						return false;
//
//		return true;
//	}
//
//	boolean requires(VNF thisVNF, VNF thatVNF, boolean recursive) {
//		if (thisVNF == thatVNF)
//			return true;
//
//		for (VNF v : thisVNF.requiredFlowVNFs) {
//			if (v == thatVNF)
//				return true;
//
//			if (recursive)
//				if (requires(v, thatVNF, true))
//					return true;
//		}
//
//		return false;
//	}

	public boolean validate(VNFChain c) {
		LinkedList<VNFInputInterface> insinf = new LinkedList<VNFInputInterface>();
		
		for (VNF v : this.dependencies) {
			insinf.addAll(v.inputInterfaces);
		}
		for (VNF v : this.terminatingNodes) {
			insinf.addAll(v.inputInterfaces);
		}
		
		// each input interface should appear at least once
		outer: for (VNFInputInterface i : insinf) {
			for (VirtualLink l : c.getEdges()) {
				if (l.VNFInputInterface == i) {
					continue outer;
				}
			}
			
//			System.err.println(i.VNF.id+"."+i.VNF.inputInterfaces.indexOf(i) + " not found.");
			return false;
		}
		
//		LinkedList<VNF> ins = new LinkedList<VNF>();
//		ins.add(this.initialNode);
//		for (VNF v : this.dependencies) {
//			ins.add(v);
//		}
//		for (VNF v : this.terminatingNodes) {
//			ins.add(v);
//		}
//		
//		// each VNF should appear at least once
//		outer: for (VNF i : ins) {
//			for (VirtualNode n : c.getVertices()) {
//				VNFChainNode nn = (VNFChainNode) n;
//				if (nn.VNFid == i.id) {
//					continue outer;
//				}
//			}
//			
////			try {
////				GraphMLExporter.export("VNet.graphml", c);
//
////				for (VNF ii : ins)
////					System.out.println(ii);
////				throw new AssertionError(i.id + " " + i.name);
////			} catch (IOException e) {
////				e.printStackTrace();
////			}
//			
//			return false;
//		}
		
		return true;
	}

	public static class DelayConstraint {

		public final VNF from, to;
		public final double maxDelay;

		public DelayConstraint(VNF from, VNF to, double maxDelay) {
			this.from = from;
			this.to = to;
			this.maxDelay = maxDelay;
		}
	}
}
