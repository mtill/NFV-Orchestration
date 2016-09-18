package algorithm;



import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.util.Pair;

import algorithm.SVNFParameters.BACKUP_STRATEGY;
import algorithm.SVNFParameters.SVNFParameter;
import algorithm.Tuples.Quintuple;
import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import algorithm.VNFAlgorithmParameters.VNFAlgorithmParameter;
import edu.uci.ics.jung.algorithms.shortestpath.MyDijkstraShortestPath;
import evaluation.metrics.VNFAllBackupPathLength;
import evaluation.metrics.VNFAvgBackupUtilization;
import evaluation.metrics.VNFBacktrackingSteps;
import evaluation.metrics.VNFBackupCostVsAllocationCost;
import evaluation.metrics.VNFConsideredCandidates;
import evaluation.metrics.VNFEvaluationMetric;
import evaluation.metrics.VNFMaxBackupPathLength;
import evaluation.metrics.VNFMaxBackupUtilization;
import evaluation.metrics.VNFMaxCapacityExceeded;
import evaluation.metrics.VNFPossibleCandidates;
import networks.VNF;
import networks.VNF.VNFInputInterface;
import networks.VNFChain;
import networks.VNFChainEntityCreator;
import networks.VNFChainNode;
import networks.VNFFG;
import networks.VNFFlowInfo;
import networks.VNFLink;
import vnreal.algorithms.utils.SubgraphBasicVN.ResourceDemandEntry;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.algorithms.utils.energy.linkStressTransformers.BackupPathTransformer;
import vnreal.algorithms.utils.energy.linkStressTransformers.DisjointPathTransformer;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.demands.FreeSlotsDemand;
import vnreal.constraints.demands.IdDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CommonResource;
import vnreal.constraints.resources.IdResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNode;

public class CoordVNF_SVNF_Algorithm extends VNFAlgorithm {

	final SVNFParameters params;
	//	private long idcounter = 0, vnetcounter = 0;

	private VNFBacktrackingSteps VNFBacktrackingSteps = null;
	private VNFPossibleCandidates VNFPossibleCandidates = null;
	private VNFConsideredCandidates VNFConsideredCandidates = null;

	//	double availableCapacity = 0;
	double maxCapacity = 0;
	public int availableCapacityExceeded = 0;
	public int maxCapacityExceeded = 0;

	public CoordVNF_SVNF_Algorithm(SVNFParameters params) {
		super();
		this.params = params;
	}

	public CoordVNF_SVNF_Algorithm(String name, SVNFParameters params) {
		super(name);
		this.params = params;
	}

	public SVNFParameters getAlgorithmParams() {
		return params;
	}

	@Override
	public LinkedList<VNFEvaluationMetric> getMetrics(VNFAlgorithmParameter param, double elapsedTimeMS) {
		//		SVNFParameter p = (SVNFParameter) param;
		LinkedList<VNFEvaluationMetric> result = super.getMetrics(param, elapsedTimeMS);

		//		if (p.backupStrategy != BACKUP_STRATEGY.NO_BACKUP) {
//		result.add(new VNFBackupCapacity());
//		result.add(new VNFBackupCapacityRatio());
//		result.add(new VNFBackupBandwidth());
//		result.add(new VNFBackupBandwidthRatio());
//		result.add(new VNFBackupSlots());
//		result.add(new VNFBackupSlotsRatio());
		result.add(new VNFMaxBackupUtilization());
//		result.add(new VNFMaxLinkBackupUtilization());
//		result.add(new VNFMaxNodeBackupUtilization());
		result.add(new VNFAvgBackupUtilization());
//		result.add(new VNFAvgLinkBackupUtilization());
//		result.add(new VNFAvgNodeBackupUtilization());
		result.add(new VNFBackupCostVsAllocationCost());
//		result.add(new VNFBackupCapacityCostVsAllocationCapacityCost());
//		result.add(new VNFBackupBWCostVsAllocationBWCost());

		result.add(new VNFAllBackupPathLength());
		result.add(new VNFMaxBackupPathLength());
		//		}

		//		result.add(new VNFAvailableCapacityExceeded(this));
		result.add(new VNFMaxCapacityExceeded(this));
		result.add(VNFBacktrackingSteps);
		result.add(VNFPossibleCandidates);
		result.add(VNFConsideredCandidates);
		return result;
	}

	@Override
	public void init(SubstrateNetwork G, VNFAlgorithmParameter param) {
		super.init(G, param);

		if (param.earlyTermination) {
			//			this.availableCapacity = 0;
			this.availableCapacityExceeded = 0;
			this.maxCapacityExceeded = 0;
			this.maxCapacity = 0;

			for (SubstrateNode s : G.getVertices()) {
				CommonResource c = (CommonResource) s.get(CommonResource.class);
				//				this.availableCapacity += c.getAvailableCapacity();
				if (c.getAvailableCapacity() > this.maxCapacity) {
					this.maxCapacity = c.getAvailableCapacity();
				}
			}
		}
	}

	public LinkedHashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mapVNFFGs(Random random, VNFAlgorithmParameter param, LinkedList<VNFFG> VNFFGs) {
//		SVNFParameter p = (SVNFParameter) param;

		LinkedHashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> result = new LinkedHashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>>();
		this.VNFBacktrackingSteps = new VNFBacktrackingSteps();
		this.VNFPossibleCandidates = new VNFPossibleCandidates();
		this.VNFConsideredCandidates = new VNFConsideredCandidates();

		LinkedList<NetworkEntity<AbstractResource>> entities = new LinkedList<NetworkEntity<AbstractResource>>();
		entities.addAll(G.getVertices());
		entities.addAll(G.getEdges());

//		if (p.backupStrategy != BACKUP_STRATEGY.NO_BACKUP) {
//			for (NetworkEntity<AbstractResource> sn : entities) {
//
//				boolean found = false;
//				for (AbstractResource r : sn.get()) {
//					if (r instanceof BackupResource) {
//						found = true;
//						break;
//					}
//				}
//
//				if (!found)
//					sn.add(new BackupResource(sn,
//							((SVNFParameter) param).backupSharingFactor));
//			}
//		}

		for (VNFFG VNFFG : VNFFGs) {
			Tuple<VNFChain, LinkedList<EntityMapping>> r = mapOneVNFFG(random, (SVNFParameter) param, G, VNFFG);
			result.put(VNFFG, r);
			//			vnetcounter++;
		}

		return result;
	}

	private Tuple<VNFChain, LinkedList<EntityMapping>> mapOneVNFFG(Random random, SVNFParameter param, SubstrateNetwork G, VNFFG VNFFG) {
		// System.out.println(VNFFG.getVNFChains(1).getFirst().getVertexCount());

		LinkedList<VNFChain> allPossibleChainings = null;
		LinkedList<VNFChain> newAllPossibleChainings = null;

		if (param.earlyTermination) {
			allPossibleChainings = VNFFG.getAllVNFChainTopologies(-1, -1, param.strategy);
			newAllPossibleChainings = new LinkedList<VNFChain>();

			for (VNFChain a : allPossibleChainings) {
//				double capdemsum = 0.0d;
				double capdemmax = 0.0d;
				for (VirtualNode v : a.getVertices()) {
					CommonDemand c = (CommonDemand) v.get(CommonDemand.class);
					double d = c == null ? 0.0d : c.getDemandedCapacity();
//					capdemsum += d;
					if (capdemmax < d)
						capdemmax = d;
				}

				boolean ok = true;
				//				if (capdemsum > this.availableCapacity) {
				//					availableCapacityExceeded++;
				//					System.err.println("!!! availableCapacityExceeded++");
				//					ok = false;
				//				}
				if (capdemmax > this.maxCapacity) {
					maxCapacityExceeded++;
					System.err.println("!!! maxCapacityExceeded++");
					ok = false;
				}
				if (ok)
					newAllPossibleChainings.add(a);
			}

			if (newAllPossibleChainings.isEmpty()) {
				return null;
			}
		}

		VNFChainEntityCreator creator = new VNFChainEntityCreator(VNFFG);

		VNFFlowEntry VNFFlowEntry = null;

		//		VNFChain bestChain = VNFFG.getVNFChains(1, param.strategy).getFirst();
		//		double bestCapacityDemand = VNFUtils.getCapacityDemand(bestChain);
		//		double availCapacityRes = VNFUtils.getAvailCapacityResources(G);
		//		double remainingCapacityResources = availCapacityRes
		//				- bestCapacityDemand;

		Tuple<VNFChain, LinkedList<EntityMapping>> result = null;
		//		if (remainingCapacityResources >= 0.0d) {
		//			SubstrateNetwork backtrackingGraph = null;
		//			SubstrateNode root = null;

		if (param.isAdvanced) {
			for (int maxPathLength = 1; maxPathLength <= param.maxPathLength; ++maxPathLength) {

				VNFFlowEntry = new VNFFlowEntry(null, null, null, new LinkedHashMap<VirtualNode, VNFFlowInfo>(), VNFFG.initialBW, null);

				//					backtrackingGraph = new SubstrateNetwork();
				//					root = VNFUtils.addBacktrackingNode(backtrackingGraph, "root", null, "");

				result = mapOneVNFFG(G, creator, VNFFlowEntry,
						new VNFChain(VNFFG.layer),
						new LinkedHashMap<NetworkEntity<?>, LinkedList<EntityMapping>>(),
						VNFFG, maxPathLength,
						param.maxNumCandidates,
						param.mapBidirectionalPaths,
						param.labelsAndVNFTypes,
//						param.backupSharingFactor,
						param.backupStrategy,
						//							backtrackingGraph, root, "  ",
						param.strategy,
						new VirtualNode(-1), new VirtualLink(-1),
						//						new LinkedHashMap<VirtualNode, LinkedList<LinkedList<SubstrateLink>>>(),
						newAllPossibleChainings,
						//						allPossibleChainings,
						null, null, null,
						param.maxBacktrackingStepsCandidates,
						random,
						param.earlyTermination,
//						param.freeBackupCapacityWeight
						param.maxSharing
						);

				//					VNFUtils.printTree(backtrackingGraph, root);

				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				if (result == null) {
					System.out.println(timestamp + ": fail -> redo");
				} else {
					System.out.println(timestamp + ": success");
					break;
				}
			}
		} else {

			VNFFlowEntry = new VNFFlowEntry(null, null, null, new LinkedHashMap<VirtualNode, VNFFlowInfo>(),
					VNFFG.initialBW, null);

			//				backtrackingGraph = new SubstrateNetwork();
			//				root = VNFUtils.addBacktrackingNode(backtrackingGraph, "root", null, "");

			result = mapOneVNFFG(G, creator, VNFFlowEntry, new VNFChain(VNFFG.layer),
					new LinkedHashMap<NetworkEntity<?>, LinkedList<EntityMapping>>(),
					VNFFG, param.maxPathLength,
					param.maxNumCandidates, param.mapBidirectionalPaths,
					param.labelsAndVNFTypes,
//					param.backupSharingFactor,
					param.backupStrategy,
					//						backtrackingGraph, root, "  ",
					param.strategy,
					new VirtualNode(-1), new VirtualLink(-1),
					//					new LinkedHashMap<VirtualNode, LinkedList<LinkedList<SubstrateLink>>>(),
					newAllPossibleChainings,
					//					allPossibleChainings,
					null, null, null,
					param.maxBacktrackingStepsCandidates,
					random,
					param.earlyTermination,
//					param.freeBackupCapacityWeight
					param.maxSharing
					);

			//				VNFUtils.printTree(backtrackingGraph, root);
		}

		// free old mappings and remember allocation decisions
		LinkedHashMap<AbstractDemand, LinkedList<AbstractResource>> oldDemands = new LinkedHashMap<AbstractDemand, LinkedList<AbstractResource>>();
		for (EntityMapping m : VNFFlowEntry.thisAssignments) {
			for (ResourceDemandEntry e : m.mappedResources) {
//				if (e.dem instanceof BandwidthDemand || e.dem instanceof BackupDemand)
				if (e.dem instanceof CommonDemand)
					continue;

				LinkedList<AbstractResource> res = oldDemands.get(e.dem);
				if (res == null) {
					res = new LinkedList<AbstractResource>();
					oldDemands.put(e.dem, res);
				}

				res.add(e.res);
			}

		}

		for (EntityMapping m : VNFFlowEntry.thisAssignments) {
			for (ResourceDemandEntry e : m.mappedResources) {
//				if (e.dem instanceof BandwidthDemand || e.dem instanceof BackupDemand)
				if (e.dem instanceof CommonDemand)
					continue;

				e.dem.free(e.res);
			}
		}

		// merge old mappings
		LinkedHashMap<AbstractDemand, LinkedList<AbstractResource>> newDemands = new LinkedHashMap<AbstractDemand, LinkedList<AbstractResource>>();
		for (Entry<AbstractDemand, LinkedList<AbstractResource>> entry : oldDemands.entrySet()) {

			AbstractDemand demand = null;
//			if (entry.getKey() instanceof BackupDemand) {
//				demand = entry.getKey();
//			} else {
				NetworkEntity<AbstractDemand> owner = (NetworkEntity<AbstractDemand>) entry.getKey().getOwner();
				demand = VNFUtils.addDemand(owner, entry.getKey(), true);
//			}

			LinkedList<AbstractResource> substrates = newDemands.get(demand);
			if (substrates == null) {
				substrates = new LinkedList<AbstractResource>();
				newDemands.put(demand, substrates);
			}
			substrates.addAll(entry.getValue());
		}

		// map new mappings
		for (Entry<AbstractDemand, LinkedList<AbstractResource>> entry : newDemands.entrySet()) {
			for (AbstractResource r : entry.getValue()) {
				entry.getKey().occupy(r);
			}
		}

		return result;
	}

	Tuple<VNFChain, LinkedList<EntityMapping>> mapOneVNFFG(SubstrateNetwork G, VNFChainEntityCreator creator,
			VNFFlowEntry VNFFlowEntry, VNFChain VNet,
			LinkedHashMap<NetworkEntity<?>, LinkedList<EntityMapping>> allAssignments,
			VNFFG VNFFG,
			Integer maxPathLength, Integer maxNumCandidates,
			boolean mapBidirectionalPaths,
			LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes,
//			int sharingFactor,
			BACKUP_STRATEGY backupStrategy,
			//			SubstrateNetwork backtrackingGraph, SubstrateNode backtrackingParentNode, String backtrackingPrefix,
			COORDVNF_STRATEGY strategy,
			VirtualNode fakeBackupVNode, VirtualLink fakeBackupVLink,
			//			LinkedHashMap<VirtualNode, LinkedList<LinkedList<SubstrateLink>>> incomingPrimarypaths,
			LinkedList<VNFChain> allPossibleChainings,
			VNFLink prevOutLink, VNFInputInterface prevInputInterface, VNF lastVNF,
			int maxBacktrackingSteps,
			Random random,
			boolean earlyTermination,
//			double freeBackupCapacityWeight
			int maxSharing
			) {

		if (isCanceled) {
			return null;
		}

		//		HashMap<SubstrateNode, String> oldstates = new HashMap<SubstrateNode, String>();
		//		HashMap<SubstrateNode, String> oldstates2 = new HashMap<SubstrateNode, String>();
		//		for (SubstrateNode n : G.getVertices()) {
		//			String str = n.toString() + "\n";
		//			String strstr = "";
		//			for (AbstractResource s : n.get()) {
		//				strstr += s.hashCode()+":"+s.getClass().getSimpleName() + ": ";
		//				for (Mapping m : s.getMappings()) {
		//					strstr += m.getDemand().getClass().getSimpleName() + "." + m.getDemand() + "\n";
		//				}
		//			}
		////					if (m.getDemand() instanceof FreeSlotsDemand)
		////						str += "\n  " + ((FreeSlotsDemand) m.getDemand()).getOriginalNodeMappedTo();
		////					if (m.getDemand() instanceof BackupDemand)
		////						str += "\n  " + ((BackupDemand) m.getDemand()).getOriginalNodeMappedTo();
		////				}
		////			}
		//			
		////			String str = n.toString() + "\n";
		////			for (Mapping m : ((FreeSlotsResource) n.get(FreeSlotsResource.class)).getMappings())
		////				str += ((FreeSlotsDemand) m.getDemand()).getOriginalOwnerDemands();
		//			
		//			oldstates.put(n, str);
		//			oldstates2.put(n, strstr);
		//		}

		//		VNFFlowInfo vv = VNFFlowEntry.VNFFlowInfos.get(VNFFlowEntry.prevVNode);
		//		if (vv != null) {
		//			System.out.print("#  ");
		//			for (VNF v : vv.flowNodes)
		//				System.out.print(v.id + " ");
		//			System.out.println("\n");
		//		}


		LinkedList<Pair<VNF, VNFInputInterface>> VNFCandidates = null;
		LinkedList<VNFInputInterface> thisFlowInputInterfaces = new LinkedList<VNFInputInterface>();
		LinkedList<VNF> thisFlowNodes = new LinkedList<VNF>();
		LinkedList<VNFLink> thisFlowLinks = new LinkedList<VNFLink>();

		VNFFlowInfo lastVNFFlowInfo = VNFFlowEntry.prevVNode == null ? null : VNFFlowEntry.VNFFlowInfos.get(VNFFlowEntry.prevVNode);
		if (lastVNFFlowInfo != null) {
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

		VNFCandidates = VNFFG.getNextVNFs(thisFlowNodes, thisFlowLinks, thisFlowInputInterfaces);

		if (VNFCandidates.isEmpty()) {
			//			SubstrateNode nodeID = VNFUtils.addBacktrackingNode(backtrackingGraph, "subflow allocated successfully", backtrackingParentNode, backtrackingPrefix);
			return new Tuple<VNFChain, LinkedList<EntityMapping>>(VNet, VNFFlowEntry.thisAssignments);
		}

		//		double bestPercentage = VNFUtils.getTotalTrafficRate(VNFCandidates.getFirst());

		Tuple<VirtualLink, CommonDemand> VLinkToParent = null;
		if (VNFFlowEntry.prevVNode != null) {
			VLinkToParent = creator.createVLink(VNFFlowEntry.prevVNFLinkDemand, VNFFlowEntry.flowDataRate, true);
		}

		Transformer<SubstrateLink, Double> transformer = new BackupPathTransformer(VLinkToParent == null ? null : VLinkToParent.y, null);
//		transformer = new BackupPathTransformer(freeBackupCapacityWeight, null, VLinkToParent == null ? null : VLinkToParent.y, null);
		//		if (backupStrategy == BACKUP_STRATEGY.COMBINED && VNFFlowEntry.lastBackupPathFromPrimaryNodeToBackupNode != null) {
		//			transformer = new DisjointPathTransformer(null, VLinkToParent == null ? null : VLinkToParent.y, VNFFlowEntry.lastBackupPathFromPrimaryNodeToBackupNode, G);
		//		} else {
		//		}
		LinkedList<Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>> candidatesPairs = getCandidates(
				G, VNet, allAssignments, creator, VNFFlowEntry.prevSNode, transformer, VNFCandidates,
				maxPathLength, maxNumCandidates, VNFFlowEntry.flowDataRate,
				VNFFlowEntry, VNFFG, VLinkToParent == null ? null : VLinkToParent.x,
						allPossibleChainings, random, earlyTermination);
		allPossibleChainings = null;
		if (candidatesPairs == null) {
			//			SubstrateNode nodeID = VNFUtils.addBacktrackingNode(backtrackingGraph, "subflow allocated successfully", backtrackingParentNode, backtrackingPrefix);
			return null;
		}
		sortCandidates(strategy, candidatesPairs, VNFFG, random);

		// System.out.println(sNodeCandidates.size() + " sNodeCandidates (" + param1 + " max)");
		this.VNFPossibleCandidates.num += candidatesPairs.size();
		candidateFor: for (Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>> candidatePair : candidatesPairs) {
			if (isCanceled || (maxBacktrackingSteps != -1 && maxBacktrackingSteps <= this.VNFBacktrackingSteps.backtrackingSteps)) {
				break;
			}

			this.VNFConsideredCandidates.num++;
			boolean linkReject = false;

			// add vNode
			VNFChainNode VNode = (VNFChainNode) candidatePair.y.get(0).getOwner();
			if (!VNet.containsVertex(VNode)) {
				VNet.addVertex(VNode, candidatePair.w.getFirst());
				VNFFlowEntry.thisNodesAdded.add(VNode);
			} else {

				for (VirtualLink l : VNet.getInEdges(VNode))
					if (l.VNFInputInterface == candidatePair.w.getSecond())
						continue candidateFor;

			}

			LinkedList<EntityMapping> backupAssignments = new LinkedList<EntityMapping>();

			EntityMapping nodeMapping = new EntityMapping(this, VNFFG, candidatePair.z, candidatePair.y);
			LinkedList<EntityMapping> dems = allAssignments.get(VNode);
			if (dems == null) {
				dems = new LinkedList<EntityMapping>();
				allAssignments.put(VNode, dems);
			}
			dems.add(nodeMapping);
			VNFFlowEntry.thisAssignments.add(nodeMapping);
			//			SubstrateNode nodeID = VNFUtils.addBacktrackingNode(backtrackingGraph, " -> " + candidatePair.y.getId(), backtrackingParentNode, backtrackingPrefix);

			//			boolean delayTooHigh = false;
			//			HashMap<VNF, Double> copyDelayInfo = new HashMap<VNF, Double>();
			EntityMapping pathToParentMapping = null;
			//			if (candidatePair.z != null) {

			// create and add vLink
			//				boolean VLinkToParentReused = false;
			if (VNFFlowEntry.prevVNode != null) {
				//					for (VirtualLink o : VNet.getOutEdges(VNFFlowEntry.prevVNode)) {
				//						if (VNet.getDest(o) == VNode) {
				//							LinkedList<AbstractDemand> newlinkdemands = new LinkedList<AbstractDemand>();
				//							for (AbstractDemand d : VLinkToParent.y) {
				//								newlinkdemands.add(d.getCopy(o));
				//							}
				//							VLinkToParentReused = true;
				//							pathToParentMapping = new EntityMapping(VNFFG, G, candidatePair.z, newlinkdemands, mapBidirectionalPaths);
				//
				//							LinkedList<LinkedList<SubstrateLink>> incoming = incomingPrimarypaths.get(VNode);
				//							if (incoming == null) {
				//								incoming = new LinkedList<LinkedList<SubstrateLink>>();
				//								incomingPrimarypaths.put(VNode, incoming);
				//							}
				//							incoming.add(candidatePair.z);
				//
				//							LinkedList<LinkedList<SubstrateLink>> thisIncoming = VNFFlowEntry.thisIncomingPrimarypathsAdded.get(VNode);
				//							if (thisIncoming == null) {
				//								thisIncoming = new LinkedList<LinkedList<SubstrateLink>>();
				//								VNFFlowEntry.thisIncomingPrimarypathsAdded.put(VNode, thisIncoming);
				//							}
				//							thisIncoming.add(candidatePair.z);
				//
				//							break;
				//						}
				//					}

				//					if (!VLinkToParentReused) {
				VNet.addEdge(VLinkToParent.x, VNFFlowEntry.prevVNode, VNode);
				//						VLinkToParent.x.setName("OUT:" + prevOutLink.source.name + "." + prevOutLink.source.outLinks.indexOf(prevOutLink) + " IN:" + candidatePair.w.getFirst().name + "." + candidatePair.w.getFirst().inputInterfaces.indexOf(candidatePair.w.getSecond())+"");
				VLinkToParent.x.VNFInputInterface = candidatePair.w.getSecond();
				VNFFlowEntry.thisLinksAdded.add(VLinkToParent.x);
				pathToParentMapping = new EntityMapping(this, VNFFG, G, candidatePair.a, VLinkToParent.y, mapBidirectionalPaths);

				//				LinkedList<LinkedList<SubstrateLink>> incoming = incomingPrimarypaths.get(VNode);
				//				if (incoming == null) {
				//					incoming = new LinkedList<LinkedList<SubstrateLink>>();
				//					incomingPrimarypaths.put(VNode, incoming);
				//				}
				//				incoming.add(candidatePair.a);

				//				LinkedList<LinkedList<SubstrateLink>> thisIncoming = VNFFlowEntry.thisIncomingPrimarypathsAdded.get(VNode);
				//				if (thisIncoming == null) {
				//					thisIncoming = new LinkedList<LinkedList<SubstrateLink>>();
				//					VNFFlowEntry.thisIncomingPrimarypathsAdded.put(VNode, thisIncoming);
				//				}
				//				thisIncoming.add(candidatePair.a);
				//					}

				LinkedList<EntityMapping> vldems = allAssignments.get(VLinkToParent.x);
				if (vldems == null) {
					vldems = new LinkedList<EntityMapping>();
					allAssignments.put(VLinkToParent.x, vldems);
				}
				vldems.add(pathToParentMapping);
				VNFFlowEntry.thisAssignments.add(pathToParentMapping);
			}

			//				double pathDelayMS = getPathDelayMS(candidate.z);
			//
			//				updateDelay: for (Entry<VNF, Double> e : VNFFlowEntry.delayInfo.entrySet()) {
			//					double newDelayMS = e.getValue() + pathDelayMS;
			//
			//					for (DelayConstraint constraint : VNFFG.delayConstraints)
			//						if (constraint.to == candidate.x && constraint.from == e.getKey())
			//							if (newDelayMS > constraint.maxDelay) {
			//								delayTooHigh = true;
			//								copyDelayInfo = null;
			//								break updateDelay;
			//							}
			//
			//					copyDelayInfo.put(e.getKey(), newDelayMS);
			//				}
			//				if (!VNFFlowEntry.flowNodes.isEmpty() && copyDelayInfo != null) {
			//					copyDelayInfo.put(VNFFlowEntry.flowNodes.getLast(), pathDelayMS);
			//				}
			//			}


			//				try {
			//					GraphMLExporter.export("steps_"+vnetcounter+"."+(idcounter++)+".graphml", VNet);
			//					System.out.println("EXPORTED: " + vnetcounter);
			//				} catch (IOException e1) {
			//					e1.printStackTrace();
			//				}

			LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>> backupCandidates = null;
			Tuple<SubstrateNode, LinkedList<SubstrateLink>> backupCandidate = null;
			CommonDemand backupDemand1 = null;
			LinkedList<AbstractDemand> backupVNodeDemands = new LinkedList<AbstractDemand>();
			if (candidatePair.w.getKey().needsBackup && (backupStrategy == BACKUP_STRATEGY.NODE || backupStrategy == BACKUP_STRATEGY.NODE_LINK)) {

				VNFChainNode backupVNode = new VNFChainNode(VNode.getVNF(), VNode.getLayer());
				backupVNode.setName(VNode.getName() + "nb");

				double capacityDemand = 0.0d;
				for (AbstractDemand d : candidatePair.y) {
					if (d instanceof IdDemand)
						continue;

					if (d instanceof CommonDemand) {
						capacityDemand = ((CommonDemand) d).getDemandedCapacity();
						continue;
					}

					if (d instanceof FreeSlotsDemand) {
						FreeSlotsDemand dd = (FreeSlotsDemand) d.getCopy(backupVNode);
						dd.isBackup = true;
						LinkedList<NetworkEntity<AbstractResource>> lst = new LinkedList<NetworkEntity<AbstractResource>>();
						lst.add(candidatePair.z);
						dd.setOriginalNodeMappedTo(lst);
						backupVNodeDemands.add(dd);
						continue;
					}

					backupVNodeDemands.add(d.getCopy(backupVNode));
				}

				if (VNode.backupDemands != null) {

					//TODO: IdDemand must not be set to the ID of the primary VNode, but to the backupVNode! -> fixed.
					//boolean bmdforfound = false;
					bmdfor: for (AbstractDemand backupMappingDemand : VNode.backupDemands) {
						if (backupMappingDemand instanceof IdDemand) { // && !VNode.backupDemands.getFirst().getMappings().isEmpty()) {
							for (Mapping backupMapping : backupMappingDemand.getMappings()) {
								NetworkEntity<?> mappedTo = backupMapping.getResource().getOwner();

								String id = ((IdResource) mappedTo.get(IdResource.class)).getId();
								IdDemand idDemand = new IdDemand(id, backupVNode);
								backupVNodeDemands.add(idDemand);

								//									bmdforfound = true;
								break bmdfor;
							}
						}

						//							if (!bmdforfound)
						//								throw new AssertionError();
					}

				}

//				LinkedList<NetworkEntity<AbstractResource>> lst = new LinkedList<NetworkEntity<AbstractResource>>();
//				lst.add(candidatePair.z);
//				backupVNodeDemands.add(new BackupDemand(capacityDemand, backupVNode, lst, "node backup -- node mapping (" + VNode.getId() + ")"));
				backupVNodeDemands.add(new CommonDemand(capacityDemand, backupVNode, "node backup -- node mapping (" + VNode.getId() + ")", true, maxSharing, candidatePair.z));

				if (VNode.backupDemands == null)
					VNode.backupDemands = new LinkedList<AbstractDemand>(backupVNodeDemands);
				else
					VNode.backupDemands.addAll(backupVNodeDemands);

				if (VNFFlowEntry.prevSNode != null) {
					VirtualLink backuppathToBackupNodeMappingVLink = new VirtualLink(VNFFG.layer);

					double thebwdemand = VLinkToParent.y.getDemandedCapacity();

//					backuppathToBackupNodeMappingVLink.isBackup = true;
					backuppathToBackupNodeMappingVLink.setName(VLinkToParent.x.getName() + "nbp");
//					backupDemand = new BackupDemand(thebwdemand, backuppathToBackupNodeMappingVLink, pathToParentMapping.substrateEntities, "node backup -- link mapping parent->backup node (" + VLinkToParent + ")");
					backupDemand1 = new CommonDemand(thebwdemand, backuppathToBackupNodeMappingVLink, "node backup -- link mapping parent->backup node (" + VLinkToParent + ")", true, maxSharing, null);
				}

				Transformer<SubstrateLink, Double> backuptransformer = new BackupPathTransformer(
//						freeBackupCapacityWeight,
//						backupDemand,
						backupDemand1,
						candidatePair.a);

				//					backupCandidates = getBackupCandidates(G, VNFFlowEntry.prevSNode, backuptransformer, candidatePair, maxPathLength, maxNumCandidates, VNFFlowEntry.flowDataRate, backupVNodeDemands);
				backupCandidates = getBackupCandidates(G, VNFFlowEntry.prevSNode, backuptransformer, maxPathLength, 1, VNFFlowEntry.flowDataRate, backupVNodeDemands, candidatePair.z, random);
				//					sortBackupCandidates(backupCandidates, VNFFG);
				if (backupCandidates.isEmpty()) {
					backupCandidates = null;
					linkReject = true;
				} else {
					backupCandidate = backupCandidates.getFirst();

					//						if (backupCandidate.x == candidatePair.y) {
					//							throw new AssertionError();
					//						}

					if (backupCandidates.size() > 1)
						throw new AssertionError();
				}
			}


			if (!linkReject) {
				if (backupCandidates != null) {
					this.VNFPossibleCandidates.num += backupCandidates.size();
					this.VNFConsideredCandidates.num++;
				}


				if (candidatePair.w.getKey().needsBackup && (backupStrategy == BACKUP_STRATEGY.NODE || backupStrategy == BACKUP_STRATEGY.NODE_LINK)) {

					// node backup -- node mapping
					EntityMapping backupNodeMapping = new EntityMapping(this, VNFFG, backupCandidate.x, backupVNodeDemands);
					backupAssignments.add(backupNodeMapping);
					LinkedList<EntityMapping> bdems = allAssignments.get(fakeBackupVNode);
					if (bdems == null) {
						bdems = new LinkedList<EntityMapping>();
						allAssignments.put(fakeBackupVNode, bdems);
					}
					bdems.add(backupNodeMapping);

					VNFFlowEntry.thisAssignments.add(backupNodeMapping);
				}

				if (candidatePair.a != null) { // at least one node has already been placed in the flow

					// node backup -- link mapping from parent node to backup node
					if (backupStrategy == BACKUP_STRATEGY.NODE || backupStrategy == BACKUP_STRATEGY.NODE_LINK) {

						if (backupCandidate != null) {
							backupCandidate.y.removeAll(candidatePair.a);
							EntityMapping pathToBackupNodeMapping = new EntityMapping(this, VNFFG, G, backupCandidate.y, backupDemand1, mapBidirectionalPaths);
//							backupDemand1.backupPath = backupCandidate.y;
							backupAssignments.add(pathToBackupNodeMapping);
							LinkedList<EntityMapping> bdems = allAssignments.get(fakeBackupVNode);
							if (bdems == null) {
								bdems = new LinkedList<EntityMapping>();
								allAssignments.put(fakeBackupVNode, bdems);
							}
							bdems.add(pathToBackupNodeMapping);
							VNFFlowEntry.thisAssignments.add(pathToBackupNodeMapping);
						}

						if (VNFFlowEntry.prevBackupNode != null) {
							
							// node backup -- link mapping from parent backup node to current node
							VirtualLink parentNodeBackupPathMappingVLink = new VirtualLink(VNFFG.layer);
							parentNodeBackupPathMappingVLink.setName(VLinkToParent.x.getName() + "nbc");
//							BackupDemand backupDemand = new BackupDemand(VNFFlowEntry.flowDataRate, parentNodeBackupPathMappingVLink, pathToParentMapping.substrateEntities, "node backup -- link backup node->current node (" + VLinkToParent + ")");
							CommonDemand backupDemand = new CommonDemand(VNFFlowEntry.flowDataRate, parentNodeBackupPathMappingVLink, "node backup -- link backup node->current node (" + VLinkToParent + ")", true, maxSharing, null);
							
//							Transformer<SubstrateLink, Double> backuppathtransformer = new BackupPathTransformer(freeBackupCapacityWeight, backupDemand, parentNodeBackupPathMappingVLinkDemands, candidatePair.a);
							Transformer<SubstrateLink, Double> backuppathtransformer = new BackupPathTransformer(backupDemand, candidatePair.a);
							List<SubstrateLink> parentNodeBackupPath = findShortestPath(G, VNFFlowEntry.prevBackupNode, candidatePair.z, backuppathtransformer, maxPathLength);
							if (parentNodeBackupPath == null) {
								linkReject = true;
							} else {
								parentNodeBackupPath.removeAll(candidatePair.a);
								EntityMapping parentNodeBackupPathMapping = new EntityMapping(this, VNFFG, G, parentNodeBackupPath, backupDemand, mapBidirectionalPaths);
								backupAssignments.add(parentNodeBackupPathMapping);
								LinkedList<EntityMapping> bldems = allAssignments.get(fakeBackupVLink);
								if (bldems == null) {
									bldems = new LinkedList<EntityMapping>();
									allAssignments.put(fakeBackupVLink, bldems);
								}
								bldems.add(parentNodeBackupPathMapping);
								VNFFlowEntry.thisAssignments.add(parentNodeBackupPathMapping);

								// link mapping from parent backup node to current backup node
								if (candidatePair.w.getKey().needsBackup) {
									VirtualLink parentNodeBackupPathMappingVLink2 = new VirtualLink(VNFFG.layer);
									parentNodeBackupPathMappingVLink2.setName(VLinkToParent.x.getName() + "nbb");
//									BackupDemand backupDemand2 = new BackupDemand(VNFFlowEntry.flowDataRate, parentNodeBackupPathMappingVLink2, pathToParentMapping.substrateEntities, "node backup -- link backup node->current backup node (" + VLinkToParent + ")");
									CommonDemand backupDemand2 = new CommonDemand(VNFFlowEntry.flowDataRate, parentNodeBackupPathMappingVLink2, "node backup -- link backup node->current backup node (" + VLinkToParent + ")", true, maxSharing, null);

//									Transformer<SubstrateLink, Double> backuppathtransformer2 = new BackupPathTransformer(freeBackupCapacityWeight, backupDemand2, parentNodeBackupPathMappingVLinkDemands2, candidatePair.a);
									Transformer<SubstrateLink, Double> backuppathtransformer2 = new BackupPathTransformer(backupDemand2, candidatePair.a);

									List<SubstrateLink> parentNodeBackupToBackupPath = findShortestPath(G, VNFFlowEntry.prevBackupNode, backupCandidate.x, backuppathtransformer2, maxPathLength);
									if (parentNodeBackupToBackupPath == null) {
										linkReject = true;
									} else {
										parentNodeBackupToBackupPath.removeAll(candidatePair.a);
										EntityMapping parentNodeBackupToBackupPathMapping = new EntityMapping(this, VNFFG, G, parentNodeBackupToBackupPath, backupDemand2, mapBidirectionalPaths);
										backupAssignments.add(parentNodeBackupToBackupPathMapping);
										bldems.add(parentNodeBackupToBackupPathMapping);
										VNFFlowEntry.thisAssignments.add(parentNodeBackupToBackupPathMapping);
									}
								}
							}
						}
					}
					
					if (prevOutLink.needsBackup && (!linkReject && (backupStrategy == BACKUP_STRATEGY.LINK || backupStrategy == BACKUP_STRATEGY.NODE_LINK))) {
						VirtualLink backupPathToParentMappingVLink = new VirtualLink(VNFFG.layer); //VLinkToParent.getCopy(true);
//						backupPathToParentMappingVLink.isBackup = true;
						backupPathToParentMappingVLink.setName(VLinkToParent.x.getName() + "lb");
						double thebwdemand = VLinkToParent.y.getDemandedCapacity();

//						BackupDemand backupDemand = new BackupDemand(thebwdemand, backupPathToParentMappingVLink, pathToParentMapping.substrateEntities, "link backup (" + candidatePair.w.getFirst().id + ")");
						CommonDemand backupDemand = new CommonDemand(thebwdemand, backupPathToParentMappingVLink, "link backup (" + candidatePair.w.getFirst().id + ")", true, maxSharing, candidatePair.a);
						
//						Transformer<SubstrateLink, Double> disjointTransformer = new DisjointPathTransformer(freeBackupCapacityWeight, backupDemand, backupPathToParentMappingVLinkDemands, candidatePair.a, G);
						Transformer<SubstrateLink, Double> disjointTransformer = new DisjointPathTransformer(backupDemand, candidatePair.a, G);
						LinkedList<SubstrateLink> backupPath = findShortestPath(G, VNFFlowEntry.prevSNode, candidatePair.z, disjointTransformer, maxPathLength);
						if (backupPath == null) {
							linkReject = true;
						} else {

//							SubstrateLink l = null;
//							CommonResource r = null;
//							if (!backupPath.isEmpty()) {
//								l = backupPath.getFirst();
//								r = (CommonResource) l.get(CommonResource.class);
//								System.out.println("PRE: " + r.getCapacity() + "  " +  r.getAvailableCapacity());
//								for (CommonDemand m : r.getMappedDemands()) {
//									System.out.println("  " + m.getDemandedCapacity() + "   " + m.maxSharing + "   " + m.isBackup);
//								}
//							}
							
							EntityMapping backupPathToParentMapping = new EntityMapping(this, VNFFG, G, backupPath, backupDemand, mapBidirectionalPaths);

//							if (l != null) {
//								System.out.println("POST: " + r.getCapacity() + "  " +  r.getAvailableCapacity());
//								for (CommonDemand m : r.getMappedDemands()) {
//									System.out.println("  " + m.getDemandedCapacity() + "   " + m.maxSharing + "   " + m.isBackup);
//								}
//								System.out.println();
//							}
							
							
							backupAssignments.add(backupPathToParentMapping);
							LinkedList<EntityMapping> bldems = allAssignments.get(fakeBackupVLink);
							if (bldems == null) {
								bldems = new LinkedList<EntityMapping>();
								allAssignments.put(fakeBackupVLink, bldems);
							}
							bldems.add(backupPathToParentMapping);
//							VLinkToParent.y.backupPath = backupPath;
							VNFFlowEntry.thisAssignments.add(backupPathToParentMapping);
						}
					}
				}
			}

			if (!linkReject) {
				boolean allChildrenMapped = true;
				LinkedList<EntityMapping> childAssignments = new LinkedList<EntityMapping>();
				LinkedList<VNFChainNode> childNodesAdded = new LinkedList<VNFChainNode>();
				LinkedList<VirtualLink> childLinksAdded = new LinkedList<VirtualLink>();
				//LinkedHashMap<VirtualNode, LinkedList<LinkedList<SubstrateLink>>> childIncomingPaths = new LinkedHashMap<VirtualNode, LinkedList<LinkedList<SubstrateLink>>>();

				LinkedHashMap<VirtualNode, VNFFlowInfo> currentVNFFlowInfos = new LinkedHashMap<VirtualNode, VNFFlowInfo>();
				for (Entry<VirtualNode, VNFFlowInfo> e : VNFFlowEntry.VNFFlowInfos.entrySet())
					currentVNFFlowInfos.put(e.getKey(), e.getValue().getCopy());

				VNFFlowInfo i = currentVNFFlowInfos.get(VNode);
				if (i == null) {
					i = new VNFFlowInfo();
					currentVNFFlowInfos.put(VNode, i);
				}

				i.flowInputInterfaces.addAll(thisFlowInputInterfaces);
				i.flowLinks.addAll(thisFlowLinks);
				i.flowNodes.addAll(thisFlowNodes);


				Collection<VirtualLink> ins = VNet.getInEdges(VNode);
				if (candidatePair.w.getFirst() == VNFFG.getInitialNode() || ins.size() == candidatePair.w.getFirst().inputInterfaces.size()) {

					LinkedList<VNFLink> outs = candidatePair.w.getFirst().outLinks;
					if (outs.isEmpty()) {
						outs = new LinkedList<VNFLink>();
						outs.add(new VNFLink(candidatePair.w.getFirst(), null, 1.0d, false));
					}

					double sumFlowRates = 0.0d;
					if (candidatePair.w.getFirst() == VNFFG.getInitialNode()) {
						sumFlowRates = VNFFG.initialBW;
					} else {
						for (VirtualLink iii : ins) {
							sumFlowRates += ((CommonDemand) iii.get(CommonDemand.class)).getDemandedCapacity();
						}
					}

					for (VNFLink out : outs) {

						double nextFlowDataRate = Math.floor(sumFlowRates * out.dataratepercentage);

						VNFFlowEntry nextVNFFlowEntry = new VNFFlowEntry(
								VNode, candidatePair.z, out.bandwidthDemand,
								currentVNFFlowInfos,
								nextFlowDataRate,
								backupCandidate == null ? null : backupCandidate.x);

						Tuple<VNFChain, LinkedList<EntityMapping>> childResult = mapOneVNFFG(G, creator,
								nextVNFFlowEntry, VNet, allAssignments, VNFFG, maxPathLength,
								maxNumCandidates, mapBidirectionalPaths,
								labelsAndVNFTypes,
//								sharingFactor,
								backupStrategy,
								//backtrackingGraph, nodeID, backtrackingPrefix + "  ",
								strategy,
								fakeBackupVNode, fakeBackupVLink,
								//								incomingPrimarypaths,
								candidatePair.x,
								out, candidatePair.w.getSecond(), candidatePair.w.getFirst(),
								maxBacktrackingSteps,
								random,
								earlyTermination,
//								freeBackupCapacityWeight
								maxSharing
								);

						if (childResult == null || (maxBacktrackingSteps != -1 && maxBacktrackingSteps <= this.VNFBacktrackingSteps.backtrackingSteps)) {
							allChildrenMapped = false;
							break;
						}
						childAssignments.addAll(nextVNFFlowEntry.thisAssignments);
						childNodesAdded.addAll(nextVNFFlowEntry.thisNodesAdded);
						childLinksAdded.addAll(nextVNFFlowEntry.thisLinksAdded);
						//						for (Entry<VirtualNode, LinkedList<LinkedList<SubstrateLink>>> e : nextVNFFlowEntry.thisIncomingPrimarypathsAdded.entrySet()) {
						//							LinkedList<LinkedList<SubstrateLink>> c = childIncomingPaths.get(e.getKey());
						//							if (c == null) {
						//								c = new LinkedList<LinkedList<SubstrateLink>>();
						//								childIncomingPaths.put(e.getKey(), c);
						//							}
						//							c.addAll(e.getValue());
						//						}
					}
				}

				if (allChildrenMapped) {
					VNFFlowEntry.thisAssignments.addAll(childAssignments);
					VNFFlowEntry.thisNodesAdded.addAll(childNodesAdded);
					VNFFlowEntry.thisLinksAdded.addAll(childLinksAdded);

					for (Entry<VirtualNode, VNFFlowInfo> e : currentVNFFlowInfos.entrySet()) {
						VNFFlowInfo ii = VNFFlowEntry.VNFFlowInfos.get(e.getKey());
						if (ii == null) {
							ii = e.getValue();
							VNFFlowEntry.VNFFlowInfos.put(e.getKey(), ii);
						}
						ii.add(e.getValue());
					}

					//					for (Entry<VirtualNode, LinkedList<LinkedList<SubstrateLink>>> e : childIncomingPaths.entrySet()) {
					//						LinkedList<LinkedList<SubstrateLink>> c = VNFFlowEntry.thisIncomingPrimarypathsAdded.get(e.getKey());
					//						if (c == null) {
					//							c = new LinkedList<LinkedList<SubstrateLink>>();
					//							VNFFlowEntry.thisIncomingPrimarypathsAdded.put(e.getKey(), c);
					//						}
					//						c.addAll(e.getValue());
					//					}

					return new Tuple<VNFChain, LinkedList<EntityMapping>>(VNet, VNFFlowEntry.thisAssignments);
				}


				Iterator<EntityMapping> childAssignmentsIterator = childAssignments.descendingIterator();
				while (childAssignmentsIterator.hasNext()) {
					EntityMapping m = childAssignmentsIterator.next();
					m.free();
					for (LinkedList<EntityMapping> ms : allAssignments.values()) {
						ms.remove(m);
					}
				}
				childAssignments.clear();

				Iterator<VirtualLink> childLinksAddedIterator = childLinksAdded.descendingIterator();
				while (childLinksAddedIterator.hasNext()) {
					VirtualLink l = childLinksAddedIterator.next();
					VNet.removeEdge(l);
					l.VNFInputInterface = null;
				}
				childLinksAdded.clear();
				Iterator<VNFChainNode> childNodesAddedIterator = childNodesAdded.descendingIterator();
				while (childNodesAddedIterator.hasNext()) {
					VNFChainNode n = childNodesAddedIterator.next();
					VNet.removeVertex(n);
				}
				childNodesAdded.clear();

			}


			Iterator<EntityMapping> backupAssignmentIterator = backupAssignments.descendingIterator();
			while (backupAssignmentIterator.hasNext()) {
				EntityMapping m = backupAssignmentIterator.next();
				m.free();
				for (LinkedList<EntityMapping> ms : allAssignments.values()) {
					ms.remove(m);
				}
				VNFFlowEntry.thisAssignments.remove(m);
			}
			backupAssignments.clear();


			//			if (pathToParentMapping != null) {
			//				VNFFlowEntry.thisIncomingPrimarypathsAdded.get(VNode).remove(candidatePair.a);
			//				incomingPrimarypaths.get(VNode).remove(candidatePair.a);
			//			}

			Iterator<EntityMapping> thisAssignmentIterator = VNFFlowEntry.thisAssignments.descendingIterator();
			while (thisAssignmentIterator.hasNext()) {
				EntityMapping m = thisAssignmentIterator.next();
				m.free();
				for (LinkedList<EntityMapping> ms : allAssignments.values()) {
					ms.remove(m);
				}
			}
			VNFFlowEntry.thisAssignments.clear();

			Iterator<VirtualLink> thisLinksAddedIterator = VNFFlowEntry.thisLinksAdded.descendingIterator();
			while (thisLinksAddedIterator.hasNext()) {
				VirtualLink l = thisLinksAddedIterator.next();
				VNet.removeEdge(l);
				l.VNFInputInterface = null;
			}
			VNFFlowEntry.thisLinksAdded.clear();
			Iterator<VNFChainNode> thisNodeAddedIterator = VNFFlowEntry.thisNodesAdded.descendingIterator();
			while (thisNodeAddedIterator.hasNext()) {
				VNFChainNode n = thisNodeAddedIterator.next();
				VNet.removeVertex(n);
			}
			VNFFlowEntry.thisNodesAdded.clear();


			//			for (SubstrateNode n : G.getVertices()) {
			//				String str = n.toString() + "\n";
			////				for (AbstractResource s : n.get()) {
			////					for (Mapping m : s.getMappings()) {
			////						strstr += m.getDemand().getClass().getSimpleName() + "." + m.getDemand() + "\n";
			////					}
			////				}
			//				
			//				String strstr = "";
			//				for (AbstractResource s : n.get()) {
			//					strstr += s.hashCode()+":"+s.getClass().getSimpleName() + ": ";
			//					for (Mapping m : s.getMappings()) {
			//						strstr += m.getDemand().getClass().getSimpleName() + "." + m.getDemand() + "\n";
			//					}
			//				}
			//				
			//				if (!oldstates.get(n).equals(str)) {
			//					throw new AssertionError("OLD STATE: " + oldstates.get(n) + "\n" + oldstates2.get(n) + "\n\n" + str + "\n" + strstr);
			//				}
			//				
			//			}

		}

		this.VNFBacktrackingSteps.backtrackingSteps++;
		//		VNFUtils.addBacktrackingNode(backtrackingGraph, "backtracking", backtrackingParentNode, backtrackingPrefix);
		return null;
	}

	LinkedList<SubstrateLink> findShortestPath(SubstrateNetwork sNetwork,
			SubstrateNode n1, SubstrateNode n2,
			Transformer<SubstrateLink, Double> transformer, int epsilon) {

		if (n1.getId() == n2.getId()) {
			return new LinkedList<SubstrateLink>();
		}

		MyDijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new MyDijkstraShortestPath<SubstrateNode, SubstrateLink>(
				sNetwork, transformer, true);

		LinkedList<SubstrateLink> path = dijkstra.getPath(n1, n2, epsilon);

		return path;
	}

	private void sortCandidates(COORDVNF_STRATEGY strategy,
			LinkedList<Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>> candidates,
			VNFFG vNFFG, Random random) {

		//		Collections.shuffle(candidates, random);

		Collections.sort(candidates,
				new Comparator<Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>>() {

			@Override
			public int compare(
					Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>> o1,
					Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>> o2) {

				if (strategy == COORDVNF_STRATEGY.BANDWIDTH) {
					double o1sum = VNFUtils.getTotalTrafficRate(o1.w.getFirst());
					double o2sum = VNFUtils.getTotalTrafficRate(o2.w.getFirst());
					if (o1sum > o2sum)
						return +1;
					if (o1sum < o2sum)
						return -1;

					double pathLength1 = (o1.a == null ? 0.0d : ((double) o1.a.size()));
					double pathLength2 = (o2.a == null ? 0.0d : ((double) o2.a.size()));

					if (pathLength1 > pathLength2)
						return +1;
					if (pathLength1 < pathLength2)
						return -1;

				} else if (strategy == COORDVNF_STRATEGY.VNF_INSTANCES) {
					if (o1.w.getFirst().outLinks.size() > o2.w.getFirst().outLinks.size())
						return +1;
					if (o1.w.getFirst().outLinks.size() < o2.w.getFirst().outLinks.size())
						return -1;
				}

				//				boolean o1free = ((FreeSlotsResource) o1.z.get(FreeSlotsResource.class)).isFree(o1.w.getFirst().FreeSlotsDemand);
				//				boolean o2free = ((FreeSlotsResource) o2.z.get(FreeSlotsResource.class)).isFree(o2.w.getFirst().FreeSlotsDemand);
				//
				//				if (!o1free && o2free)
				//					return +1;
				//				if (o1free && !o2free)
				//					return -1;


				//				int indexo1 = candidates.indexOf(o1);
				//				int indexo2 = candidates.indexOf(o2);
				//				if (indexo1 < indexo2)
				//					return -1;
				//				if (indexo1 > indexo2)
				//					return 1;

				return 0;
			}

		});

	}

	//	private void sortBackupCandidates(
	//			LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>> candidates,
	//			VNFFG vNFFG) {
	//
	//		Collections
	//		.sort(candidates,
	//				new Comparator<Tuple<SubstrateNode, LinkedList<SubstrateLink>>>() {
	//
	//			@Override
	//			public int compare(
	//					Tuple<SubstrateNode, LinkedList<SubstrateLink>> o1,
	//					Tuple<SubstrateNode, LinkedList<SubstrateLink>> o2) {
	//
	//				int o1length = o1.y == null ? 0 : o1.y.size();
	//				int o2length = o2.y == null ? 0 : o2.y.size();
	//				if (o1length > o2length)
	//					return +1;
	//				if (o1length < o2length)
	//					return -1;
	//
	//				//				double o1weight = ((BackupResource) o1.x.get(BackupResource.class)).getAvailableBackupCapacity();
	//				//				double o2weight = ((BackupResource) o2.x.get(BackupResource.class)).getAvailableBackupCapacity();
	//				//
	//				//				if (o1weight < o2weight)
	//				//					return +1;
	//				//				if (o1weight > o2weight)
	//				//					return -1;
	//
	//				return 0;
	//			}
	//
	//		});
	//
	//	}

	// private double computeCandidateWeight(Triple<VNF, SubstrateNode,
	// LinkedList<SubstrateLink>> o1, double candidateWeight) {
	// double isNotHere = 1.0d;
	//
	// int numOfSameType = 0;
	// for (AbstractResource res : o1.y.get()) {
	// for (Mapping m : res.getMappings()) {
	// AbstractDemand d = m.getDemand();
	// if (d instanceof FreeSlotsDemand) {
	// if (((FreeSlotsDemand) d).VNFType.equals(o1.x.FreeSlotsDemand.VNFType)) {
	// numOfSameType++;
	// }
	// }
	// }
	// }
	//
	// if (numOfSameType > 0)
	// isNotHere = 0.0d;
	//
	// double pathLength = (o1.z == null ? 0.0d : ((double) o1.z.size()));
	// double result = ((candidateWeight * pathLength) + ((1.0d-candidateWeight)
	// * isNotHere));
	// return result;
	// }

	public static Double getDataRateInOptimalChain(VNFChain optimalChain,
			VNFChainNode node) {
		Double maxDataRateInOptimalStream = null;

		for (VirtualNode n : optimalChain.getVertices()) {
			VNFChainNode vn = (VNFChainNode) n;
			if (vn.VNFid == node.VNFid) {
				for (VirtualLink vl : optimalChain.getOutEdges(vn)) {
					double thisBW = Utils.getBandwidthDemand(vl);
					if (maxDataRateInOptimalStream == null || thisBW > maxDataRateInOptimalStream)
						maxDataRateInOptimalStream = thisBW;
				}
			}
		}

		return maxDataRateInOptimalStream;
	}

	public static class VNFFlowEntry {
		final VNFChainNode prevVNode; // prev VNode that has already been embedded successfully on top of prevSNode
		final SubstrateNode prevSNode; // prev SNode that has already been embedded successfully
		final CommonDemand prevVNFLinkDemand;
		final double flowDataRate;
		final LinkedHashMap<VirtualNode, VNFFlowInfo> VNFFlowInfos;

		final SubstrateNode prevBackupNode;

		//		final LinkedHashMap<VirtualNode, LinkedList<LinkedList<SubstrateLink>>> thisIncomingPrimarypathsAdded;
		final LinkedList<EntityMapping> thisAssignments;
		final LinkedList<VNFChainNode> thisNodesAdded;
		final LinkedList<VirtualLink> thisLinksAdded;

		public VNFFlowEntry(VNFChainNode prevVNode, SubstrateNode prevSNode,
				CommonDemand prevVNFLinkDemand,
				LinkedHashMap<VirtualNode, VNFFlowInfo> VNFFlowInfos,
				double flowDataRate,
				SubstrateNode prevBackupNode) {

			this.prevVNode = prevVNode;
			this.prevSNode = prevSNode;
			this.prevVNFLinkDemand = prevVNFLinkDemand;
			this.VNFFlowInfos = VNFFlowInfos;
			this.flowDataRate = flowDataRate;

			this.thisAssignments = new LinkedList<EntityMapping>();

			this.prevBackupNode = prevBackupNode;
			//			this.thisIncomingPrimarypathsAdded = new LinkedHashMap<VirtualNode, LinkedList<LinkedList<SubstrateLink>>>();

			this.thisNodesAdded = new LinkedList<VNFChainNode>();
			this.thisLinksAdded = new LinkedList<VirtualLink>();
		}
	}

	LinkedList<Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>> getCandidates(
			SubstrateNetwork G, VNFChain VNet,
			LinkedHashMap<NetworkEntity<?>, LinkedList<EntityMapping>> allAssignments,
			VNFChainEntityCreator creator,
			SubstrateNode prevSNode,
			final Transformer<SubstrateLink, Double> linkTransformer,
			LinkedList<Pair<VNF, VNFInputInterface>> VNFCandidatesPairs,
			int maxPathLength,
			int maxNumCandidates,
			double flowDataRate,
			VNFFlowEntry VNFFlowEntry,
			VNFFG VNFFG,
			VirtualLink vLinkToParent,
			LinkedList<VNFChain> allPossibleChainings,
			Random random,
			boolean earlyTermination
			) {

		LinkedList<Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>> result = new LinkedList<Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>>();

		LinkedHashMap<Pair<VNF, VNFInputInterface>, Pair<LinkedList<VNFChain>, List<AbstractDemand>>> VNFChainNodesPairs = new LinkedHashMap<Pair<VNF, VNFInputInterface>, Pair<LinkedList<VNFChain>, List<AbstractDemand>>>();
		for (Pair<VNF, VNFInputInterface> vPair : VNFCandidatesPairs) {
			Tuple<VNFChainNode, LinkedList<AbstractDemand>> newdemands = creator.createVNode(vPair.getFirst(), flowDataRate, false);
			LinkedList<VNFChainNode> instances = VNet.VNFInstances.get(vPair.getFirst());
			if (instances != null) {
				for (VNFChainNode i : instances) {
					int ins = vPair.getFirst().inputInterfaces.size();
					if (ins == 0 && VNFFG.getInitialNode() != vPair.getFirst())
						ins = 1;

					if (VNFFG.getTerminatingNodes().contains(vPair.getFirst()) || VNet.getInEdges(i).size() < ins) {

						boolean posfound = false;
						allfor: for (LinkedList<EntityMapping> lm : allAssignments.values()) {
							for (EntityMapping m : lm) {
								for (ResourceDemandEntry resdem : m.mappedResources) {
									if (resdem.dem.getOwner() == i) {

										NetworkEntity<?> mappedTo = resdem.res.getOwner();

										String id = ((IdResource) mappedTo.get(IdResource.class)).getId();
										IdDemand idDemand = new IdDemand(id, i);
										newdemands.y.add(idDemand);

										posfound = true;
										break allfor;
									}
								}
							}
						}

						if (!posfound)
							throw new AssertionError();

						for (AbstractDemand d : newdemands.y) {
							d.setOwner(i);
						}

						break;
					}
				}
			}


			VNFChainNode VNode = (VNFChainNode) newdemands.y.get(0).getOwner();
			boolean VNodeAdded = false;
			if (!VNet.containsVertex(VNode)) {
				VNet.addVertex(VNode, newdemands.x.getVNF());
				VNodeAdded = true;
			}
			if (VNFFlowEntry.prevVNode != null) {
				VNet.addEdge(vLinkToParent, VNFFlowEntry.prevVNode, VNode);
				vLinkToParent.VNFInputInterface = vPair.getSecond();
			}

			//			if (earlyTermination) {
			//				LinkedList<VNFChain> newAllPossibleChainings = new LinkedList<VNFChain>();
			//				//									linkReject = true;
			//				//									for (VNFChain c : allPossibleChainings) {
			//				//										if (c.containsNet(VNet)) {
			//				//											linkReject = false;
			//				//											break;
			//				//										}
			//				//									}
			//
			//				for (VNFChain chain : allPossibleChainings) {
			//					if (chain.containsNet(VNet)) {
			//						LinkedList<VirtualNode> unmappedNodes = chain.getUnmappedNodes(VNet);
			//						
			//						double capdemsum = 0.0d;
			//						for (VirtualNode v : unmappedNodes) {
			//							CapacityDemand c = (CapacityDemand) v.get(CapacityDemand.class);
			//							double d = c == null ? 0.0d : c.getDemandedCapacity();
			//							capdemsum += d;
			//						}
			//
			////						if (capdemsum > this.availableCapacity) {
			////							this.availableCapacityExceeded++;
			//////							System.err.println("availableCapacityExceeded2++");
			////						} else {
			////							System.err.println("capdemsum: " + capdemsum + "; availableCapacity: " + this.availableCapacity);
			//							newAllPossibleChainings.add(chain);
			////						}
			//					}
			//				}
			//
			//				if (newAllPossibleChainings.isEmpty()) {
			////					System.err.println("newAllPossibleChainings.isEmpty()");
			////					System.out.println(G);
			////					System.out.println(VNFFG);
			//				} else {
			//					VNFChainNodesPairs.put(vPair, new Pair<LinkedList<VNFChain>, List<AbstractDemand>>(newAllPossibleChainings, newdemands.y));
			//				}
			//			} else {
			VNFChainNodesPairs.put(vPair, new Pair<LinkedList<VNFChain>, List<AbstractDemand>>(null, newdemands.y));
			//			}


			if (VNFFlowEntry.prevVNode != null) {
				VNet.removeEdge(vLinkToParent);
			}
			if (VNodeAdded)
				VNet.removeVertex(VNode);
		}

		if (VNFChainNodesPairs.isEmpty())
			return null;

		if (prevSNode == null) {
			LinkedList<SubstrateNode> nodes = new LinkedList<SubstrateNode>(G.getVertices());
			Collections.shuffle(nodes, random);
			outer: for (SubstrateNode n : nodes) {
				for (Entry<Pair<VNF, VNFInputInterface>, Pair<LinkedList<VNFChain>, List<AbstractDemand>>> VNFCandidate : VNFChainNodesPairs.entrySet()) {
					if (maxNumCandidates != -1 && result.size() == maxNumCandidates)
						break outer;
					if (EntityMapping.fulfills(n.get(), VNFCandidate.getValue().getSecond())) {
						result.add(new Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>(VNFCandidate.getValue().getFirst(), VNFCandidate.getKey(), VNFCandidate.getValue().getSecond(), n, null));
					}
				}
			}
		} else {

			LinkedList<SubstrateNode> visited = new LinkedList<SubstrateNode>();
			LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>> queue = new LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>>();
			queue.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(prevSNode, new LinkedList<SubstrateLink>()));
			visited.add(prevSNode);

			for (Entry<Pair<VNF, VNFInputInterface>, Pair<LinkedList<VNFChain>, List<AbstractDemand>>> VNFCandidate : VNFChainNodesPairs.entrySet()) {
				if (maxNumCandidates != -1 && result.size() == maxNumCandidates)
					break;
				if (EntityMapping.fulfills(prevSNode.get(), VNFCandidate.getValue().getSecond())) {
					result.add(new Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>(
							VNFCandidate.getValue().getFirst(),
							VNFCandidate.getKey(), VNFCandidate.getValue().getSecond(),
							prevSNode, new LinkedList<SubstrateLink>()));
				}
			}

			while (!queue.isEmpty()) {
				Tuple<SubstrateNode, LinkedList<SubstrateLink>> entry = queue.poll();
				LinkedList<SubstrateLink> outEdges = new LinkedList<SubstrateLink>(	G.getOutEdges(entry.x));
				Collections.sort(outEdges, new Comparator<SubstrateLink>() {

					@Override
					public int compare(SubstrateLink arg0, SubstrateLink arg1) {
						double i0 = linkTransformer.transform(arg0);
						double i1 = linkTransformer.transform(arg1);

						if (i0 < i1)
							return -1;
						if (i0 > i1)
							return +1;

						return 0;
					}
				});

				outer: for (SubstrateLink out : outEdges) {
					if (linkTransformer.transform(out) != Double.POSITIVE_INFINITY) {
						SubstrateNode opp = G.getOpposite(entry.x, out);
						if (!visited.contains(opp)) {
							visited.add(opp);

							LinkedList<SubstrateLink> path = new LinkedList<SubstrateLink>(entry.y);
							path.add(out);

							boolean validPath = false;
							if (maxPathLength == -1) {
								validPath = true;
							} else {
								if (path.size() <= maxPathLength) {
									validPath = true;
								}
							}

							if (validPath) {
								for (Entry<Pair<VNF, VNFInputInterface>, Pair<LinkedList<VNFChain>, List<AbstractDemand>>> VNFCandidate : VNFChainNodesPairs.entrySet()) {
									if (maxNumCandidates != -1 && result.size() == maxNumCandidates)
										break outer;
									if (EntityMapping.fulfills(opp.get(), VNFCandidate.getValue().getSecond())) {
										result.add(new Quintuple<LinkedList<VNFChain>, Pair<VNF, VNFInputInterface>, List<AbstractDemand>, SubstrateNode, LinkedList<SubstrateLink>>(
												VNFCandidate.getValue().getFirst(),
												VNFCandidate.getKey(),
												VNFCandidate.getValue().getSecond(), opp,
												path));
									}
								}
								queue.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(opp, path));
							}
						}
					}
				}
			}
		}

		// System.out.println("from " + (prevSNode == null ? "null" :
		// prevSNode.getId()));
		// for (Triple<VNF, SubstrateNode, LinkedList<SubstrateLink>> r :
		// result) {
		// if (r.z == null)
		// System.out.println("  NULL");
		// else
		// for (SubstrateLink l : r.z)
		// System.out.println("  " + l.getId());
		// System.out.println("to " + r.y.getId());
		// }
		return result;
	}


	LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>> getBackupCandidates(
			SubstrateNetwork G,
			SubstrateNode prevSNode, final Transformer<SubstrateLink, Double> linkTransformer,
			int maxPathLength,
			int maxNumCandidates,
			double flowDataRate,
			List<AbstractDemand> backupVNodeDemands,
			SubstrateNode y,
			Random random) {
		LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>> result = new LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>>();

		if (prevSNode == null) {
			LinkedList<SubstrateNode> nodes = new LinkedList<SubstrateNode>(G.getVertices());
			Collections.shuffle(nodes, random);
			for (SubstrateNode n : nodes) {
				if (maxNumCandidates != -1 && result.size() == maxNumCandidates)
					break;
				if (n != prevSNode && EntityMapping.fulfills(n.get(), backupVNodeDemands)) {
					result.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(n, null));
				}
			}
		} else {

			LinkedList<SubstrateNode> visited = new LinkedList<SubstrateNode>();
			LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>> queue = new LinkedList<Tuple<SubstrateNode, LinkedList<SubstrateLink>>>();
			queue.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(prevSNode, new LinkedList<SubstrateLink>()));
			visited.add(prevSNode);

			if (prevSNode != y && EntityMapping.fulfills(prevSNode.get(), backupVNodeDemands) && (maxNumCandidates == -1 || result.size() < maxNumCandidates)) {
				result.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(prevSNode, new LinkedList<SubstrateLink>()));
			}

			while (!queue.isEmpty()) {
				Tuple<SubstrateNode, LinkedList<SubstrateLink>> entry = queue.poll();
				LinkedList<SubstrateLink> outEdges = new LinkedList<SubstrateLink>(G.getOutEdges(entry.x));
				Collections.sort(outEdges, new Comparator<SubstrateLink>() {

					@Override
					public int compare(SubstrateLink arg0, SubstrateLink arg1) {
						double i0 = linkTransformer.transform(arg0);
						double i1 = linkTransformer.transform(arg1);

						if (i0 < i1)
							return -1;
						if (i0 > i1)
							return +1;

						return 0;
					}
				});

				for (SubstrateLink out : outEdges) {
					if (maxNumCandidates != -1 && result.size() == maxNumCandidates)
						break;
					if (linkTransformer.transform(out) != Double.POSITIVE_INFINITY) {
						SubstrateNode opp = G.getOpposite(entry.x, out);
						if (!visited.contains(opp)) {
							visited.add(opp);

							LinkedList<SubstrateLink> path = new LinkedList<SubstrateLink>(entry.y);
							path.add(out);

							boolean validPath = false;
							if (maxPathLength == -1) {
								validPath = true;
							} else {
								//								double sum = 0.0d;
								//								for (SubstrateLink l : path) {
								//									sum += linkTransformer.transform(l);
								//								}

								if (path.size() <= maxPathLength) {
									validPath = true;
								}
							}

							if (validPath) {
								if (opp != y && EntityMapping.fulfills(opp.get(), backupVNodeDemands)) {
									result.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(opp, path));
								}
								queue.add(new Tuple<SubstrateNode, LinkedList<SubstrateLink>>(opp, path));
							}
						}
					}
				}
			}
		}

		//		System.out.println("from " + (prevSNode == null ? "null" : prevSNode.getId()));
		//		for (Triple<VNF, SubstrateNode, LinkedList<SubstrateLink>> r : result) {
		//			if (r.z == null)
		//				System.out.println("  NULL");
		//			else
		//				for (SubstrateLink l : r.z)
		//					System.out.println("  "  + l.getId());
		//			System.out.println("to " + r.y.getId());
		//		}
		return result;
	}
	
	
//	public LinkedList<ResourceDemandEntry> reorganize(SubstrateNetwork sNet, SubstrateLink failedLink, int maxPathLength, boolean mapbidirectional) {
//		LinkedList<ResourceDemandEntry> result = new LinkedList<ResourceDemandEntry>();
//		CommonResource r = (CommonResource) failedLink.get(CommonResource.class);
//		
//		// was liegt auf diesem Link?
//		for (Mapping m : new LinkedList<Mapping>(r.getMappings())) {
//			CommonDemand d = (CommonDemand) m.getDemand();
//			
//			SubstrateNode from = sNet.getSource(d.primaryPath.getFirst());
//			SubstrateNode to = sNet.getDest(d.primaryPath.getLast());
//			
//			// free old path ressources -- von allen _Pfaden_ (!), die durch den ausgefallenen Link führen
//			for (Mapping mappedTo : new LinkedList<Mapping>(d.getMappings())) {
//				mappedTo.getDemand().free(mappedTo.getResource());
//			}
//			
//			//find new backup/primary path
//			Transformer<SubstrateLink, Double> transformer = new DisjointPathTransformer(d, d.primaryPath, sNet);
//			LinkedList<SubstrateLink> replacement = findShortestPath(sNet, from, to, transformer, maxPathLength);
//			result.addAll(new EntityMapping(this, null, sNet, replacement, d, mapbidirectional).mappedResources);
//			
////			if (d.isBackup) {
////				d.backupOrPrimaryPath
////			} else {
////				d.backupOrPrimaryPath
////			}
//		}
//		
//		return result;
//	}
	
	
	// void sortCandidates(LinkedList<Triple<VNF, SubstrateNode,
	// LinkedList<SubstrateLink>>> result, VNFFG VNFFG, CandidatePreferenceMode
	// CandidatePreferenceMode) {
	//
	// switch (CandidatePreferenceMode) {
	//
	// case RANDOM:
	// Collections.shuffle(result);
	// break;
	//
	// case ALPHA_BETA:
	//
	// Collections.sort(result, new Comparator<Triple<VNF, SubstrateNode,
	// LinkedList<SubstrateLink>>>() {
	// @Override
	// public int compare(Triple<VNF, SubstrateNode, LinkedList<SubstrateLink>>
	// o1, Triple<VNF, SubstrateNode, LinkedList<SubstrateLink>> o2) {
	//
	// int o1pathsize = 0;
	// if (o1.z != null)
	// o1pathsize = o1.z.size();
	//
	// int o2pathsize = 0;
	// if (o2.z != null)
	// o2pathsize = o2.z.size();
	//
	// if (o1pathsize < o2pathsize){
	// return -1;
	// }
	// if (o2pathsize < o1pathsize) {
	// return +1;
	// }
	//
	// return 0;
	// }
	//
	// });
	// break;
	//
	// case FREE_SLOTS_FIRST:
	// Collections.sort(result, new Comparator<MyEntry<SubstrateNode,
	// LinkedList<SubstrateLink>>>() {
	// @Override
	// public int compare(MyEntry<SubstrateNode, LinkedList<SubstrateLink>> o1,
	// MyEntry<SubstrateNode, LinkedList<SubstrateLink>> o2) {
	// boolean o1f = EntityMapping.hasFreeSlots(o1.getKey(), VNFFG, true,
	// vNode.type, vNode);
	// boolean o2f = EntityMapping.hasFreeSlots(o2.getKey(), VNFFG, true,
	// vNode.type, vNode);
	//
	// if (o1f && !o2f)
	// return -1;
	// if (!o1f && o2f)
	// return +1;
	//
	// int o1size = 0;
	// if (o1.getValue() != null)
	// o1size = o1.getValue().size();
	//
	// int o2size = 0;
	// if (o2.getValue() != null)
	// o2size = o2.getValue().size();
	//
	// if (o1size < o2size)
	// return -1;
	// if (o2size < o1size)
	// return +1;
	//
	// return 0;
	// }
	// });
	//
	// break;
	//
	// default:
	// throw new AssertionError();
	// }
	// }

}
