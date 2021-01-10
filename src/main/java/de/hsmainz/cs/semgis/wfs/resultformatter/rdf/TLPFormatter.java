package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class TLPFormatter extends ResultFormatter {

	public TLPFormatter() {
		this.urlformat="tlp";
		this.label="Tulip File Format (TLP)";
		this.mimeType="text/tlp";
		this.exposedType="text/tlp";
		this.fileextension="tlp";
		this.definition="https://tulip.labri.fr/TulipDrupal/?q=tlp-file-format";
		this.constructQuery=false;
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
