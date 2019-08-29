package de.hsmainz.cs.semgis.importer.connectors.resultformatter;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public abstract class ResultFormatter {

	public final static Map<String,ResultFormatter> resultMap=new TreeMap<>();
	
	static {
		resultMap.put("geojson", new GeoJSONFormatter());
		resultMap.put("geojsonld", new GeoJSONLDFormatter());
		resultMap.put("geohash", new GeoHashFormatter());
		resultMap.put("geouri", new GeoURIFormatter());
		resultMap.put("kml", new KMLFormatter());
		resultMap.put("gml", new GMLFormatter());
		resultMap.put("gpx", new GPXFormatter());
		resultMap.put("csv", new CSVFormatter());
		resultMap.put("topojson", new TopoJSONFormatter());
		resultMap.put("polyshape", new PolyshapeFormatter());		
	}

	public abstract String formatter(ResultSet results) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
