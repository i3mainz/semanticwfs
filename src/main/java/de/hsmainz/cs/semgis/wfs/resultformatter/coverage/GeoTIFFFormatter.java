package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GeoTIFFFormatter extends CoverageResultFormatter {

	public GeoTIFFFormatter() {
		this.mimeType="image/tiff";
		this.exposedType="image/tiff";
		this.urlformat="geotiff";
		this.label="GeoTIFF";
		this.fileextension="tiff";
		this.definition="https://www.ogc.org/standards/geotiff";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,
			Boolean onlyproperty,Boolean onlyhits,String srsName,
			String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException {
		
		// TODO Auto-generated method stub
		return null;
	}

}
