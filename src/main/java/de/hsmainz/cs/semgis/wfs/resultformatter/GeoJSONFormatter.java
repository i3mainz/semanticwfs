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
	public String formatter(ResultSet results, Integer offset,String startingElement,String featuretype) {
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
		while (results.hasNext()) {
			System.out.println(i);
			if (i < offset) {
				i++;
				continue;
			}
			QuerySolution solu = results.next();
			JSONObject jsonobj = new JSONObject();
			Iterator<String> varnames = solu.varNames();
			JSONObject properties = new JSONObject();
			List<JSONObject> geoms = new LinkedList<JSONObject>();
			int geomvars = 0;
			if (!first) {
				if (!geomvarname.isEmpty() && solu.get(geomvarname) != null
						&& solu.get(geomvarname).toString().equals(lastgeom)) {
					newobject = false;
				} else {
					newobject = true;
				}
				if (newobject) {
					// System.out.println("Geomvars: "+geomvars);
					int geomcounter = 0;
					for (JSONObject geom : geoms) {
						JSONObject geojsonobj = new JSONObject();
						geojsonobj.put("type", "Feature");
						geojsonobj.put("properties", properties);
						geojsonobj.put("geometry", geom);
						allfeatures.get(geomcounter % geomvars).put(geojsonobj);
						geomcounter++;
					}
				}
			}
			if (newobject) {
				geomvars = 0;
				jsonobj = new JSONObject();
				properties = new JSONObject();
				geoms = new LinkedList<JSONObject>();
			}
			while (varnames.hasNext()) {
				String name = varnames.next();
				System.out.println(name);
				if (newobject) {
					if (name.endsWith("_geom")) {
						System.out.println("Geomvar: "+name);
						geomvars++;
						geomvarname = name;
						if (first) {
							JSONObject geojsonresult = new JSONObject();
							geojsonresults.put("type", "FeatureCollection");
							geojsonresults.put("name", name);
							JSONArray features = new JSONArray();
							allfeatures.add(features);
							geojsonresults.put("features", features);
						}
						AsGeoJSON geojson = new AsGeoJSON();
						lastgeom = solu.get(name).toString();
						try {
							NodeValue val = geojson.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),
									solu.getLiteral(name).getDatatype()));
							JSONObject geomobj = new JSONObject(val.asNode().getLiteralValue().toString());
							geoms.add(geomobj);
						} catch (Exception e) {
							e.printStackTrace();
						}
						properties.put(name, solu.get(name));
					}
				} else {
					properties.put(name, lastgeom);
				}
				if (name.endsWith("_rel") || name.equals("rel")) {
					relationName = solu.get(name).toString();
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
				System.out.println(geojsonresults);
				obj.put(jsonobj);
				// }
			}
			first = false;
			int geomcounter = 0;
			for (JSONObject geom : geoms) {
				JSONObject geojsonobj = new JSONObject();
				geojsonobj.put("type", "Feature");
				geojsonobj.put("properties", properties);
				geojsonobj.put("geometry", geom);
				System.out.println(geojsonobj);
				if(geomvars>0 && !allfeatures.isEmpty()) {
					allfeatures.get(geomcounter % geomvars).put(geojsonobj);
					geomcounter++;
				}
			}
		}
		System.out.println(obj);
		System.out.println(geojsonresults);
		return geojsonresults.toString(2);
	}

}
