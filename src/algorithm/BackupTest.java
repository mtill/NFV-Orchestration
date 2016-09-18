package algorithm;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BackupDemand;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.resources.BackupResource;
import vnreal.constraints.resources.CommonResource;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;

public class BackupTest {
	
	public static void main(String[] args) {
		SubstrateLink l = new SubstrateLink();
		final int backupSharingFactor = 3;
		
		CommonResource r = new CommonResource(30.25d, l);
		l.add(r);
		
		BackupResource b = new BackupResource(l, backupSharingFactor);
		l.add(b);
		
//		Utils.occupyResource(new BackupDemand(5, 3, null), b);
//		System.out.println(b.getAvailableBackupCapacity(3));
//		System.out.println("---");
		Utils.occupyResource(new BackupDemand(10, null, null, ""), b);
		System.out.println(b.getAvailableFreeBackupCapacity());
		System.out.println(b.getAvailableBackupCapacity());
		System.out.println("---");
		Utils.occupyResource(new BackupDemand(10, null, null, ""), b);
		System.out.println(b.getAvailableFreeBackupCapacity());
		System.out.println(b.getAvailableBackupCapacity());
		System.out.println("---");
		Utils.occupyResource(new BackupDemand(15, null, null, ""), b);
		System.out.println(b.getAvailableFreeBackupCapacity());
		System.out.println(b.getAvailableBackupCapacity());
		System.out.println("---");
		Utils.occupyResource(new BackupDemand(8, null, null, ""), b);
		System.out.println(b.getAvailableFreeBackupCapacity());
		System.out.println(b.getAvailableBackupCapacity());
		System.out.println("---");
		Utils.occupyResource(new BackupDemand(18, null, null, ""), b);
		System.out.println(b.getAvailableFreeBackupCapacity());
		System.out.println(b.getAvailableBackupCapacity());
	}

	public static void main2() {
		SubstrateLink sl = new SubstrateLink();
		CommonResource bwr = new CommonResource(100, sl);
		BackupResource br = new BackupResource(sl, 2);
		
		Utils.occupyResource(new CommonDemand(50d, null), bwr);
		
		sl.add(bwr);
		sl.add(br);
		
		
		HashMap<AbstractDemand, Integer> active = new HashMap<AbstractDemand, Integer>();
		int ids = 0;
		
		Random random = new Random();
		for (int i = 0; i < 100000; i++) {
			double r = Utils.rnd(0d, 100d, random);
			
			if (random.nextBoolean()) {
				CommonDemand bwd = new CommonDemand(r, null);
				if (bwr.fulfills(bwd)) {
					Utils.occupyResource(bwd, bwr);
					active.put(bwd, ids++);
					System.out.println("occupy bandwidth demand " + bwd);
				}
			} else {
				BackupDemand d = new BackupDemand(r, null, null, "");
				if (br.fulfills(d)) {
					Utils.occupyResource(d, br);
					active.put(d, ids++);
					System.out.println("occupy backup demand " + d);
				}
			}
			
			
			if (random.nextDouble() < 0.3) {
				if (random.nextBoolean()) {
					if (active.size() > 0) {
						LinkedList<AbstractDemand> keys = new LinkedList<AbstractDemand>(active.keySet());
						Collections.shuffle(keys);
						for (Mapping m : new LinkedList<Mapping>(keys.getFirst().getMappings())) {
							m.getDemand().free(m.getResource());
						}
						System.out.println(active.get(keys.getFirst()) + ": free demand " + keys.getFirst());
					}
				}
			}
			
//			System.out.println(bwr.getAvailableBandwidth() + "  " + br.getAvailableBackupCapacity(2d) + "  " + br.getAvailableFreeSlotCapacity(2d));
		}
	}

}
