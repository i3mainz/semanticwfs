package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.util.Tuple;

public class CovJSONFormatter extends CoverageResultFormatter {

	public CovJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/prs.coverage+json";
		this.urlformat="covjson";
		this.label="CoverageJSON (COVJSON)";
		this.fileextension="covjson";
		this.definition="https://covjson.org";
	}
	
	public Set<String> getGeometryTypes(JSONObject geojson){
		Set<String> result=new TreeSet<String>();
		for(int i=0;i<geojson.getJSONArray("features").length();i++) {
			JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
			result.add(feature.getJSONObject("geometry").getString("type"));
		}
		return result;
	}
	
	
	public Tuple<String,String> getDomainTypeFromGeometryTypes(Set<String> types) {
		if(types.size()==1) {
			if(types.contains("Polygon")) {
				return new Tuple<>("Coverage","MultiPolygon");
			}
			if(types.contains("LineString")) {
				return new Tuple<>("Coverage","Trajectory");
			}
			if(types.contains("Point")) {
				return new Tuple<>("CoverageCollection","Point");
			}
		}
		return new Tuple<>("CoverageCollection","");
	}
		
	public Tuple<String,String> getDomainTypeFromGeometryTypes(String type) {
		if(type.contains("Polygon")) {
			return new Tuple<>("Coverage","MultiPolygon");
		}
		if(type.contains("LineString")) {
			return new Tuple<>("CoverageCollection","Trajectory");
		}
		if(type.contains("Point")) {
			return new Tuple<>("CoverageCollection","Point");
		}
		return new Tuple<>("CoverageCollection","");
	}
	
	public JSONObject createObservableParametersAndRanges(Map<String,Tuple<Boolean,String>> parammap, JSONObject result, JSONObject geojson,String srsName) {	
		JSONObject parameters=new JSONObject();
		result.put("parameters",parameters);
		Map<String,Integer> encodings=new TreeMap<String,Integer>();
		for(String param:parammap.keySet()) {
			if(parammap.get(param).getOne()) {
				JSONObject parameter=new JSONObject();
				parameter.put("type", "Parameter");
				JSONObject paramdescription=new JSONObject();
				paramdescription.put("description", paramdescription);
				JSONObject categoryEncoding=new JSONObject();
				parameter.put("categoryEncoding", categoryEncoding);
				if(parammap.get(param).getTwo().equals("string")) {
					JSONArray categories=new JSONArray();
					parameter.put("categories", categories);
					Integer catcounter=1;
					for(String cat:columnsval.get(param).keySet()) {						
						if(!cat.equals(param)) {
							if(cat.startsWith("[")) {
								JSONArray arr=new JSONArray(cat);
								for(int i=0;i<arr.length();i++) {
									JSONObject category=new JSONObject();
									categories.put(category);
									category.put("id", arr.get(i).toString());
									JSONObject categorylabel=new JSONObject();
									category.put("label", categorylabel);
									if(arr.get(i).toString().contains("/")) {
										categorylabel.put("en", arr.get(i).toString().substring(arr.get(i).toString().lastIndexOf('/')+1));							
									}else {
										categorylabel.put("en", arr.get(i).toString());
									}
									encodings.put(arr.get(i).toString(), catcounter);
									categoryEncoding.put(arr.get(i).toString(),catcounter++);
								}
							}else {
								JSONObject category=new JSONObject();
								categories.put(category);
								category.put("id", cat);
								JSONObject categorylabel=new JSONObject();
								category.put("label", categorylabel);
								if(cat.contains("/")) {
									categorylabel.put("en", cat.substring(cat.lastIndexOf('/')+1));							
								}else {
									categorylabel.put("en", cat);
								}	
								encodings.put(cat, catcounter);
								categoryEncoding.put(cat,catcounter++);
								
							}

						}
					}
				}

				
				/*JSONObject unit=new JSONObject();		
				parameter.put("unit", unit);
				JSONObject unitlabel=new JSONObject();
				unitlabel.put("en", "meter");
				JSONObject unitsymbol=new JSONObject();
				unit.put("label", unitlabel);
				unit.put("symbol", unitsymbol);
				unitsymbol.put("value","meter");
				unitsymbol.put("type","http://www.opengis.net/def/uom/UCUM/");*/
				JSONObject observedProperty=new JSONObject();
				parameter.put("observedProperty", observedProperty);
				observedProperty.put("id", param);
				JSONObject observedPropertyLabel=new JSONObject();
				observedProperty.put("label",observedPropertyLabel);
				observedPropertyLabel.put("en", param.substring(param.lastIndexOf('/')+1));
				parameters.put(param,parameter);
			}	
		}
		Tuple<String,String> types=this.getDomainTypeFromGeometryTypes(this.getGeometryTypes(geojson));
		if(!types.getTwo().isEmpty()) {
			result.put("domainType", types.getTwo());
			result.put("type", types.getOne());
			JSONArray referencing=new JSONArray();
			result.put("referencing", referencing);
			JSONObject ref=new JSONObject();
			referencing.put(ref);
			JSONArray coordinates=new JSONArray();
			coordinates.put("x");
			coordinates.put("y");
			ref.put("coordinates", coordinates);
			JSONObject system=new JSONObject();
			ref.put("system", system);
			system.put("type", "GeographicCRS");
			system.put("id", "http://www.opengis.net/def/crs/EPSG/0/"+srsName.substring(srsName.lastIndexOf(':')+1));
			if(types.getOne().contains("Collection")) {
				JSONArray coverages=new JSONArray();
				result.put("coverages", coverages);
				for(int i=0;i<geojson.getJSONArray("features").length();i++) {
					JSONObject coverage=new JSONObject();
					coverage.put("type", "Coverage");
					coverages.put(coverage);
					JSONObject covdomain=new JSONObject();
					coverage.put("domain",covdomain);
					covdomain.put("type", "Domain");
					JSONObject covaxes=new JSONObject();
					covdomain.put("axes", covaxes);
					JSONObject x=new JSONObject();
					JSONObject y=new JSONObject();
					x.put("values", new JSONArray());
					y.put("values",new JSONArray());
					covaxes.put("x", x);
					covaxes.put("y", y);
					JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
					Geometry geom=geojsonreader.read(feature.getJSONObject("geometry").toString());
					for(Coordinate coord:geom.getCoordinates()) {
						x.getJSONArray("values").put(coord.getX());
						y.getJSONArray("values").put(coord.getY());
					}
					JSONObject ranges=new JSONObject();
					coverage.put("ranges",ranges);
					for(String key:parammap.keySet()) {
						if(parammap.get(key).getOne()) {
						JSONObject range=new JSONObject();
							ranges.put(key,range);
							range.put("type", "NdArray");
							if(parammap.get(key).getTwo().equals("string")) {
								range.put("dataType","integer");
							}else {
								range.put("dataType",parammap.get(key).getTwo());
							}
							range.put("values", new JSONArray());	
						}
					}
					for(String key:feature.getJSONObject("properties").keySet()) {
						if(parammap.get(key).getOne()) {
							JSONArray arr=ranges.getJSONObject(key).getJSONArray("values");
							if(parammap.get(key).getTwo().equals("string") && encodings.containsKey(feature.getJSONObject("properties").get(key).toString())) {
								arr.put(encodings.get(feature.getJSONObject("properties").get(key).toString()));
							}else {
								arr.put(feature.getJSONObject("properties").get(key));							
							}
						}
					}
				}
			}else {
				result.put("domain",new JSONObject());
				JSONObject domain=result.getJSONObject("domain");
				JSONObject axes=new JSONObject();
				domain.put("axes", axes);
				JSONObject x=new JSONObject();
				JSONObject y=new JSONObject();
				x.put("values", new JSONArray());
				y.put("values",new JSONArray());
				axes.put("x", x);
				axes.put("y", y);
				for(int i=0;i<geojson.getJSONArray("features").length();i++) {
					JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
					Geometry geom=geojsonreader.read(feature.getJSONObject("geometry").toString());
					for(Coordinate coord:geom.getCoordinates()) {
						x.getJSONArray("values").put(coord.getX());
						y.getJSONArray("values").put(coord.getY());
					}
				}
				JSONObject ranges=new JSONObject();
				result.put("ranges",ranges);
				for(String key:parammap.keySet()) {
					JSONObject range=new JSONObject();
					ranges.put(key,range);
					range.put("type", "NdArray");
					range.put("dataType",parammap.get(key).getTwo());
					range.put("values", new JSONArray());		
				}
				for(int i=0;i<geojson.getJSONArray("features").length();i++) {
					JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
					for(String key:feature.getJSONObject("properties").keySet()) {
						if(parammap.get(key).getOne()) {
							JSONArray arr=ranges.getJSONObject(key).getJSONArray("values");
							arr.put(feature.getJSONObject("properties").get(key));
						}
					}
				}
			}

		}else {
			//String geomtype=geojson.getJSONObject("featuresgetJSONObject("geometry").getString("type");
			//JSONArray coordinates=geojson.getJSONObject("geometry").getJSONArray("coordinates");
		}
		return result;
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY, Boolean coverage, Writer out) throws XMLStreamException, JSONException, IOException {	
		JSONObject result=new JSONObject();
		System.out.println("COVERAGEJSON Formatter: "+coverage);
		if(coverage) {
			String lastInd=null;
			System.out.println("Has Result? "+results.hasNext());
			while(results.hasNext()) {
				QuerySolution solu=results.next();
				Iterator<String> varnames=solu.varNames();
				if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
					lastQueriedElemCount++;
				}
				while(varnames.hasNext()) {
					String name=varnames.next();
					System.out.println(name+" "+solu.get(name).toString());
					if(name.endsWith("_geom")) {
						String res=this.parseCoverageLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
								solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
						result=convertLiteralDataToCoverageJSON(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2));
					}else if(name.equalsIgnoreCase(indvar)){
						continue;
					}
				}
				lastInd=solu.get(indvar).toString();
			}
		}else {
			ResultFormatter format = resultMap.get("geojson");
			JSONObject geojson = new JSONObject(
					format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,
							srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,null));
			lastQueriedElemCount=format.lastQueriedElemCount;
			result=new JSONObject();
			result.put("type", "Coverage");
			result=createObservableParametersAndRanges(CoverageResultFormatter.extractObservableColumns(geojson),result,geojson,srsName);
		}
		return result.toString(2);
	}

}
