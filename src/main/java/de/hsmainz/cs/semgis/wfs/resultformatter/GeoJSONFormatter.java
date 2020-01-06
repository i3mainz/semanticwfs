package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.NodeValue;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.converters.AsGeoJSON;


public class GeoJSONFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset) {
	    JSONObject geojsonresults=new JSONObject();
	    List<JSONArray> allfeatures=new LinkedList<JSONArray>();
	    JSONObject result=new JSONObject();
	    JSONArray obj=new JSONArray();
    	Boolean first=true;
    	int i=0;
	    while(results.hasNext()) {
	    	if(i<offset) {
	    		i++;
	    		continue;
	    	}
	    	QuerySolution solu=results.next();
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
	    			    geojsonresults.put("type", "FeatureCollection");
	    			    geojsonresults.put("name", name);
	    			    JSONArray features=new JSONArray();
	    			    allfeatures.add(features);

	    			    //geojsonresults.put(geojsonresult);
	    			    geojsonresults.put("features",features);
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
    			try {
    				Literal lit=solu.getLiteral(name);
    				properties.put(name,lit.getString());
    			}catch(Exception e) {
    				properties.put(name,solu.get(name));	
    			}  		
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
	    return geojsonresults.toString(2);
	}

}
