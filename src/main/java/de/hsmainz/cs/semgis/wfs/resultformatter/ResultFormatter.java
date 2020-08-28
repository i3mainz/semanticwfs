package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.ResultStyleFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Abstract class to downlift query results.
 *
 */
public abstract class ResultFormatter {

	public static Map<String,ResultFormatter> resultMap=new TreeMap<String, ResultFormatter>();
	
	public Integer lastQueriedElemCount=0;
	
	public String mimeType="text/plain";
	
	public String exposedType="application/vnd.geojson";
	
	public String urlformat="json";
	
	public Map<String,String> contextMapper=new TreeMap<>();
	
	public ResultStyleFormatter styleformatter;
	
	public static ResultFormatter getFormatter(String formatString) {
		formatString=formatString.toLowerCase();
		if(resultMap.containsKey(formatString)) {
			return resultMap.get(formatString);
		}
		if(formatString.equals("application/vnd.geo+json+ld") || formatString.contains("geojsonld") || formatString.contains("geo+json+ld")) {
			return resultMap.get("geojsonld");
		}
		if(formatString.equals("application/geojson") || formatString.contains("geojson") || formatString.contains("geo+json")) {
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
		if(formatString.contains("svg")) {
			return resultMap.get("svg");
		}
		if(formatString.contains("osm")) {
			return resultMap.get("osm");
		}
		if(formatString.contains("ttl")) {
			return resultMap.get("ttl");
		}
		if(formatString.contains("rdfjson")) {
			return resultMap.get("rdfjson");
		}
		if(formatString.contains("rdf")) {
			return resultMap.get("rdf");
		}
		if(formatString.contains("n3")) {
			return resultMap.get("n3");
		}
		if(formatString.contains("nt")) {
			return resultMap.get("nt");
		}
		if(formatString.contains("rt")) {
			return resultMap.get("rt");
		}
		if(formatString.contains("nq")) {
			return resultMap.get("nq");
		}
		if(formatString.contains("trig")) {
			return resultMap.get("trig");
		}
		if(formatString.contains("trix")) {
			return resultMap.get("trix");
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
			return resultMap.get("geojson");
		}
		if(formatString.contains("kml")) {
			return resultMap.get("kml");
		}
		if(formatString.contains("geouri")) {
			return resultMap.get("geouri");
		}
		if(formatString.contains("geohash")) {
			return resultMap.get("geohash");
		}
		if(formatString.contains("geotiff")) {
			return resultMap.get("geotiff");
		}
		if(formatString.contains("polyshape")) {
			return resultMap.get("polyshape");
		}
		return resultMap.get("html");
	}
	
	static {
		//resultMap.put("geotiff", new GeoTIFFFormatter());
		//resultMap.put("covjson", new CovJSONFormatter());
		//resultMap.put("gmlcov", new GMLCOVFormatter());	
		resultMap.put("geojson", new GeoJSONFormatter());
		resultMap.put("geojsonld", new GeoJSONLDFormatter());
		resultMap.put("geohash", new GeoHashFormatter());
		resultMap.put("geouri", new GeoURIFormatter());
		resultMap.put("json", new GeoJSONFormatter());
		resultMap.put("jsonld", new JSONLDFormatter());
		resultMap.put("html", new HTMLFormatter());
		resultMap.put("trig", new TrigFormatter());
		resultMap.put("trix", new TrixFormatter());
		resultMap.put("kml", new KMLFormatter());
		resultMap.put("ttl", new TTLFormatter());
		resultMap.put("n3", new N3Formatter());
		resultMap.put("nt", new NTFormatter());
		resultMap.put("nq", new NQuadsFormatter());
		resultMap.put("rt", new RDFThriftFormatter());
		resultMap.put("osm", new OSMFormatter());
		resultMap.put("rdf", new RDFFormatter());
		resultMap.put("rdfjson", new RDFJSONFormatter());
		resultMap.put("gml", new GMLFormatter());
		resultMap.put("svg", new SVGFormatter());
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
			Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,
			String epsg,List<String> eligiblenamespaces,List<String> noteligiblenamespaces,
			StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
