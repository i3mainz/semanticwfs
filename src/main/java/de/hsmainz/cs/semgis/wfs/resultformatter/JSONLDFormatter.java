package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONLDFormatter extends WFSResultFormatter {

	public JSONLDFormatter() {
		this.mimeType="application/jsonld";
		this.exposedType="application/jsonld";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,String epsg,List<String> eligiblenamespaces,List<String> noteligiblenamespaces) throws XMLStreamException {
	    JSONArray obj=new JSONArray();
	    JSONObject context=new JSONObject();
	    Boolean first=true;
    	List<String> varnamesList=new LinkedList<String>();
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
	    	this.lastQueriedElemCount++;
	    	JSONObject jsonobj=new JSONObject();
		    jsonobj.put("@context",context);
		    if(first) {
		    	Iterator<String> varnames = solu.varNames();
		    	while(varnames.hasNext()) {
		    		varnamesList.add(varnames.next());
		    	}
		    	for(String varnamee:varnamesList) {
		    		try {
		    			Literal lit=solu.getLiteral(varnamee);
		    			context.put(varnamee, lit.getDatatypeURI());
		    		}catch(Exception e) {
		    			context.put(varnamee,solu.get(varnamee));	
		    		}  		
		    	}
		    	first=false;
	    	}
	    	for(String name:varnamesList) {
	    		jsonobj.put("@context", context);
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
