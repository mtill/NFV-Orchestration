package networks.generators;

import java.util.LinkedList;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;

public abstract class VNFFGGeneratorParameters {
	
	public abstract LinkedList<VNFFGGeneratorParameter> getParams();

	public String getSuffix(String prefix) {
		return Utils.toString(this);	}
	
	
	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", " = ", true);
	}
	
}
