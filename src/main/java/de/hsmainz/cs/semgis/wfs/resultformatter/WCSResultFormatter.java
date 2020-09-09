package de.hsmainz.cs.semgis.wfs.resultformatter;

import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.CovJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.GMLCOVFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.GeoTIFFFormatter;

public abstract class WCSResultFormatter extends ResultFormatter {

	static {
		resultMap.put("geotiff", new GeoTIFFFormatter());
		resultMap.put("covjson", new CovJSONFormatter());
		resultMap.put("gmlcov", new GMLCOVFormatter());	
	}
	
}
