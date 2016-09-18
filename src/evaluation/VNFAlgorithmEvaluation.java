/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package evaluation;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.stream.XMLStreamException;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithm;
import algorithm.VNFAlgorithmParameters.VNFAlgorithmParameter;
import algorithm.VNFUtils;
import cern.colt.Arrays;
import evaluation.metrics.AcceptedVnrRatio;
import networks.VNF;
import networks.VNFChain;
import networks.VNFFG;
import networks.VNFIDSource;
import networks.generators.RandomVNFFGGeneratorParameters.RandomVNFFGGeneratorParameter;
import networks.generators.VNFFGGenerator;
import networks.generators.VNFFGGeneratorParameter;
import networks.generators.VNFFGGeneratorParameters;
import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.network.NetworkGenerator;
import tests.generators.network.NetworkGeneratorParameter;
import tests.generators.network.NetworkGeneratorParameters;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.IdDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.IdResource;
import vnreal.core.Consts;
import vnreal.io.GraphMLExporter;
import vnreal.io.VNFFGExporter;
import vnreal.mapping.Mapping;
import vnreal.network.IDSource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public final class VNFAlgorithmEvaluation {

	public static boolean DEBUG = false;
	protected final NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator;
	protected final VNFFGGenerator VNFFGGenerator;

	public VNFAlgorithmEvaluation(NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator, VNFFGGenerator VNFFGGenerator) {
		this.sNetGenerator = sNetGenerator;
		this.VNFFGGenerator = VNFFGGenerator;
	}

	public static void execSysinfoTool(String name) {
		java.lang.Runtime r = java.lang.Runtime.getRuntime();
		try {
			r.exec(Consts.SYSINFOTOOL + " " + name + "_sysinfo.txt").waitFor();
		} catch (IOException e1) {
			throw new AssertionError();
		} catch (InterruptedException e) {
			throw new AssertionError();
		}
	}

	public void executeTests(String name,
			boolean export, int numScenarios, long maxRuntimeInSeconds, boolean learnTimeout,
			DistributionParameter distributionParameter,
			NetworkGeneratorParameters sNetParams,
			ConstraintsGenerator<SubstrateNetwork> sNetConstraintsGenerator,
			VNFFGGeneratorParameters VNFFGGeneratorParameters,
			Integer[] numVNFChainsArray,
			LinkedList<VNFAlgorithm> algorithms,
			boolean setRandomIDDemands,
			boolean generateDuplicateEdges,
			boolean removeIDDemands)
					throws IOException, InterruptedException, ExecutionException, XMLStreamException {

		//		XMLOutputFactory factory = XMLOutputFactory.newInstance();

		int numAlgoParams = 0;
		int maxEvents = 1;
		if (distributionParameter != null)
			maxEvents = distributionParameter.numEvents;

		LinkedList<Request> activeRequests = new LinkedList<Request>();
		HashMap<VNFAlgorithm, VNFCSVPrintWriterDataReceiver> resultwriters = new HashMap<VNFAlgorithm, VNFCSVPrintWriterDataReceiver>();
		
		String fullname = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + name;
		File dir = new File(Consts.RESULTS_DIR, fullname);
		if (dir.exists())
			throw new AssertionError("directory exists: " + dir.getName());
		if (!dir.mkdir())
			throw new AssertionError("cannot create directory: " + dir.getAbsolutePath());
		File xmldir = null;
		if (export) {
			xmldir = new File(dir.getAbsoluteFile(), "xml");
			if (!xmldir.mkdir())
				throw new AssertionError("cannot create directory: " + xmldir.getName());
		}
		
		AcceptedVnrRatio acceptance = new AcceptedVnrRatio();
		
		HashMap<VNFAlgorithm, List<VNFAlgorithmParameter>> algoParams = new HashMap<VNFAlgorithm, List<VNFAlgorithmParameter>>();
		for (VNFAlgorithm algorithm : algorithms) {
			LinkedList<VNFAlgorithmParameter> params = algorithm.getAlgorithmParams().getAlgorithmParamsAsList();
			numAlgoParams += params.size();
			algoParams.put(algorithm, params);

			PrintWriter paramswriter = new PrintWriter(new FileWriter(dir.getAbsolutePath() + File.separator + algorithm.name.replace("_",  "-") + "_params.txt", false), true);
			paramswriter.println("numScenarios: " + numScenarios
					+ "\nmaxRuntimeInSeconds: " + maxRuntimeInSeconds + "\n\n\n"
					+ (distributionParameter == null ? "distributionParameter:null" : distributionParameter.toString("distributionParameter.")) + "\n\n"
					+ sNetParams.toString("sNetParams.") + "\n\n"
					+ VNFFGGeneratorParameters.toString("VNFFGGeneratorParameters.") + "\n"
					+ "numVNFRsArray: " + Arrays.toString(numVNFChainsArray) + "\n\n\n"
					+ algorithm.getAlgorithmParams().toString("algorithmParams."));
			paramswriter.close();
		}
		
		final int total = numScenarios * sNetParams.getParams().size() * numVNFChainsArray.length * VNFFGGeneratorParameters.getParams().size() * numAlgoParams * maxEvents;
		int pos = 0;
		
		
		HashMap<String, String> scenarioNames = new HashMap<String, String>();
		for (NetworkGeneratorParameter sNetparameter : sNetParams.getParams()) {
			for (int numVNFChains : numVNFChainsArray) {
				for (VNFFGGeneratorParameter VNFFGparameter : VNFFGGeneratorParameters.getParams()) {

					for (VNFAlgorithm algorithm : algorithms) {
						for (VNFAlgorithmParameter algoParam : algoParams.get(algorithm)) {
							String preSuffix = getSuffix(sNetparameter, sNetConstraintsGenerator, numVNFChains, VNFFGparameter, algorithm, algoParam);
							scenarioNames.put(preSuffix, null);
						}
					}
				}
			}
		}
		getScenarioNames(scenarioNames);
		
		DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		
		for (int numScenario = 0; numScenario < numScenarios; numScenario++) {
			long seed = System.currentTimeMillis();

			for (NetworkGeneratorParameter sNetparameter : sNetParams.getParams()) {

				for (int numVNFChains : numVNFChainsArray) {
					for (VNFFGGeneratorParameter VNFFGparameter : VNFFGGeneratorParameters.getParams()) {

						algoFor: for (VNFAlgorithm algorithm : algorithms) {
							
							VNFCSVPrintWriterDataReceiver resultwriter = resultwriters.get(algorithm);
							if (resultwriter == null) {
								resultwriter = new VNFCSVPrintWriterDataReceiver(dir, algorithm, false);
								resultwriters.put(algorithm, resultwriter);
							}

							for (VNFAlgorithmParameter algoParam : algoParams.get(algorithm)) {
//								SVNFParameter p = (SVNFParameter) algoParam;
//								if (p.backupStrategy == BACKUP_STRATEGY.NO_BACKUP && p.backupSharingFactor > 1)
//									continue;

								int layerCounter = 1;
								int numEvents = 0;
								long timeSlot = 0;
								Random sNetTopologyRandom = new Random(seed);
								Random sNetConstraintsRandom = new Random(seed);
								Random eventRandom = new Random(seed);
								Random algorithmRandom = new Random(seed);

								IDSource.reset();
								VNFIDSource.reset();
								final SubstrateNetwork sNet = sNetGenerator.generate(sNetTopologyRandom, null, sNetparameter);
								if (sNetConstraintsGenerator != null)
									sNetConstraintsGenerator.addConstraints(sNet, sNetConstraintsRandom);
								if (generateDuplicateEdges)
									sNet.generateDuplicateEdges();
								
								algorithm.init(sNet, algoParam);

								int thisNumVNFChains = numVNFChains;
								if (numVNFChains == -1) {
									thisNumVNFChains = sNet.getVertexCount() / ((RandomVNFFGGeneratorParameter) VNFFGparameter).numVNFsPerChain;
									System.err.println("WARNING: stress mode activated! (" + thisNumVNFChains + ")");
								}
								
								boolean timeoutDueToSNet = (algorithm.timeoutSNSize != null && algorithm.timeoutSNSize >= sNet.getVertexCount());
								boolean timeoutDueToPathLength = (algorithm.timeoutMaxPathLength != null && algorithm.timeoutMaxPathLength >= algoParam.maxPathLength);
								if (timeoutDueToSNet) {
									System.out.println("skip due to sNet size");
									continue algoFor;
								}
								if (timeoutDueToPathLength) {
									System.out.println("skip due to max path length");
									continue algoFor;
								}
								
								while (numEvents < maxEvents) {
									pos++;
									long nextEvent = 0;
									if (distributionParameter != null)
										nextEvent = timeSlot + Math.round(Utils.exponentialDistribution(eventRandom, distributionParameter.lambda));

									if (distributionParameter != null) {
										for (Request reqs : new LinkedList<Request>(activeRequests)) {
											if (reqs.termTime <= nextEvent) {
												// System.out.println("DESTROY!");
												activeRequests.remove(reqs);

												for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> e : reqs.mappingResult.entrySet()) {
													if (e.getValue() != null) {
														algorithm.unmap(sNet, e.getValue().x);
													}
												}
											}
										}
									}

									timeSlot = nextEvent;
									numEvents++;

									long termTime = -1;
									if (distributionParameter != null) {
										long duration = Math.round(Utils.exponentialDistribution(eventRandom, distributionParameter.mu_L));
										termTime = timeSlot + duration;
									}
									// System.out.println("INIT " + timeSlot  + " " + nextEvent + " ");
									Request request = new Request(timeSlot, termTime);
									activeRequests.add(request);

									whiletrue: while (true) {
										final LinkedList<VNFFG> VNFFGs = new LinkedList<VNFFG>();
										for (int numVNFFGs = 0; numVNFFGs < thisNumVNFChains; ++numVNFFGs) {
											Random VNFFGtopologyRandom = new Random(seed+numVNFFGs);
											Random VNFFGconstraintsRandom = new Random(seed+numVNFFGs);
											
											VNFFG v = VNFFGGenerator.generate(layerCounter++, VNFFGtopologyRandom, VNFFGconstraintsRandom, VNFFGparameter, sNet);
											VNFFGs.add(v);
//											System.out.println(v);
											//										if (!v.validate())
											//											throw new AssertionError();
//
											//										System.err.println(1+v.dependencies.size());
//											LinkedList<VNFChain> chains = v.getAllVNFChainTopologies(-1, COORDVNF_STRATEGY.BANDWIDTH);
//											if (chains != null) {
//												System.out.println(chains.size() + " possible chainings");
//												
//												if (chains.size() > 1) {
//
//													try {
//														Runtime.getRuntime().exec(new String[] {"/usr/bin/bash", "-c", "/bin/rm /home/beck/workspace/papers/VNF-FGE/code/src_DAG/*.graphml"}).waitFor();
//													} catch (InterruptedException | IOException e1) {
//														// TODO Auto-generated catch block
//														e1.printStackTrace();
//													}
//
//													int i = 0;
//													for (VNFChain c : chains) {
//														GraphMLExporter.export("Chaining" + i + ".graphml", c);
//														i++;
//													}
//
//													System.out.println(v);
//													System.exit(1);
//												}
//											}
//
//											if (DEBUG) {
//												System.out.println(v);
//												if (chains.isEmpty())
//													System.err.println("WARNING: no valid chaining found for this scenario");
//												else {
//													System.err.println(chains.getFirst().getVertexCount() + "   "   + v.dependencies.size());
//													try {
//														GraphMLExporter.export("RNET_" + new Random().nextInt(100) + ".graphml", chains.getFirst());
//													} catch (IOException e) {
//														// TODO Auto-generated catch block
//														e.printStackTrace();
//													}
//												}

											//											for (VNF e : v.dependencies)
											//												System.out.println(e);
											//											VNFChain chain = v.getBestVNFChain(COORDVNF_STRATEGY.BANDWIDTH);
											//											try {
											//												GraphMLExporter.export("RNET_" + new Random().nextInt(100) + ".graphml", chain);
											//											} catch (IOException e) {
											//												// TODO Auto-generated catch block
											//												e.printStackTrace();
											//											}

//										}

										//								XMLStreamWriter writer = null;
										//								FileOutputStream scenarioFile = null;
										//								try {
										//									System.out.println(Consts.RESULTS_DIR + File.pathSeparator + "_scenario.txt");
										//									scenarioFile = new FileOutputStream(Consts.RESULTS_DIR + "_scenario.txt", false);
										//									writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(scenarioFile, "UTF-8"));
										//									VNFFGExporter.export(writer, v);
										//									writer.flush();
										//									scenarioFile.flush();
										//								} finally {
										//									if (writer != null)
										//										writer.close();
										//									if (scenarioFile != null)
										//										scenarioFile.close();
										//								}
									}

									if (setRandomIDDemands) {
										for (VNFFG v : VNFFGs) {
											LinkedList<SubstrateNode> sNodes = new LinkedList<SubstrateNode>(sNet.getVertices());
											//ids += ("sNet: " + ((ChainingPaperSNetGeneratorParameter) sNetparameter).network) + "\n\n";
											SubstrateNode r = sNodes.get(sNetConstraintsRandom.nextInt(sNodes.size()));
											IdResource id = (IdResource) VNFUtils.getResource(r.get(), IdResource.class);
											v.getInitialNode().IdDemand = new IdDemand(id.getId(), null);
											//									ids += ("init: " + v.getInitialNode().name + " -- " + id.getId()) + "\n";

										}
									}

									// clear ID Demands
									if (removeIDDemands) {
										for (VNFFG v : VNFFGs) {
											v.getInitialNode().IdDemand = null;
											for (VNF vv : v.dependencies)
												vv.IdDemand = null;
											for (VNF t : v.getTerminatingNodes())
												t.IdDemand = null;
										}
									}


									String preSuffix = getSuffix(sNetparameter, sNetConstraintsGenerator, numVNFChains, VNFFGparameter, algorithm, algoParam);
									String scenarioName = scenarioNames.get(preSuffix);
									String exportfilename = algorithm.name.replace("_",  "-") + "_scenario:" + numScenario + "_timeSlot:" + timeSlot + "_" + scenarioName;
									String scenarioSuffix = "scenario:" + numScenario + "_timeSlot:" + timeSlot + "_" + scenarioName;
									System.out.println("[" + dateFormat.format(new Date()) + ", " + pos + "/" + total + "] " + fullname + "/" + exportfilename);


									long startTime = System.currentTimeMillis();
									ExecutorService service = Executors.newSingleThreadExecutor();
									Future<HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>>> future = service.submit(new Callable<HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>>>() {
										@Override
										public HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> call() {
											return algorithm.mapVNFFGs(algorithmRandom, algoParam, VNFFGs);
										}
									});

									boolean timeout = false;
									HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult = null;
									try {
										if (future != null) {
											if (maxRuntimeInSeconds > -1) {
												mappingResult = future.get(maxRuntimeInSeconds, TimeUnit.SECONDS);
											} else {
												mappingResult = future.get();
											}
										}
									} catch(TimeoutException e) {
										timeout = true;

										algorithm.setCanceled();
										
//										future.cancel(true);
//										while(!future.isDone()) {
//											Thread.sleep(1000);
//										}
										
										service.shutdown(); // Disable new tasks from being submitted
										try {
											// Wait a while for existing tasks to terminate
											if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
												service.shutdownNow(); // Cancel currently executing tasks
												// Wait a while for tasks to respond to being cancelled
												if (!service.awaitTermination(60, TimeUnit.SECONDS))
													System.err.println("Pool did not terminate");
											}
										} catch (InterruptedException ie) {
											// (Re-)Cancel if current thread also interrupted
											service.shutdownNow();
											// Preserve interrupt status
											Thread.currentThread().interrupt();
										}
										
										continue algoFor;
									} finally {
										service.shutdown();
									}
									long elapsedTimeMS = System.currentTimeMillis() - startTime;
									System.err.println(VNFFGs.size() + " VNFFRs embedded in " + elapsedTimeMS + "ms");
									
									//							if (setRandomIDDemands) {
									//								done = (successPercentage == 1.0d);
									//								if (done)
									//									System.out.println(ids);
									//								else
									//									System.out.println("REPEAT!" + successPercentage);
									//							}

									if (DEBUG) {
										System.out.println("## substrate network: ##");
										System.out.println(sNet);
										System.out.println("## mapping result: ##");
										for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> e : mappingResult.entrySet())
											System.out.println(e.getValue());
										System.out.println("##########");
									}

									if (timeout) {
										System.out.println("Timeout after " + (elapsedTimeMS  / 1000) + " seconds");

										request.mappingResult = new HashMap<>();
										for (VNFFG VNFFG : VNFFGs)
											request.mappingResult.put(VNFFG, null);

										if (learnTimeout) {
											if (algorithm.getAlgorithmParams().maxPathLengthArray.length > 1)
												algorithm.timeoutMaxPathLength = algoParam.maxPathLength;
											else
												algorithm.timeoutSNSize = sNet.getVertexCount();
										}

									} else {
										request.mappingResult = mappingResult;
										
//										for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> e : mappingResult.entrySet()) {
//											if (e.getValue() == null)
//												continue;
//											
//											LinkedList<VNFChain> chainings = e.getKey().getAllVNFChainTopologies(-1, COORDVNF_STRATEGY.BANDWIDTH);
//											if (chainings.size() > 1) {
//												
//												System.out.println(e.getKey());
//
//												try {
//													Runtime.getRuntime().exec(new String[] {"/usr/bin/bash", "-c", "/bin/rm /home/beck/workspace/papers/VNF-FGE/code/src_DAG/*.graphml"}).waitFor();
//												} catch (InterruptedException | IOException e1) {
//													// TODO Auto-generated catch block
//													e1.printStackTrace();
//												}
//												
//												GraphMLExporter.export("VNet.graphml", e.getValue().x);
//												int i = 0;
//												for (VNFChain chain : e.getKey().getAllVNFChainTopologies(-1, COORDVNF_STRATEGY.BANDWIDTH)) {
//													try {
//														GraphMLExporter.export("Chaining" + i + ".graphml", chain);
//													} catch (IOException x) {
//														x.printStackTrace();
//													}
//													i++;
//												}
//											}
//										}
										
										
										for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> v : request.mappingResult.entrySet()) {
											if (v.getValue() != null) {
												if (!v.getKey().validate(v.getValue().x)) {
//													System.err.println("OOPS! Embedding seems to be invalid -- repeating with newly generated scenario");
//													for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> v2 : request.mappingResult.entrySet()) {
//													if (v2.getValue() != null) {
//														for (EntityMapping m : v2.getValue().y)
//															m.free();
////														sNet.unmap(v2.getValue().x.getLayer());
////														v2.getValue().clearVnrMappings();
//													}
													
													GraphMLExporter.export("/home/beck/fail.graphml", v.getValue().x);
													System.out.println(v.getKey());
													
													throw new AssertionError("OOPS! Embedding seems to be invalid");
//												}
//												
//												continue whiletrue;
												}
											}
										}
										
										
										if (elapsedTimeMS / (1000 * 60) > 10) {
											int VNFFGpos = 0;
											for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> v : request.mappingResult.entrySet()) {
												VNFFGExporter.export(dir.getAbsolutePath() + File.separator + "STRESS_" + exportfilename + "_VNFFG" + VNFFGpos + ".xml", v.getKey());

												if (v.getValue() != null)
													GraphMLExporter.export(dir.getAbsolutePath() + File.separator + "STRESS_" + exportfilename + "_VNFChain" + VNFFGpos + ".graphml", v.getValue().x);

												VNFFGpos++;
											}
											GraphMLExporter.export(dir.getAbsolutePath() + File.separator + "STRESS_" + exportfilename + "_sNet.graphml", sNet);
										}

										if (export) {
											int VNFFGpos = 0;
											for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> v : request.mappingResult.entrySet()) {
												VNFFGExporter.export(xmldir.getAbsolutePath() + File.separator + exportfilename + "_VNFFG" + VNFFGpos + ".xml", v.getKey());

												if (v.getValue() != null)
													GraphMLExporter.export(xmldir.getAbsolutePath() + File.separator + exportfilename + "_VNFChain" + VNFFGpos + ".graphml", v.getValue().x);

												VNFFGpos++;
											}
											GraphMLExporter.export(xmldir.getAbsolutePath() + File.separator + exportfilename + "_sNet.graphml", sNet);
										}

//										for (VNFEvaluationMetric m : algorithm.getMetrics(algoParam, elapsedTimeMS)) {
//											System.out.println("    " + m.getClass().getSimpleName() + ": " + m.calculate(sNet, request.mappingResult));
//										}
										System.out.println("    Acceptance: " + acceptance.calculate(sNet, request.mappingResult));

										resultwriter.receive(scenarioSuffix, sNet, request.mappingResult, algorithm.getMetrics(algoParam, elapsedTimeMS));
									}
									
									break;
									} // while(true)

								}
							}
						}


					}



				}

			}
		}

		for (VNFCSVPrintWriterDataReceiver w : resultwriters.values())
			w.finish();
	}

	private String getSuffix(
			NetworkGeneratorParameter sNetparameter,
			ConstraintsGenerator<SubstrateNetwork> sNetConstraintsGenerator,
			int numVNFChains, VNFFGGeneratorParameter VNFFGparameter,
			VNFAlgorithm algorithm, VNFAlgorithmParameter algoParam) {
		
		String scenarioSuffix =
				(sNetparameter == null ? "sNetparameter:null" : sNetparameter.getSuffix("sNetparameter.")) + "_" +
				(sNetConstraintsGenerator == null ? "sNetConstraintsGenerator:null" : sNetConstraintsGenerator.getSuffix("sNetConstraintsGenerator.")) + "_" +
				"numVNFRs:" + numVNFChains + "_" +
				(VNFFGparameter == null ? "VNFFGparameter:null" : VNFFGparameter.getSuffix("VNFFGparameter.")) + "_" +
				(algoParam == null ? "algoParam:null" : algoParam.getSuffix("algoParam."));
		
		return scenarioSuffix;
	}
	
	public static void getScenarioNames(HashMap<String, String> scenarioNames) {
		for (Entry<String, String> scenario1 : scenarioNames.entrySet()) {
			String[] split1 = scenario1.getKey().split("_");
			for (String split1entry : split1) {
				String[] kv1 = split1entry.split(":");
				boolean addsplit1entry = false;
				
				for (Entry<String, String> scenario2 : scenarioNames.entrySet()) {
					if (scenario1.getKey().equals(scenario2.getKey()))
						continue;
					
					String[] split2 = scenario2.getKey().split("_");
					boolean foundHere = false;
					for (String split2entry : split2) {
						String[] kv2 = split2entry.split(":");
						if (kv2[0].equals(kv1[0])) {
							foundHere = true;
							if (kv1.length > 1 && kv2.length > 1 && !kv2[1].equals(kv1[1])) {
								addsplit1entry = true;
								break;
							}
						}
					}
					
					if (!foundHere)
						addsplit1entry = true;
					if (addsplit1entry)
						break;
				}
				
				if (addsplit1entry) {
					if (scenarioNames.get(scenario1.getKey()) == null) {
						scenarioNames.put(scenario1.getKey(), split1entry);
					} else {
						scenarioNames.put(scenario1.getKey(), scenario1.getValue() + "_" + split1entry);
					}
				}
			}
			
			if (scenarioNames.get(scenario1.getKey()) == null)
				scenarioNames.put(scenario1.getKey(), "");
		}
	}

	public static boolean isMapped(SubstrateNetwork snet, int layer) {
		for (SubstrateNode n : snet.getVertices()) {
			for (AbstractResource d : n) {
				if (!d.getMappings().isEmpty()) {
					for (Mapping m : d.getMappings()) {
						if (m.getDemand().getOwner().getLayer() == layer)
							return true;
					}
				}
			}
		}

		return false;
	}

	public static class Request {
		public long initTime;
		public long termTime;
		public HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult;

		public Request(long initTime, long termTime) {
			this.initTime = initTime;
			this.termTime = termTime;
		}
	}

	//	protected void removeRandomNodes(NetworkStack stack, int n) {
	//		SubstrateNetwork snet = stack.getSubstrate();
	//		LinkedList<SubstrateNode> snodes = new LinkedList<SubstrateNode>(snet.getVertices());
	//
	//		Collections.shuffle(snodes);
	//
	//		for (int i = 0; i < n && !snodes.isEmpty(); ++i) {
	//			snet.removeVertex(snodes.pollFirst());
	//		}
	//	}

}
