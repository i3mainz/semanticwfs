package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GMLCOVFormatter extends WCSResultFormatter {

	public GMLCOVFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/gmlcov";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,
			Boolean onlyproperty,Boolean onlyhits,String srsName,
			String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
