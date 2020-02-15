package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public class CovJSONFormatter extends WCSResultFormatter {

	public CovJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/covjson";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,String mapstyle) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
