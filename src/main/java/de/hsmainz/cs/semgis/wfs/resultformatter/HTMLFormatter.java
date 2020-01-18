package de.hsmainz.cs.semgis.wfs.resultformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class HTMLFormatter extends ResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype) throws XMLStreamException {
		JSONObject geojson=new JSONObject(resultMap.get("geojson").formatter(results,offset,startingElement,featuretype));
		System.out.println(geojson);
		StringBuilder builder=new StringBuilder();
		builder.append("<script>var overlayMaps={}; var overlayControl; var markercollection=[];var geojson="+geojson.toString()+"</script>");
		builder.append("<div id=\"mapid\" style=\"height: 500px;\"><script>var map = L.map('mapid',{fullscreenControl: true,fullscreenControlOptions: {position: 'topleft'}}).setView([51.505, -0.09], 13); var layer=L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'});");
		builder.append("var baseMaps = {\"OSM\": layer}; baseMaps[\"OSM\"].addTo(map);" + 
				"	L.control.scale({\r\n" + 
				"	position: 'bottomright',\r\n" + 
				"	imperial: false\r\n" + 
				"}).addTo(map);" + 
				"var layercontrol=L.control.layers(baseMaps,overlayMaps).addTo(map);");
		builder.append("var geojsonlayer=L.geoJSON(geojson, {coordsToLatLng: function (coords) {return new L.LatLng(coords[1], coords[0], coords[2]);},"+
				"		onEachFeature: function (feature, layer) {\r\n" + 
				"	 var popup=\"Official Data<br/><ul>\"\r\n" + 
				"	 for(prop in feature.properties){\r\n" + 
				"	 	popup+=\"<li>\"+prop+\" - \"+feature.properties[prop]+\"</li>\"\r\n" + 
				"	 }\r\n" + 
				"	 popup+=\"</ul>\"\r\n" + 
				"	 console.log(feature)\r\n" + 
				"         layer.bindPopup(popup);\r\n" + 
				"     }}).addTo(map);map.fitBounds(geojsonlayer.getBounds());</script></div>");
		builder.append("<table width=\"100%\" align=\"center\" id=\"queryres\" class=\"tablesorter\" border=\"1\"><tr>");
		Boolean first=true;
		JSONArray features=geojson.getJSONArray("features");
		for(int i=0;i<features.length();i++) {
			if(first) {
				builder.append("<thead><tr>");
				for(String key:features.getJSONObject(0).getJSONObject("properties").keySet()) {
					builder.append("<th>"+key+"</th>");
				}
				builder.append("</tr></thead><tbody>");
				first=false;
			}
			System.out.println(builder.toString());
			for(String key:features.getJSONObject(i).getJSONObject("properties").keySet()) {
				System.out.println(key);
				String value=features.getJSONObject(i).getJSONObject("properties").get(key).toString();
				if(value.contains("http")) {
					builder.append("<td align=\"center\"><a href=\""+value+"\" target=\"_blank\">"+value+"</a></td>");
				}else {
					builder.append("<td align=\"center\">"+value+"</td>");
				}
			}
			builder.append("</tr>");
		}
		builder.append("</tbody></table>");
		System.out.println(builder.toString());
		return builder.toString();
	}

}
