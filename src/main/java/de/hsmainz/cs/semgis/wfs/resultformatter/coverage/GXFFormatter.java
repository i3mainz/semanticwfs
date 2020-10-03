package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GXFFormatter extends CoverageResultFormatter {

	public GXFFormatter() {
		this.mimeType="application/gxf";
		this.exposedType="application/gxf";
		this.urlformat="gxf";
		this.label="Grid Exchange Format (GXF)";
		this.fileextension="gxf";
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
