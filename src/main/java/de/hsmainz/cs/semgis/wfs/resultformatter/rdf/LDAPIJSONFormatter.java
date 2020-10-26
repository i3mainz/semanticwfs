package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

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
		while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
	    	jGenerator.writeStartObject();
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
    			try {
    				Literal lit=solu.getLiteral(name);
    				jGenerator.writeStringField(name, lit.getString());
    			}catch(Exception e) {
    				jGenerator.writeStringField(name, solu.get(name).toString());
    			}  		
	    	}
    		jGenerator.writeEndObject();
			/*if(lastQueriedElemCount%FLUSHTHRESHOLD==0)
				jGenerator.flush();*/
	    }		
		jGenerator.writeEndArray();
		jGenerator.writeEndObject();
		jGenerator.writeEndObject();
		jGenerator.close();
	    return "";
	}

}
