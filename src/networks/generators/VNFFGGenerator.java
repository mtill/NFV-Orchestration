package networks.generators;

import java.util.Random;

import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public interface VNFFGGenerator {
	
	public VNFFG generate(int layer, Random topologyRandom, Random constraintsRandom, VNFFGGeneratorParameter p, SubstrateNetwork originalSNet);
	
}
