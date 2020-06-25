package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;


public class GPXFormatter extends WFSResultFormatter {

	public GPXFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/gpx";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		//try {
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		writer.writeStartDocument();
		writer.writeStartElement("gpx");
		writer.writeAttribute("version","1.0");
		writer.writeStartElement("name");
		writer.writeCharacters(featuretype);
		writer.writeEndElement();
		for(int i=0;i<geojson.getJSONArray("features").length();i++) {
			writer.writeStartElement("trk");
			writer.writeStartElement("name");
			writer.writeCharacters(featuretype);
			writer.writeEndElement();
			for(String key:geojson.getJSONArray("features").getJSONObject(i).getJSONObject("properties").keySet()) {
				String val=geojson.getJSONArray("features").getJSONObject(i).getJSONObject("properties").get(key).toString();
				writer.writeStartElement(key);
				writer.writeCharacters(val);
				writer.writeEndElement();
			}
			writer.writeStartElement("trkseg");
			String[] coords=geojson.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").toString().split(",");			
			for(int j=0;j<coords.length-1;j++) {
				writer.writeStartElement("trkpt");
				System.out.println(geojson.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates"));
				writer.writeAttribute("lat", coords[j].toString());
				writer.writeAttribute("lon", coords[j+1].toString());
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
