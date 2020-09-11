package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.locationtech.jts.io.WKTReader;

import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.HexTuplesFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.N3Formatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.NQuadsFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.NTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.RDFFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.RDFJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.RDFThriftFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.TTLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.TrigFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.TrixFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.CSVFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GPXFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoHashFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONLDFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONSeqFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoURIFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONLDFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONPFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONSeqFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.KMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.LatLonTextFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.MVTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.MapMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.OSMFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.OSMLinkFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.SVGFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.WKBFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.WKTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.XLSFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.XLSXFormatter;
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
	
	public String label="JSON";
	
	public String fileextension="json";
	
	public Map<String,String> contextMapper=new TreeMap<>();
	
	public ResultStyleFormatter styleformatter;
	
	public WKTReader reader=new WKTReader();
	
	public static Map<String,String> labelMap=new TreeMap<>();
	
	public static final String[] mediatypes= {
			MediaType.TEXT_PLAIN, 
			"application/vnd.geo+json+ld",
			"text/csv",
			
	};
	
	public static ResultFormatter getFormatter(String formatString) {
		formatString=formatString.toLowerCase();
		if(resultMap.containsKey(formatString)) {
			return resultMap.get(formatString);
		}
		if(formatString.contains("geojsonseq")) {
			return resultMap.get("geojsonseq");
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
		if(formatString.contains("osmlink")) {
			return resultMap.get("osmlink");
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
		if(formatString.contains("jsonp")) {
			return resultMap.get("jsonp");
		}
		if(formatString.contains("jsonn")) {
			return resultMap.get("jsonn");
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
		if(formatString.contains("jsonseq")) {
			return resultMap.get("jsonseq");
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
		if(formatString.contains("wkt")) {
			return resultMap.get("wkt");
		}
		if(formatString.contains("mvt")) {
			return resultMap.get("mvt");
		}
		if(formatString.contains("latlon")) {
			return resultMap.get("latlon");
		}
		if(formatString.contains("wkb")) {
			return resultMap.get("wkb");
		}
		if(formatString.contains("geouri")) {
			return resultMap.get("geouri");
		}
		if(formatString.contains("mapml")) {
			return resultMap.get("mapml");
		}
		if(formatString.contains("geohash")) {
			return resultMap.get("geohash");
		}
		if(formatString.contains("geotiff")) {
			return resultMap.get("geotiff");
		}
		if(formatString.contains("xlsx")) {
			return resultMap.get("xlsx");
		}
		if(formatString.contains("xls")) {
			return resultMap.get("xls");
		}
		if(formatString.contains("polyshape")) {
			return resultMap.get("polyshape");
		}
		if(formatString.contains("hextuples")) {
			return resultMap.get("hextuples");
		}
		return resultMap.get("html");
	}
	
	static {
		//resultMap.put("geotiff", new GeoTIFFFormatter());
		//resultMap.put("covjson", new CovJSONFormatter());
		//resultMap.put("gmlcov", new GMLCOVFormatter());
		ResultFormatter format=new GeoJSONFormatter();
		resultMap.put("geojson", format);
		resultMap.put("json", format);
		labelMap.put("geojson",format.label);
		resultMap.put(format.mimeType, format);
		format=new GeoJSONSeqFormatter();
		resultMap.put("geojsonseq", format);
		labelMap.put("geojsonseq",format.label);
		resultMap.put(format.mimeType, format);
		format=new GeoJSONLDFormatter();
		resultMap.put("geojsonld", format);
		labelMap.put("geojsonld",format.label);
		resultMap.put(format.mimeType, format);
		format=new GeoHashFormatter();
		resultMap.put("geohash", format);
		labelMap.put("geohash",format.label);
		resultMap.put(format.mimeType, format);
		format=new GeoURIFormatter();
		resultMap.put("geouri", format);
		labelMap.put("geouri",format.label);
		resultMap.put(format.mimeType, format);
		format=new TrigFormatter();
		resultMap.put("trig", format);
		labelMap.put("trig",format.label);
		resultMap.put(format.mimeType, format);
		format=new TrixFormatter();
		resultMap.put("trix", format);
		labelMap.put("trix",format.label);
		resultMap.put(format.mimeType, format);
		format=new KMLFormatter();
		resultMap.put("kml", format);
		labelMap.put("kml",format.label);
		resultMap.put(format.mimeType, format);
		format=new GMLFormatter();
		resultMap.put("gml", format);
		labelMap.put("gml",format.label);
		resultMap.put(format.mimeType, format);
		format=new N3Formatter();
		resultMap.put("n3", format);
		labelMap.put("n3",format.label);
		resultMap.put(format.mimeType, format);
		format=new NQuadsFormatter();
		resultMap.put("nq", format);
		labelMap.put("nq",format.label);
		resultMap.put(format.mimeType, format);
		format=new NTFormatter();
		resultMap.put("nt", format);
		labelMap.put("nt",format.label);
		resultMap.put(format.mimeType, format);
		format=new RDFThriftFormatter();
		resultMap.put("rt", format);
		labelMap.put("rt",format.label);
		resultMap.put(format.mimeType, format);
		format=new NTFormatter();
		resultMap.put("nt", format);
		labelMap.put("nt",format.label);
		resultMap.put(format.mimeType, format);
		format=new MapMLFormatter();
		resultMap.put("mapml", format);
		labelMap.put("mapml",format.label);
		resultMap.put(format.mimeType, format);
		format=new OSMFormatter();
		resultMap.put("osm", format);
		labelMap.put("osm",format.label);
		resultMap.put(format.mimeType, format);
		format=new WKTFormatter();
		resultMap.put("wkt", format);
		labelMap.put("wkt",format.label);
		resultMap.put(format.mimeType, format);
		format=new WKBFormatter();
		resultMap.put("wkb", format);
		labelMap.put("wkb",format.label);
		resultMap.put(format.mimeType, format);
		format=new GPXFormatter();
		resultMap.put("gpx", format);
		labelMap.put("gpx",format.label);
		resultMap.put(format.mimeType, format);
		format=new RDFFormatter();
		resultMap.put("rdf", format);
		labelMap.put("rdf",format.label);
		resultMap.put(format.mimeType, format);
		format=new RDFJSONFormatter();
		resultMap.put("rdfjson", format);
		labelMap.put("rdfjson",format.label);
		resultMap.put(format.mimeType, format);
		format=new TTLFormatter();
		resultMap.put("ttl", format);
		labelMap.put("ttl",format.label);
		resultMap.put(format.mimeType, format);
		format=new SVGFormatter();
		resultMap.put("svg", format);
		labelMap.put("svg",format.label);
		resultMap.put(format.mimeType, format);
		format=new CSVFormatter();
		resultMap.put("csv", format);
		labelMap.put("csv",format.label);
		resultMap.put(format.mimeType, format);
		format=new JSONSeqFormatter();
		resultMap.put("jsonseq", format);
		labelMap.put("jsonseq",format.label);
		resultMap.put(format.mimeType, format);
		format=new JSONLDFormatter();
		resultMap.put("jsonld", format);
		labelMap.put("jsonld",format.label);
		resultMap.put(format.mimeType, format);
		format=new JSONPFormatter();
		resultMap.put("jsonp", format);
		labelMap.put("jsonp",format.label);
		resultMap.put(format.mimeType, format);
		format=new JSONFormatter();
		resultMap.put("jsonn", format);
		labelMap.put("jsonn",format.label);
		resultMap.put(format.mimeType, format);
		format=new HexTuplesFormatter();
		resultMap.put("hextuples", format);
		labelMap.put("hextuples",format.label);
		resultMap.put(format.mimeType, format);
		format=new LatLonTextFormatter();
		resultMap.put("latlon", format);
		labelMap.put("latlon",format.label);
		resultMap.put(format.mimeType, format);
		format=new MVTFormatter();
		resultMap.put("mvt", format);
		labelMap.put("mvt",format.label);
		resultMap.put(format.mimeType, format);
		format=new OSMLinkFormatter();
		resultMap.put("osmlink", format);
		labelMap.put("osmlink",format.label);
		resultMap.put(format.mimeType, format);
		format=new HTMLFormatter();
		resultMap.put("html", format);
		labelMap.put("html",format.label);
		resultMap.put(format.mimeType, format);
		format=new GeoURIFormatter();
		resultMap.put("geouri", format);
		labelMap.put("geouri",format.label);
		resultMap.put(format.mimeType, format);
		format=new XLSFormatter();
		resultMap.put("xls", format);
		labelMap.put("xls",format.label);
		resultMap.put(format.mimeType, format);
		format=new XLSXFormatter();
		resultMap.put("xlsx", format);
		labelMap.put("xlsx",format.label);
		resultMap.put(format.mimeType, format);
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
