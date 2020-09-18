package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.CovJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.GMLCOVFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.GeoTIFFFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.coverage.XYZASCIIFormatter;
import de.hsmainz.cs.semgis.wfs.util.Tuple;

public abstract class CoverageResultFormatter extends ResultFormatter {

	static {
		ResultFormatter format=new XYZASCIIFormatter();
		resultMap.put("xyz", format);
		labelMap.put("xyz",format.label);
		resultMap.put(format.mimeType, format);
		format=new CovJSONFormatter();
		resultMap.put("covjson", format);
		labelMap.put("covjson",format.label);
		resultMap.put(format.mimeType, format);
		resultMap.put("geotiff", new GeoTIFFFormatter());
		resultMap.put("covjson", new CovJSONFormatter());
		resultMap.put("gmlcov", new GMLCOVFormatter());	
	}
	
	
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
	
}
