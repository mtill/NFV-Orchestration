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
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class DisjointPathTransformer implements
		Transformer<SubstrateLink, Double> {

//	private double backupLinkWeight;
	private CommonDemand bandwidthDemand;
	private Collection<SubstrateLink> firstPath;
//	private final BackupDemand backupDemand;
	private final SubstrateNetwork sNet;
//	private final double freeBackupCapacityWeight;

	public DisjointPathTransformer(
//			double freeBackupCapacityWeight,
//			BackupDemand backupDemand,
			CommonDemand bandwidthDemand, Collection<SubstrateLink> firstPath, SubstrateNetwork sNet) {
//		this.backupLinkWeight = backupLinkWeight;
		this.bandwidthDemand = bandwidthDemand;
		this.firstPath = firstPath;
//		this.backupDemand = backupDemand;
		this.sNet = sNet;
//		this.freeBackupCapacityWeight = freeBackupCapacityWeight;
	}

	public Double transform(SubstrateLink l) {
//		if (this.firstPath.contains(l)) {
//			return Double.POSITIVE_INFINITY;
//		}
		SubstrateNode dest = sNet.getDest(l);
		int c = 0;
		for (SubstrateLink sl : this.firstPath) {
			c++;
			
			if (sl == l)
				return Double.POSITIVE_INFINITY;
			
			if (c == this.firstPath.size()) // ignore last element
				break;
			
			SubstrateNode o = sNet.getDest(sl);
			if (o == dest) {
				return Double.POSITIVE_INFINITY;
			}
		}
		
		if (!Utils.fulfills(this.bandwidthDemand, l.get())) {
			return Double.POSITIVE_INFINITY;
		}
		return 1.0d;
		
//		CommonResource res = ((CommonResource) l.get(CommonResource.class));
//		List<CommonDemand> demands = res.getMappedDemands();
//		demands.add(this.bandwidthDemand);
//		double thedem = CommonResource.getRequiredCapacity(demands);
//		if (res.getCapacity() < thedem)
//			return Double.POSITIVE_INFINITY;
//		return thedem;
	}
}
