package de.hsmainz.cs.semgis.wfs.resultformatter;

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

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.webservice.WebService;

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
	
	public String[] splitURL(String url) {
		String[] res=new String[]{"",""};
		if(url.contains("http") && url.contains("#")){
			res[0]=url.substring(0,url.lastIndexOf('#')+1);
			res[1]=url.substring(url.lastIndexOf('#')+1);
			return res;
		}else if(url.contains("http") && url.contains("/")){
			res[0]=url.substring(0,url.lastIndexOf('/')+1);
			res[1]=url.substring(url.lastIndexOf('/')+1);
			return res;
		}
		return null;
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
			String[] splitted=splitURL(uri);
			if(splitted!=null) {	
				System.out.println(WebService.nameSpaceCache);
				//writer.setPrefix(splitted[0].substring(splitted[0].length()-4,splitted[0].length()-1), splitted[0]);
				System.out.println(splitted[0]);
				writer.writeStartElement("tag");
				writer.writeAttribute("k", splitted[0]+splitted[1]);
			}else {
				writer.writeStartElement("tag");
				writer.writeAttribute("k", uri);
			}
		}
		for(String key:obj.keySet()) {
			if(!key.equals("http://www.opengis.net/ont/geosparql#hasGeometry") 
					&& !key.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
					&& !key.equalsIgnoreCase("the_geom")) {
			String[] splitted=splitURL(key);
			if(splitted!=null) {	
				System.out.println(WebService.nameSpaceCache);
				//writer.setPrefix(splitted[0].substring(splitted[0].length()-4,splitted[0].length()-1), splitted[0]);
				System.out.println(splitted[0]);
				writer.writeStartElement("tag");
				writer.writeStartElement("k",splitted[0]+splitted[1]);
			}else {
				writer.writeStartElement("tag");
				writer.writeStartElement("k",key);
			}
			try {
				addTagsFromJSONObject(obj.getJSONObject(key),writer,curfeatureid);
			}catch(Exception e) {
				String val=obj.get(key).toString();
				if(val.contains("^^")) {
					writer.writeAttribute("v",val.substring(0,val.lastIndexOf("^^")));
				}else {
					splitted=splitURL(val);
					if(splitted!=null) {		
						//writer.setPrefix(splitted[0].substring(splitted[0].length()-4,splitted[0].length()-1), splitted[0]);
						writer.writeAttribute("v",splitted[1]);
					}else {
						writer.writeAttribute("v",val);
					}
				}
			}
			writer.writeEndElement();
			}
		}
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
		System.out.println(WebService.nameSpaceCache);
		System.out.println(featuretype.toLowerCase());
		for(String ns:WebService.nameSpaceCache.get(featuretype.toLowerCase()).keySet()) {
			writer.setPrefix(WebService.nameSpaceCache.get(featuretype.toLowerCase()).get(ns),ns);
		}
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
			addTagsFromJSONObject(feature.getJSONObject("properties"), writer, feature.get("id").toString());
			/*for(String key:feature.getJSONObject("properties").keySet()) {
				
				writer.writeStartElement("tag");
				writer.writeAttribute("k", key);
				writer.writeAttribute("v", feature.getJSONObject("properties").get(key).toString());
				writer.writeEndElement();
			}*/
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		return strwriter.toString();
	}

	
	
}
