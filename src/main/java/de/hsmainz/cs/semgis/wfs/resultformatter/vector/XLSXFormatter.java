package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class XLSXFormatter extends ResultFormatter {


	public XLSXFormatter() {
		this.urlformat="xls";
		this.label="MS Excel (XLSX)";
		this.mimeType="application/msexcel";
		this.exposedType="application/msexcel";
		this.fileextension="xlsx";
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
