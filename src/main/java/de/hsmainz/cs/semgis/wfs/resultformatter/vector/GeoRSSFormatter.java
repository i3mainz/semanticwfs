package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
import org.json.JSONException;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GeoRSSFormatter extends ResultFormatter {

	
	/**
	 * Constructor for this class.
	 */
	public GeoRSSFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/georss";
		this.urlformat="georss";
		this.label="GeoRSS";
		this.fileextension="rss";
		this.definition="https://www.ogc.org/standards/georss";
	}
	
	public void collectColumns(XMLStreamWriter writer,JSONObject obj,String nameprefix) throws JSONException, XMLStreamException {
		for(String key:obj.keySet()) {		
			String namekey="";
			String val="";
			if(key.startsWith("http")) {
				if(key.contains("#")) {
					namekey=key.substring(key.lastIndexOf('#')+1);
				}else {
					namekey=key.substring(key.lastIndexOf('/')+1);
				}
			}
			try {
				if(nameprefix.isEmpty()) {
					collectColumns(writer,obj.getJSONObject(key),namekey);	
				}else {
					collectColumns(writer,obj.getJSONObject(key),nameprefix+"."+namekey);
				}
				
			}catch(Exception e) {
				String tagname=key;
				tagname=namekey;
				if(!nameprefix.isEmpty()) {
					tagname=nameprefix+"."+namekey;
				}
				if(!tagname.isEmpty() && !key.equals("http://www.opengis.net/ont/geosparql#hasGeometry") && !key.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				writer.writeStartElement(tagname);
				if(obj.get(key).toString().contains("http")) {
					if(obj.get(key).toString().contains("^^")) {
						val=obj.get(key).toString().substring(1,obj.get(key).toString().lastIndexOf("^^"));
					}else if(obj.get(key).toString().contains("#")) {
						val=obj.get(key).toString().substring(obj.get(key).toString().lastIndexOf('#')+1);
					}else {
						val=obj.get(key).toString().substring(obj.get(key).toString().lastIndexOf('/')+1);
					}
				}else {
					val=obj.get(key).toString();
				}
				writer.writeCharacters(val);
				writer.writeEndElement();
				}
			}
		}
	}
	
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException {
		return null;
	}
	
}
