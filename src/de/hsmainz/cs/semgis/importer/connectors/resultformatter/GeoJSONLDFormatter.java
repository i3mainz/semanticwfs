package de.hsmainz.cs.semgis.importer.connectors.resultformatter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.expr.NodeValue;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeoJSONLDFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results) {
		List<QuerySolution> test=ResultSetFormatter.toList(rs);
	    JSONArray geojsonresults=new JSONArray();
	    

	    List<JSONArray> allfeatures=new LinkedList<JSONArray>();
	    JSONObject result=new JSONObject();
	    JSONArray obj=new JSONArray();
    	Boolean first=true;
	    for(QuerySolution solu:test) {
	    	JSONObject jsonobj=new JSONObject();
	    	Iterator<String> varnames = solu.varNames();
	    	List<String> varnamesList=new LinkedList<String>();
	    	while(varnames.hasNext()) {
	    		varnamesList.add(varnames.next());
	    	}
	    	JSONObject properties=new JSONObject();
	    	List<JSONObject> geoms=new LinkedList<JSONObject>();
	    	int geomvars=0;
	    	for(String varname:varnamesList) {

	    		String name=varname;
	    		if(name.endsWith("_geom")) {
	    			if(first) {
	    			    JSONObject geojsonresult=new JSONObject();
	    			    JSONObject context=new JSONObject();
	    			    geojsonresult.put("@context",context);
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
	    			    featuresschema*.put("@id", "geojson:features");
	    			    context.put("geometry","geojson:geometry");
	    			    context.put("id", "@id");
	    			    context.put("properties", "geojson:properties");
	    			    context.put("type", "@type");
	    			    context.put("description", "http://purl.org/dc/terms/description");
	    			    context.put("title", "http://purl.org/dc/terms/title");
	    			    for(String varnamee:varnamesList) {
	    			    	context.put(varname, solu.get(name).asNode().getURI());
	    			    }
	    			    geojsonresult.put("type", "FeatureCollection");
	    			    geojsonresult.put("name", name);
	    			    JSONArray features=new JSONArray();
	    			    allfeatures.add(features);

	    			    geojsonresults.put(geojsonresult);
	    			    geojsonresults.getJSONObject(geojsonresults.length()-1).put("features",features);
	    			}
	    			geomvars++;
	    			AsGeoJSON geojson=new AsGeoJSON();
	    			try {
	    			NodeValue val=geojson.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),solu.getLiteral(name).getDatatype()));
	    			JSONObject geomobj=new JSONObject(val.asNode().getLiteralValue().toString());
	    			geoms.add(geomobj);

	    			}catch(Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	    		jsonobj.put(name, solu.get(name));
	    		properties.put(name, solu.get(name));
	    		obj.put(jsonobj);
	    	}
		    first=false;
	    	int geomcounter=0;
	    	for(JSONObject geom:geoms) {
		    	JSONObject geojsonobj=new JSONObject();
		    	geojsonobj.put("type", "Feature");
		    	geojsonobj.put("properties", properties);
		    	geojsonobj.put("geometry", geom);
		    	allfeatures.get(geomcounter%geomvars).put(geojsonobj);  
		    	geomcounter++;
		    }
	    }	    	
	    result.put("geojson", geojsonresults);
	    result.put("data", obj);
	    result.put("size", test.size());
	    return geojsonresults.toString();
	}

}
