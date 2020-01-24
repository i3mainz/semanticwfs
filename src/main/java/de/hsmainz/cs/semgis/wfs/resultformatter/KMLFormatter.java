package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

public class KMLFormatter extends WFSResultFormatter {

	public KMLFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/inkml+xml";
	}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,
			String featuretype,String propertytype,String typeColumn,Boolean onlyproperty,Boolean onlyhits) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		writer.writeStartDocument();
		writer.writeStartElement("Document");
		writer.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
		String rel="",val="",curfeaturetype="",lastInd="";
	    while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
			if(solu.get(featuretype.toLowerCase())!=null) {
				curfeaturetype=solu.get(featuretype.toLowerCase()).toString();
				if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
					curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
				}
				if(!solu.get(featuretype.toLowerCase()).toString().equals(lastInd) || lastInd.isEmpty()) {
					if(!lastInd.isEmpty()) {
						writer.writeEndElement();
						writer.writeEndElement();  
					}
					writer.writeStartElement("Placemark");
					writer.writeStartElement("ExtendedData");
					lastQueriedElemCount++;
				}
			}
			Iterator<String> varnames=solu.varNames();
			while(varnames.hasNext()) {
				String name=varnames.next();
				if(name.contains("_geom")) {
					writer.writeStartElement(name);
					writer.writeStartElement("coordinates");
					writer.writeCharacters(solu.get(name).toString().substring(solu.get(name).toString().indexOf('(')+1,solu.get(name).toString().indexOf(')')).trim().replace(" ",","));
					writer.writeEndElement();
					writer.writeEndElement();
				}else if(name.equalsIgnoreCase(featuretype)){
					continue;
				}else if("rel".equalsIgnoreCase(name) || name.contains("_rel")){
					rel=solu.get(name).toString();
				}else if("val".equalsIgnoreCase(name) || name.contains("_val")){
					val=solu.get(name).toString();
				}else {
					if((name.contains("http") || name.contains("file:/")) && name.contains("#")) {
						writer.writeStartElement(name.substring(name.lastIndexOf('#')+1));
						writer.writeCharacters(solu.get(name).toString());
						writer.writeEndElement();	
					}else {
						writer.writeStartElement(name);
						writer.writeCharacters(solu.get(name).toString());
						writer.writeEndElement();
					}
				}
			}
			if(!rel.isEmpty() && !val.isEmpty()) {
				if((rel.contains("http") || rel.contains("file:/")) && rel.contains("#")) {
					writer.writeStartElement(rel.substring(rel.lastIndexOf('#')+1));
					writer.writeCharacters(val);
					writer.writeEndElement();	
				}else {
					writer.writeStartElement(rel);
					writer.writeCharacters(val);
					writer.writeEndElement();	
				}
				rel="";
				val="";
			}
			lastInd=solu.get(featuretype.toLowerCase()).toString();	
	    }
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();	
		return strwriter.toString();
	}

}
