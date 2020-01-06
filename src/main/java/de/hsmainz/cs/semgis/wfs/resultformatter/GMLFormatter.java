package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class GMLFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=factory.createXMLStreamWriter(strwriter);
		writer.writeStartDocument();
		writer.writeStartElement("member");
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
			writer.writeStartElement("classname");
			Iterator<String> iter=solu.varNames();
			while(iter.hasNext()) {
				String curvar=iter.next();
				writer.writeStartElement(curvar);
				writer.writeCharacters(solu.get(curvar).toString());
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
