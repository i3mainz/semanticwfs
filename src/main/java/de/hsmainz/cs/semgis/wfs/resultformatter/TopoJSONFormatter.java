package de.hsmainz.cs.semgis.wfs.resultformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public class TopoJSONFormatter extends WFSResultFormatter {

	public TopoJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/topojson";
	}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn,Boolean onlyproperty) throws XMLStreamException {
		return null;
	}

}
