package de.hsmainz.cs.semgis.importer.connectors.resultformatter;

import java.util.Map;
import java.util.TreeMap;
import org.apache.jena.query.ResultSet;

public abstract class ResultFormatter {

	public final static Map<String,ResultFormatter> resultMap=new TreeMap<>();
	
	static {
		resultMap.put("geojson", new GeoJSONFormatter());	
	}

	public abstract String formatter(ResultSet results);

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
