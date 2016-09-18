package evaluation.metrics;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFMaxDelayMS extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		Double max = null;
		
		for (List<SubstrateLink> path : VNFAvgDelayMS.getAllSubstratePathElements(sNet).values()) {
			double current = VNFAvgDelayMS.getPathDelay(path);
			if (max == null || current > max)
				max = current;
		}
		
		if (max == null)
			return Double.NaN;
		
		return max;
	}

}
