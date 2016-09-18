package networks.generators;

import java.util.LinkedList;

public class RandomVNFFGGeneratorParameters extends VNFFGGeneratorParameters {
	
	public final RandomVNFFGDemandGenerator RandomVNFFGDemandGenerator;
	public final Integer[] numVNFsPerChainArray;
	public final Double[] numOutLinksProbabilities;
	public final Double edgesToRemove;
	public final Double[] initialDataRatesArray;
	public final Integer[] minChainingPossibilitiesArray;
	public final Integer[] maxChainingPossibilitiesArray;
	public final Double maxNodeDeviation;
	public final Double[] backupProbabilitiesArray;
	
	public RandomVNFFGGeneratorParameters(
			RandomVNFFGDemandGenerator RandomVNFFGDemandGenerator,
			Integer[] numVNFsPerChainArray,
			Double[] numOutLinksProbabilities,
			Double edgesToRemove,
			Double[] initialDataRatesArray,
			Integer[] minChainingPossibilitiesArray,
			Integer[] maxChainingPossibilitiesArray,
			Double maxNodeDeviation,
			Double[] backupProbabilitiesArray) {
		
		if (minChainingPossibilitiesArray.length != maxChainingPossibilitiesArray.length)
			throw new AssertionError();
		
		this.RandomVNFFGDemandGenerator = RandomVNFFGDemandGenerator;
		this.numVNFsPerChainArray = numVNFsPerChainArray;
		this.numOutLinksProbabilities = numOutLinksProbabilities;
		this.edgesToRemove = edgesToRemove;
		this.initialDataRatesArray = initialDataRatesArray;
		this.minChainingPossibilitiesArray = minChainingPossibilitiesArray;
		this.maxChainingPossibilitiesArray = maxChainingPossibilitiesArray;
		this.maxNodeDeviation = maxNodeDeviation;
		this.backupProbabilitiesArray = backupProbabilitiesArray;
	}
	
	
	@Override
	public LinkedList<VNFFGGeneratorParameter> getParams() {
		LinkedList<VNFFGGeneratorParameter> result =
				new LinkedList<VNFFGGeneratorParameter>();
		
		for (int i = 0; i < this.minChainingPossibilitiesArray.length; ++i)
			for (int numVNFsPerChain : this.numVNFsPerChainArray)
				for (Double initialDataRates : initialDataRatesArray)
					for (Double backupProbabilities : backupProbabilitiesArray)
						result.add(new RandomVNFFGGeneratorParameter(RandomVNFFGDemandGenerator, numVNFsPerChain, numOutLinksProbabilities, edgesToRemove, initialDataRates, this.minChainingPossibilitiesArray[i], this.maxChainingPossibilitiesArray[i], maxNodeDeviation, backupProbabilities));
		
		return result;
	}
	
	
	public static class RandomVNFFGGeneratorParameter extends VNFFGGeneratorParameter {
		
		public final RandomVNFFGDemandGenerator RandomVNFFGDemandGenerator;
		public final int numVNFsPerChain;
		public final Double[] numOutLinksProbabilities;
		public final Double edgesToRemove;
		public final Double initialDataRate;
		public final Integer minChainingPossibilities;
		public final Integer maxChainingPossibilities;
		public final Double maxNodeDeviation;
		public final Double backupProbabilities;
		
		public RandomVNFFGGeneratorParameter(
				RandomVNFFGDemandGenerator RandomVNFFGDemandGenerator,
				int numVNFsPerChain,
				Double[] numOutLinksProbabilities,
				Double edgesToRemove,
				Double initialDataRate,
				Integer minChainingPossibilities,
				Integer maxChainingPossibilities,
				Double maxNodeDeviation,
				Double backupProbabilities) {
			
			this.RandomVNFFGDemandGenerator = RandomVNFFGDemandGenerator;
			this.numVNFsPerChain = numVNFsPerChain;
			this.numOutLinksProbabilities = numOutLinksProbabilities;
			this.edgesToRemove = edgesToRemove;
			this.initialDataRate = initialDataRate;
			this.minChainingPossibilities = minChainingPossibilities;
			this.maxChainingPossibilities = maxChainingPossibilities;
			this.maxNodeDeviation = maxNodeDeviation;
			this.backupProbabilities = backupProbabilities;
		}
		
	}

}
