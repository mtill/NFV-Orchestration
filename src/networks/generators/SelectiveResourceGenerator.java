package networks.generators;


import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.constraints.RandomResourceGenerator;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CommonResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.constraints.resources.IdResource;
import vnreal.constraints.resources.StaticDelayResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class SelectiveResourceGenerator implements ConstraintsGenerator<SubstrateNetwork> {
	
	public final boolean generateIDResources;
	public final Integer[] CPUResources; 
	public final Integer[] BandwidthResources;
	public final Integer[] DelayResources;
	public final int numProvidedLabels;
	public final String[] labels;
	
	public final int numberOfNodesOfferingResources;
	
	public SelectiveResourceGenerator(boolean generateIDResources, Integer[] CPUResources, Integer[] BandwidthResources, Integer[] DelayResources,
			int numProvidedLabels, String[] labels, int numberOfNodesOfferingResources) {
		this.generateIDResources = generateIDResources;
		this.CPUResources = CPUResources;
		this.BandwidthResources = BandwidthResources;
		this.DelayResources = DelayResources;
		this.numProvidedLabels = numProvidedLabels;
		this.labels = labels;
		
		this.numberOfNodesOfferingResources = numberOfNodesOfferingResources;
	}
	
	@Override
	public void addConstraints(SubstrateNetwork network, Random random) {
		if (generateIDResources)
			RandomResourceGenerator.generateIdResources(network);
		
		if (CPUResources != null && CPUResources.length > 0) {
			generateRandomCPUResources(network, CPUResources, random);
		}
		
		if (BandwidthResources != null && BandwidthResources.length > 0) {
			generateRandomBandwidthResources(network, BandwidthResources, random);
		}
		
		if (DelayResources != null && DelayResources.length > 0) {
			generateRandomDelayResources(network, DelayResources, random);
		}
		
		if (labels != null)
			RandomResourceGenerator.generateRandomLabelResources(network, numProvidedLabels, labels, random);
		
		int count = 0;
		LinkedList<SubstrateNode> nodes = new LinkedList<SubstrateNode>(network.getVertices());
		Collections.shuffle(nodes);
		for (SubstrateNode sn : nodes) {
			count++;
			
			if (count > this.numberOfNodesOfferingResources) {
				for (AbstractResource res : new LinkedList<AbstractResource>(sn.get())) {
					if (!(res instanceof IdResource))
						sn.remove(res);
				}
			}
			
		}
		
	}


	@Override
	public String getSuffix(String prefix) {
		return Utils.toString(this);
	}
	
	
	public static void generateRandomCPUResources(SubstrateNetwork sNetwork,
			Integer[] cpuResources, Random random) {

		for (SubstrateNode n : sNetwork.getVertices()) {
			CpuResource cpu = new CpuResource(n);
			int pos = Utils.rnd(0, cpuResources.length - 1, random);
			int value = cpuResources[pos];
			cpu.setCycles((double) value);
			n.add(cpu);
		}
	}
	
	public static void generateRandomBandwidthResources(
			SubstrateNetwork sNetwork,
			Integer[] bandwidthResources,
			Random random) {

		for (SubstrateLink l : sNetwork.getEdges()) {
			CommonResource bw = new CommonResource(l);
			int pos = Utils.rnd(0, bandwidthResources.length - 1, random);
			int value = bandwidthResources[pos];
			bw.setBandwidth((double) value);
			l.add(bw);
		}
	}
	
	public static void generateRandomDelayResources(SubstrateNetwork network,
			Integer[] DelayResources, Random random) {

		for (SubstrateLink l : network.getEdges()) {
			int pos = Utils.rnd(0, DelayResources.length - 1, random);
			int value = DelayResources[pos];
			l.add(new StaticDelayResource(l, value));
		}
	}

	
}
