package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.wololo.jts2geojson.GeoJSONReader;

import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.CovJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.XYZASCIIFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.CypherFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.HDTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.HexTuplesFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.LDAPIJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.N3Formatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.NQuadsFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.NTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.RDFEXIFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.RDFFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.RDFJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.RDFThriftFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.TTLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.TrigFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.rdf.TrixFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.BSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.CSVFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.EWKTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GPXFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoHashFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONLDFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoJSONSeqFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.GeoURIFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.HexWKBFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONLDFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONPFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.JSONSeqFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.KMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.LatLonTextFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.MVTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.MapMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.ODSFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.OSMFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.OSMLinkFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.OpenLocationCodeFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.PostgreSQLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.SVGFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.TWKBFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.WKBFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.WKTFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.XLSFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.XLSXFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.vector.YAMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.ResultStyleFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.util.ReprojectionUtils;

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
	
	public String definition="";
	
	public String fileextension="json";
	
	public Boolean constructQuery=false;
	
	public Map<String,String> contextMapper=new TreeMap<>();
	
	public ResultStyleFormatter styleformatter;
	
	public WKTReader wktreader=new WKTReader();
	
	public WKBReader wkbreader=new WKBReader();
	
	public GeoJSONReader geojsonreader=new GeoJSONReader();
	
	public static Map<String,String> labelMap=new TreeMap<>();
	
	public static Set<String> vectorLiteralMap=new TreeSet<>();
	
	public static Set<String> coverageLiteralMap=new TreeSet<>();
	
	public static final Integer FLUSHTHRESHOLD=20;

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
		if(formatString.contains("ldapi")) {
			return resultMap.get("ldapi");
		}
		if(formatString.contains("gpx")) {
			return resultMap.get("gpx");
		}
		if(formatString.contains("svg")) {
			return resultMap.get("svg");
		}
		if(formatString.contains("cypher")) {
			return resultMap.get("cypher");
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
		if(formatString.contains("olc")) {
			return resultMap.get("olc");
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
		if(formatString.contains("hdt")) {
			return resultMap.get("hdt");
		}
		if(formatString.contains("ewkt")) {
			return resultMap.get("ewkt");
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
		if(formatString.contains("xyz")) {
			return resultMap.get("xyz");
		}
		if(formatString.contains("covjson")) {
			return resultMap.get("covjson");
		}
		if(formatString.contains("polyshape")) {
			return resultMap.get("polyshape");
		}
		if(formatString.contains("hextuples")) {
			return resultMap.get("hextuples");
		}
		return resultMap.get("html");
	}
	
	static void addToMaps(String key,ResultFormatter format) {
		resultMap.put(key, format);
		labelMap.put(key,format.label);
		resultMap.put(format.mimeType, format);
	}
	
	public String formatter(Model results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		return null;
	}
	
	static {
		vectorLiteralMap.add("http://www.opengis.net/ont/geosparql#wktLiteral");
		vectorLiteralMap.add("http://www.opengis.net/ont/geosparql#geoJSONLiteral");
		vectorLiteralMap.add("http://www.opengis.net/ont/geosparql#wkbLiteral");
		vectorLiteralMap.add("http://www.opengis.net/ont/geosparql#gmlLiteral");
		coverageLiteralMap.add("http://www.opengis.net/ont/geosparql#ascLiteral");
		coverageLiteralMap.add("http://www.opengis.net/ont/geosparql#covJSONLiteral");
		coverageLiteralMap.add("http://www.opengis.net/ont/geosparql#rastwkbLiteral");
		coverageLiteralMap.add("http://www.opengis.net/ont/geosparql#xyzLiteral");
		ResultFormatter format=new GeoJSONFormatter();
		addToMaps("geojson", format);
		addToMaps("json", format);
		addToMaps("geojsonseq",new GeoJSONSeqFormatter());
		addToMaps("geojsonld",new GeoJSONLDFormatter());
		addToMaps("geohash", new GeoHashFormatter());
		addToMaps("geouri", new GeoURIFormatter());
		addToMaps("trig", new TrigFormatter());
		addToMaps("trix", new TrixFormatter());
		addToMaps("cypher", new CypherFormatter());
		addToMaps("kml", new KMLFormatter());
		addToMaps("gml", new GMLFormatter());
		addToMaps("ldapi", new LDAPIJSONFormatter());
		addToMaps("n3", new N3Formatter());
		addToMaps("nq", new NQuadsFormatter());
		addToMaps("nt", new NTFormatter());
		addToMaps("olc", new OpenLocationCodeFormatter());
		addToMaps("rt", new RDFThriftFormatter());
		addToMaps("mapml", new MapMLFormatter());
		addToMaps("osm", new OSMFormatter());
		addToMaps("wkt", new WKTFormatter());
		addToMaps("ewkt", new EWKTFormatter());
		addToMaps("hexwkb", new HexWKBFormatter());
		addToMaps("wkb", new WKBFormatter());
		addToMaps("twkb", new TWKBFormatter());
		addToMaps("gpx", new GPXFormatter());
		addToMaps("psql", new PostgreSQLFormatter());
		addToMaps("rdf", new RDFFormatter());
		addToMaps("hdt", new HDTFormatter());
		addToMaps("rdfjson", new RDFJSONFormatter());
		addToMaps("rdfexi", new RDFEXIFormatter());
		addToMaps("ttl", new TTLFormatter());
		addToMaps("svg", new SVGFormatter());
		addToMaps("csv", new CSVFormatter());
		addToMaps("jsonseq", new JSONSeqFormatter());
		addToMaps("jsonld", new JSONLDFormatter());
		addToMaps("jsonp", new JSONPFormatter());
		addToMaps("jsonn", new JSONFormatter());
		addToMaps("hextuples", new HexTuplesFormatter());
		addToMaps("latlon", new LatLonTextFormatter());
		addToMaps("mvt", new MVTFormatter());
		addToMaps("osmlink", new OSMLinkFormatter());
		addToMaps("html", new HTMLFormatter());
		addToMaps("xls", new XLSFormatter());
		addToMaps("xlsx", new XLSXFormatter());
		addToMaps("xyz", new XYZASCIIFormatter());
		addToMaps("covjson", new CovJSONFormatter());
		addToMaps("yaml", new YAMLFormatter());
		addToMaps("bson", new BSONFormatter());
		addToMaps("ods", new ODSFormatter());
	}
	
	public Geometry parseVectorLiteral(String literalValue, String literalType, String epsg, String srsName) {
		Geometry geom=null;
		if(literalType.toLowerCase().contains("wkt")) {
			try {
				geom=this.wktreader.read(literalValue);
			} catch (ParseException e) {
				return null;
			}
		}
		else if(literalType.toLowerCase().contains("geojson")) {
			geom=this.geojsonreader.read(literalValue);
		}
		else if(literalType.toLowerCase().contains("wkb")) {
			try {
				geom=this.wkbreader.read(WKBReader.hexToBytes(literalValue));
			} catch (ParseException e) {
				return null;
			}
		}
		if(geom!=null) {
			geom=ReprojectionUtils.reproject(geom, epsg,srsName);
			return geom;
		}
		return null;
	}
	public String parseCoverageLiteral(String literalValue, String literalType,String epsg, String srsName) {
		if(literalType.contains("wkb")) {
			
		}else if(literalType.contains("covjson")) {
			
		}else if(literalType.contains("xyz")) {
			return literalValue;
		}
		return null;
	}
	
	public Object parseLiteral(String literalValue, String literalType, String epsg, String srsName) {
		Geometry geom=parseVectorLiteral(literalValue, literalType, epsg, srsName);
		if(geom!=null) {
			return geom;
		}
		String cov=parseCoverageLiteral(literalValue, literalType, epsg, srsName);
		if(cov!=null) {
			return cov;
		}
		return null;
	}
	
	public abstract String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,
			Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,
			String epsg,List<String> eligiblenamespaces,List<String> noteligiblenamespaces,
			StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, IOException;

}
