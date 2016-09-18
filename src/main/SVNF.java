package main;


import java.util.LinkedHashMap;
import java.util.LinkedList;

import algorithm.CoordVNF_SVNF_Algorithm;
import algorithm.SVNFParameters;
import algorithm.SVNFParameters.BACKUP_STRATEGY;
import algorithm.VNFAlgorithm;
import algorithm.VNFAlgorithmParameters.COORDVNF_STRATEGY;
import evaluation.DistributionParameter;
import evaluation.VNFAlgorithmEvaluation;
import networks.generators.RandomDAGVNFFGGenerator;
import networks.generators.RandomVNFFGDemandGenerator;
import networks.generators.RandomVNFFGGeneratorParameters;
import networks.generators.VNFFGGenerator;
import tests.generators.constraints.RandomResourceGenerator;
import tests.generators.constraints.RandomResourceGenerator.RandomResourceGeneratorParameter;
import tests.generators.network.BarabasiAlbertNetworkGenerator;
import tests.generators.network.BarabasiAlbertNetworkGenerator.BarabasiAlbertNetworkGeneratorParameters;
import vnreal.constraints.resources.AbstractResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class SVNF {
	
	public static void main(String[] args) throws Throwable {
		
		final String PROJECT_NAME = "SVNF";
//		VNFAlgorithmEvaluation.DEBUG = true;
//		ResilientCoordVNF.DEBUG = true;
		
		final int numLabels = 3;
		final int numVNFTypesPerLabel = 10;
		
//		final Integer[] numVNFChainsArray = { 1,2,3,4,5,6,7,8,9,10 };
		final Integer[] numVNFChainsArray = { 10 };
//		final Integer[] numVNFsPerChainArray = new Integer[] { 20,18,16,14,12,10,8,6,4 };
		final Integer[] numVNFsPerChainArray = new Integer[] { 10 };
//		final Integer[] numVNFsPerChainArray = new Integer[] { 4,6,8,10,12,14,16 };
		final double edgesToRemove = 0.8;
//		final Double[] discreteProbabilities = new Double[] { 1.0 };
//		final Double[] discreteProbabilities = new Double[] { 0.7,0.20,0.1 };
		final Double[] discreteProbabilities = new Double[] { 0.33,0.33,0.33 };
		final Double[] initialDataRatesArray = new Double[] { 50d };
//		final Double[] initialDataRatesArray = new Double[] { 5d,10d,15d,20d,25d,30d,35d,40d,45d,50d,55d,60d,65d,70d,75d,80d,85d,90d,95d,100d };
//		final Double[] initialDataRatesArray = new Double[] { 10d,20d,30d,40d,50d,60d,70d,80d,90d,100d };
//		final Double[] bandwidthPercentages = new Double[] {0.6, 0.8, 1.0};
		final Double minBandwidthPercentages = 0.75;
		final Double maxBandwidthPercentages = 1.25;
//		final Double minBandwidthPercentages = 1.0;
//		final Double maxBandwidthPercentages = 1.0;
		final Double[] capacityPer100MBits = new Double[] { 100d };
		final boolean staticModel = false;
		final Integer[] minChainingPossibilitiesArray = new Integer[] { 2 };
		final Integer[] maxChainingPossibilitiesArray = new Integer[] { 100 };
		final Double maxNodeDeviation = 1.5;
		
//		final Double[] backupProbabilityArray = new Double[] {0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0};
//		final Double[] backupProbabilityArray = new Double[] {0.0, 0.25, 0.5, 0.75, 1.0};
		final Double[] backupProbabilityArray = new Double[] {0.5};
		
		final RandomVNFFGDemandGenerator RandomVNFFGDemandGenerator =
				new RandomVNFFGDemandGenerator(numLabels, numVNFTypesPerLabel, false, capacityPer100MBits, minBandwidthPercentages, maxBandwidthPercentages);
		LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes = RandomVNFFGDemandGenerator.generateLabelsAndTypes();

		VNFFGGenerator VNFFGGenerator = new RandomDAGVNFFGGenerator(staticModel);

		final RandomVNFFGGeneratorParameters VNFFGGeneratorParameters =
				new RandomVNFFGGeneratorParameters(RandomVNFFGDemandGenerator, numVNFsPerChainArray, discreteProbabilities, edgesToRemove, initialDataRatesArray, minChainingPossibilitiesArray, maxChainingPossibilitiesArray, maxNodeDeviation, backupProbabilityArray);

		final int Barabasi_sEdgesToAttach = 2;
		final Integer[] Barabasi_sNumTimeStepsArray = { 50 };

		final BarabasiAlbertNetworkGeneratorParameters sNetParams = new BarabasiAlbertNetworkGeneratorParameters(Barabasi_sEdgesToAttach, Barabasi_sNumTimeStepsArray);
		final BarabasiAlbertNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator = BarabasiAlbertNetworkGenerator.createSubstrateNetworkInstance();

//		final Integer[] numSNodesArray = { 50 };
//		final double waxman_salpha = 0.3;
//		final double waxman_sbeta = 0.3;
//		final boolean forceConnectivity = true;
//
//		final WaxmanNetworkGeneratorParameters sNetParams = new WaxmanNetworkGeneratorParameters(numSNodesArray, waxman_salpha, waxman_sbeta, forceConnectivity);
//		final WaxmanNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator = WaxmanNetworkGenerator.createSubstrateNetworkInstance();
		
		
		final Integer minFreeSlotsResources = 4;
		final Integer maxFreeSlotsResources = 4;
		final Integer minCapacity = 50;
		final Integer maxCapacity = 100;
		final Integer minBandwidthResource = 50;
		final Integer maxBandwidthResource = 100;
		final Integer minDelayResource = -1;
		final Integer maxDelayResource = -1;
		
		final RandomResourceGenerator constraintsGenerator = new RandomResourceGenerator(new RandomResourceGeneratorParameter(true, labelsAndVNFTypes, minFreeSlotsResources, maxFreeSlotsResources, -1, -1, minCapacity, maxCapacity, minBandwidthResource, maxBandwidthResource, minDelayResource, maxDelayResource, new LinkedList<String>(labelsAndVNFTypes.keySet()).toArray(new String[]{})));
		constraintsGenerator.useCommonConstraints = true;
		
		// ################## algorithm ##################
		final Integer[] maxPathLengthArray = new Integer[] { 10 };
//		final Integer[] maxPathLengthArray = new Integer[] { 1,2,3,4,5,6,7,8,9,10 };
		final Boolean[] isAdvanced = new Boolean[] { true };
		final Boolean[] earlyTerminationArray = new Boolean[] { false };
//		final Integer[] maxNumCandidatesArray = new Integer[] { 1,5,10,15,20,25,30,35,40,45,50 };
//		final Integer[] maxNumCandidatesArray = new Integer[] { 1,5,10,15,20 };
//		final Integer[] maxNumCandidatesArray = new Integer[] { 1,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30 };
		final Integer[] maxNumCandidatesArray = new Integer[] { 1,2,3,4,5 };
//		final Integer[] maxNumCandidatesArray = new Integer[] { 4 };
		final Integer[] maxBacktrackingStepsArray = new Integer[] { -1 };
//		final Integer[] maxBacktrackingStepsArray = new Integer[] { 20000 };
		final boolean mapBidirectionalPaths = false;
		final COORDVNF_STRATEGY[] strategies = new COORDVNF_STRATEGY[] {COORDVNF_STRATEGY.BANDWIDTH};
		
//		final Double[] backupLinkWeights = new Double[] { 1.0 };
		final BACKUP_STRATEGY[] backupStrategies = new BACKUP_STRATEGY[] {BACKUP_STRATEGY.NO_BACKUP, BACKUP_STRATEGY.NODE, BACKUP_STRATEGY.LINK, BACKUP_STRATEGY.NODE_LINK };
//		final BACKUP_STRATEGY[] backupStrategies = new BACKUP_STRATEGY[] {BACKUP_STRATEGY.NO_BACKUP, BACKUP_STRATEGY.LINK };
//		final BACKUP_STRATEGY[] backupStrategies = new BACKUP_STRATEGY[] { BACKUP_STRATEGY.LINK };
//		final BACKUP_STRATEGY[] backupStrategies = new BACKUP_STRATEGY[] {BACKUP_STRATEGY.NO_BACKUP };
		
//		final Integer[] backupSharingFactor = new Integer[] {3};
//		final Integer[] backupSharingFactor = new Integer[] {1,2,3,4,5,6,7,8,9,10};
		
//		final Double[] freeBackupCapacityWeightArray = new Double[] {1.0d};
		

//		final Integer[] maxSharingArray = new Integer[] {1,2,3,4,5,6,7,8,9,10};
		final Integer[] maxSharingArray = new Integer[] {1};

		final boolean setRandomIDDemands = false;
		final boolean removeIDDemands = false;
		final boolean generateDuplicateEdges = true;
		final int numScenarios = 50;
		final long maxRuntimeInSeconds = -1;
		final boolean learnTimeout = false;
//		final DistributionParameter distributionParameter = new DistributionParameter(500, 4d/100d, 1d/1000d);
		final DistributionParameter distributionParameter = null;
		
		final boolean export = false;
		
		VNFAlgorithmEvaluation eval = new VNFAlgorithmEvaluation(sNetGenerator, VNFFGGenerator);
		SVNFParameters algoParam = new SVNFParameters(maxSharingArray, earlyTerminationArray, strategies, backupStrategies, labelsAndVNFTypes, maxPathLengthArray, maxNumCandidatesArray, maxBacktrackingStepsArray, isAdvanced, mapBidirectionalPaths);
		LinkedList<VNFAlgorithm> algorithms = new LinkedList<VNFAlgorithm>();
		algorithms.add(new CoordVNF_SVNF_Algorithm("SVNF", algoParam));		
		
		
		// ################## run ##################
		try {
			eval.executeTests(PROJECT_NAME,
					export, numScenarios, maxRuntimeInSeconds, learnTimeout,
					distributionParameter,
					sNetParams,
					constraintsGenerator,
					VNFFGGeneratorParameters, numVNFChainsArray,
					algorithms,
					setRandomIDDemands, generateDuplicateEdges, removeIDDemands);
		} catch(Throwable e) {
			throw(e);
		}
	}

}
