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
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=factory.createXMLStreamWriter(strwriter);
		//writer.writeStartDocument();
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
			writer.writeStartElement(startingElement);
			writer.writeStartElement(featuretype);
			Iterator<String> iter=solu.varNames();
			while(iter.hasNext()) {
				String curvar=iter.next();
				if(curvar.contains("_geom")) {
					writer.writeStartElement("geometry");
					if(solu.get(curvar).toString().contains("wkt")) {
						writer.writeStartElement("gml:"+solu.get(curvar).toString().substring(0,solu.get(curvar).toString().indexOf('(')));
						writer.writeStartElement("gml:pos");
						writer.writeCharacters(solu.get(curvar).toString().substring(solu.get(curvar).toString().indexOf('(')+1,solu.get(curvar).toString().lastIndexOf(')')));
						writer.writeEndElement();
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}else {
					writer.writeStartElement(curvar);
					writer.writeCharacters(solu.get(curvar).toString());
					writer.writeEndElement();
				}
			}
			writer.writeEndElement();    
			writer.writeEndElement();
	    }
		//writer.writeEndDocument();
		writer.flush();	
		return strwriter.toString();
	}

}
