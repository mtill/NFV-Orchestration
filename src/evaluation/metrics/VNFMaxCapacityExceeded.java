package evaluation.metrics;

import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.CoordVNF_SVNF_Algorithm;
import algorithm.Tuples.Tuple;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFMaxCapacityExceeded extends VNFEvaluationMetric {
	
	final CoordVNF_SVNF_Algorithm SVNF;
	
	public VNFMaxCapacityExceeded(CoordVNF_SVNF_Algorithm SVNF) {
		this.SVNF = SVNF;
	}
	
	public Double calculate(
			SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult) {
		
		return (double) this.SVNF.maxCapacityExceeded;
	}

}
