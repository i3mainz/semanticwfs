package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONLDFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype) throws XMLStreamException {
		List<QuerySolution> test=ResultSetFormatter.toList(results);
		JSONObject result=new JSONObject();
	    JSONArray obj=new JSONArray();
	    JSONObject context=new JSONObject();
	    Boolean first=true;
    	List<String> varnamesList=new LinkedList<String>();
	    for(QuerySolution solu:test) {
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
