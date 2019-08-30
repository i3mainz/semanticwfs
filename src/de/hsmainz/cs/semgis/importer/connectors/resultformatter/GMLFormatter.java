package de.hsmainz.cs.semgis.importer.connectors.resultformatter;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class GMLFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=factory.createXMLStreamWriter(strwriter);
		writer.writeStartDocument();
		writer.writeStartElement("member");
		List<QuerySolution> test=ResultSetFormatter.toList(results);
	    for(QuerySolution solu:test) {
			writer.writeStartElement("classname");
			while(solu.varNames().hasNext()) {
				String curvar=solu.varNames().next();
				writer.writeStartElement(curvar);
				writer.writeCharacters(solu.get("curvar").toString());
				writer.writeEndElement();
			}
			writer.writeEndElement();    	
	    }
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();	
		return strwriter.toString();
	}

}
