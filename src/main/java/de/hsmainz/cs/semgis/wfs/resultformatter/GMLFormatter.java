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

public class GMLFormatter extends WFSResultFormatter {

	public GMLFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/gml";
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
				writer.writeStartElement(splitted[0],splitted[1]);
			}else {
				writer.writeStartElement(uri);
			}
			writer.writeAttribute("gml:id", curfeatureid);
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
				writer.writeStartElement(splitted[0],splitted[1]);
			}else {
				writer.writeStartElement(key);
			}
			try {
				addTagsFromJSONObject(obj.getJSONObject(key),writer,curfeatureid);
			}catch(Exception e) {
				String val=obj.get(key).toString();
				if(val.contains("^^")) {
					writer.writeCharacters(val.substring(0,val.lastIndexOf("^^")));
				}else {
					splitted=splitURL(val);
					if(splitted!=null) {		
						//writer.setPrefix(splitted[0].substring(splitted[0].length()-4,splitted[0].length()-1), splitted[0]);
						writer.writeCharacters(splitted[1]);
					}else {
						writer.writeCharacters(val);
					}
				}
			}
			writer.writeEndElement();
			}
		}
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,String srsName,
			String indvar,String epsg,
			List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat)  {
		ResultFormatter format = resultMap.get("geojson");
		try {
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat));
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
		JSONArray features=geojson.getJSONArray("features");
		for(int i=0;i<features.length();i++) {
			writer.writeStartElement(startingElement);
			JSONObject feature=features.getJSONObject(i);
			String curfeaturetype=feature.getString("id");
			if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
				curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
			}else if(curfeaturetype.startsWith("http")) {
				curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('/')+1);
			}
			//writer.writeStartElement(featuretype);	
			//writer.writeAttribute("gml:id", curfeaturetype);
			addTagsFromJSONObject(feature.getJSONObject("properties"), writer,curfeaturetype);	
			writer.writeStartElement("the_geom");
			writer.writeStartElement("gml:"+feature.getJSONObject("geometry").getString("type"));
			writer.writeAttribute("gml:id",curfeaturetype+"_GEOMETRY");
			writer.writeStartElement("gml:pos");
			writer.writeCharacters(feature.getJSONObject("geometry").getJSONArray("coordinates").toString().replace("[", "").replace("]", "").replace(",", " "));
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
		}
		
		writer.flush();	
		System.out.println(strwriter.toString());
		return strwriter.toString();
		}catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		//System.out.println(strwriter.toString());
		/*String lastInd="";		
		Map<String,String> rel=new TreeMap<String,String>();
		Map<String,String> val=new TreeMap<String,String>();
		String lat="",lon="",rdftype="";
		JSONObject structure=new JSONObject();
		Boolean first=true;
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
			String curfeaturetype="";
			if(solu.get(indvar)!=null) {
				curfeaturetype=solu.get(indvar).toString();
				if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
					curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
				}
				if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
					lastQueriedElemCount++;
					if(!first && !onlyproperty) {
						writer.writeEndElement();    
						writer.writeEndElement();
					}
					if(!onlyproperty) {
						writer.writeStartElement(startingElement);
						System.out.println(WebService.nameSpaceCache);
						System.out.println(featuretype.toLowerCase());
						for(String ns:WebService.nameSpaceCache.get(featuretype.toLowerCase()).keySet()) {
							writer.setPrefix(WebService.nameSpaceCache.get(featuretype.toLowerCase()).get(ns),ns);
						}
						writer.writeStartElement(featuretype);				
						writer.writeAttribute("gml:id", curfeaturetype);
					}
					first=false;
				}
			}
			Iterator<String> iter=solu.varNames();
			while(iter.hasNext()) {
				String curvar=iter.next();
				if(curvar.contains("_geom")) {
					writer.writeStartElement("the_geom");
					if(solu.get(curvar).toString().contains("wkt")) {
						writer.writeStartElement("gml:"+solu.get(curvar).toString().substring(0,solu.get(curvar).toString().indexOf('(')));
						writer.writeAttribute("gml:id",curfeaturetype+"_GEOMETRY");
						//writer.writeAttribute("srsName", srsName);
						writer.writeStartElement("gml:pos");
						writer.writeCharacters(solu.get(curvar).toString().substring(solu.get(curvar).toString().indexOf('(')+1,solu.get(curvar).toString().lastIndexOf(')')));
						writer.writeEndElement();
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}else if(curvar.equalsIgnoreCase(indvar)){
					continue;
				}else if("rel".equalsIgnoreCase(curvar) || curvar.contains("_rel") || curvar.matches("rel[0-9]+$")){
					rel.put(curvar,solu.get(curvar).toString());
				}else if("val".equalsIgnoreCase(curvar) || curvar.contains("_val") || curvar.matches("val[0-9]+$")){
					val.put(curvar,solu.get(curvar).toString());
				}else if("lat".equalsIgnoreCase(curvar)){
					lat=solu.get(curvar).toString();
				}else if("lon".equalsIgnoreCase(curvar)){
					lon=solu.get(curvar).toString();
				}else {
					writer.writeStartElement(curvar);
					writer.writeCharacters(solu.get(curvar).toString());
					writer.writeEndElement();
				}
			}
			if(!rel.isEmpty() && !val.isEmpty()) {
				if(rel.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					rdftype=val;
				}
				if(!rel.equals("http://www.opengis.net/ont/geosparql#hasGeometry") && !rel.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				String[] splitted=splitURL(rel);
				if(splitted!=null) {	
					System.out.println(WebService.nameSpaceCache);
					//writer.setPrefix(splitted[0].substring(splitted[0].length()-4,splitted[0].length()-1), splitted[0]);
					System.out.println(splitted[0]);
					writer.writeStartElement(splitted[0],splitted[1]);
				}else {
					writer.writeStartElement(rel);
				}
				if(val.contains("^^")) {
					writer.writeCharacters(val.substring(0,val.lastIndexOf("^^")));
				}else {
					splitted=splitURL(val);
					if(splitted!=null) {		
						//writer.setPrefix(splitted[0].substring(splitted[0].length()-4,splitted[0].length()-1), splitted[0]);
						writer.writeCharacters(splitted[1]);
					}else {
						writer.writeCharacters(val);
					}
				}		
				writer.writeEndElement();
				}
				rel.clear();
				val.clear();
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
				writer.writeStartElement("gml:Point");
				writer.writeAttribute("gml:id",curfeaturetype+"_GEOMETRY");
					//writer.writeAttribute("srsName", srsName);
				writer.writeStartElement("gml:pos");
				writer.writeCharacters(lon+" "+lat);
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();
				lat="";
				lon="";
			}
			lastInd=solu.get(indvar).toString();
	    }
	    if(!onlyproperty) {
	    	writer.writeEndElement();    
	    	writer.writeEndElement();
	    }
		//writer.writeEndDocument();*/

	}

}
