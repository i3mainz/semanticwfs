package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
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
		// TODO Auto-generated method stub
		return null;
	}

}
