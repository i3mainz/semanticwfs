package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

/**
 * Abstract class to describe a class applying a style to a downlifted dataset.
 *
 */
public abstract class ResultStyleFormatter {

	public static Map<String,ResultStyleFormatter> resultMap=new TreeMap<String, ResultStyleFormatter>();
	
	public Integer lastQueriedElemCount=0;
	
	public String mimeType="text/plain";
	
	public String exposedType="application/vnd.geo+json";
	
	public String styleAttribute="";

	/**
	 * Gets a style formatter for a chosen style type.
	 * @param formatString the formatString indicating a given style type
	 * @return a ResultStyleFormatter generating the chosen style type
	 */
	public static ResultStyleFormatter getFormatter(String formatString) {
		formatString=formatString.toLowerCase();
		if(resultMap.containsKey(formatString)) {
			return resultMap.get(formatString);
		}
		formatString=formatString.replace("+","");
		if(formatString.contains("mapcss")) {
			return resultMap.get("mapcss");
		}
		if(formatString.contains("geojsoncss")) {
			return resultMap.get("geojsoncss");
		}
		if(formatString.contains("sld")) {
			return resultMap.get("sld");
		}
		return null;
	}
	
	static { 
		resultMap.put("geojsoncss", new GeoJSONCSSFormatter());
		resultMap.put("mapcss", new MapCSSFormatter());
		resultMap.put("sld", new SLDFormatter());
	}
	
	/*public convertCoordinates() {
		ProjCoordinate coord = new ProjCoordinate(5439627.33, 5661628.09);
        System.out.println(coord);

        ProjCoordinate target = new ProjCoordinate();
        CRSFactory crsFactory = new CRSFactory();
        CoordinateTransformFactory f = new CoordinateTransformFactory();
        CoordinateTransform t;
	}*/
	
	/**
	 * 
	 * @param results
	 * @param featuretype
	 * @return
	 * @throws XMLStreamException
	 */
	public abstract String formatter(ResultSet results,String featuretype) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}

	/**
	 * Formats a geometry according to a given geometrytype using information from a given styleobj.
	 * @param geometrytype the geometry
	 * @param styleobj the styleobject to use for formatting
	 * @return the formatString to use for styling
	 */
	public abstract String formatGeometry(String geometrytype, StyleObject styleobj);
	
}
