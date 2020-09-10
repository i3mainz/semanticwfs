package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.GeoJSONCSSFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GeoJSONSeqFormatter extends ResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public GeoJSONSeqFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/geo+json-seq";
		this.urlformat="geojsonseq";
		this.label="GeoJSON Sequential";
		this.styleformatter=new GeoJSONCSSFormatter();
		this.fileextension="geojson";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		char record_separator = 0x1e;
		JSONObject geojson=new JSONObject( 
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY));
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		StringBuilder builder=new StringBuilder();
		for(int i=0;i<geojson.getJSONArray("features").length();i++) {
			builder.append(record_separator+geojson.getJSONArray("features").getJSONObject(i).toString()+System.lineSeparator());
		}
		return builder.toString();
	}

	
	

}
