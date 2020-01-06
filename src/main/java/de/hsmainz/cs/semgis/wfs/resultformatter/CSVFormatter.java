package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;

public class CSVFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results) {
		List<QuerySolution> test=ResultSetFormatter.toList(results);
		System.out.println("QuerySolutions: "+test.size());
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
	    			try {
	    				Literal lit=solu.getLiteral(name);
	    				resultCSV.append(lit.getString()+",");
	    			}catch(Exception e) {
	    				resultCSV.append(solu.get(name)+",");	
	    			}
	    		}else {
	    			try {
	    				Literal lit=solu.getLiteral(name);
	    				resultCSV.append(lit.getString()+",");
	    			}catch(Exception e) {
	    				resultCSV.append(solu.get(name)+",");	
	    			}  			
	    		}
	    	}
	    	if(first) {
	    		resultCSVHeader.delete(resultCSVHeader.length()-1, resultCSVHeader.length());
		    	resultCSVHeader.append(System.lineSeparator());
	    		first=false;
	    	}else {
	    		resultCSV.delete(resultCSV.length()-1, resultCSV.length());
		    	resultCSV.append(System.lineSeparator());
	    	}
	    }
	    return resultCSVHeader.toString()+resultCSV;
	}

}
