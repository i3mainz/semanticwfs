package de.hsmainz.cs.semgis.wfs.resultformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public class TopoJSONFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype) throws XMLStreamException {
		return null;
	}

}
