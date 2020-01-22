package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public abstract class ResultFormatter {

	public static Map<String,ResultFormatter> resultMap=new TreeMap<String, ResultFormatter>();
	
	public Integer lastQueriedElemCount=0;
	
	public String mimeType="text/plain";
	
	public String exposedType="application/vnd.geo+json";
	
	public static ResultFormatter getFormatter(String formatString) {
		if(resultMap.containsKey(formatString)) {
			return resultMap.get(formatString);
		}
		if(formatString.contains("geojson")) {
			return resultMap.get("geojson");
		}
		if(formatString.contains("gml")) {
			return resultMap.get("gml");
		}
		if(formatString.contains("geojsonld")) {
			return resultMap.get("geojsonld");
		}
		if(formatString.contains("html")) {
			return resultMap.get("html");
		}
		if(formatString.contains("csv")) {
			return resultMap.get("csv");
		}
		if(formatString.contains("gpx")) {
			return resultMap.get("gpx");
		}
		if(formatString.contains("geohash")) {
			return resultMap.get("geohash");
		}
		if(formatString.contains("json")) {
			return resultMap.get("json");
		}
		if(formatString.contains("kml")) {
			return resultMap.get("kml");
		}
		if(formatString.contains("geouri")) {
			return resultMap.get("geouri");
		}
		if(formatString.contains("covjson")) {
			return resultMap.get("covjson");
		}
		if(formatString.contains("geotiff")) {
			return resultMap.get("geotiff");
		}
		if(formatString.contains("jsonld")) {
			return resultMap.get("jsonld");
		}
		if(formatString.contains("topojson")) {
			return resultMap.get("topojson");
		}
		if(formatString.contains("polyshape")) {
			return resultMap.get("polyshape");
		}
		return null;
	}
	
	static {
		//resultMap.put("geotiff", new GeoTIFFFormatter());
		//resultMap.put("covjson", new CovJSONFormatter());
		//resultMap.put("gmlcov", new GMLCOVFormatter());	
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
		//resultMap.put("topojson", new TopoJSONFormatter());
		//resultMap.put("polyshape", new PolyshapeFormatter());
	}
	
	public abstract String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
