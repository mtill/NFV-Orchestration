package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.constraints.resources.AbstractResource;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class VNFRunningNodes extends VNFEvaluationMetric {

	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		double result = 0;
		
		for (SubstrateNode s : sNet.getVertices()) {
			for (AbstractResource d : s.get()) {
				if (!d.getMappings().isEmpty()) {
					result++;
					break;
				}
			}
		}
		
		return result;
	}

}
