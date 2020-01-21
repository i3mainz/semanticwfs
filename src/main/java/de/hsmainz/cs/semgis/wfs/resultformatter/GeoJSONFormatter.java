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
	public String formatter(ResultSet results, Integer offset, String startingElement, String featuretype) {
		JSONObject geojsonresults = new JSONObject();
		List<JSONArray> allfeatures = new LinkedList<JSONArray>();
		JSONObject result = new JSONObject();
		JSONArray obj = new JSONArray();
		Boolean first = true;
		int i = 0;
		String geomvarname = "";
		String relationName = "";
		String lastgeom = "";
		Integer counter = 0;
		Boolean newobject = true;
		geojsonresults.put("type", "FeatureCollection");
		geojsonresults.put("name", featuretype);
		JSONArray features = new JSONArray();
		allfeatures.add(features);
		geojsonresults.put("features", features);
		String rel = "", val = "", lastInd = "";
		JSONObject jsonobj = new JSONObject();

		JSONObject properties = new JSONObject();
		List<JSONObject> geoms = new LinkedList<JSONObject>();
		while (results.hasNext()) {
			// System.out.println(i);
			if (i < offset) {
				i++;
				continue;
			}
			QuerySolution solu = results.next();
			Iterator<String> varnames = solu.varNames();
			int geomvars = 0;
			//System.out.println(solu.get(featuretype.toLowerCase()).toString() + " - " + lastInd);
			if (!solu.get(featuretype.toLowerCase()).toString().equals(lastInd) || lastInd.isEmpty()) {
				//System.out.println("NEW OBJECT!");
				newobject = true;
			} else {
				newobject = false;
			}
			if (newobject) {
				// System.out.println("Geomvars: "+geomvars);
				int geomcounter = 0;
				//System.out.println("CREATING NEW FEATURE!");
				//System.out.println(geoms);
				for (JSONObject geom : geoms) {
					JSONObject geojsonobj = new JSONObject();
					geojsonobj.put("type", "Feature");
					geojsonobj.put("properties", properties);
					geojsonobj.put("geometry", geom);
					geojsonobj.put("id",lastInd);
					//allfeatures.get(geomcounter).put(geojsonobj);
					features.put(geojsonobj);
					//System.out.println(geojsonobj);
					geomcounter++;
				}
				geomvars=0;
				jsonobj = new JSONObject();
				properties = new JSONObject();
				geoms = new LinkedList<JSONObject>();
			}
			while (varnames.hasNext()) {
				String name = varnames.next();
				// System.out.println(name);
				// if (newobject) {
				if ((name.endsWith("_geom") && solu.get(name) instanceof Literal)
						|| (solu.get(name) instanceof Literal && solu.getLiteral(name) != null
								&& solu.getLiteral(name).getDatatype().toString().contains("wktLiteral"))) {
					// System.out.println("Geomvar: "+name);
					geomvars++;
					geomvarname = name;
					AsGeoJSON geojson = new AsGeoJSON();
					lastgeom = solu.get(name).toString();
					try {
						NodeValue nodeval = geojson.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),
								solu.getLiteral(name).getDatatype()));
						JSONObject geomobj = new JSONObject(nodeval.asNode().getLiteralValue().toString());
						//System.out.println("ADD GEOMETRY!");
						//System.out.println(geomobj);
						geoms.add(geomobj);
					} catch (Exception e) {
						e.printStackTrace();
					}
					properties.put(name, solu.get(name));
				}
				/*
				 * } else { properties.put(name, lastgeom); }
				 */
				if (name.endsWith("_rel") || name.equals("rel")) {
					relationName = solu.get(name).toString();
					rel = solu.get(name).toString();
				} else if (name.endsWith("_val") || name.equals("val")) {
					val = solu.get(name).toString();
				} else {
					if (!relationName.isEmpty()) {
						// System.out.println("Putting property: "+relationName+" - "+solu.get(name));
						properties.put(relationName, solu.get(name));
					} else {
						properties.put(name, solu.get(name));
					}
				}
				// System.out.println(relationName);
				// System.out.println(name);
				// System.out.println(solu.get(name));
				// if(!geojsonout) {
				jsonobj.put(name, solu.get(name));
				// System.out.println(geojsonresults);
				obj.put(jsonobj);
				// }
			}
			if (!rel.isEmpty() && !val.isEmpty()) {
				if(!rel.equals("http://www.opengis.net/ont/geosparql#hasGeometry") &&
						!rel.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					properties.put(rel, val);
				}
				rel = "";
				val = "";
			}
			first = false;
			/*
			 * int geomcounter = 0; for (JSONObject geom : geoms) { JSONObject geojsonobj =
			 * new JSONObject(); geojsonobj.put("type", "Feature");
			 * geojsonobj.put("properties", properties); geojsonobj.put("geometry", geom);
			 * System.out.println(geojsonobj); if(geomvars>0 && !allfeatures.isEmpty()) {
			 * allfeatures.get(geomcounter % geomvars).put(geojsonobj); geomcounter++; } }
			 */
			lastInd = solu.get(featuretype.toLowerCase()).toString();
		}
		if(features.length()==0) {
			int geomcounter=0;
			for (JSONObject geom : geoms) {
				JSONObject geojsonobj = new JSONObject();
				geojsonobj.put("type", "Feature");
				geojsonobj.put("properties", properties);
				geojsonobj.put("geometry", geom);
				geojsonobj.put("id",lastInd);
				//allfeatures.get(geomcounter).put(geojsonobj);
				features.put(geojsonobj);
				System.out.println(geojsonobj);
				geomcounter++;
			}
		}
		System.out.println(obj);
		System.out.println(geojsonresults.toString(2));
		return geojsonresults.toString(2);
	}

}
