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

import de.hsmainz.cs.semgis.importer.connectors.converters.AsGeoJSON;


public class GeoJSONFormatter extends WFSResultFormatter {

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
	    	JSONObject properties=new JSONObject();
	    	List<JSONObject> geoms=new LinkedList<JSONObject>();
	    	int geomvars=0;
	    	while(varnames.hasNext()) {

	    		String name=varnames.next();
	    		if(name.endsWith("_geom")) {
	    			if(first) {
	    			    JSONObject geojsonresult=new JSONObject();
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
	    			//JSONObject geomobj=new JSONObject(val.asNode().getLiteralValue().toString());
	    			geoms.add(new JSONObject(val.asString()));

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
