package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
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
		this.urlformat="osm";
		this.label="OSM/XML (OSM)";
		this.fileextension="osm";
		this.definition="https://wiki.openstreetmap.org/wiki/OSM_XML";
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
				writer.writeStartElement("tag");
				if(nameprefix.isEmpty()) {
					writer.writeAttribute("k",namekey);	
				}else {
					writer.writeAttribute("k",nameprefix+"."+namekey);
				}
				String val=obj.get(key).toString();
				if(val.contains("^^")) {
					writer.writeAttribute("v",val.substring(0,val.lastIndexOf("^^")));
				}else if (val.contains("#")) {
					writer.writeAttribute("v",val.substring(val.lastIndexOf('#') + 1));
				} else if(val.startsWith("http")) {
					writer.writeAttribute("v",val.substring(val.lastIndexOf('/') + 1));
				}else {
					writer.writeAttribute("v",val);
				}
				writer.writeEndElement();
			}
			}
		}
	}
	

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException, JSONException, IOException {
		ResultFormatter format = resultMap.get("geojson");
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,
						indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
		if(out!=null) {
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out));
		}else {
			StringWriter strwriter=new StringWriter();
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		}
		writer.writeStartDocument();
		writer.writeStartElement("osm");
		writer.writeAttribute("version", "0.6");
		writer.writeAttribute("generator","semanticwfs");
		System.out.println(WebService.nameSpaceCache);
		System.out.println(featuretype.toLowerCase());
		for(String ns:WebService.nameSpaceCache.get(featuretype.toLowerCase()).keySet()) {
			writer.setPrefix(WebService.nameSpaceCache.get(featuretype.toLowerCase()).get(ns),ns);
		}
		Integer nodecounter=0;
		for(int i=0;i<geojson.getJSONArray("features").length();i++) {
			JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
			if(feature.getJSONObject("geometry").getString("type").equalsIgnoreCase("Point")) {
				writer.writeStartElement("node");
				writer.writeAttribute("id","-"+(i+1));
				writer.writeAttribute("lat", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)+"");
				writer.writeAttribute("lon", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)+"");
			}else if(feature.getJSONObject("geometry").getString("type").contains("Multi")) {
				JSONArray ways=feature.getJSONObject("geometry").getJSONArray("coordinates");		
				int j=0;
				List<Integer> wayids=new LinkedList<Integer>();
				for(j=0;j<ways.length();j++) {			
					for(int l=0;l<feature.getJSONObject("geometry").getJSONArray("coordinates").length();l++) {
						writer.writeStartElement("node");
						writer.writeAttribute("id","-"+nodecounter++);
						writer.writeAttribute("lat", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)+"");
						writer.writeAttribute("lon", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)+"");
						writer.writeEndElement();
						i++;
					}
					writer.writeStartElement("way");
					writer.writeAttribute("id","-"+nodecounter++);
					wayids.add(nodecounter-1);
					for(int k=1;k<i;k++) {
						writer.writeStartElement("nd");
						writer.writeAttribute("ref", "-"+k);
						writer.writeEndElement();
					}
				}				
				writer.writeStartElement("relation");
				writer.writeAttribute("id","-"+nodecounter++);
				for(Integer wayid:wayids) {
					writer.writeStartElement("member");
					writer.writeAttribute("type", "way");
					writer.writeAttribute("id", "-"+wayid);
					writer.writeEndElement();
				}
			}else {
				for(int j=0;j<feature.getJSONObject("geometry").getJSONArray("coordinates").length();j++) {
					writer.writeStartElement("node");
					writer.writeAttribute("id","-"+nodecounter++);
					writer.writeAttribute("lat", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)+"");
					writer.writeAttribute("lon", feature.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)+"");
					writer.writeEndElement();
				}
				writer.writeStartElement("way");
				writer.writeAttribute("id","-"+nodecounter++);
				for(int k=1;k<nodecounter-1;k++) {
					writer.writeStartElement("nd");
					writer.writeAttribute("ref", "-"+k);
					writer.writeEndElement();
				}
			}
			addTagsFromJSONObject(feature.getJSONObject("properties"), writer,feature.get("id").toString(),"");
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		return null;
	}

	
	
}
