package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

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

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to JSON.
 */
public class JSONFormatter extends VectorResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public JSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/json";
		this.urlformat="jsonn";
		this.label="JSON";
		this.fileextension="json";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, IOException {
		JsonFactory jfactory = new JsonFactory();
		JsonGenerator jGenerator = jfactory.createGenerator(out);
		jGenerator.writeStartObject();
		jGenerator.writeNumberField("amount", this.lastQueriedElemCount);
		jGenerator.writeArrayFieldStart("features");
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
		jGenerator.close();
	    return "";
	}

}
