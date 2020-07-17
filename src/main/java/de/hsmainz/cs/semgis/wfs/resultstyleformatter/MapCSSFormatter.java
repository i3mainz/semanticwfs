package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

/**
 * Reads an ontological style description and formats it to OSM Map CSS.
 *
 */
public class MapCSSFormatter extends ResultStyleFormatter {

	@Override
	public String formatter(ResultSet results,String featuretype) throws XMLStreamException {
		return null;
	}

	
	@Override
	public String formatGeometry(String geometrytype, StyleObject styleobj) {
		StringBuilder builder=new StringBuilder();
		builder.append("{}");
		return builder.toString();
	}

}
