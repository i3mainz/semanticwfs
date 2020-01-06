package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class GeoHashFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset) throws XMLStreamException {
		List<QuerySolution> test=ResultSetFormatter.toList(results);
    	Boolean first=true;
    	StringBuilder resultCSV=new StringBuilder();
    	StringBuilder resultCSVHeader=new StringBuilder();
	    for(QuerySolution solu:test) {
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		if(first) {
	    		    resultCSVHeader.append(name+",");
	    		}
	    		if(name.endsWith("_geom")) {
	    			
	    		}else {
		    		resultCSV.append(solu.get(name));	    			
	    		}
	    	}
	    	resultCSV.append(System.lineSeparator());
	    	if(first) {
	    		resultCSVHeader.delete(resultCSVHeader.length()-1, resultCSVHeader.length());
	    	}      	
	    }
	    return resultCSVHeader.toString()+resultCSV;
	}

}
