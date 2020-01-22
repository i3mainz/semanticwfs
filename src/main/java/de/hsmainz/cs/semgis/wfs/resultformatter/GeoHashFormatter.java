package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class GeoHashFormatter extends WFSResultFormatter {

	public GeoHashFormatter() {
		this.mimeType="text/plain";
		this.exposedType="application/geohash";
	}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn) throws XMLStreamException {
    	Boolean first=true;
    	StringBuilder resultCSV=new StringBuilder();
    	StringBuilder resultCSVHeader=new StringBuilder();
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
	    	this.lastQueriedElemCount++;
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
