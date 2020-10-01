package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;


/**
 * Formats a query result to GPX.
 *
 */
public class GPXFormatter extends VectorResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public GPXFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/gpx";
		this.urlformat="gpx";
		this.label="GPS Exchange Format (GPX)";
		this.fileextension="gpx";
		this.definition="https://www.topografix.com/gpx.asp";
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
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, JSONException, IOException {
		ResultFormatter format = resultMap.get("geojson");
		//try {
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,
						indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer=null;
		StringWriter strwriter=null;
		if(out!=null) {
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out));				
		}else {
			strwriter=new StringWriter();
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));			
		}
		writer.writeStartDocument();
		writer.writeStartElement("gpx");
		writer.writeAttribute("version","1.0");
		writer.writeStartElement("name");
		writer.writeCharacters(featuretype);
		writer.writeEndElement();
		for(int i=0;i<geojson.getJSONArray("features").length();i++) {
			String[] coords=geojson.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").toString().split(",");
			if(geojson.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getString("type").equalsIgnoreCase("Point")) {
				writer.writeStartElement("wpt");
				writer.writeAttribute("lat", coords[0].replace("[", "").replace("]",""));
				writer.writeAttribute("lon", coords[1].replace("]", "").replace("[",""));
				collectColumns(writer, geojson.getJSONArray("features").getJSONObject(i).getJSONObject("properties"),"");
				writer.writeEndElement();
			}else {
				writer.writeStartElement("trk");
				writer.writeStartElement("name");
				writer.writeCharacters(featuretype);
				writer.writeEndElement();
				collectColumns(writer, geojson.getJSONArray("features").getJSONObject(i).getJSONObject("properties"),"");
				writer.writeStartElement("trkseg");					
				for(int j=0;j<coords.length-1;j++) {
					writer.writeStartElement("trkpt");
					System.out.println(geojson.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates"));
					writer.writeAttribute("lat", coords[j].toString().replace("[", "").replace("]",""));
					writer.writeAttribute("lon", coords[j+1].toString().replace("]", "").replace("[",""));
					writer.writeEndElement();
				}
				writer.writeEndElement();
				writer.writeEndElement();				
			}

		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		if(out==null)
			return "";
		return strwriter.toString();
	}

}
