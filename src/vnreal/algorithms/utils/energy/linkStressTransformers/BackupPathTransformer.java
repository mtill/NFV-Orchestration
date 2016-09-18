/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package vnreal.algorithms.utils.energy.linkStressTransformers;

import java.util.Collection;

import org.apache.commons.collections15.Transformer;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.CommonDemand;
import vnreal.network.substrate.SubstrateLink;

public class BackupPathTransformer implements
		Transformer<SubstrateLink, Double> {

//	private double backupLinkWeight;
	private CommonDemand bwdemand;
	private Collection<SubstrateLink> linksToIgnoreWrtDemands;
//	private final BackupDemand backupDemand;
//	private final double freeBackupCapacityWeight;
	
	public BackupPathTransformer(
//			double freeBackupCapacityWeight,
//			BackupDemand backupDemand,
			CommonDemand bwdemand, Collection<SubstrateLink> linksToIgnoreWrtDemands) {
//		this.backupLinkWeight = backupLinkWeight;
		this.bwdemand = bwdemand;
		this.linksToIgnoreWrtDemands = linksToIgnoreWrtDemands;
//		this.backupDemand = backupDemand;
//		this.freeBackupCapacityWeight = freeBackupCapacityWeight;
	}

	public Double transform(SubstrateLink l) {
		if (linksToIgnoreWrtDemands == null || !linksToIgnoreWrtDemands.contains(l)) {
			if (!Utils.fulfills(this.bwdemand, l.get())) {
				return Double.POSITIVE_INFINITY;
			}
		}
		
//		if (this.backupDemand != null) {
//			BackupResource r = (BackupResource) l.get(BackupResource.class);
//			double free = r.getAvailableFreeBackupCapacity();
//			if (free >= this.backupDemand.demandedBackupCapacity) {
//				return freeBackupCapacityWeight;
//			}
////		} else {
////			if (free > 0.0d)
////				return 2.0d;
//		}
		
		return 1.0d;
	}
}
