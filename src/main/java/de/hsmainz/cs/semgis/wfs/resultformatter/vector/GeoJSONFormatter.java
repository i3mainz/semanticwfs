package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.GeoJSON;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.GeoJSONCSSFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.ResultStyleFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.util.ReprojectionUtils;

/**
 * Formats a query result to GeoJSON.
 */
public class GeoJSONFormatter extends VectorResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public GeoJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/geojson";
		this.urlformat="json";
		this.label="GeoJSON";
		this.styleformatter=new GeoJSONCSSFormatter();
		this.fileextension="geojson";
		this.definition="https://geojson.org";
	}
	
	/**
	 * Constructor for this class receiving a customized style formatter.
	 */
	public GeoJSONFormatter(ResultStyleFormatter styleformatter) {
		this.mimeType="application/json";
		this.exposedType="application/geojson";
		this.styleformatter=styleformatter;
	}
	
	
	public String formatJSONObject(ResultSet results, String startingElement, 
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,
			Boolean onlyhits,String srsName,
			String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) {
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
				//System.out.println("NEW OBJECT!");
				//System.out.println("HasStyle??? - "+mapstyle);
				//System.out.println(latlist+" - "+lonlist);
				if(!latlist.isEmpty() && !lonlist.isEmpty()) {
					if(latlist.size()==1 && lonlist.size()==1) {
						Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(0)), Double.valueOf(latlist.get(0)), epsg,srsName);
						JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+coord.x+","+coord.y+"]}");
						geoms.add(geomobj);
						properties.put("lon",coord.x);
						properties.put("lat", coord.y);
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("Point", mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
					}else if(latlist.get(latlist.size()-1).equals(latlist.get(0))
							&& lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
						JSONObject geomobj=new JSONObject();
						geomobj.put("type","Polygon");
						JSONArray arr=new JSONArray();
						JSONArray arr2=new JSONArray();
						arr.put(arr2);
						String lit="Polygon(";
						geomobj.put("coordinates",arr);
						for(int i=0;i<latlist.size();i++) {
							JSONArray arr3=new JSONArray();
							Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
							arr3.put(coord.x);
							arr3.put(coord.y);
							lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
							//lit+=lonlist.get(i)+" "+latlist.get(i)+",";
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
							Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
							arr2.put(coord.x);
							arr2.put(coord.y);
							lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
							//lit+=lonlist.get(i)+" "+latlist.get(i)+",";
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
						if(!style.isEmpty()) {
							if(this.styleformatter.styleAttribute.equalsIgnoreCase("properties")) {
								for(String key:style.keySet()) {
									properties.put(key, style.get(key));
								}
							}else if(this.styleformatter.styleAttribute.isEmpty()){
								geojsonobj.put("style", style);
							}else {
								geojsonobj.put(this.styleformatter.styleAttribute, style);
							}
						}
							
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
				if (name.endsWith("_geom")) {
					// System.out.println("Geomvar: "+name);
					geomvars++;
					geomvarname = name;
					
					lastgeom = solu.get(name).toString();
					Geometry geom=this.parseVectorLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
							solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
					if(geom!=null) {
						GeoJSONWriter writer = new GeoJSONWriter();
			            GeoJSON geomobj = writer.write(geom);
			            geoms.add(new JSONObject(geomobj.toString()));
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry(geomobj.getType(), mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
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
				Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(0)), Double.valueOf(latlist.get(0)), epsg,srsName);
				JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+coord.x+","+coord.y+"]}");
				geoms.add(geomobj);
				properties.put("lon",coord.x);
				properties.put("lat", coord.y);
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
					Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
					arr3.put(coord.x);
					arr3.put(coord.y);
					//arr3.put(lonlist.get(i));
					//arr3.put(latlist.get(i));
					lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
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
					Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
					arr2.put(coord.x);
					arr2.put(coord.y);
					//arr2.put(lonlist.get(i));
					//arr2.put(latlist.get(i));
					lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
					//lit+=lonlist.get(i)+" "+latlist.get(i)+",";
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
	
	public String formatJSONStreaming(ResultSet results, String startingElement, 
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,
			Boolean onlyhits,String srsName,
			String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws IOException {
		lastQueriedElemCount=0;
		this.contextMapper.clear();
		/*JsonFactory jfactory = new JsonFactory();
		JsonGenerator jGenerator = jfactory.createGenerator(out);
		jGenerator.writeStartObject();
		jGenerator.writeNumberField("amount", this.lastQueriedElemCount);
		jGenerator.writeStartArray();*/
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
				//System.out.println("NEW OBJECT!");
				//System.out.println("HasStyle??? - "+mapstyle);
				//System.out.println(latlist+" - "+lonlist);
				if(!latlist.isEmpty() && !lonlist.isEmpty()) {
					if(latlist.size()==1 && lonlist.size()==1) {
						Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(0)), Double.valueOf(latlist.get(0)), epsg,srsName);
						JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+coord.x+","+coord.y+"]}");
						geoms.add(geomobj);
						properties.put("lon",coord.x);
						properties.put("lat", coord.y);
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry("Point", mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
					}else if(latlist.get(latlist.size()-1).equals(latlist.get(0))
							&& lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
						JSONObject geomobj=new JSONObject();
						geomobj.put("type","Polygon");
						JSONArray arr=new JSONArray();
						JSONArray arr2=new JSONArray();
						arr.put(arr2);
						String lit="Polygon(";
						geomobj.put("coordinates",arr);
						for(int i=0;i<latlist.size();i++) {
							JSONArray arr3=new JSONArray();
							Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
							arr3.put(coord.x);
							arr3.put(coord.y);
							lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
							//lit+=lonlist.get(i)+" "+latlist.get(i)+",";
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
							Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
							arr2.put(coord.x);
							arr2.put(coord.y);
							lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
							//lit+=lonlist.get(i)+" "+latlist.get(i)+",";
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
						if(!style.isEmpty()) {
							if(this.styleformatter.styleAttribute.equalsIgnoreCase("properties")) {
								for(String key:style.keySet()) {
									properties.put(key, style.get(key));
								}
							}else if(this.styleformatter.styleAttribute.isEmpty()){
								geojsonobj.put("style", style);
							}else {
								geojsonobj.put(this.styleformatter.styleAttribute, style);
							}
						}
							
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
				if (name.endsWith("_geom")) {
					// System.out.println("Geomvar: "+name);
					geomvars++;
					geomvarname = name;
					
					lastgeom = solu.get(name).toString();
					Geometry geom=this.parseVectorLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
							solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
					if(geom!=null) {
						GeoJSONWriter writer = new GeoJSONWriter();
			            GeoJSON geomobj = writer.write(geom);
			            geoms.add(new JSONObject(geomobj.toString()));
						if(mapstyle!=null) {
							JSONObject geojsonstyle=new JSONObject(this.styleformatter.formatGeometry(geomobj.getType(), mapstyle));
							System.out.println("Got style? - "+geojsonstyle);
							for(String key:geojsonstyle.keySet()) {
								style.put(key,geojsonstyle.get(key));
							}
						}
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
				Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(0)), Double.valueOf(latlist.get(0)), epsg,srsName);
				JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+coord.x+","+coord.y+"]}");
				geoms.add(geomobj);
				properties.put("lon",coord.x);
				properties.put("lat", coord.y);
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
					Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
					arr3.put(coord.x);
					arr3.put(coord.y);
					//arr3.put(lonlist.get(i));
					//arr3.put(latlist.get(i));
					lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
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
					Coordinate coord=ReprojectionUtils.reproject(Double.valueOf(lonlist.get(i)), Double.valueOf(latlist.get(i)), epsg,srsName);
					arr2.put(coord.x);
					arr2.put(coord.y);
					//arr2.put(lonlist.get(i));
					//arr2.put(latlist.get(i));
					lit+=coord.x+" "+coord.y;//lonlist.get(i)+" "+latlist.get(i)+",";
					//lit+=lonlist.get(i)+" "+latlist.get(i)+",";
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
	
	@Override
	public String formatter(ResultSet results, String startingElement, 
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,
			Boolean onlyhits,String srsName,
			String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws IOException {
		if(out!=null) {
			return this.formatJSONStreaming(results, startingElement, featuretype, propertytype, typeColumn, onlyproperty, onlyhits, srsName, indvar, epsg, eligiblenamespaces, noteligiblenamespaces, mapstyle, alternativeFormat, invertXY, coverage, out);
		}
		return this.formatJSONObject(results, startingElement, featuretype, propertytype, typeColumn, onlyproperty, onlyhits, srsName, indvar, epsg, eligiblenamespaces, noteligiblenamespaces, mapstyle, alternativeFormat, invertXY, coverage, out);
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
	
	/**
	 * Adds a list of key and values to a JSONObject
	 * @param properties the JSONObject
	 * @param rell the list of relations
	 * @param vall the list of values
	 */
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
	
	/**
	 * Adds a key/value pair to a JSONObject, creates a JSONArray if neccessary.
	 * @param properties the JSONObject
	 * @param rel Relation to add
	 * @param val Value to add
	 */
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
