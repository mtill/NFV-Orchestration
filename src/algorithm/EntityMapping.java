package algorithm;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import networks.VNFFG;
import vnreal.algorithms.utils.SubgraphBasicVN.ResourceDemandEntry;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class EntityMapping {
	
//	public static void main(String[] args) {
//		
//		VNFFG VNFFG = new VNFFG(100d);
//		VNFFG.n_reqs.put("CACHE", 2);
//		
//		SubstrateNode sn = new SubstrateNode();
//		sn.add(new CpuResource(100d, sn));
//		VNFChainNode vn1 = new VNFChainNode("CACHE");
//		vn1.add(new CpuDemand(30d, vn1));
//		VNFChainNode vn2 = new VNFChainNode("CACHE");
//		vn2.add(new CpuDemand(30d, vn2));
//		VNFChainNode vn3 = new VNFChainNode("CACHE");
//		vn3.add(new CpuDemand(30d, vn3));
//		VNFChainNode vn4 = new VNFChainNode("CACHE");
//		vn4.add(new CpuDemand(30d, vn4));
//		VNFChainNode vn5 = new VNFChainNode("CACHE");
//		vn5.add(new CpuDemand(30d, vn5));
//
//		System.out.println(sn.get());
//		EntityMapping m1 = new EntityMapping(VNFFG, sn, vn1);
//		System.out.println(sn.get());
//		EntityMapping m2 = new EntityMapping(VNFFG, sn, vn2);
//		System.out.println(sn.get());
//		EntityMapping m3 = new EntityMapping(VNFFG, sn, vn3);
//		System.out.println(sn.get());
//		EntityMapping m4 = new EntityMapping(VNFFG, sn, vn4);
//		System.out.println(sn.get());
//		EntityMapping m5 = new EntityMapping(VNFFG, sn, vn5);
//		System.out.println(sn.get());
//		
//		m1.free(); System.out.println("1: " + sn.get());
//		m5.free(); System.out.println("2: " + sn.get());
//		m3.free(); System.out.println("3: " + sn.get());
//		m3 = new EntityMapping(VNFFG, sn, vn3); System.out.println("4: " + sn.get());
//		m3.free(); System.out.println("5: " + sn.get());
//	}
	

	final CoordVNF_SVNF_Algorithm SVNF;
	public final VNFFG VNFFG;
	public final Collection<NetworkEntity<AbstractResource>> substrateEntities;
	public Collection<AbstractDemand> virtual;
	public final LinkedList<ResourceDemandEntry> mappedResources;

//	public EntityMapping(VNFFG VNFFG, SubstrateNode substrate, VNFChainNode virtual) {
//		this.VNFFG = VNFFG;
//		this.substrateEntities = new LinkedList<NetworkEntity<AbstractResource>>();
//		this.substrateEntities.add(substrate);
//		this.virtual = virtual.get();
//		this.mappedResources = new LinkedList<ResourceDemandEntry>();
//		
//		occupyResources(VNFFG, virtual.get(), substrate);
//	}
	
	public EntityMapping(CoordVNF_SVNF_Algorithm SVNF, VNFFG VNFFG, SubstrateNode substrate, List<AbstractDemand> virtual) {
		this.SVNF = SVNF;
		this.VNFFG = VNFFG;
		LinkedList<NetworkEntity<AbstractResource>> substrateEntities = new LinkedList<NetworkEntity<AbstractResource>>();
		substrateEntities.add(substrate);
		this.substrateEntities = substrateEntities;
		
		this.virtual = virtual;
		this.mappedResources = new LinkedList<ResourceDemandEntry>();
		
		occupyResources(virtual, substrate);
	}
	
	public EntityMapping(CoordVNF_SVNF_Algorithm SVNF, VNFFG VNFFG, SubstrateNode substrate, AbstractDemand dem) {
		LinkedList<AbstractDemand> virtual = new LinkedList<AbstractDemand>();
		virtual.add(dem);
		
		this.SVNF = SVNF;
		this.VNFFG = VNFFG;
		LinkedList<NetworkEntity<AbstractResource>> substrateEntities = new LinkedList<NetworkEntity<AbstractResource>>();
		substrateEntities.add(substrate);
		this.substrateEntities = substrateEntities;
		
		this.virtual = virtual;
		this.mappedResources = new LinkedList<ResourceDemandEntry>();
		
		occupyResources(virtual, substrate);
	}
	
//	public EntityMapping(VNFFG VNFFG, SubstrateNetwork sNet, List<SubstrateLink> substrate, VirtualLink virtual, boolean mapBidirectionalPaths) {
//		this.VNFFG = VNFFG;
//		this.substrateEntities = new LinkedList<NetworkEntity<AbstractResource>>(substrate);
//		this.virtual = virtual.get();
//		this.mappedResources = new LinkedList<ResourceDemandEntry>();
//		
//		occupyPathResources(VNFFG, virtual.get(), substrate, sNet, mapBidirectionalPaths);
//	}
	
	public EntityMapping(CoordVNF_SVNF_Algorithm SVNF, VNFFG VNFFG, SubstrateNetwork sNet, List<SubstrateLink> substrate, List<AbstractDemand> virtual, boolean mapBidirectionalPaths) {
		this.SVNF = SVNF;
		this.VNFFG = VNFFG;
		this.substrateEntities = Collections.unmodifiableCollection(substrate);
		this.virtual = virtual;
		this.mappedResources = new LinkedList<ResourceDemandEntry>();
		
		occupyPathResources(virtual, substrate, sNet, mapBidirectionalPaths);
	}
	
	public EntityMapping(CoordVNF_SVNF_Algorithm SVNF, VNFFG VNFFG, SubstrateNetwork sNet, List<SubstrateLink> substrate, AbstractDemand dem, boolean mapBidirectionalPaths) {
		LinkedList<AbstractDemand> virtual = new LinkedList<AbstractDemand>();
		virtual.add(dem);
		
		this.SVNF = SVNF;
		this.VNFFG = VNFFG;
		this.substrateEntities = Collections.unmodifiableCollection(substrate);
		this.virtual = virtual;
		this.mappedResources = new LinkedList<ResourceDemandEntry>();
		
		occupyPathResources(virtual, substrate, sNet, mapBidirectionalPaths);
	}
	
	public static boolean fulfills(
			Collection<AbstractResource> substrate,
			Collection<AbstractDemand> virtual) {
		
		for (AbstractDemand dem : virtual) {
//			if (dem.getMappings().isEmpty()) {
				boolean found = false;
				for (AbstractResource res : substrate) {
					if (res.accepts(dem) && res.fulfills(dem)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
//			}
		}

		return true;
	}
	
	public void free() {
		Iterator<ResourceDemandEntry> iterator = this.mappedResources.descendingIterator();
		while (iterator.hasNext()) {
			ResourceDemandEntry entry = iterator.next();
			entry.dem.free(entry.res);
			
//			if (entry.dem instanceof CapacityDemand) {
//				CapacityDemand c = (CapacityDemand) entry.dem;
//				this.SVNF.availableCapacity += c.getDemandedCapacity();
//			}
		}
		this.mappedResources.clear();
	}
	
	
	private void occupyResources(
			List<AbstractDemand> virtual,
			NetworkEntity<AbstractResource> substrate) {
		
		for (AbstractDemand dem : virtual) {
//			if (dem.getMappings().isEmpty()) {
				boolean found = false;
				for (AbstractResource res : substrate.get()) {
					if (res.accepts(dem)) {
						if (res.fulfills(dem) && dem.occupy(res)) {
							mappedResources.add(new ResourceDemandEntry(res, dem));
							found = true;
							
//							if (dem instanceof CapacityDemand) {
//								CapacityDemand c = (CapacityDemand) dem;
//								this.SVNF.availableCapacity -= c.getDemandedCapacity();
//							}
							
							break;
						}
					}
				}

				if (!found) {
					String mappings = "MAPPINGS: {";
					for (AbstractResource r : substrate.get())
						for (Mapping m : r.getMappings())
							mappings += m.getDemand() + "\n ";
					mappings += "}";

					throw new AssertionError(dem + "\n\n" + substrate.get() + "\n\n" + mappings);
				}
//			}
		}
		
	}
	
	private void occupyPathResources(
			List<AbstractDemand> vl,
			List<SubstrateLink> path,
			SubstrateNetwork sNet,
			boolean mapBidirectionalPaths) {

		for (SubstrateLink e : path) {
			occupyResources(vl, e);
			
			if (mapBidirectionalPaths && sNet.isDirected()) {
				
				SubstrateNode source = sNet.getSource(e);
				SubstrateNode target = sNet.getDest(e);
				
				if (source == target)
					continue;
				
				for (SubstrateLink oppLink : sNet.getOutEdges(target)) {
					if (sNet.getDest(oppLink) == source) {
						occupyResources(vl, oppLink);
						break;
					}
				}
			}
			
		}
		
	}

}
