package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class HDTFormatter extends ResultFormatter {

	public HDTFormatter() {
		this.urlformat="hdt";
		this.label="Header Dictionary Triples (HDT)";
		this.mimeType="text/hdt";
		this.exposedType="text/hdt";
		this.fileextension="hdt";
		this.definition="https://www.w3.org/Submission/2011/03/";
		this.constructQuery=false;
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException, IOException {
		ResultFormatter format = resultMap.get("ttl");
		String ttl=format.formatter(results,startingElement, featuretype,propertytype, typeColumn, 
				onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out);
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		HDT hdt;
		try {
			File temp=new File("temp.ttl");
			FileWriter writer=new FileWriter(temp);
			writer.write(ttl);
			writer.close();
			hdt = HDTManager.generateHDT("temp.ttl", null, RDFNotation.parse("TURTLE"), new HDTSpecification(),null);
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
			hdt.saveToHDT(bOutput, null);
			 return new String(bOutput.toByteArray(), "UTF-8");
		} catch (IOException | ParserException e1) {
			e1.printStackTrace();
			return "";
		}

	}

}
