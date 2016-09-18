package networks.generators;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;

public abstract class VNFFGGeneratorParameter {

	public String getSuffix(String prefix) {
		return Utils.toString(prefix, this);
	}
	
	
	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", " = ");
	}
	
}
