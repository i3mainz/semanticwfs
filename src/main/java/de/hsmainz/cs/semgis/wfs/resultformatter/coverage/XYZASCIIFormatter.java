package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class XYZASCIIFormatter extends ResultFormatter {

	public XYZASCIIFormatter() {
		this.mimeType="text/xyz";
		this.exposedType="text/xyz";
		this.urlformat="xyz";
		this.label="XYZ ASCII Format";
		this.fileextension="xyz";
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
