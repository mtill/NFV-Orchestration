package algorithm;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import tests.scenarios.AlgorithmParameters;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;


public class VNFAlgorithmParameters extends AlgorithmParameters {

	public final Boolean[] earlyTerminationArray;
	protected final LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes;
	public final Integer[] maxPathLengthArray;
	public final Boolean[] isAdvanced;
	public final Integer[] maxNumCandidatesArray;
	public final Integer[] maxBacktrackingStepsCandidatesArray;
	public final boolean mapBidirectionalPaths;
	public final COORDVNF_STRATEGY[] strategies;

	public VNFAlgorithmParameters(Boolean[] earlyTerminationArray, COORDVNF_STRATEGY[] strategies, LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes,
			Integer[] maxPathLengthArray, Integer[] maxNumCandidatesArray, Integer[] maxBacktrackingStepsCandidatesArray, Boolean[] isAdvanced, boolean mapBidirectionalPaths) {
		this.earlyTerminationArray = earlyTerminationArray;
		this.labelsAndVNFTypes = labelsAndVNFTypes;
		this.maxPathLengthArray = maxPathLengthArray;
		this.isAdvanced = isAdvanced;
		this.maxNumCandidatesArray = maxNumCandidatesArray;
		this.maxBacktrackingStepsCandidatesArray = maxBacktrackingStepsCandidatesArray;
		this.mapBidirectionalPaths = mapBidirectionalPaths;
		this.strategies = strategies;
	}

	public LinkedList<VNFAlgorithmParameter> getAlgorithmParamsAsList() {
		LinkedList<VNFAlgorithmParameter> result = new LinkedList<VNFAlgorithmParameter>();

		for (boolean earlyTermination : this.earlyTerminationArray)
		for (COORDVNF_STRATEGY strategy : strategies)
			for (Integer maxPathLength : maxPathLengthArray)
				for (Integer maxNumCandidates : this.maxNumCandidatesArray)
					for (Integer maxBacktrackingStepsCandidates : this.maxBacktrackingStepsCandidatesArray)
						for (Boolean isAdv : this.isAdvanced)
							result.add(new VNFAlgorithmParameter(earlyTermination, strategy, labelsAndVNFTypes, maxPathLength, maxNumCandidates, maxBacktrackingStepsCandidates, isAdv, this.mapBidirectionalPaths));

		return result;
	}

	@Override
	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", " = ", true);
	}


	public static class VNFAlgorithmParameter {

		public final boolean earlyTermination;
		protected final LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes;
		public final Integer maxPathLength;
		public final boolean isAdvanced;
		public final Integer maxNumCandidates;
		public final Integer maxBacktrackingStepsCandidates;
		public final boolean mapBidirectionalPaths;
		public final COORDVNF_STRATEGY strategy;

		public VNFAlgorithmParameter(boolean earlyTermination, COORDVNF_STRATEGY strategy, LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes, Integer maxPathLength, Integer maxNumCandidates, Integer maxBacktrackingStepsCandidates, boolean isAdvanced, boolean mapBidirectionalPaths) {
			this.earlyTermination = earlyTermination;
			this.labelsAndVNFTypes = labelsAndVNFTypes;
			this.maxPathLength = maxPathLength;
			this.isAdvanced = isAdvanced;
			this.maxNumCandidates = maxNumCandidates;
			this.maxBacktrackingStepsCandidates = maxBacktrackingStepsCandidates;
			this.mapBidirectionalPaths = mapBidirectionalPaths;
			this.strategy = strategy;
		}

		public String getSuffix(String prefix) {
			return Utils.toString(prefix, this, true);
		}

	}
	
	public enum COORDVNF_STRATEGY {BANDWIDTH, VNF_INSTANCES, LATENCY, NR};

}
