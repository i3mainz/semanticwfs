package de.hsmainz.cs.semgis.wfs.resultformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public class GeoTIFFFormatter extends WCSResultFormatter {

	public GeoTIFFFormatter() {
		this.mimeType="image/tiff";
		this.exposedType="image/tiff";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,Boolean onlyproperty,Boolean onlyhits) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
