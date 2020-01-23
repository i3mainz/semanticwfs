package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONFormatter extends WFSResultFormatter {

	public JSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/json";
	}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn,Boolean onlyproperty) throws XMLStreamException {
	    JSONArray obj=new JSONArray();
	    while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
	    	JSONObject jsonobj=new JSONObject();
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
    			try {
    				Literal lit=solu.getLiteral(name);
    				jsonobj.put(name,lit.getString());
    			}catch(Exception e) {
    				jsonobj.put(name,solu.get(name));	
    			}  		
	    	}
    		obj.put(jsonobj);
	    }	    	
	    return obj.toString(2);
	}

}
