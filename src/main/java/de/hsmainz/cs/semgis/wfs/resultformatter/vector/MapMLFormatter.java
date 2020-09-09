package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.StringWriter;
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
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.webservice.WebService;

public class MapMLFormatter extends ResultFormatter {

	public MapMLFormatter() {
		this.urlformat="mapml";
		this.label="Map Markup Language (MapML)";
		this.mimeType="text/mapml";
		this.exposedType="text/mapml";
	}
	
	public void addTagsFromJSONObject(JSONObject obj,XMLStreamWriter writer,String curfeatureid) throws XMLStreamException {
		if(obj.has("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
			String uri="";
			try {
				JSONArray arr=obj.getJSONArray("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			for(int i=0;i<arr.length();i++) {
				if(arr.get(i).toString().startsWith("http")) {
					uri=arr.getString(i).toString();
					break;
				}
			}
			}catch(JSONException e) {
				uri=obj.get("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").toString();
			}		
			writer.writeStartElement("th");
			writer.writeAttribute("scope", "row");
			writer.writeCharacters(uri);
			writer.writeEndElement();
		}
		for(String key:obj.keySet()) {
			if(!key.equals("http://www.opengis.net/ont/geosparql#hasGeometry") 
					&& !key.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
					&& !key.equalsIgnoreCase("the_geom")) {
				writer.writeStartElement("th");
				writer.writeAttribute("scope", "row");
				writer.writeCharacters(key);
				writer.writeEndElement();
			try {
				addTagsFromJSONObject(obj.getJSONObject(key),writer,curfeatureid);
			}catch(Exception e) {
				String val=obj.get(key).toString();
				if(val.contains("^^")) {
					writer.writeCharacters(val.substring(0,val.lastIndexOf("^^")));
				}else {
					writer.writeStartElement("td");
					writer.writeAttribute("itemprop", key);
					writer.writeCharacters(val);
					writer.writeEndElement();
				}
			}
			}
		}
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,String srsName,
			String indvar,String epsg,
			List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY)  {
		ResultFormatter format = resultMap.get("geojson");
		try {
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		//writer.writeStartDocument();
		System.out.println(WebService.nameSpaceCache);
		System.out.println(featuretype.toLowerCase());
		for(String ns:WebService.nameSpaceCache.get(featuretype.toLowerCase()).keySet()) {
			writer.setPrefix(WebService.nameSpaceCache.get(featuretype.toLowerCase()).get(ns),ns);
		}
		writer.writeStartElement("mapml");
		writer.writeStartElement("head");
		writer.writeStartElement("title");
		writer.writeCharacters(featuretype);
		writer.writeEndElement();
		writer.writeStartElement("base");
		writer.writeAttribute("href", WebService.wfsconf.getString("baseurl"));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("body");
		JSONArray features=geojson.getJSONArray("features");
		for(int i=0;i<features.length();i++) {
			JSONObject feature=features.getJSONObject(i);
			writer.writeStartElement("feature");
			writer.writeAttribute("id", feature.optString("id"));
			writer.writeAttribute("itemscope","itemscope");
			writer.writeAttribute("itemtype", WebService.wfsconf.getString("baseurl"));
			writer.writeStartElement("properties");
			writer.writeStartElement("div");
			writer.writeAttribute("class", "table-container");
			writer.writeStartElement("table");
			writer.writeStartElement("caption");
			writer.writeCharacters("Feature Properties");
			writer.writeEndElement();
			writer.writeStartElement("tbody");
			String curfeaturetype=feature.getString("id");
			if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
				curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
			}else if(curfeaturetype.startsWith("http")) {
				curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('/')+1);
			}
			addTagsFromJSONObject(feature.getJSONObject("properties"), writer,curfeaturetype);	
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("geometry");
			writer.writeStartElement(feature.getJSONObject("geometry").getString("type"));
			writer.writeCharacters(feature.getJSONObject("geometry").getJSONArray("coordinates").toString().replace("[", "").replace("]", "").replace(",", " "));
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.flush();	
		System.out.println(strwriter.toString());
		return strwriter.toString();
		}catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}

}
