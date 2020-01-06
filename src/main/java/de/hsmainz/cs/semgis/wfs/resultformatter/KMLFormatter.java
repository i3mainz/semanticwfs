package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class KMLFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset) throws XMLStreamException {
		String serviceType="WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=factory.createXMLStreamWriter(strwriter);
		writer.writeStartDocument();
		writer.writeStartElement("Document");
		List<QuerySolution> test=ResultSetFormatter.toList(results);
	    for(QuerySolution solu:test) {
			writer.writeStartElement("Placemark");
			writer.writeStartElement("ExtendedData");
			while(solu.varNames().hasNext()) {
				String curvar=solu.varNames().next();
				writer.writeStartElement(curvar);
				writer.writeCharacters(solu.get("curvar").toString());
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
