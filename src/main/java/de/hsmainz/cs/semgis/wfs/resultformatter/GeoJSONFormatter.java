package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.NodeValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.converters.AsGeoJSON;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.GeoJSONCSSFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GeoJSONFormatter extends WFSResultFormatter {

	public GeoJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/geo+json";
		this.styleformatter=new GeoJSONCSSFormatter();
	}
	
	
	private AsGeoJSON geojson = new AsGeoJSON();
	
	@Override
	public String formatter(ResultSet results, String startingElement, 
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,
			Boolean onlyhits,String srsName,
			String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) {
		lastQueriedElemCount=0;
		this.contextMapper.clear();
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
		//TODO Write GeoJSON styles
		JSONArray features = new JSONArray();
		allfeatures.add(features);
		geojsonresults.put("features", features);
		List<String> latlist=new LinkedList<String>();
		List<String> lonlist=new LinkedList<String>();
		Map<String,String> rel = new TreeMap<String,String>();
		Map<String,String> val=new TreeMap<String,String>();
		String lastInd = "",lat="",lon="";
		JSONObject jsonobj = new JSONObject();
		JSONObject properties = new JSONObject();
		JSONObject style = new JSONObject();
		List<JSONObject> geoms = new LinkedList<JSONObject>();
		while (results.hasNext()) {
			// System.out.println(i);
			QuerySolution solu = results.next();
			Iterator<String> varnames = solu.varNames();
			int geomvars = 0;
			//System.out.println(solu.get(featuretype.toLowerCase()).toString() + " - " + lastInd);
			if (!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
				System.out.println("NEW OBJECT!");
				System.out.println("HasStyle??? - "+mapstyle);
				System.out.println(latlist+" - "+lonlist);
				if(!latlist.isEmpty() && !lonlist.isEmpty()) {
					if(latlist.size()==1 && lonlist.size()==1) {
						JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+lonlist.get(0)+","+latlist.get(0)+"]}");
						geoms.add(geomobj);
						properties.put("lat", latlist.get(0));
						properties.put("lon",lonlist.get(0));
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("Point", mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
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
							lit+=lonlist.get(i)+" "+latlist.get(i)+",";
							arr2.put(arr3);
						}
						geoms.add(geomobj);
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("Polygon", mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
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
							lit+=lonlist.get(i)+" "+latlist.get(i)+",";
							arr.put(arr2);
						}
						geoms.add(geomobj);
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("LineString", mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
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
					if(!style.isEmpty())
						geojsonobj.put("style", style);
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
						if(!style.isEmpty())
							geojsonobj.put("style", style);
						geojsonobj.put("id",lastInd);
						if(!geoms.isEmpty())
							geojsonobj.put("geometry",geoms.get(0));
						//allfeatures.get(geomcounter).put(geojsonobj);
						features.put(geojsonobj);
				}
				geomvars=0;
				jsonobj = new JSONObject();
				properties = new JSONObject();
				style=new JSONObject();
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
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry(geomobj.getString("type"), mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					addKeyVal(properties, name, solu.get(name).toString());
				}
				if (name.endsWith("_rel") || name.equals("rel") || name.matches("rel[0-9]+$")) {
					relationName = solu.get(name).toString();
					rel.put(name,solu.get(name).toString());
				}else if (name.endsWith("_val") || name.equals("val") || name.matches("val[0-9]+$")) {
					val.put(name,solu.get(name).toString());
				}else if (name.equals("lat")) {
					lat = solu.get(name).toString();
				}else if (name.equals("lon")) {
					lon = solu.get(name).toString();
				}else if (name.equalsIgnoreCase(indvar)) {
					continue;
				} else {
					if (!relationName.isEmpty()) {
						// System.out.println("Putting property: "+relationName+" - "+solu.get(name));
						addKeyVal(properties, relationName, solu.get(name).toString());
					} else {
						addKeyVal(properties, name, solu.get(name).toString());
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
				System.out.println("Rel: "+rel.toString());
				System.out.println("Val: "+val.toString());
				if(!rel.values().iterator().next().equals("http://www.opengis.net/ont/geosparql#hasGeometry") && rel.size()==1) {
					addKeyVal(properties, rel.values().iterator().next(), val.values().iterator().next());
				}else if(rel.size()>1) {
					/*String rlstr="";
					Iterator<String> relit=rel.values().iterator();
					while(relit.hasNext()) {
						rlstr=relit.next();
						if(relit.hasNext()) {
							rlstr+=".";
						}
					}
					Iterator<String> valit=val.values().iterator();
					String valstr="";
					while(valit.hasNext()) {
						valstr=valit.next();
					}*/
					addKeyValList(properties, rel.values(), val.values());
				}else {
					addKeyVal(properties, rel.values().iterator().next(), val.values().iterator().next());
				}

				rel.clear();
				val.clear();
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
				if(mapstyle!=null) {
					JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("Point", mapstyle));
					System.out.println("Got style? - "+geojsonstyle);
					for(String key:geojsonstyle.keySet()) {
						style.put(key,geojsonstyle.get(key));
					}
				}
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
					lit+=lonlist.get(i)+" "+latlist.get(i)+",";
					arr2.put(arr3);
				}
				geoms.add(geomobj);
				if(mapstyle!=null) {
					JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("Polygon", mapstyle));
					System.out.println("Got style? - "+geojsonstyle);
					for(String key:geojsonstyle.keySet()) {
						style.put(key,geojsonstyle.get(key));
					}
				}
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
					lit+=lonlist.get(i)+" "+latlist.get(i)+",";
					arr.put(arr2);
				}
				geoms.add(geomobj);
				if(mapstyle!=null) {
					JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("LineString", mapstyle));
					System.out.println("Got style? - "+geojsonstyle);
					for(String key:geojsonstyle.keySet()) {
						style.put(key,geojsonstyle.get(key));
					}
				}
				properties.put("geometry", lit.substring(0,lit.length()-1)+")");
			}
			latlist.clear();
			lonlist.clear();
		}
		if(!onlyproperty) {
			for (JSONObject geom : geoms) {
				JSONObject geojsonobj = new JSONObject();
				geojsonobj.put("type", "Feature");
				geojsonobj.put("properties", properties);
				if(!style.isEmpty())
					geojsonobj.put("style", style);
				geojsonobj.put("geometry", geom);
				geojsonobj.put("id",lastInd);
				//allfeatures.get(geomcounter).put(geojsonobj);
				features.put(geojsonobj);
				//System.out.println(geojsonobj);
			}
		}else {
			JSONObject geojsonobj = new JSONObject();
			geojsonobj.put("type", "Feature");
			if(!style.isEmpty())
				geojsonobj.put("style", style);
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
	
	public void relToMap(String keyPath) {
		if(keyPath.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
			return;
		}
		if(!keyPath.contains(";")) {
			if (keyPath.contains("#")) {
				this.contextMapper.put(keyPath,keyPath.substring(keyPath.lastIndexOf('#') + 1));
			} else {
				this.contextMapper.put(keyPath,keyPath.substring(keyPath.lastIndexOf('/') + 1));
			}
		}
		String result="";
		String[] splitted=keyPath.split(";");
		int i=0;
		for(i=0;i<splitted.length;i++) {	
			if (splitted[i].contains("#")) {
				result+=splitted[i].substring(splitted[i].lastIndexOf('#') + 1);
			} else {
				result+=splitted[i].substring(splitted[i].lastIndexOf('/') + 1);
			}
			if(i<splitted.length-1) {
				result+=".";
			}
		}
		this.contextMapper.put(keyPath,result);
	}
	
	public void addKeyValList(JSONObject properties,Collection<String> rell,Collection<String> vall) {
		//System.out.println("AddKeyValList");
		//System.out.println(rell.toString());
		//System.out.println(vall.toString());
		Iterator<String> reliter=rell.iterator();
		Iterator<String> valiter=vall.iterator();
		String lastval="";
		while(valiter.hasNext()) {
			lastval=valiter.next();
		}
		//System.out.println(properties);
		while(reliter.hasNext()) {
			String rel=reliter.next();
			if(properties.has(rel)) {
				if(reliter.hasNext()) {
					properties=properties.getJSONObject(rel);
					continue;
				}
				try {
					properties.getJSONArray(rel).put(lastval);
				}catch(JSONException e) {
					String oldval=properties.getString(rel);
					if(!this.contextMapper.containsKey(rel)) {
						this.relToMap(rel);
					}
					properties.put(rel,new JSONArray());
					properties.getJSONArray(rel).put(oldval);
					properties.getJSONArray(rel).put(lastval);
				}
			}else {
				if(reliter.hasNext()) {
					if(!this.contextMapper.containsKey(rel)) {
						this.relToMap(rel);
					}
					properties.put(rel, new JSONObject());
					properties=properties.getJSONObject(rel);
					continue;
				}else {
					if(!this.contextMapper.containsKey(rel)) {
						this.relToMap(rel);
					}
					properties.put(rel, lastval);	
				}			
			}
			//properties=properties.getJSONObject(rel);
		}

	}	
	
	
	public void addKeyVal(JSONObject properties,String rel,String val) {
		if(!this.contextMapper.containsKey(rel)) {
			this.relToMap(rel);
		}
		if(properties.has(rel)) {			
			try {
				properties.getJSONArray(rel).put(val);
			}catch(JSONException e) {
				String oldval=properties.getString(rel);
				properties.put(rel,new JSONArray());
				properties.getJSONArray(rel).put(oldval);
				properties.getJSONArray(rel).put(val);
			}
		}else {
			properties.put(rel, val);
		}
	}

}
