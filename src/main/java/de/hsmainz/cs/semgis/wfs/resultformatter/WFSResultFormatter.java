package de.hsmainz.cs.semgis.wfs.resultformatter;

public abstract class WFSResultFormatter extends ResultFormatter{
	
	String featureType="";
	
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

}
