package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result in OpenStreetMap XML.
 */
public class OSMFormatter extends ResultFormatter {
	
	/**
	 * Constructor for this class.
	 */
	public OSMFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/osm+xml";
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		writer.writeStartDocument();
		writer.writeStartElement("osm");
		writer.writeAttribute("version", "0.6");
		writer.writeAttribute("generator","semanticwfs");
		for(int i=0;i<geojson.getJSONArray("features").length();i++) {
			JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
			if(feature.getJSONObject("geometry").getString("type").equalsIgnoreCase("Point")) {
				writer.writeStartElement("node");
				writer.writeAttribute("id","-"+i);
				writer.writeAttribute("lat", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)+"");
				writer.writeAttribute("lon", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)+"");
			}else if(feature.getJSONObject("geometry").getString("type").contains("Multi")) {
				writer.writeStartElement("relation");
				writer.writeAttribute("id","-"+i);
			}else {
				writer.writeStartElement("way");
				writer.writeAttribute("id","-"+i);
			}
			for(String key:feature.getJSONObject("properties").keySet()) {
				writer.writeStartElement("tag");
				writer.writeAttribute("k", key);
				writer.writeAttribute("v", feature.getJSONObject("properties").get(key).toString());
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
