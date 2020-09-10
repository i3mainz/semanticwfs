package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class HDTFormatter extends ResultFormatter {

	public HDTFormatter() {
		this.urlformat="hdt";
		this.label="Header Dictionary Triples (HDT)";
		this.mimeType="text/hdt";
		this.exposedType="text/hdt";
		this.fileextension="hdt";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
