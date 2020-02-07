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

	public GeoJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/geo+json";
	}
	
	private AsGeoJSON geojson = new AsGeoJSON();
	
	@Override
	public String formatter(ResultSet results, String startingElement, 
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,String epsg) {
		lastQueriedElemCount=0;
		JSONObject geojsonresults = new JSONObject();
		List<JSONArray> allfeatures = new LinkedList<JSONArray>();
		JSONObject result = new JSONObject();
		JSONArray obj = new JSONArray();
		Boolean first = true;
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
		List<String> latlist=new LinkedList<String>();
		List<String> lonlist=new LinkedList<String>();
		String rel = "", val = "", lastInd = "",lat="",lon="";
		JSONObject jsonobj = new JSONObject();

		JSONObject properties = new JSONObject();
		List<JSONObject> geoms = new LinkedList<JSONObject>();
		while (results.hasNext()) {
			// System.out.println(i);
			QuerySolution solu = results.next();
			Iterator<String> varnames = solu.varNames();
			int geomvars = 0;
			//System.out.println(solu.get(featuretype.toLowerCase()).toString() + " - " + lastInd);
			if (!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
				System.out.println("NEW OBJECT!");
				System.out.println(latlist+" - "+lonlist);
				if(!latlist.isEmpty() && !lonlist.isEmpty()) {
					if(latlist.size()==1 && lonlist.size()==1) {
						JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+lonlist.get(0)+","+latlist.get(0)+"]}");
						geoms.add(geomobj);
						properties.put("lat", latlist.get(0));
						properties.put("lon",lonlist.get(0));
					}else if(latlist.get(latlist.size()-1).equals(latlist.get(0)) && lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
						JSONObject geomobj=new JSONObject();
						geomobj.put("type","Polygon");
						JSONArray arr=new JSONArray();
						JSONArray arr2=new JSONArray();
						arr.put(arr2);
						String lit="Polygon(";
						geomobj.put("coordinates",arr);
						for(int i=0;i<latlist.size();i++) {
							JSONArray arr3=new JSONArray();
							arr3.put(lonlist.get(i));
							arr3.put(latlist.get(i));
							lit+=lonlist.get(i)+" "+lonlist.get(i)+",";
							arr2.put(arr3);
						}
						geoms.add(geomobj);
						properties.put("geometry", lit.substring(0,lit.length()-1)+")");
					}else if(!latlist.get(latlist.size()-1).equals(latlist.get(0)) || !lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
						JSONObject geomobj=new JSONObject();
						geomobj.put("type","LineString");
						JSONArray arr=new JSONArray();
						String lit="LineString(";
						geomobj.put("coordinates",arr);
						for(int i=0;i<latlist.size();i++) {
							JSONArray arr2=new JSONArray();
							arr2.put(lonlist.get(i));
							arr2.put(latlist.get(i));
							lit+=lonlist.get(i)+" "+lonlist.get(i)+",";
							arr.put(arr2);
						}
						geoms.add(geomobj);
						properties.put("geometry", lit.substring(0,lit.length()-1)+")");
					}
					latlist.clear();
					lonlist.clear();
				}
				newobject = true;
			} else {
				newobject = false;
			}
			if (newobject) {
				lastQueriedElemCount++;
				// System.out.println("Geomvars: "+geomvars);
				if(!onlyproperty) {
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
				}else {
						JSONObject geojsonobj = new JSONObject();
						geojsonobj.put("type", "Feature");
						geojsonobj.put("properties", properties);
						geojsonobj.put("id",lastInd);
						if(!geoms.isEmpty())
							geojsonobj.put("geometry",geoms.get(0));
						//allfeatures.get(geomcounter).put(geojsonobj);
						features.put(geojsonobj);
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
				}else if (name.endsWith("_val") || name.equals("val")) {
					val = solu.get(name).toString();
				}else if (name.equals("lat")) {
					lat = solu.get(name).toString();
				}else if (name.equals("lon")) {
					lon = solu.get(name).toString();
				}else if (name.equalsIgnoreCase(indvar)) {
					continue;
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
			if(!lat.isEmpty() && !lon.isEmpty()) {
				System.out.println("LatLon: "+lat+","+lon);
				if(lat.contains("^^")) {
					lat=lat.substring(0,lat.indexOf("^^"));
				}
				if(lon.contains("^^")) {
					lon=lon.substring(0,lon.indexOf("^^"));
				}
				lonlist.add(lon);
				latlist.add(lat);
				lat="";
				lon="";
			}
			first = false;
			lastInd = solu.get(indvar).toString();
		}
		System.out.println("LastLat: "+latlist.toString()+" "+lonlist.toString());
		if(!latlist.isEmpty() && !lonlist.isEmpty()) {
			if(latlist.size()==1 && lonlist.size()==1) {
				JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+lonlist.get(0)+","+latlist.get(0)+"]}");
				geoms.add(geomobj);
				properties.put("lat", latlist.get(0));
				properties.put("lon",lonlist.get(0));
			}else if(latlist.get(latlist.size()-1).equals(latlist.get(0)) && lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
				JSONObject geomobj=new JSONObject();
				geomobj.put("type","Polygon");
				JSONArray arr=new JSONArray();
				JSONArray arr2=new JSONArray();
				arr.put(arr2);
				String lit="Polygon(";
				geomobj.put("coordinates",arr);
				for(int i=0;i<latlist.size();i++) {
					JSONArray arr3=new JSONArray();
					arr3.put(lonlist.get(i));
					arr3.put(latlist.get(i));
					lit+=lonlist.get(i)+" "+lonlist.get(i)+",";
					arr2.put(arr3);
				}
				geoms.add(geomobj);
				properties.put("geometry", lit.substring(0,lit.length()-1)+")");
			}else if(!latlist.get(latlist.size()-1).equals(latlist.get(0)) || !lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
				JSONObject geomobj=new JSONObject();
				geomobj.put("type","LineString");
				JSONArray arr=new JSONArray();
				String lit="LineString(";
				geomobj.put("coordinates",arr);
				for(int i=0;i<latlist.size();i++) {
					JSONArray arr2=new JSONArray();
					arr2.put(lonlist.get(i));
					arr2.put(latlist.get(i));
					lit+=lonlist.get(i)+" "+lonlist.get(i)+",";
					arr.put(arr2);
				}
				geoms.add(geomobj);
				properties.put("geometry", lit.substring(0,lit.length()-1)+")");
			}
			latlist.clear();
			lonlist.clear();
		}
		if(!onlyproperty) {
			int geomcounter=0;
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
		}else {
			JSONObject geojsonobj = new JSONObject();
			geojsonobj.put("type", "Feature");
			geojsonobj.put("properties", properties);
			geojsonobj.put("id",lastInd);
			if(!geoms.isEmpty())
				geojsonobj.put("geometry",geoms.get(0));
			//allfeatures.get(geomcounter).put(geojsonobj);
			features.put(geojsonobj);
		}
		//System.out.println(obj);
		//System.out.println(geojsonresults.toString(2));
		return geojsonresults.toString(2);
	}

}
