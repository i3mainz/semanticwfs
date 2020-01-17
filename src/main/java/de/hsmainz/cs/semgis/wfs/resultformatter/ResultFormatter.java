package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public abstract class ResultFormatter {

	public static Map<String,ResultFormatter> resultMap=new TreeMap<String, ResultFormatter>();
	
	static {
		resultMap.put("geotiff", new GeoTIFFFormatter());
		resultMap.put("covjson", new CovJSONFormatter());
		resultMap.put("gmlcov", new GMLCOVFormatter());	
		resultMap.put("geojson", new GeoJSONFormatter());
		resultMap.put("geojsonld", new GeoJSONLDFormatter());
		resultMap.put("geohash", new GeoHashFormatter());
		resultMap.put("geouri", new GeoURIFormatter());
		resultMap.put("json", new JSONFormatter());
		resultMap.put("jsonld", new JSONLDFormatter());
		resultMap.put("html", new HTMLFormatter());
		resultMap.put("kml", new KMLFormatter());
		resultMap.put("gml", new GMLFormatter());
		resultMap.put("gpx", new GPXFormatter());
		resultMap.put("csv", new CSVFormatter());
		resultMap.put("topojson", new TopoJSONFormatter());
		resultMap.put("polyshape", new PolyshapeFormatter());
	}
	
	public abstract String formatter(ResultSet results,Integer offset,String startingElement) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
