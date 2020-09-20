package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.CovJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.GMLCOVFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.GeoTIFFFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.XYZASCIIFormatter;
import de.hsmainz.cs.semgis.wfs.util.Tuple;

public abstract class CoverageResultFormatter extends ResultFormatter {
	
	public static Map<String,Map<String,Integer>> columnsval;
		
	public static Map<String,Tuple<Boolean,String>> extractObservableColumns(JSONObject geojson) {
		Map<String,Tuple<Boolean,String>> columns=new TreeMap<String,Tuple<Boolean,String>>();
		columnsval=new TreeMap<String,Map<String,Integer>>();
		JSONArray features=geojson.getJSONArray("features");
		for(int i=0;i<features.length();i++) {
			JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
			for(String key:feature.getJSONObject("properties").keySet()) {
				if(!columns.containsKey(key)) {
					try {
						Number valuenum=feature.getNumber(key);
						columns.put(key, new Tuple<>(true,"float"));
					}catch(Exception e) {
						try {
							boolean valuebool=feature.getBoolean(key);
							columns.put(key, new Tuple<>(true,"bool"));
						}catch(Exception ex) {
							if(!columnsval.containsKey(key)) {
								columnsval.put(key, new TreeMap<>());
							}
							if(!columnsval.get(key).containsKey(feature.getJSONObject("properties").get(key).toString())) {
								columnsval.get(key).put(feature.getJSONObject("properties").get(key).toString(), 0);
							}
							columnsval.get(key).put(key, columnsval.get(key).put(feature.getJSONObject("properties").get(key).toString(), columnsval.get(key).get(feature.getJSONObject("properties").get(key).toString())+1));
							
						}
					}
				}
			}
		}
		for(String key:geojson.getJSONArray("features").getJSONObject(0).getJSONObject("properties").keySet()) {
			if(!columns.containsKey(key)) {
				if(columnsval.get(key).size()==geojson.getJSONArray("features").length()) {
					columns.put(key, new Tuple<>(false,null));
				}else {
					columns.put(key, new Tuple<>(true,"string"));
				}
			}
		}
		return columns;
	}
	
	public static JSONObject addCovJSONParameterAndRange(JSONObject covjson,String parameterName,String parameterURI,List<Object> values,String dataType) {
		covjson.getJSONObject("parameters").put(parameterName, new JSONObject());
		JSONObject param=covjson.getJSONObject("parameters").getJSONObject(parameterName);
		param.put("type", "Parameter");
		param.put("description", new JSONObject());
		param.getJSONObject("description").put("en",parameterName);
		param.put("observedProperty",new JSONObject());	
		param.getJSONObject("observedProperty").put("id",parameterURI);
		param.getJSONObject("observedProperty").put("label",new JSONObject());
		param.getJSONObject("observedProperty").getJSONObject("label").put("en",parameterName);
		
		JSONObject range=covjson.getJSONObject("ranges").put(parameterName, new JSONObject());
		range.put("type", "NdArray");
		range.put("dataType", dataType);
		range.put("values",new JSONArray(values));
		return covjson;
	}
	
	public static JSONObject generateCOVJSONForXY(String coverageType) {
		JSONObject result=new JSONObject();
		result.put("type", "Coverage");
		JSONObject domain=new JSONObject();
		result.put("domain", domain);
		domain.put("type", "Domain");
		domain.put("domainType",coverageType);
		JSONObject axes=new JSONObject();
		axes.put("x", new JSONObject());
		axes.getJSONObject("x").put("values",new JSONArray());
		axes.put("y", new JSONObject());
		axes.getJSONObject("y").put("values",new JSONArray());
		JSONObject referencing=new JSONObject();
		domain.put("referencing", referencing);
		JSONArray coordinates=new JSONArray();
		referencing.put("coordinates",coordinates);
		coordinates.put("x");
		coordinates.put("y");
		referencing.put("system", new JSONObject());
		referencing.getJSONObject("system").put("type","GeographicCRS");
		referencing.getJSONObject("system").put("id", "http://www.opengis.net/def/crs/EPSG/0/4326");
		result.put("parameters", new JSONObject());
		result.put("ranges", new JSONObject());
		return result;
	}
	
	public static JSONObject convertLiteralDataToCoverageJSON(String literalValue,String literalType) {
		JSONObject result=new JSONObject();
		switch(literalType) {
		case "http://www.opengis.net/ont/geosparql#xyzLiteral":
			result=generateCOVJSONForXY("Grid");
			String param="altitude";
			result=addCovJSONParameterAndRange(result, param, "http://www.opengis.net/ont/geosparql#altitude", new LinkedList<>(), "float");
			JSONArray xarray=result.getJSONObject("domain").getJSONObject("axes").getJSONObject("x").getJSONArray("values");
			JSONArray yarray=result.getJSONObject("domain").getJSONObject("axes").getJSONObject("y").getJSONArray("values");
			JSONArray zarray=result.getJSONObject("ranges").getJSONObject(param).getJSONArray("values");
			for(String line:literalValue.split(System.lineSeparator())) {
				String[] coord=line.split(" "); 
				xarray.put(Double.valueOf(coord[0]));
				yarray.put(Double.valueOf(coord[1]));
				zarray.put(Double.valueOf(coord[2]));
			}
			break;
		}
		return result;
	}
	
}
