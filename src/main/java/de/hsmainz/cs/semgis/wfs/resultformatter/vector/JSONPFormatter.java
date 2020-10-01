package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class JSONPFormatter extends ResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public JSONPFormatter() {
		this.mimeType="text/javascript";
		this.exposedType="text/javascript";
		this.urlformat="jsonp";
		this.label="JSONP";
		this.fileextension="js";
		this.definition="http://jsonp.eu";
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, IOException {
		ResultFormatter format = resultMap.get("geojson");
		String res = 
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,
						srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out);
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		return "parseResponse("+res+")";
	}
	
}
