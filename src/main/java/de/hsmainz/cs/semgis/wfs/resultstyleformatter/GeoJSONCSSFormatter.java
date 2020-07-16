package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Reads an ontological style description and formats it to GeoJSONCSS.
 *
 */
public class GeoJSONCSSFormatter extends ResultStyleFormatter {

	/**
	 * Constructor for this class.
	 */
	public GeoJSONCSSFormatter() {
		this.styleAttribute="style";
	}
	
	@Override
	public String formatter(ResultSet results,String featuretype) throws XMLStreamException {
		return null;
	}
	
	/**
	 * Converts a CSS literal given in the ontology to a JSON representation.
	 * @param cssString the literal value
	 * @return the JSON object to contain the style information
	 */
	public JSONObject cssLiteralToJSON(String cssString) {
		JSONObject styleproperties=new JSONObject();
		if(cssString==null)
			return styleproperties;
		cssString=cssString.substring(0,cssString.indexOf("^^"));
		if(cssString.contains(";")) {
			for(String statement:cssString.split(";")) {
				String[] split=statement.split(":");
				styleproperties.put(split[0].replace("\\","").replace("\"","").replace("{","").replace("}","").trim(),
						split[1].replace("\\","").replace("\"","").replace("{","").replace("}","").trim());
			}
		}else if(cssString.contains(",")) {
			for(String statement:cssString.split(",")) {
				String[] split=statement.split(":");
				styleproperties.put(split[0].replace("\\","").replace("\"","").replace("{","").replace("}","").trim(),
						split[1].replace("\\","").replace("\"","").replace("{","").replace("}","").trim());
			}
		}else {
			String[] split=cssString.split(":");
			styleproperties.put(split[0].replace("\\","").replace("\"","").replace("{","").replace("}","").trim(),
					split[1].replace("\\","").replace("\"","").replace("{","").replace("}","").trim());
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
			System.out.println(props.toString());
		    return props.toString();
		}
		if(geometrytype.contains("LineString")) {
			JSONObject props=cssLiteralToJSON(styleobj.lineStringStyle);
			 if(styleobj.lineStringImage!=null) {
			    	JSONObject iconobj=new JSONObject();
			    	if(styleobj.lineStringImage.contains("svg")) {
			    		iconobj.put("iconUrl", "url('data:image/svg+xml;utf8,"+styleobj.lineStringImage+"')");
			    	}else if(styleobj.lineStringImage.contains("http")) {
			    		iconobj.put("iconUrl", styleobj.lineStringImage);
			    	}else {
			    		iconobj.put("iconUrl", styleobj.lineStringImage);
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
			if(styleobj.hatch!=null) {
				JSONObject hatch=cssLiteralToJSON(styleobj.hatch);
				props.put("hatch",hatch);
			}
			System.out.println(props);
			return props.toString();
		}
		if(geometrytype.contains("Polygon")) {
			JSONObject props=cssLiteralToJSON(styleobj.polygonStyle);
			if(styleobj.polygonImage!=null) {
		    	JSONObject iconobj=new JSONObject();
		    	if(styleobj.polygonImage.contains("svg")) {
		    		iconobj.put("iconUrl", "url('data:image/svg+xml;utf8,"+styleobj.polygonImage+"')");
		    	}else if(styleobj.polygonImage.contains("http")) {
		    		iconobj.put("iconUrl", styleobj.polygonImage);
		    	}else {
		    		iconobj.put("iconUrl", styleobj.polygonImage);
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
			if(styleobj.hatch!=null) {
				JSONObject hatch=cssLiteralToJSON(styleobj.hatch);
				props.put("hatch",hatch);
			}
			System.out.println(props);
			return props.toString();
		}
		return "{}";
	}

}
