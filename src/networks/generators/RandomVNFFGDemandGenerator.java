package networks.generators;


import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;

import algorithm.VNFUtils;
import networks.VNF;
import networks.VNFFG;
import networks.VNFLink;
import networks.generators.RandomVNFFGGeneratorParameters.RandomVNFFGGeneratorParameter;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.FreeSlotsDemand;
import vnreal.constraints.demands.IdDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class RandomVNFFGDemandGenerator {
	
	public final int numLabels;
	public final int numVNFTypesPerLabel;
	
	public final boolean setIdDemands;
	public final Double[] capacityPer100MBits;
	public final Double minBandwidthPercentage;
	public final Double maxBandwidthPercentage;
	
	
	private LinkedHashMap<String, LinkedList<String>> labelsAndVNFTypes;
	
	
	public RandomVNFFGDemandGenerator(
			int numLabels,
			int numVNFTypesPerLabel,
			boolean setIdDemands,
			Double[] capacityPer100MBits,
			Double minBandwidthPercentage,
			Double maxBandwidthPercentage) {
		
		this.numLabels = numLabels;
		this.numVNFTypesPerLabel = numVNFTypesPerLabel;
		
		this.setIdDemands = setIdDemands;
		this.capacityPer100MBits = capacityPer100MBits;
		this.minBandwidthPercentage = minBandwidthPercentage;
		this.maxBandwidthPercentage = maxBandwidthPercentage;
		
		
		this.labelsAndVNFTypes = generateLabelsAndTypes();
	}
	
	
	public void generate(Random random, VNFFG VNFFG, SubstrateNetwork sNet, RandomVNFFGGeneratorParameter p) {
		
		LinkedList<String> labels = new LinkedList<String>(labelsAndVNFTypes.keySet());
		
		VNFFG.getInitialNode().IdDemand = null;
		if (setIdDemands) {
			SubstrateNode initialSNode = VNFUtils.getRandomNode(random, sNet);
			VNFFG.getInitialNode().IdDemand = new IdDemand(initialSNode.getName() + "", null);
		}
		
		LinkedList<VNF> VNFs = new LinkedList<VNF>();
		VNFs.addAll(VNFFG.dependencies);
		
		if (!setIdDemands) {
			VNFs.add(VNFFG.getInitialNode());
			if (VNFFG.getTerminatingNodes() != null)
				VNFs.addAll(VNFFG.getTerminatingNodes());
		}
		
		LinkedList<VNFLink> VNFLinksBackup = new LinkedList<VNFLink>();
		for (VNF VNF : VNFs) {
			String demLabel = labels.get(Utils.rnd(0, labels.size() - 1, random));
			LinkedList<String> types = new LinkedList<String>(labelsAndVNFTypes.get(demLabel));
			String VNFType = types.get(Utils.rnd(0, types.size() - 1, random));

			VNF.FreeSlotsDemand = new FreeSlotsDemand(demLabel, VNFType, null, new LinkedList<NetworkEntity<AbstractResource>>());
			
			VNF.capacityPer100MBits = capacityPer100MBits[Utils.rnd(0, capacityPer100MBits.length - 1, random)];
			
			if (VNF.outLinks.isEmpty()) {
				new VNFLink(VNF, null, 1.0d, true);
			}
			
			VNFLinksBackup.addAll(VNF.outLinks);
		}

		Collections.shuffle(VNFs, random);
		Iterator<VNF> VNFsBackupIterator = VNFs.iterator();
		
		Collections.shuffle(VNFLinksBackup, random);
		Iterator<VNFLink> VNFsLinkBackupIterator = VNFLinksBackup.iterator();

		//		System.err.println((int) (Math.round((p.backupProbabilities * ((double) VNFs.size())))) + "  von  " + VNFs.size());
		for (int i = 0; i < (int) (Math.round((p.backupProbabilities * ((double) VNFs.size())))); ++i)
			VNFsBackupIterator.next().needsBackup = true;

		//		System.err.println("! " + (int) (Math.round((p.backupProbabilities * ((double) VNFLinksBackup.size())))) + "  von  " + VNFLinksBackup.size());
		for (int i = 0; i < (int) (Math.round((p.backupProbabilities * ((double) VNFLinksBackup.size())))); ++i)
			VNFsLinkBackupIterator.next().needsBackup = true;

		VNFFG.initialBW = p.initialDataRate; //initialDataRates[Utils.rnd(0, initialDataRates.length - 1, random)];
		
		for (VNF VNF : VNFFG.dependencies) {
			for (VNFLink l : VNF.outLinks) {
//				double dataratePercentage = bandwidthPercentages[Utils.rnd(0, bandwidthPercentages.length - 1, random)];
				double dataratePercentage = Utils.rnd(this.minBandwidthPercentage, this.maxBandwidthPercentage, random);
//				l.dataratepercentage = dataratePercentage;
				l.dataratepercentage = (dataratePercentage / ((double) VNF.outLinks.size()));
			}
		}
		for (VNFLink l : VNFFG.getInitialNode().outLinks) {
//			double dataratePercentage = bandwidthPercentages[Utils.rnd(0, bandwidthPercentages.length - 1, random)];
			double dataratePercentage = Utils.rnd(this.minBandwidthPercentage, this.maxBandwidthPercentage, random);
//			l.dataratepercentage = dataratePercentage;
			l.dataratepercentage = (dataratePercentage / ((double) VNFFG.getInitialNode().outLinks.size()));
		}
		
		for (VNF term : VNFFG.getTerminatingNodes()) {
			term.IdDemand = null;
			
			if (setIdDemands) {
				term.capacityPer100MBits = 0.0d;
				term.FreeSlotsDemand = null;
				SubstrateNode terminatingSNode = VNFUtils.getRandomNode(random, sNet);
				term.IdDemand = new IdDemand(terminatingSNode.getName() + "", null);
			}
		}
		
	}
	
	public LinkedHashMap<String, LinkedList<String>> generateLabelsAndTypes() {
		if (labelsAndVNFTypes == null) {
			labelsAndVNFTypes = new LinkedHashMap<String, LinkedList<String>>();

			for (int i = 0; i < numLabels; ++i) {
				String label = "label" + i;
				LinkedList<String> VNFTypes = new LinkedList<String>();
				for (int j = 0; j < numVNFTypesPerLabel; ++j) {
					VNFTypes.add(label + "_type" + j);
				}

				labelsAndVNFTypes.put(label, VNFTypes);
			}
		}

		return labelsAndVNFTypes;
	}
	
}
