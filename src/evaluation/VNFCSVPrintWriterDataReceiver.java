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
package evaluation;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;

import algorithm.EntityMapping;
import algorithm.Tuples.Tuple;
import algorithm.VNFAlgorithm;
import evaluation.metrics.VNFEvaluationMetric;
import networks.VNFChain;
import networks.VNFFG;
import vnreal.network.substrate.SubstrateNetwork;

public class VNFCSVPrintWriterDataReceiver {

	private final VNFAlgorithm algorithm;
	private final boolean appendToFile;
	private String fieldNames = null;
	private final LinkedList<CSVEntry> content;
	private final File dir;

	public VNFCSVPrintWriterDataReceiver(File dir, VNFAlgorithm algorithm, boolean appendToFile) {
		this.dir = dir;
		this.algorithm = algorithm;
		this.appendToFile = appendToFile;
		this.content = new LinkedList<CSVEntry>();
	}
	
	
	public void receive(String scenarioSuffix, SubstrateNetwork sNet,
			HashMap<VNFFG, Tuple<VNFChain, LinkedList<EntityMapping>>> mappingResult,
			LinkedList<VNFEvaluationMetric> metrics) {
		
		String[] split = scenarioSuffix.split("_");
		String values = "";
		String myfieldNames = "";
		boolean firstEntry = true;
		for (String s : split) {
			String[] subsplit = s.split(":");
			myfieldNames += (firstEntry ? "" : "_") + subsplit[0];
			values += (firstEntry ? "" : "_") + "\"" + subsplit[1] + "\"";
			
			firstEntry = false;
		}

		
		for (VNFEvaluationMetric metric : metrics) {
			myfieldNames += "_Metric." + metric.getClass().getSimpleName();
			Double val = metric.calculate(sNet, mappingResult);
			String strval = null;
			if (val == null || val.isNaN())
				strval = "NaN";
			else if (val.isInfinite())
				strval = "Inf";
			else
				strval = val + "";
			
			values += "_\"" + strval + "\"";
		}
		
		if (this.fieldNames == null) {
			this.fieldNames = myfieldNames;
		}
		
		this.content.add(new CSVEntry(sNet.getVertexCount(), values));
	}
	
	
	public void finish() throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(dir + File.separator + this.algorithm.name.replace("_",  "-") + "_out.txt", this.appendToFile), true);
		writer.println(this.fieldNames);
		
		for (CSVEntry entry : this.content)
			if (this.algorithm.timeoutSNSize == -1 || this.algorithm.timeoutSNSize > entry.sNetSize)
				writer.println(entry.line);
		
		writer.close();
	}
	
	private static class CSVEntry {
		public final int sNetSize;
		public final String line;
		
		public CSVEntry(int sNetSize, String line) {
			this.sNetSize = sNetSize;
			this.line = line;
		}
	}

}
