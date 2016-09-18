package networks.generators;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import networks.VNF;
import networks.VNF.VNFInputInterface;
import networks.VNFChain;
import networks.VNFFG;
import networks.VNFFG.Counter;
import networks.VNFLink;
import networks.generators.RandomVNFFGGeneratorParameters.RandomVNFFGGeneratorParameter;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;


public class RandomDAGVNFFGGenerator implements VNFFGGenerator {
	
	// -Xmx2048m -XX:-UseGCOverheadLimit -XX:+UseConcMarkSweepGC
	public static void main(String[] args) {

		Random topologyRandom = new Random();
		Random constraintsRandom = new Random();
		
//		long f = org.apache.commons.math.util.MathUtils.factorial(10);

		final Double[] discreteProbabilities = new Double[] { 0.33,0.33,0.33 };
		final int[] maxtriesArray = new int[] { 1,5,10,15,20 };
//		final int[] maxtriesArray = new int[] { 5 };
//		final int[] numVNFsPerChainArray = new int[] { 5,10,15,20 };
		final int[] numVNFsPerChainArray = new int[] { 10 };
		final double[] edgesToRemoveArray = new double[] { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 };
//		final double[] edgesToRemoveArray = new double[] { 0.9 };
		final Integer[] minChainingPossibilitiesArray = new Integer[] { 1, 2, 80 };
		final Integer[] maxChainingPossibilitiesArray = new Integer[] { 1, 10, 100 };
		final double[] maxNodeDeviationArray = new double[] { 3.0 };
//		final double[] maxNodeDeviationArray = new double[] { -1.0 };
		final int numscenarios = 50;
		final Double initialDataRate = 100d;
		
		System.out.println("scenario_maxtries_edgesToRemove_Metric.numVertices_Metric.numChains_Metric.outedges_Metric.inedges_"
				+ "Metric.outedges1_Metric.outedges2_Metric.outedges3_"
				+ "Metric.inedges1_Metric.inedges2_Metric.inedges3_"
				+ "Metric.netcounter_"
				+ "Metric.maxDeps_"
				+ "Metric.RuntimeMS");
		for (int ix = 0; ix < minChainingPossibilitiesArray.length; ++ix)
		for (int maxtries : maxtriesArray) {
			for (double edgesToRemove : edgesToRemoveArray) {
				for (int numVNFsPerChain : numVNFsPerChainArray) {
					for (double maxNodeDeviation : maxNodeDeviationArray) {

						RandomDAGVNFFGGenerator g = new RandomDAGVNFFGGenerator(false, maxtries);
						RandomVNFFGGeneratorParameter p =
								new RandomVNFFGGeneratorParameter(null, numVNFsPerChain, discreteProbabilities, edgesToRemove, initialDataRate, minChainingPossibilitiesArray[ix], maxChainingPossibilitiesArray[ix], maxNodeDeviation, 0.0d);

						for (int i = 0; i < numscenarios; ++i) {
							long start = System.currentTimeMillis();
							VNFFG VNFFG = g.generate(0, topologyRandom, constraintsRandom, p, null);
							long elapsed = System.currentTimeMillis() - start;
							
							int maxdeps = 0;
							HashMap<VNFLink, Integer> deps = new HashMap<VNFLink, Integer>();
							for (VNF v : VNFFG.dependencies) {
								for (VNFInputInterface interf : v.inputInterfaces) {
									for (VNFLink req : interf.requiredFlowVNFLinks) {
										Integer in = deps.get(req);
										if (in == null) {
											in = 0;
										}
										in++;

										deps.put(req, in);
									}
								}
							}
							for (Integer in : deps.values()) {
								if (maxdeps < in)
									maxdeps = in;
							}
							
							
							Counter netcounter = new Counter();
							LinkedList<VNFChain> all = VNFFG.getAllVNFChainTopologies(-1, -1, COORDVNF_STRATEGY.BANDWIDTH, netcounter);
							
							double vertexcountsum = 0.0d;
							double alloutedgessum = 0.0d;
							double allinedgessum = 0.0d;
							
							double alloutedgessum1 = 0.0d;
							double allinedgessum1 = 0.0d;
							
							double alloutedgessum2 = 0.0d;
							double allinedgessum2 = 0.0d;
							
							double alloutedgessum3 = 0.0d;
							double allinedgessum3 = 0.0d;

							for (VNFChain a : all) {
								vertexcountsum += a.getVertexCount();

								double outedgessum = 0.0d;
								double inedgessum = 0.0d;
								
								double outedgessum1 = 0.0d;
								double inedgessum1 = 0.0d;
								
								double outedgessum2 = 0.0d;
								double inedgessum2 = 0.0d;
								
								double outedgessum3 = 0.0d;
								double inedgessum3 = 0.0d;
								
								for (VirtualNode n : a.getVertices()) {
									outedgessum += a.getOutEdges(n).size();
									inedgessum += a.getInEdges(n).size();
									
									if (a.getOutEdges(n).size() == 1)
										outedgessum1++;
									if (a.getOutEdges(n).size() == 2)
										outedgessum2++;
									if (a.getOutEdges(n).size() == 3)
										outedgessum3++;
									
									if (a.getInEdges(n).size() == 1)
										inedgessum1++;
									if (a.getInEdges(n).size() == 2)
										inedgessum2++;
									if (a.getInEdges(n).size() == 3)
										inedgessum3++;
									
								}
								alloutedgessum += outedgessum / ((double) a.getVertexCount());
								allinedgessum += inedgessum / ((double) a.getVertexCount());
								
								alloutedgessum1 += outedgessum1;
								alloutedgessum2 += outedgessum2;
								alloutedgessum3 += outedgessum3;
								
								allinedgessum1 += inedgessum1;
								allinedgessum2 += inedgessum2;
								allinedgessum3 += inedgessum3;
							}
							double numVerticesavg = all.size() == 0 ? 0.0d : (vertexcountsum / ((double) all.size()));
							int numChains = all.size();
							double alloutedgesavg = all.size() == 0 ? 0.0d : (alloutedgessum / ((double) all.size()));
							double allinedgesavg = all.size() == 0 ? 0.0d : (allinedgessum / ((double) all.size()));
							
							System.out.println(i + "_"
//									+ numVNFsPerChain + "_"
									+ maxtries + "_"
									+ edgesToRemove + "_"
									+ numVerticesavg + "_"
									+ numChains + "_"
									+ alloutedgesavg + "_"
									+ allinedgesavg + "_"
									+ alloutedgessum1 + "_" + alloutedgessum2 + "_" + alloutedgessum3 + "_"
									+ allinedgessum1 + "_" + allinedgessum2 + "_" + allinedgessum3 + "_"
									+ netcounter.i + "_"
									+ maxdeps + "_"
									+ elapsed);
							
						}
					}
				}
			}
		}
	}
	

	final boolean staticModel;
	final int maxtries;
	
	public RandomDAGVNFFGGenerator(boolean staticModel) {
		this(staticModel, 2);
	}
	
	public RandomDAGVNFFGGenerator(boolean staticModel, int maxtries) {
		this.staticModel = staticModel;
		this.maxtries = maxtries;
	}

//	public VNFFG generate(int layer, Random random, VNFFGGeneratorParameter objp, SubstrateNetwork originalSNet) {
//		RandomVNFFGGeneratorParameter p = (RandomVNFFGGeneratorParameter) objp;
//		
////		if (p.edgesToRemove == -1.0d) {
////			return generate2(layer, random, objp, originalSNet);
////		}
//		
//		int[] numsToGenerate = new int[p.numOutLinksProbabilities.length];
//		for (int i = 0; i < numsToGenerate.length; ++i) {
//			numsToGenerate[i] = i+1;
//		}
//		
//		double[] probs = new double[p.numOutLinksProbabilities.length];
//		for (int i = 0; i < p.numOutLinksProbabilities.length; ++i)
//			probs[i] = p.numOutLinksProbabilities[i];
//		
//		org.apache.commons.math3.random.RandomGenerator g = new org.apache.commons.math3.random.RandomGenerator() {
//			@Override
//			public void setSeed(long arg0) {
//				throw new AssertionError();
//			}
//			@Override
//			public void setSeed(int[] arg0) {
//				throw new AssertionError();
//			}
//			@Override
//			public void setSeed(int arg0) {
//				throw new AssertionError();
//			}
//			
//			@Override
//			public long nextLong() {
//				return random.nextLong();
//			}
//			
//			@Override
//			public int nextInt(int arg0) {
//				return random.nextInt(arg0);
//			}
//			
//			@Override
//			public int nextInt() {
//				return random.nextInt();
//			}
//			
//			@Override
//			public double nextGaussian() {
//				return random.nextGaussian();
//			}
//			
//			@Override
//			public float nextFloat() {
//				return random.nextFloat();
//			}
//			
//			@Override
//			public double nextDouble() {
//				return random.nextDouble();
//			}
//			
//			@Override
//			public void nextBytes(byte[] arg0) {
//				random.nextBytes(arg0);
//			}
//			
//			@Override
//			public boolean nextBoolean() {
//				return random.nextBoolean();
//			}
//		};
//		EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(g, numsToGenerate, probs);
//
//		return generateOneVNFFG(layer, p, distribution, originalSNet, random);
//	}

//	private VNFFG generateOneVNFFG(int layer,
//			RandomVNFFGGeneratorParameter p,
//			EnumeratedIntegerDistribution distribution,
//			SubstrateNetwork sNet, Random random) {
//
//		DAG tree = generateDAG(layer, p.numVNFsPerChain, distribution, random);
//		VNFFG result = getVNFFG(random, p, tree, sNet, staticModel);
//		
////		try {
////			GraphMLExporter.export("DAG_" + new Random().nextInt(100), result.getVNFChains(1, COORDVNF_STRATEGY.BANDWIDTH).getFirst());
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		
//		return result;
//	}

	
	public VNFFG generate(int layer, Random topologyRandom, Random demandRandom, VNFFGGeneratorParameter objp, SubstrateNetwork originalSNet) {
		RandomVNFFGGeneratorParameter p = (RandomVNFFGGeneratorParameter) objp;

		int[] numsToGenerate = new int[p.numOutLinksProbabilities.length];
		for (int i = 0; i < numsToGenerate.length; ++i) {
			numsToGenerate[i] = i+1;
		}

		double[] probs = new double[p.numOutLinksProbabilities.length];
		for (int i = 0; i < p.numOutLinksProbabilities.length; ++i)
			probs[i] = p.numOutLinksProbabilities[i];

		org.apache.commons.math3.random.RandomGenerator g = new org.apache.commons.math3.random.RandomGenerator() {
			@Override
			public void setSeed(long arg0) {
				throw new AssertionError();
			}
			@Override
			public void setSeed(int[] arg0) {
				throw new AssertionError();
			}
			@Override
			public void setSeed(int arg0) {
				throw new AssertionError();
			}

			@Override
			public long nextLong() {
				return topologyRandom.nextLong();
			}

			@Override
			public int nextInt(int arg0) {
				return topologyRandom.nextInt(arg0);
			}

			@Override
			public int nextInt() {
				return topologyRandom.nextInt();
			}

			@Override
			public double nextGaussian() {
				return topologyRandom.nextGaussian();
			}

			@Override
			public float nextFloat() {
				return topologyRandom.nextFloat();
			}

			@Override
			public double nextDouble() {
				return topologyRandom.nextDouble();
			}

			@Override
			public void nextBytes(byte[] arg0) {
				topologyRandom.nextBytes(arg0);
			}

			@Override
			public boolean nextBoolean() {
				return topologyRandom.nextBoolean();
			}
		};
		EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(g, numsToGenerate, probs);
		int maxnodes = -1;
		if (p.maxNodeDeviation != -1.0d)
			maxnodes = (int) (((double) p.numVNFsPerChain) * p.maxNodeDeviation);

//		System.out.println("start");
		while (true) {
			VNFFG result = new VNFFG(layer);
			result.initialBW = 50d;

			if (!recAdd(p, maxnodes, maxtries, distribution, topologyRandom, result, new LinkedList<VNFLink>(), 0))
				continue;
			
			LinkedList<VNFChain> all = result.getAllVNFChainTopologies(p.maxChainingPossibilities, maxnodes, COORDVNF_STRATEGY.BANDWIDTH);
			if (all != null && (all.size() >= p.minChainingPossibilities) && (p.maxChainingPossibilities == -1 || all.size() <= p.maxChainingPossibilities)) {
//				System.err.println("Number of valid chainings: " + all.size());
				
				if (p.RandomVNFFGDemandGenerator != null)
					p.RandomVNFFGDemandGenerator.generate(demandRandom, result, originalSNet, p);
				
				return result;
			}
//			System.out.println("Repeating generation of VNFFG (" + (all == null ? "no" : all.size()) + " valid chainings)");
			
		}
		
		
	}
	
	
	boolean recAdd(RandomVNFFGGeneratorParameter p, int maxnodes, int maxtries, EnumeratedIntegerDistribution distribution, Random topologyRandom, VNFFG result, LinkedList<VNFLink> VNFOutputs, int depth) {
		if (depth == p.numVNFsPerChain)
			return true;
		
		for (int numtry = 0; numtry < maxtries; numtry++) {
//			System.out.println(depth+"."+numtry);
			VNF VNF = new VNF(staticModel);
			VNF.inputInterfaces.clear();
			
			boolean success = true;
			if (depth == 0) {
				result.setInitialNode(VNF);
			} else {
				result.dependencies.add(VNF);
				
				LinkedList<VNFLink> VNFOutputsCopy = new LinkedList<VNFLink>(VNFOutputs);
				Collections.shuffle(VNFOutputsCopy, topologyRandom);
				Iterator<VNFLink> iter = VNFOutputsCopy.iterator();
				int indegree = distribution.sample();
				if (indegree == 0)
					indegree = 1;
				//			int indegree = 1;
				for (int n = 0; n < indegree; n++) {
					if (!iter.hasNext()) {
						success = false;
						break;
					}
					
					VNFInputInterface x = new VNFInputInterface(VNF);
					VNF.inputInterfaces.add(x);

					VNFLink req = iter.next();
					x.requiredFlowVNFLinks.add(req);

					if (p.edgesToRemove != -1 && topologyRandom.nextDouble() < p.edgesToRemove) {
						VNFOutputs.remove(req);
					}
				}
				
				if (success && VNF.inputInterfaces.isEmpty()) {
//					VNFInputInterface x = new VNFInputInterface(VNF);
//					VNF.inputInterfaces.add(x);
					throw new AssertionError(); 
				}
			}

			if (success) {
				int outdegree = distribution.sample();
				if (outdegree == 0)
					outdegree = 1;
				for (int n = 0; n < outdegree; n++) {
					VNFLink l = new VNFLink(VNF, null, 1.0d, true);
					VNFOutputs.add(l);
				}

				//			System.out.println(p.maxChainingPossibilities + ", " + maxnodes);
				LinkedList<VNFChain> all = result.getAllVNFChainTopologies(p.maxChainingPossibilities, maxnodes, COORDVNF_STRATEGY.BANDWIDTH);
				if (all == null) {
					success = false;
				} else {
					if (p.maxChainingPossibilities != -1 && p.maxChainingPossibilities < all.size()) {
						success = false;
					}
					for (VNFChain a : all) {
						if (!result.validate(a)) {
							success = false;
							break;
						}
					}
				}
				all = null;
			}
			
			if (success)
				if (recAdd(p, maxnodes, maxtries, distribution, topologyRandom, result, VNFOutputs, depth+1))
					return true;

			if (depth == 0)
				result.setInitialNode(null);
			else
				result.dependencies.remove(VNF);
			VNFOutputs.removeAll(VNF.outLinks);
			
		}
		
		return false;
	}


//	VNFFG getVNFFG(
//			Random random,
//			RandomVNFFGGeneratorParameter p,
//			DAG DAG,
//			SubstrateNetwork sNet,
//			boolean staticModel) {
//
////		LinkedList<VirtualLink> ignoreEdges = new LinkedList<VirtualLink>();
////		LinkedList<VirtualLink> allEdges = new LinkedList<VirtualLink>(DAG.DAG.getEdges());
////		Collections.shuffle(allEdges, random);
////		Iterator<VirtualLink> iterator = allEdges.iterator();
////		int numIgnoreEdges = (int) (((double) DAG.DAG.getEdgeCount()) * p.edgesToRemove);
////		for (int i = 0; i < numIgnoreEdges && iterator.hasNext(); ++i) {
////			ignoreEdges.add(iterator.next());
////		}
//
//		VNFFG result = new VNFFG(DAG.DAG.getLayer());
////		HashMap<VirtualNode, VNF> map = new HashMap<VirtualNode, VNF>();
////		getDependencies(result, DAG.DAG, null, DAG.root, map, ignoreEdges, staticModel);
//		getDependencies2(result, DAG.DAG, staticModel);
//		for (VNF v : result.dependencies)
//			if (v.inputInterfaces.isEmpty())
//				v.inputInterfaces.add(new VNFInputInterface(v));
//		
////		System.err.println("!" + DAG.DAG.getVertexCount());
////		System.err.println(result.dependencies.size() + " " + result.getTerminatingNodes().size() + "+1");
//
//		if (p.RandomVNFFGDemandGenerator != null)
//			p.RandomVNFFGDemandGenerator.generate(random, result, sNet, p);
//		
//		return result;
//	}

//	void getDependencies(VNFFG VNFFG,
//			VirtualNetwork DAG,
//			VNFLink lastUncutOutLink,
//			VirtualNode currentVNode,
//			HashMap<VirtualNode, VNF> createdVNFs,
//			LinkedList<VirtualLink> ignoreEdges,
//			boolean staticModel) {
//
//		boolean isNew = false;
//		VNF currentVNF = createdVNFs.get(currentVNode);
//		if (currentVNF == null) {
//			isNew = true;
//			currentVNF = new VNF(staticModel);
//			currentVNF.name = currentVNode.getName();
//			currentVNF.inputInterfaces.clear();
//			createdVNFs.put(currentVNode, currentVNF);
//		}
//
//		VNFInputInterface i = new VNFInputInterface(currentVNF);
//		if (lastUncutOutLink != null) {
//			i.requiredFlowVNFLinks.add(lastUncutOutLink);
//			currentVNF.inputInterfaces.add(i);
//		}
//		
//		if (!isNew)
//			return;
//		
////		if (DAG.getOutEdges(currentVNode).isEmpty()) {
////			VNFFG.addTerminatingNode(currentVNF);
////		} else {
//			if (DAG.getInEdges(currentVNode).isEmpty())
//				VNFFG.setInitialNode(currentVNF);
//			else
//				VNFFG.dependencies.add(currentVNF);
//
//			Collection<VirtualLink> out = DAG.getOutEdges(currentVNode);
//			if (!out.isEmpty()) {
//				for (VirtualLink o : out) {
//					VNFLink link = new VNFLink(currentVNF, null, true);
//
//					VirtualNode nextVNode = DAG.getDest(o);
//					if (ignoreEdges.contains(o) && out.size() == 1) {
//						getDependencies(VNFFG, DAG, lastUncutOutLink, nextVNode, createdVNFs, ignoreEdges, staticModel);
//					} else {
//						getDependencies(VNFFG, DAG, link, nextVNode, createdVNFs, ignoreEdges, staticModel);
//					}
//				}
//			} else {
//				new VNFLink(currentVNF, null, true);
//			}
////		}
//		
//	}
	
//	void getDependencies2(VNFFG VNFFG,
//			VirtualNetwork DAG,
//			boolean staticModel) {
//		
//		HashMap<VirtualNode, VNF> VNFs = new HashMap<VirtualNode, VNF>();
//		
//		for (VirtualLink vl : DAG.getEdges()) {
//			VirtualNode source = DAG.getSource(vl);
//			
//			VNF sourceVNF = VNFs.get(source);
//			if (sourceVNF == null) {
//				sourceVNF = new VNF(staticModel);
//				sourceVNF.inputInterfaces.clear();
//				VNFs.put(source, sourceVNF);
//				
//				if (DAG.getInEdges(source).size() == 0 && VNFFG.getInitialNode() == null)
//					VNFFG.setInitialNode(sourceVNF);
//				else
//					VNFFG.dependencies.add(sourceVNF);
//			}
//			
//			VirtualNode dest = DAG.getDest(vl);
//			VNF destVNF = VNFs.get(dest);
//			if (destVNF == null) {
//				destVNF = new VNF(staticModel);
//				destVNF.inputInterfaces.clear();
//				VNFs.put(dest, destVNF);
//				
//				VNFFG.dependencies.add(destVNF);
//			}
//			
//			VNFInputInterface i = new VNFInputInterface(destVNF);
//			destVNF.inputInterfaces.add(i);
//			
//			VNFLink o = new VNFLink(sourceVNF, null, true);
//			i.requiredFlowVNFLinks.add(o);
//		}
//		
//	}


	DAG generateDAG(int layer, int numnodes, EnumeratedIntegerDistribution distribution, Random random) {
		VirtualNetwork result = new VirtualNetwork(layer);
		HashMap<VirtualNode, Integer> outdegrees = new HashMap<VirtualNode, Integer>();
		LinkedList<VirtualNode> nodes = new LinkedList<VirtualNode>();
		VirtualNode root = null;

		while (result.getVertexCount() < numnodes) {
			final VirtualNode n = new VirtualNode(layer);
			if (root == null)
				root = n;
			result.addVertex(n);

			outdegrees.put(n, distribution.sample());

			if (!nodes.isEmpty()) {
				
				final int indegree = distribution.sample();
				int indegreecounter = 0;
				
				Collections.shuffle(nodes, random);
				for (VirtualNode rn : nodes) {
					if (result.getOutEdges(rn).size() < outdegrees.get(rn)) {
						VirtualLink nl = new VirtualLink(layer);
						result.addEdge(nl, rn, n);
						
						indegreecounter++;
						if (indegreecounter >= indegree) {
							break;
						}
					}
				}
				
				// ensure that graph is connected!
				if (indegreecounter == 0) {
					VirtualLink nl = new VirtualLink(layer);
					result.addEdge(nl, nodes.getFirst(), n);
				}
			}

			nodes.add(n);
		}

		return new DAG(root, result);
	}
	
	static class DAG {
		public final VirtualNode root;
		public final VirtualNetwork DAG;
		
		public DAG(VirtualNode root, VirtualNetwork DAG) {
			this.root = root;
			this.DAG = DAG;
		}
	}


}
