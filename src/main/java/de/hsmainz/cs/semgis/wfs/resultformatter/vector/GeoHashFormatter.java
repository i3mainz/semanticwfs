package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GeoHashFormatter extends VectorResultFormatter {

	public GeoHashFormatter() {
		this.mimeType="text/plain";
		this.exposedType="application/geohash";
		this.urlformat="geohash";
		this.label="GeoHash";
		this.fileextension="txt";
		this.definition="http://geohash.org";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,
			Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,
			String epsg,List<String> eligiblenamespaces,List<String> noteligiblenamespace
			,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException {
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
