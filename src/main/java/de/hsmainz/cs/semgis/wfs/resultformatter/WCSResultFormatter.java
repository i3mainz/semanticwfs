package de.hsmainz.cs.semgis.wfs.resultformatter;

public abstract class WCSResultFormatter extends ResultFormatter {

	static {
		resultMap.put("geotiff", new GeoTIFFFormatter());
		resultMap.put("covjson", new CovJSONFormatter());
		resultMap.put("gmlcov", new GMLCOVFormatter());	
	}
	
}
