package de.hsmainz.cs.semgis.wfs.resultformatter;

import de.hsmainz.cs.semgis.wfs.resultformatter.vector.CSVFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GPXFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoHashFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONLDFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoURIFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.KMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.PolyshapeFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.TopoJSONFormatter;

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
