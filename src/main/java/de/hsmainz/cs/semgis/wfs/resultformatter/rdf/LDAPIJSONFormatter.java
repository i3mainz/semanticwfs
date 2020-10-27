package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class LDAPIJSONFormatter extends ResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public LDAPIJSONFormatter() {
		this.mimeType="text/ldapi";
		this.exposedType="text/ldapi";
		this.urlformat="ldapi";
		this.label="Linked Data API JSON (LDAPI)";
		this.fileextension="json";
		this.definition="https://www.github.com/UKGovLD/linked-data-api/";
		this.constructQuery=false;
	}
	
	/**
	 * Adds a key/value pair to a JSONObject, creates a JSONArray if neccessary.
	 * @param properties the JSONObject
	 * @param rel Relation to add
	 * @param val Value to add
	 */
	public void addKeyVal(JSONObject properties,String rel,String val) {
		if(!this.contextMapper.containsKey(rel)) {
			this.relToMap(rel);
		}
		if(properties.has(rel)) {			
			try {
				properties.getJSONArray(rel).put(val);
			}catch(JSONException e) {
				String oldval=properties.getString(rel);
				properties.put(rel,new JSONArray());
				properties.getJSONArray(rel).put(oldval);
				properties.getJSONArray(rel).put(val);
			}
		}else {
			properties.put(rel, val);
		}
	}
	
	public void writeAsStream(JSONObject properties, JSONObject style,JSONObject geom, JsonGenerator jGenerator) throws IOException {
		for(String key:properties.keySet()) {
			jGenerator.writeStringField(key, properties.get(key).toString());
		}
		jGenerator.writeEndObject();
		if(!style.isEmpty()) {
			jGenerator.writeObjectFieldStart("style");
			for(String key:style.keySet()) {
				jGenerator.writeStringField(key, style.get(key).toString());
			}
			jGenerator.writeEndObject();
		}
		jGenerator.writeObjectFieldStart("geometry");
		jGenerator.writeStringField("type", geom.getString("type"));
		jGenerator.writeFieldName("coordinates");
		jGenerator.writeRaw(":"+geom.getJSONArray("coordinates").toString());
		jGenerator.writeEndObject();
		jGenerator.writeEndObject();
	}
	
	public void relToMap(String keyPath) {
		if(keyPath.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
			return;
		}
		if(!keyPath.contains(";")) {
			if (keyPath.contains("#")) {
				this.contextMapper.put(keyPath,keyPath.substring(keyPath.lastIndexOf('#') + 1));
			} else {
				this.contextMapper.put(keyPath,keyPath.substring(keyPath.lastIndexOf('/') + 1));
			}
		}
		String result="";
		String[] splitted=keyPath.split(";");
		int i=0;
		for(i=0;i<splitted.length;i++) {	
			if (splitted[i].contains("#")) {
				result+=splitted[i].substring(splitted[i].lastIndexOf('#') + 1);
			} else {
				result+=splitted[i].substring(splitted[i].lastIndexOf('/') + 1);
			}
			if(i<splitted.length-1) {
				result+=".";
			}
		}
		this.contextMapper.put(keyPath,result);
	}

	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		JsonFactory jfactory = new JsonFactory();
		JsonGenerator jGenerator = jfactory.createGenerator(out);
		jGenerator.writeStartObject();
		jGenerator.writeStringField("format", "linked-data-api");
		jGenerator.writeStringField("version","0.1");
		jGenerator.writeObjectFieldStart("result");
		jGenerator.writeStringField("_about", featuretype);
		jGenerator.writeStringField("metadata", featuretype);
		jGenerator.writeStringField("partOf", featuretype);
		jGenerator.writeStringField("next", featuretype);
		jGenerator.writeStringField("first", featuretype);
		jGenerator.writeNumberField("page", 0);
		jGenerator.writeNumberField("pageSize", 10);	
		jGenerator.writeArrayFieldStart("contains");
		String indid="";
		List<String> rel=new LinkedList<String>();
		List<String> val=new LinkedList<String>();
		JSONObject properties=new JSONObject();
		while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
	    	if(indid.isEmpty() || !indid.equals(solu.get(indvar).toString())) {
	    		if(!indid.isEmpty()) {
	    			for(String key:properties.keySet()) {
	    				jGenerator.writeStringField(key, properties.get(key).toString());
	    			}
	    			properties=new JSONObject();
	        		jGenerator.writeEndObject();	    			
	    		}
	    		indid=solu.get(indvar).toString();
		    	jGenerator.writeStartObject();	
		    	jGenerator.writeStringField("_about", indid);
	    	}
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		if(name.contains("rel")) {
	    			if("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(name)) {
	    				rel.add("type");
	    			}else if("http://www.w3.org/2000/01/rdf-schema#label".equals(name)) {
	    				rel.add("name");
	    			}
	    			rel.add(solu.get(name).toString());
	    		}else if(name.contains("val")) {
	       			try {
	    				Literal lit=solu.getLiteral(name);
	    				val.add(lit.getString());
	    			}catch(Exception e) {
	    				val.add(solu.get(name).toString());
	    			}  	
	    		}else if(!name.equalsIgnoreCase(indvar)) {
	       			try {
	    				Literal lit=solu.getLiteral(name);
	    				jGenerator.writeStringField(name, lit.getString());
	    			}catch(Exception e) {
	    				jGenerator.writeStringField(name, solu.get(name).toString());
	    			}  	
	    		}
 	
	    	}
	    	if(!rel.isEmpty() && !val.isEmpty()) {
	    		int i=0;
	    		for(;i<rel.size();i++) {
	    			addKeyVal(properties, rel.get(i), val.get(i));	    			
	    		}
	    		rel.clear();
	    		val.clear();
	    	}
			/*if(lastQueriedElemCount%FLUSHTHRESHOLD==0)
				jGenerator.flush();*/
	    }	
		for(String key:properties.keySet()) {
			jGenerator.writeStringField(key, properties.get(key).toString());
		}
		jGenerator.writeEndObject();
		jGenerator.writeEndArray();
		jGenerator.writeEndObject();
		jGenerator.writeEndObject();
		jGenerator.close();
	    return "";
	}

}
