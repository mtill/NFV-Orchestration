package algorithm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import networks.VNFFG;
import networks.generators.RandomDAGVNFFGGenerator;
import networks.generators.RandomVNFFGDemandGenerator;
import networks.generators.RandomVNFFGGeneratorParameters;
import networks.generators.VNFFGGenerator;
import networks.generators.VNFFGGeneratorParameter;
import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.constraints.RandomResourceGenerator;
import tests.generators.constraints.RandomResourceGenerator.RandomResourceGeneratorParameter;
import tests.generators.network.WaxmanNetworkGenerator;
import tests.generators.network.WaxmanNetworkGenerator.WaxmanNetworkGeneratorParameters;
import vnreal.constraints.resources.AbstractResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class VNFFGAnalyzer {

	public static void main(String[] args) {

		final Integer[] numVNFsPerChainArray = new Integer[] { 10 };
		final Double edgesToRemove = 0.25;
		final Double[] discreteProbabilities = new Double[] { 0.75, 0.2, 0.05 };
		final Double[] initialDataRatesArray = new Double[] { 50d };
		final Double minBandwidthPercentages = 0.75;
		final Double maxBandwidthPercentages = 1.25;
		final Double[] capacityPer100MBits = new Double[] { 100d };
		final boolean staticModel = false;
		final int numLabels = 3;
		final int numVNFTypesPerLabel = 3;
		final Integer[] minChainingPossibilitiesArray = new Integer[] { 1, 2, 80 };
		final Integer[] maxChainingPossibilitiesArray = new Integer[] { 1, 10, 100 };
		final Double maxNodeDeviation = 1.2;
		final Double[] backupProbabilityArray = new Double[] {0.5};


		final RandomVNFFGDemandGenerator RandomVNFFGDemandGenerator =
				new RandomVNFFGDemandGenerator(numLabels, numVNFTypesPerLabel, false, capacityPer100MBits, minBandwidthPercentages, maxBandwidthPercentages);
		HashMap<String, LinkedList<String>> labelsAndVNFTypes = RandomVNFFGDemandGenerator.generateLabelsAndTypes();

		final RandomVNFFGGeneratorParameters VNFFGGeneratorParameters =
				new RandomVNFFGGeneratorParameters(RandomVNFFGDemandGenerator, numVNFsPerChainArray, discreteProbabilities, edgesToRemove, initialDataRatesArray, minChainingPossibilitiesArray, maxChainingPossibilitiesArray, maxNodeDeviation, backupProbabilityArray);



		VNFFGGenerator VNFFGGenerator = new RandomDAGVNFFGGenerator(staticModel);


		final Integer[] numSNodesArray = { 10 };
		final double waxman_salpha = 0.3;
		final double waxman_sbeta = 0.3;
		final boolean forceConnectivity = true;

		final WaxmanNetworkGeneratorParameters sNetParams = new WaxmanNetworkGeneratorParameters(numSNodesArray, waxman_salpha, waxman_sbeta, forceConnectivity);
		final WaxmanNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator = WaxmanNetworkGenerator.createSubstrateNetworkInstance();


		final Integer minFreeSlotsResources = 4;
		final Integer maxFreeSlotsResources = 4;
		final Integer minCapacity = 50;
		final Integer maxCapacity = 100;
		final Integer minBandwidthResource = 50;
		final Integer maxBandwidthResource = 100;
		final Integer minDelayResource = -1;
		final Integer maxDelayResource = -1;

		final ConstraintsGenerator<SubstrateNetwork> constraintsGenerator =
				new RandomResourceGenerator(new RandomResourceGeneratorParameter(true, labelsAndVNFTypes, minFreeSlotsResources, maxFreeSlotsResources, -1, -1, minCapacity, maxCapacity, minBandwidthResource, maxBandwidthResource, minDelayResource, maxDelayResource, new LinkedList<String>(labelsAndVNFTypes.keySet()).toArray(new String[]{})));

//		final COORDVNF_STRATEGY strategy = COORDVNF_STRATEGY.BANDWIDTH;


		Random random = new Random();
		SubstrateNetwork sNet = sNetGenerator.generate(random, constraintsGenerator, sNetParams.getParams().getFirst());

		for (VNFFGGeneratorParameter p : VNFFGGeneratorParameters.getParams()) {
			for (int i = 0; i < 30; ++i) {
				VNFFG v = VNFFGGenerator.generate(i + 1, random, null, p, sNet);
//					System.out.println(v.getVNFChains(1, strategy).getFirst().getVertexCount());
//					for (VNF f : v.dependencies) {
//						System.out.println(f);
//					}
//					for (VNF f : v.dependencies)
//						System.out.print(f.outLinks.size() + " ");
//					System.out.println();
//				System.out.println("#######");
			}
		}


	}


}
