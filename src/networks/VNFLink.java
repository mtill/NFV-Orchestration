package networks;

import vnreal.constraints.demands.CommonDemand;

public class VNFLink {
	
	public final VNF source;
	public final CommonDemand bandwidthDemand;
	public Double dataratepercentage;
	
	public boolean needsBackup = false;
	
	public VNFLink(VNF source, CommonDemand bandwidthDemand, boolean register) {
		this(source, bandwidthDemand, null, register);	
	}
	
	public VNFLink(VNF source, CommonDemand bandwidthDemand, Double dataratepercentage, boolean register) {
		if (dataratepercentage != null && dataratepercentage < 0.0d)
			throw new AssertionError();
		
		this.source = source;
		this.bandwidthDemand = bandwidthDemand;
		this.dataratepercentage = dataratepercentage;
		
		if (register)
			source.outLinks.add(this);
	}
	
	public String toString() {
		return source.name + "." + source.outLinks.indexOf(this) + "--" + dataratepercentage + "-->";
	}

}
