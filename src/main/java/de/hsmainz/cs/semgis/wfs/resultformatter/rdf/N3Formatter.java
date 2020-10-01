package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to N3.
 */
public class N3Formatter extends ResultFormatter{

	/**
	 * Constructor for this class.
	 */
	public N3Formatter() {
		this.mimeType="text/n3";
		this.exposedType="text/n3";
		this.urlformat="n3";
		this.label="Notation3 (N3)";
		this.fileextension="n3";
		this.definition="https://www.w3.org/TeamSubmission/n3/";
		this.constructQuery=true;
	}
	
	@Override
	public String formatter(Model results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		results.write(out,"N3");
		out.flush();
		return "";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, 
			StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY, Boolean coverage,Writer out)
			throws XMLStreamException, IOException {
		ResultFormatter format = resultMap.get("ttl");
		String ttl=format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,
				onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out);
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		OntModel model=ModelFactory.createOntologyModel();
		InputStream result = new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8));
		model.read(result, null, "TTL");
		if(out!=null) {
			model.write(out,"N3");
			return "";
		}else {
			System.out.println("RDF Formatter!!!!");
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
			model.write(bOutput,"N3");
			try {
				return new String(bOutput.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return "";
			}	
		}
		
	}

}
