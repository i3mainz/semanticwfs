package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.KMLStyleFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to KML.
 * Attributes will be stored as KML ExtendedData.
 * Hierarchies of XML data will be stored as KML attribute names.
 * KML requires that styles are formatted within the dataset, therefore this class has to utilize the KMLStyleFormatter implementation.
 *
 */
public class KMLFormatter extends VectorResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public KMLFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/inkml+xml";
		this.urlformat="kml";
		this.styleformatter=new KMLStyleFormatter();
		this.label="Keyhole Markup Language (KML)";
		this.fileextension="kml";
		this.definition="https://www.ogc.org/standards/kml";
	}
	
	/**
	 * Adds tags from a GeoJSON object from which the KML result is created.
	 * @param obj the JSONObject to process
	 * @param writer the XMLWriter to write out KML
	 * @param curfeatureid the current feature id to process
	 * @param nameprefix a nameprefix to store for recursive calls
	 * @throws XMLStreamException if there was an error writing XML
	 */
	public void addTagsFromJSONObject(JSONObject obj,XMLStreamWriter writer,String curfeatureid,String nameprefix) throws XMLStreamException {
		for(String key:obj.keySet()) {
			String namekey="";
			if (key.contains("#")) {
				namekey=key.substring(key.lastIndexOf('#') + 1);
			} else {
				namekey=key.substring(key.lastIndexOf('/') + 1);
			}
			if(!key.equals("http://www.opengis.net/ont/geosparql#hasGeometry") 
					&& !key.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
					&& !key.equalsIgnoreCase("the_geom")) {
			try {
				if(nameprefix.isEmpty()) {
					addTagsFromJSONObject(obj.getJSONObject(key),writer,curfeatureid,namekey);	
				}else {
					addTagsFromJSONObject(obj.getJSONObject(key),writer,curfeatureid,nameprefix+"."+namekey);
				}
			}catch(Exception e) {
				writer.writeStartElement("Data");
				if(nameprefix.isEmpty()) {
					writer.writeAttribute("name",namekey);	
				}else {
					writer.writeAttribute("name",nameprefix+"."+namekey);
				}
				writer.writeStartElement("displayName");
				if(nameprefix.isEmpty()) {
					if(namekey.startsWith("http")) {
						writer.writeCharacters(namekey.substring(namekey.lastIndexOf('/')+1));
					}else {
						writer.writeCharacters(namekey);
					}			
				}else {
					writer.writeCharacters(nameprefix+"."+namekey);
				}
				writer.writeEndElement();
				writer.writeStartElement("value");
				String val=obj.get(key).toString();
				if(val.contains("^^")) {
					writer.writeCharacters(val.substring(0,val.lastIndexOf("^^")));
				}else if (val.contains("#")) {
					writer.writeCharacters(val.substring(val.lastIndexOf('#') + 1));
				} else if(val.startsWith("http")) {
					writer.writeCharacters(val.substring(val.lastIndexOf('/') + 1));
				}else {
					writer.writeCharacters(val);
				}
				writer.writeEndElement();
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
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, JSONException, IOException {
		ResultFormatter format = resultMap.get("geojson");
		//try {
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,
						onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,
						mapstyle,alternativeFormat,invertXY,coverage,out));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=null;
		XMLStreamWriter writer=null;
		if(out!=null) {
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out));
		}else {
			strwriter=new StringWriter();	
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		}
		writer.writeStartDocument();
		writer.writeStartElement("kml");
		writer.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
		writer.writeStartElement("Document");
		writer.writeStartElement("Style");
		writer.writeCharacters("");
		writer.flush();
		if(out!=null) {
			out.write(this.styleformatter.formatGeometry("LineString", mapstyle));
			out.write(this.styleformatter.formatGeometry("Polygon", mapstyle));
			out.write(this.styleformatter.formatGeometry("Point", mapstyle));
		}else {
			strwriter.write(this.styleformatter.formatGeometry("LineString", mapstyle));
			strwriter.write(this.styleformatter.formatGeometry("Polygon", mapstyle));
			strwriter.write(this.styleformatter.formatGeometry("Point", mapstyle));			
		}
		writer.writeEndElement();
		JSONArray features=geojson.getJSONArray("features");
		for(int i=0;i<features.length();i++) {
			writer.writeStartElement("Placemark");
			writer.writeStartElement("name");
			JSONObject feature=features.getJSONObject(i);
			String curfeaturetype=feature.getString("id");
			if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
				curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
			}else if(curfeaturetype.startsWith("http")) {
				curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('/')+1);
			}
			writer.writeCharacters(curfeaturetype);
			writer.writeEndElement();
			writer.writeStartElement("ExtendedData");
			//writer.writeStartElement(featuretype);	
			//writer.writeAttribute("gml:id", curfeaturetype);
			addTagsFromJSONObject(feature.getJSONObject("properties"), writer,curfeaturetype,"");	
			writer.writeEndElement();
			writer.writeStartElement(feature.getJSONObject("geometry").getString("type"));
			writer.writeStartElement("coordinates");
			writer.writeCharacters(feature.getJSONObject("geometry").getJSONArray("coordinates").toString().replace("[", "").replace("]", "").replace(",", " "));
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			if(i%FLUSHTHRESHOLD==0)
				writer.flush();
		}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();	
		return "";
	}

}
