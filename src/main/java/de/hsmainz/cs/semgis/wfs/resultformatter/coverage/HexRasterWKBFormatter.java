package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.geotoolkit.coverage.wkb.WKBRasterWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class HexRasterWKBFormatter extends CoverageResultFormatter {

	WKBRasterWriter wkbrastwriter=new WKBRasterWriter();
	
	public HexRasterWKBFormatter() {
		this.mimeType="application/hexrasterwkb";
		this.exposedType="application/hexrasterwkb";
		this.urlformat="hexrastwkb";
		this.label="Hexadecimal RasterWKB (WKB)";
		this.fileextension="wkb";
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
