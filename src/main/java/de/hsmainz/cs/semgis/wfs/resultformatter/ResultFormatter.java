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
		formatString=formatString.toLowerCase();
		if(resultMap.containsKey(formatString)) {
			return resultMap.get(formatString);
		}
		formatString=formatString.replace("+","");
		if(formatString.contains("geojsonld") || formatString.contains("geo+json+ld")) {
			return resultMap.get("geojsonld");
		}
		if(formatString.contains("geojson") || formatString.contains("geo+json")) {
			return resultMap.get("geojson");
		}
		if(formatString.contains("gml")) {
			return resultMap.get("gml");
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
		if(formatString.contains("ttl")) {
			return resultMap.get("ttl");
		}
		if(formatString.contains("geohash")) {
			return resultMap.get("geohash");
		}
		if(formatString.contains("topojson")) {
			return resultMap.get("topojson");
		}
		if(formatString.contains("covjson")) {
			return resultMap.get("covjson");
		}
		if(formatString.contains("jsonld")) {
			return resultMap.get("jsonld");
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
		if(formatString.contains("geotiff")) {
			return resultMap.get("geotiff");
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
		resultMap.put("ttl", new RDFFormatter());
		resultMap.put("gml", new GMLFormatter());
		resultMap.put("gpx", new GPXFormatter());
		resultMap.put("csv", new CSVFormatter());
		//resultMap.put("topojson", new TopoJSONFormatter());
		//resultMap.put("polyshape", new PolyshapeFormatter());
	}
	
	/*public convertCoordinates() {
		ProjCoordinate coord = new ProjCoordinate(5439627.33, 5661628.09);
        System.out.println(coord);

        ProjCoordinate target = new ProjCoordinate();
        CRSFactory crsFactory = new CRSFactory();
        CoordinateTransformFactory f = new CoordinateTransformFactory();
        CoordinateTransform t;
	}*/
	
	public abstract String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,
			Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
