package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.WFSResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to GeoJSON-LD.
 *
 */
public class GeoJSONLDFormatter extends WFSResultFormatter {
	
	/**
	 * Constructor for this class.
	 */
	public GeoJSONLDFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/vnd.geo+json+ld";
		this.urlformat="geojsonld";
		this.label="GeoJSON-LD";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		String res = 
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY);
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		JSONObject context=new JSONObject();	  
	    context.put("geojson","https://purl.org/geojson/vocab#");
	    context.put("Feature","geojson:Feature");
	    context.put("FeatureCollection","geojson:FeatureCollection");
	    context.put("GeometryCollection","geojson:GeometryCollection");
	    context.put("LineString","geojson:LineString");
	    context.put("MultiLineString", "geojson:MultiLineString");
	    context.put("MultiPoint","geojson:MultiPoint");
	    context.put("MultiPolygon", "geojson:MultiPolygon");
	    context.put("Point", "geojson:Point");
	    context.put("Polygon", "geojson:Polygon");
	    JSONObject bbox=new JSONObject();
	    context.put("bbox", bbox);
	    bbox.put("@container", "@list");
	    bbox.put("@id","geojson:bbox");
	    JSONObject coordinates=new JSONObject();
	    context.put("coordinates", coordinates);
	    coordinates.put("@container", "@list");
	    coordinates.put("@id", "geojson:coordinates");
	    JSONObject featuresschema=new JSONObject();
	    featuresschema.put("@container", "@set");
	    featuresschema.put("@id", "geojson:features");
	    context.put("geometry","geojson:geometry");
	    context.put("id", "@id");
	    context.put("properties", "geojson:properties");
	    context.put("type", "@type");
	    Map<String,Integer> alreadymatched=new TreeMap<String,Integer>();
	    if(format.contextMapper!=null && !format.contextMapper.keySet().isEmpty()) {
		    for(String key:format.contextMapper.keySet()) {
		    	if(!alreadymatched.containsKey(format.contextMapper.get(key))) {
		    		alreadymatched.put(format.contextMapper.get(key),1);
		    		res=res.replace(key,format.contextMapper.get(key));
		    	}else {
		    		res=res.replace(key+alreadymatched.get(format.contextMapper.get(key))+1,format.contextMapper.get(key));
		    		alreadymatched.put(format.contextMapper.get(key),alreadymatched.get(format.contextMapper.get(key))+1);
		    	}
		    	context.put(format.contextMapper.get(key),key);
		    }
	    }
	    JSONObject geojsonresults=new JSONObject(res);
	    geojsonresults.put("@context",context);
		return geojsonresults.toString(2);
	}

}
