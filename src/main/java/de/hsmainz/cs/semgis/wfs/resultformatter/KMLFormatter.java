package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

public class KMLFormatter extends WFSResultFormatter {

	public KMLFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/inkml+xml";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		writer.writeStartDocument();
		writer.writeStartElement("Document");
		writer.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
		String rel="",val="",curfeaturetype="",lastInd="",lon="",lat="";
	    while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
			if(solu.get(indvar)!=null) {
				curfeaturetype=solu.get(indvar).toString();
				if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
					curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
				}
				if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
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
				}else if("lat".equalsIgnoreCase(name)){
					lat=solu.get(name).toString();
				}else if("lon".equalsIgnoreCase(name)){
					lon=solu.get(name).toString();
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
			if(!lat.isEmpty() && !lon.isEmpty()) {
				System.out.println("LatLon: "+lat+","+lon);
				if(lat.contains("^^")) {
					lat=lat.substring(0,lat.indexOf("^^"));
				}
				if(lon.contains("^^")) {
					lon=lon.substring(0,lon.indexOf("^^"));
				}
				writer.writeStartElement("the_geom");
				writer.writeStartElement("coordinates");
				writer.writeCharacters(lon+" "+lat);
				writer.writeEndElement();
				writer.writeEndElement();
				lat="";
				lon="";
			}
			lastInd=solu.get(indvar).toString();	
	    }
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();	
		return strwriter.toString();
	}

}
