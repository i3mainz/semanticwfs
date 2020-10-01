package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class JSONSeqFormatter extends ResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public JSONSeqFormatter() {
		this.mimeType="application/json-seq";
		this.exposedType="application/json-seq";
		this.urlformat="jsonseq";
		this.label="JSON Sequential";
		this.fileextension="json";
		this.definition="https://tools.ietf.org/html/rfc7464";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, IOException {
		char record_separator = 0x1e;
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
	    	out.write(record_separator+jsonobj.toString()+System.lineSeparator());
	    }	    	
		return "";
	}

}
