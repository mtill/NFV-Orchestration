package algorithm;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;


public class SVNFParameters extends VNFAlgorithmParameters {
	
	public final Integer[] maxSharingArray;
//	public final Double[] backupLinkWeights;
	public final BACKUP_STRATEGY[] backupStrategies;
//	public final Integer[] backupSharingFactorArray;
//	public final Double[] freeBackupCapacityWeightArray;

//	public SVNFParameters(Double[] freeBackupCapacityWeightArray, Boolean[] earlyTerminationArray, COORDVNF_STRATEGY[] strategies, BACKUP_STRATEGY[] backupStrategies, Integer[] backupSharingFactorArray, LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes, Integer[] maxPathLengthArray, Integer[] maxNumCandidatesArray, Integer[] maxBacktrackingStepsCandidatesArray, Boolean[] isAdvanced, boolean mapBidirectionalPaths) {
	public SVNFParameters(Integer[] maxSharingArray, Boolean[] earlyTerminationArray, COORDVNF_STRATEGY[] strategies, BACKUP_STRATEGY[] backupStrategies, LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes, Integer[] maxPathLengthArray, Integer[] maxNumCandidatesArray, Integer[] maxBacktrackingStepsCandidatesArray, Boolean[] isAdvanced, boolean mapBidirectionalPaths) {
		super(earlyTerminationArray, strategies, labelsAndVNFTypes, maxPathLengthArray, maxNumCandidatesArray, maxBacktrackingStepsCandidatesArray, isAdvanced, mapBidirectionalPaths);
		this.maxSharingArray = maxSharingArray;
//		this.backupLinkWeights = backupLinkWeights;
		this.backupStrategies = backupStrategies;
//		this.backupSharingFactorArray = backupSharingFactorArray;
//		this.freeBackupCapacityWeightArray = freeBackupCapacityWeightArray;
	}

	public LinkedList<VNFAlgorithmParameter> getAlgorithmParamsAsList() {
		LinkedList<VNFAlgorithmParameter> result = new LinkedList<VNFAlgorithmParameter>();

		for (boolean earlyTermination : earlyTerminationArray)
			for (COORDVNF_STRATEGY strategy : strategies)
				for (BACKUP_STRATEGY backupStrategy : backupStrategies)
					for (Integer maxPathLength : maxPathLengthArray)
						for (Integer maxNumCandidates : this.maxNumCandidatesArray)
								for (Integer maxBacktrackingSteps : this.maxBacktrackingStepsCandidatesArray)
									for (Boolean isAdv : this.isAdvanced)
										for (Integer maxSharing : maxSharingArray)
												result.add(new SVNFParameter(maxSharing, earlyTermination, strategy, backupStrategy, labelsAndVNFTypes, maxPathLength, isAdv, maxNumCandidates, maxBacktrackingSteps, this.mapBidirectionalPaths));

		return result;
	}

	@Override
	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", " = ", true);
	}


	public static class SVNFParameter extends VNFAlgorithmParameter {
		
		public final Integer maxSharing;
//		public final double backupLinkWeight;
		public final BACKUP_STRATEGY backupStrategy;
//		public final int backupSharingFactor;
//		public final double freeBackupCapacityWeight;
		
		public SVNFParameter(Integer maxSharing, boolean earlyTermination, COORDVNF_STRATEGY strategy, BACKUP_STRATEGY backupStrategy, LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes, Integer maxPathLength, boolean isAdvanced, Integer maxNumCandidates, Integer maxBacktrackingSteps, boolean mapBidirectionalPaths) {
			super(earlyTermination, strategy, labelsAndVNFTypes, maxPathLength, maxNumCandidates, maxBacktrackingSteps, isAdvanced, mapBidirectionalPaths);
			this.maxSharing = maxSharing;
//			this.backupLinkWeight = backupLinkWeight;
			this.backupStrategy = backupStrategy;
//			this.backupSharingFactor = backupSharingFactor;
//			this.freeBackupCapacityWeight = freeBackupCapacityWeight;
		}

		@Override
		public String getSuffix(String prefix) {
			return Utils.toString(prefix, this, true);
		}

	}
	
	
	public static enum BACKUP_STRATEGY {NO_BACKUP, NODE, LINK, NODE_LINK }

}
