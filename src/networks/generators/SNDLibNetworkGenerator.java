package networks.generators;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Random;

import algorithm.VNFConsts;
import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.network.NetworkGenerator;
import tests.generators.network.NetworkGeneratorParameter;
import tests.generators.network.NetworkGeneratorParameters;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.core.Scenario;
import vnreal.io.SNDlibImporter;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;


public class SNDLibNetworkGenerator {

	private SNDLibNetworkGenerator() {
		throw new Error();
	}

	public static class SNDLibSubstrateNetworkGenerator implements NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> {

		public SNDLibSubstrateNetworkGenerator() {
		}

		@Override
		public SubstrateNetwork generate(Random random,
				ConstraintsGenerator<SubstrateNetwork> constraintsGenerator,
				NetworkGeneratorParameter objParam) {

			SNDLibNetworkGeneratorParameter param = (SNDLibNetworkGeneratorParameter) objParam;

			try {
				SubstrateNetwork snet = sndImportSubstrate(param.networkName);
				if (constraintsGenerator != null)
					constraintsGenerator.addConstraints(snet, random);
				return snet;
			} catch (FileNotFoundException e) {
				throw new Error(e.getMessage());
			}
		}
		
		@Override
		public void setLayer(int layer) {
			throw new AssertionError();
		}

	}
	
	
	public static class SNDLibVirtualNetworkGenerator implements NetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> {

		public SNDLibVirtualNetworkGenerator() {
		}

		@Override
		public VirtualNetwork generate(Random random,
				ConstraintsGenerator<VirtualNetwork> constraintsGenerator,
				NetworkGeneratorParameter objParam) {

			SNDLibNetworkGeneratorParameter param = (SNDLibNetworkGeneratorParameter) objParam;

			try {
				VirtualNetwork vnet = sndImportVirtual(param.networkName);
				constraintsGenerator.addConstraints(vnet, random);
				return vnet;
			} catch (FileNotFoundException e) {
				throw new Error(e.getMessage());
			}
		}
		
		@Override
		public void setLayer(int layer) {
			throw new AssertionError();
		}

	}
	
	
	static SubstrateNetwork sndImportSubstrate(String name) throws FileNotFoundException {
		SNDlibImporter importer = new SNDlibImporter(VNFConsts.PROJECT_DIR + "sndlib/" +
				name, false);
		importer.setType(true);
		Scenario scenario = new Scenario();
		importer.setNetworkStack(scenario);
		return scenario.getNetworkStack().getSubstrate();
	}
	
	
	static VirtualNetwork sndImportVirtual(String name) throws FileNotFoundException {
		SNDlibImporter importer = new SNDlibImporter(VNFConsts.PROJECT_DIR + "sndlib/" +
				name, false);
		importer.setType(false);
		Scenario scenario = new Scenario();
		scenario.setNetworkStack(new NetworkStack(new SubstrateNetwork(), new LinkedList<VirtualNetwork>()));
		importer.setNetworkStack(scenario);
		return (VirtualNetwork) scenario.getNetworkStack().getLayer(1);
	}
	
	
	public static class SNDLibNetworkGeneratorParameter implements NetworkGeneratorParameter {

		public final String networkName;

		public SNDLibNetworkGeneratorParameter(String networkName) {
			this.networkName = networkName;
		}

		@Override
		public String getSuffix(String prefix) {
			return prefix + "networkName:" + networkName;
		}
		
		@Override
		public String toString(String prefix) {
			return prefix + "networkName:" + networkName;
		}

	}

	public static class SNDLibNetworkGeneratorParameters extends NetworkGeneratorParameters {

		public final String[] networkNameArray;

		public SNDLibNetworkGeneratorParameters(String[] networkNameArray) {
			this.networkNameArray = networkNameArray;
		}

		@Override
		public LinkedList<NetworkGeneratorParameter> getParams() {
			LinkedList<NetworkGeneratorParameter> result =
					new LinkedList<NetworkGeneratorParameter>();

			for (String networkName : networkNameArray)
				result.add(new SNDLibNetworkGeneratorParameter(networkName));

			return result;
		}

	}
	
}
