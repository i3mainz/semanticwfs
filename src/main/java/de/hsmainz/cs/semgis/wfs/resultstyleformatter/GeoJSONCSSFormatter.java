package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeoJSONCSSFormatter extends ResultStyleFormatter {

	@Override
	public String formatter(ResultSet results,String featuretype) throws XMLStreamException {
		JSONObject styleproperties=new JSONObject();
		while(results.hasNext()) {
			
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	public JSONObject cssLiteralToJSON(String cssString) {
		JSONObject styleproperties=new JSONObject();
		if(cssString.contains(";")) {
			for(String statement:cssString.split(";")) {
				String[] split=statement.split(":");
				styleproperties.put(split[0],split[1]);
			}
		}else {
			String[] split=cssString.split(":");
			styleproperties.put(split[0],split[1]);
		}
		return styleproperties;
	}

	@Override
	public String formatGeometry(String geometrytype,StyleObject styleobj) {
		if(geometrytype.contains("Point")) {
		    JSONObject props=cssLiteralToJSON(styleobj.pointStyle);
		    if(styleobj.pointImage!=null) {
		    	JSONObject iconobj=new JSONObject();
		    	if(styleobj.pointImage.contains("svg")) {
		    		iconobj.put("iconUrl", "url('data:image/svg+xml;utf8,"+styleobj.pointImage+"')");
		    	}else if(styleobj.pointImage.contains("http")) {
		    		iconobj.put("iconUrl", styleobj.pointImage);
		    	}else {
		    		iconobj.put("iconUrl", styleobj.pointImage);
		    	}
		    	JSONArray size=new JSONArray();
		    	size.put(32);
		    	size.put(32);
		    	JSONArray anchor=new JSONArray();
		    	size.put(16);
		    	size.put(16);
		    	iconobj.put("iconSize",size);
		    	iconobj.put("iconAnchor", anchor);
				props.put("icon", iconobj);
		    }
		}
		if(geometrytype.contains("LineString")) {
			return cssLiteralToJSON(styleobj.lineStringStyle).toString();
		}
		if(geometrytype.contains("Polygon")) {
			return cssLiteralToJSON(styleobj.polygonStyle).toString();
		}
		return "{}";
	}

}
