package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype) throws XMLStreamException {
		List<QuerySolution> test=ResultSetFormatter.toList(results);
	    JSONArray obj=new JSONArray();
	    for(QuerySolution solu:test) {
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
