package vnreal.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import networks.VNF;
import networks.VNF.VNFInputInterface;
import networks.VNFFG;
import networks.VNFLink;


public class VNFFGExporter {
	
	public static void export(String filename, VNFFG vs) throws XMLStreamException, FileNotFoundException {
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = new IndentingXMLStreamWriter(outputFactory.createXMLStreamWriter(new FileOutputStream(filename)));
//		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(filename));
		
		ExportIDSource idSource = new ExportIDSource();
		
		writer.writeStartDocument("UTF-8", "1.0");
		
		writer.setDefaultNamespace("http://sourceforge.net/projects/alevin/");
		writer.writeStartElement("VNFFG");
		writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		writer.writeStartElement("VNFFG");
		writer.writeAttribute("layer", vs.layer+"");
		writer.writeAttribute("initialBW", vs.initialBW+"");
		
		writer.writeStartElement("initialVNF");
		exportVNF(writer, idSource, vs.getInitialNode());
		writer.writeEndElement();
		
		for (VNF t : vs.getTerminatingNodes()) {
			writer.writeStartElement("terminatingVNF");
			writer.writeAttribute("exportId", idSource.getId(t));
			writer.writeEndElement();
		}
		
		writer.writeStartElement("Dependencies");
		for (VNF v : vs.dependencies) {
			exportVNF(writer, idSource, v);
		}
		writer.writeEndElement();//End of Dependencies
		

		writer.writeEndElement();//End of VNFFG
		
		writer.writeEndDocument();
		
	}
	
	private static void exportVNF(XMLStreamWriter writer, ExportIDSource idSource, VNF v) throws XMLStreamException {
		writer.writeStartElement("VNF");
		writer.writeAttribute("exportId", idSource.getId(v)+"");
		writer.writeAttribute("id", v.id+"");
		if (v.name != null)
			writer.writeAttribute("name", v.name);
		if (v.type != null)
			writer.writeAttribute("type", v.type);
		writer.writeAttribute("capacityPer100MBits", v.capacityPer100MBits+"");
		writer.writeAttribute("staticModel", v.staticModel ? "true" : "false");
		
		if (v.FreeSlotsDemand != null)
			XMLExporter.exportDemand(writer, v.FreeSlotsDemand, "Demand", idSource.getId(v.FreeSlotsDemand));
		if (v.IdDemand != null)
			XMLExporter.exportDemand(writer, v.IdDemand, "Demand", idSource.getId(v.IdDemand));
		
		writer.writeStartElement("outLinks");
		for (VNFLink l : v.outLinks) {
			exportVNFLink(writer, l, idSource, "VNFLink");
		}
		writer.writeEndElement();
		
		for (VNFInputInterface i : v.inputInterfaces) {
			writer.writeStartElement("inputInterface");
			
			for (VNFLink l : i.requiredFlowVNFLinks) {
				writer.writeStartElement("requiredFlowVNFLink");
				writer.writeAttribute("exportId", idSource.getId(l));
				writer.writeEndElement();
			}
			
			for (VNF l : i.requiredFlowVNFs) {
				writer.writeStartElement("requiredFlowVNF");
				writer.writeAttribute("exportId", idSource.getId(l));
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
		}
		
		writer.writeEndElement();//End of VNF
	}

	private static void exportVNFLink(XMLStreamWriter writer, VNFLink l, ExportIDSource idSource, String tagname) throws XMLStreamException {
		writer.writeStartElement(tagname);
		writer.writeAttribute("exportId", idSource.getId(l));
		writer.writeAttribute("sourceID", l.source.id+"");
		
		if (l.bandwidthDemand != null) {
			XMLExporter.exportDemand(writer, l.bandwidthDemand, "BandwidthDemand", idSource.getId(l.bandwidthDemand));
		}
		
		writer.writeAttribute("dataratepercentage", l.dataratepercentage+"");
		
		writer.writeEndElement();
	}
	

}
