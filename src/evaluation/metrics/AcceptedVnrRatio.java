package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class AcceptedVnrRatio extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		if (mappingResult == null)
			throw new AssertionError("No requests");
		
		int size = mappingResult.keySet().size();
		if (size == 0)
			throw new AssertionError("No requests");
		
		int numSuccess = 0;
		for (Entry<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> r : mappingResult.entrySet()) {
			if (r.getValue() != null)
				numSuccess++;
		}
		
		double successPercentage = ((((double) numSuccess) / ((double) size)) * 100.0d);
		return successPercentage;
	}

}
