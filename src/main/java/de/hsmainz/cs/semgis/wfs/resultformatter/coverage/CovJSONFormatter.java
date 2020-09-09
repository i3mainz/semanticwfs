package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.WCSResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class CovJSONFormatter extends WCSResultFormatter {

	public CovJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/covjson";
		this.urlformat="covjson";
		this.label="CoverageJSON";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
