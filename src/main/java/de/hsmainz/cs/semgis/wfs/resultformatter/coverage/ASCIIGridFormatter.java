package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class ASCIIGridFormatter extends ResultFormatter {

	public ASCIIGridFormatter() {
		this.mimeType="text/asc";
		this.exposedType="text/asc";
		this.urlformat="asc";
		this.label="ARC/INFO ASCII GRID Format (ASC)";
		this.fileextension="asc";
		this.definition="https://gdal.org/drivers/raster/aaigrid.html";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
