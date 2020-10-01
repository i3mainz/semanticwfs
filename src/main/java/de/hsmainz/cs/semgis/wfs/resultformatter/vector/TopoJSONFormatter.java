package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to TopoJSON.
 *
 */
public class TopoJSONFormatter extends VectorResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public TopoJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/topojson";
		this.urlformat="topojson";
		this.label="TopoJSON";
		this.fileextension="topojson";
		this.definition="https://github.com/topojson/topojson";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,
			StyleObject mapstyle,Boolean alternativeFormat,
			Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException {
		return null;
	}

}
