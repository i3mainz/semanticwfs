package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class KMLFormatter extends WFSResultFormatter {

	public KMLFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/inkml+xml";
	}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=factory.createXMLStreamWriter(strwriter);
		writer.writeStartDocument();
		writer.writeStartElement("Document");
		writer.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
	    while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
			writer.writeStartElement("Placemark");
			writer.writeStartElement("ExtendedData");
			Iterator<String> varnames=solu.varNames();
			while(varnames.hasNext()) {
				String curvar=varnames.next();
				writer.writeStartElement(curvar);
				writer.writeCharacters(solu.get(curvar).toString());
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndElement();    	
	    }
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();	
		return strwriter.toString();
	}

}
