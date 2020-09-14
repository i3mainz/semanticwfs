package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.listener.ProgressListener;

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
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY) throws XMLStreamException {
		ResultFormatter format = resultMap.get("ttl");
		String ttl=format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY);
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		InputStream targetStream = new ByteArrayInputStream(ttl.getBytes());
		HDT hdt;
		try {
			hdt = HDTManager.loadHDT(targetStream, new ProgressListener() {		
				@Override
				public void notifyProgress(float level, String message) {
					// TODO Auto-generated method stub			
				}
			});
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
			hdt.saveToHDT(bOutput, null);
			 return new String(bOutput.toByteArray(), "UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
			return "";
		}

	}

}
