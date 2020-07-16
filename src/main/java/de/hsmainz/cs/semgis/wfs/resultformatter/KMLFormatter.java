package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.KMLStyleFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to KML.
 * Attributes will be stored as KML ExtendedData.
 * Hierarchies of XML data will be stored as KML attribute names.
 * KML requires that styles are formatted within the dataset, therefore this class has to utilize the KMLStyleFormatter implementation.
 *
 */
public class KMLFormatter extends WFSResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public KMLFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/inkml+xml";
		this.styleformatter=new KMLStyleFormatter();
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
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		//try {
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		writer.writeStartDocument();
		writer.writeStartElement("kml");
		writer.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
		writer.writeStartElement("Document");
		writer.writeStartElement("Style");
		writer.writeCharacters("");
		writer.flush();
		strwriter.write(this.styleformatter.formatGeometry("LineString", mapstyle));
		strwriter.write(this.styleformatter.formatGeometry("Polygon", mapstyle));
		strwriter.write(this.styleformatter.formatGeometry("Point", mapstyle));
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
		}
		
		/*writer.flush();	
		System.out.println(strwriter.toString());
		return strwriter.toString();
		}catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
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
				}else if(name.equalsIgnoreCase(indvar)){
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
	    }*/
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();	
		return strwriter.toString();
	}

}
