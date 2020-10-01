package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

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
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.webservice.WebService;

public class MapMLFormatter extends ResultFormatter {

	public MapMLFormatter() {
		this.urlformat="mapml";
		this.label="Map Markup Language (MapML)";
		this.mimeType="text/mapml";
		this.exposedType="text/mapml";
		this.fileextension="mapml";
		this.definition="https://maps4html.org/MapML/spec/";
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
					writer.writeStartElement("th");
					writer.writeAttribute("scope", "row");
					writer.writeCharacters(tagname);
					writer.writeEndElement();
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
				writer.writeStartElement("td");
				writer.writeAttribute("itemprop", tagname);
				writer.writeCharacters(val);
				writer.writeEndElement();
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
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out)  {
		ResultFormatter format = resultMap.get("geojson");
		try {
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,
						onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,
						alternativeFormat,invertXY,coverage,out));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=null;
		if(out!=null) {
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out));				
		}else {
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));			
		}
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
			writer.writeAttribute("itemtype", featuretype);
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
			collectColumns(writer,feature.getJSONObject("properties"),"");	
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("geometry");
			writer.writeStartElement(feature.getJSONObject("geometry").getString("type"));
			writer.writeStartElement("coordinates");
			writer.writeCharacters(feature.getJSONObject("geometry").getJSONArray("coordinates").toString().replace("[", "").replace("]", "").replace(",", " "));
			writer.writeEndElement();
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
