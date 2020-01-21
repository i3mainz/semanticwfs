package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class HTMLFormatter extends ResultFormatter {

	public String htmlHeader="";
	
	public HTMLFormatter() {
		super();
		try {
			this.htmlHeader=readFile("htmltemplate.txt", StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn) throws XMLStreamException {
		ResultFormatter format=resultMap.get("geojson");
		JSONObject geojson=new JSONObject(format.formatter(results,offset,startingElement,featuretype,typeColumn));
		this.lastQueriedElemCount=format.lastQueriedElemCount;
		//System.out.println(geojson);
		StringBuilder builder=new StringBuilder();
		builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\""+typeColumn+"\"; var markercollection=[];var geojson="+geojson.toString()+"</script>");
		builder.append(htmlHeader);
		/*builder.append("<div id=\"mapid\" style=\"height: 500px;\"><script>var map = L.map('mapid',{fullscreenControl: true,fullscreenControlOptions: {position: 'topleft'}}).setView([51.505, -0.09], 13); var layer=L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'});");
		builder.append("var baseMaps = {\"OSM\": layer}; baseMaps[\"OSM\"].addTo(map);" + 
				"	L.control.scale({\r\n" + 
				"	position: 'bottomright',\r\n" + 
				"	imperial: false\r\n" + 
				"}).addTo(map);" + 
				"var layercontrol=L.control.layers(baseMaps,overlayMaps).addTo(map);");
		builder.append("var geojsonlayer=L.geoJSON(geojson, {coordsToLatLng: function (coords) {return new L.LatLng(coords[1], coords[0], coords[2]);},"+
				"		onEachFeature: function (feature, layer) {\r\n" + 
				"	 var popup=\"Item: <a href='\"+feature.id+\"'>\"+(feature.id.includes('#')?feature.id.substring(feature.id.lastIndexOf('#')+1):feature.id)+\"</a><br/>Properties:<ul>\"\r\n" + 
				"   Object.keys(feature.properties).sort().forEach(function(prop) {\r\n" +
				" if(prop.includes(\"http\") && prop.includes(\"#\")){\r\n"+
				"	 	popup+=\"<li><a href='\"+prop+\"' target='_blank'>\"+prop.substring(prop.lastIndexOf('#')+1)+\"</a> - \"\r\n" +
				" }else if(prop.includes(\"http\")){\r\n"+
				"	 	popup+=\"<li><a href='\"+prop+\"' target='_blank'>\"+prop.substring(prop.lastIndexOf('/')+1)+\"</a> - \"\r\n" +
				" }else{\r\n"+
				"	 	popup+=\"<li>\"+prop+\" - \"\r\n" + 
				"	 }\r\n" + 
				" if(feature.properties[prop].includes(\"http\") && feature.properties[prop].includes(\"^^\")){\r\n"+
				"     popup+=\"<a href='\"+feature.properties[prop]+\"' target='_blank'>\"+feature.properties[prop].substring(0,feature.properties[prop].lastIndexOf('^')-1)+\"</a></li>\"\r\n"+
				" }else if(feature.properties[prop].includes(\"http\") && feature.properties[prop].includes(\"#\")){\r\n"+
				"     popup+=\"<a href='\"+feature.properties[prop]+\"' target='_blank'>\"+feature.properties[prop].substring(feature.properties[prop].lastIndexOf('#')+1)+\"</a></li>\"\r\n"+				
				" }else if(feature.properties[prop].includes(\"http\") || feature.properties[prop].includes(\"file:/\")){\r\n"+
				"     popup+=\"<a href='\"+feature.properties[prop]+\"' target='_blank'>\"+feature.properties[prop].substring(feature.properties[prop].lastIndexOf('/')+1)+\"</a></li>\"\r\n"+
				" }else{\r\n"+
				"  popup+=feature.properties[prop]+\"</li>\"\r\n"+
				"	 }});popup+=\"</ul>\"\r\n" + 
				"	 console.log(feature)\r\n" + 
				"         layer.bindPopup(popup,{maxWidth : 560});\r\n" + 
				"     }}).addTo(map);map.fitBounds(geojsonlayer.getBounds());</script></div>");*/
		builder.append("<table width=\"100%\" align=\"center\" id=\"queryres\" class=\"tablesorter\" border=\"1\">");
		Boolean first=true;
		JSONArray features=geojson.getJSONArray("features");
		for(int i=0;i<features.length();i++) {
			if(first) {
				builder.append("<thead><tr>");
				for(String key:features.getJSONObject(0).getJSONObject("properties").keySet()) {
					if(key.contains("http")) {
						if(key.contains("#")) {
							builder.append("<th align=\"center\"><a href=\""+key+"\" target=\"_blank\">"+key.substring(key.lastIndexOf('#')+1)+"</a></td>");
						}else {
							builder.append("<th align=\"center\"><a href=\""+key+"\" target=\"_blank\">"+key.substring(key.lastIndexOf('/')+1)+"</a></td>");
						}
					}else {
						builder.append("<th align=\"center\"><a href=\""+key+"\" target=\"_blank\">"+key+"</a></td>");
					}
				}
				builder.append("</tr></thead>");
				first=false;
			}
			builder.append("<tbody><tr>");
			//System.out.println(builder.toString());
			for(String key:features.getJSONObject(i).getJSONObject("properties").keySet()) {
				//System.out.println(key);
				String value=features.getJSONObject(i).getJSONObject("properties").get(key).toString();
				if(value.contains("http")) {
					if(value.contains("^^")) {
						builder.append("<td align=\"center\"><a href=\""+value+"\" target=\"_blank\">"+value.substring(0,value.lastIndexOf('^')-1)+"</a></td>");
					}else if(value.contains("#")) {
						builder.append("<td align=\"center\"><a href=\""+value+"\" target=\"_blank\">"+value.substring(value.lastIndexOf('#')+1)+"</a></td>");
					}else {
						builder.append("<td align=\"center\"><a href=\""+value+"\" target=\"_blank\">"+value+"</a></td>");
					}
				}else {
					builder.append("<td align=\"center\">"+value+"</td>");
				}
			}
			builder.append("</tr>");
		}
		builder.append("</tbody></table>");
		//System.out.println(builder.toString());
		return builder.toString();
	}

}
