package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.triplestore.TripleStoreConnector;
import de.hsmainz.cs.semgis.wfs.webservice.WebService;

public class HTMLFormatter extends ResultFormatter {

	public String htmlHeader = "";
	
	public String htmlHeader2 ="";

	public HTMLFormatter() {
		super();
		try {
			this.htmlHeader = readFile("htmltemplate.txt", StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.htmlHeader2 = readFile("htmltemplate2.txt", StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.mimeType = "text/html";
		this.exposedType = "text/html";
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype,
			String propertytype, 
			String typeColumn, Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle));
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		// System.out.println(geojson);
		StringBuilder builder = new StringBuilder();
		if (!onlyproperty) {
			builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\"" + typeColumn
					+ "\"; var markercollection=[];var epsg=\""+epsg+"\"; var geojson=" + geojson.toString() + "</script>");
			builder.append(htmlHeader);
		}
		
		builder.append("</div><div class=\"row\"><div class=\"left col-sm-12\"><select id=\"styles\">");
		builder.append("<option value=\""+featuretype+"_DefaultStyle\">"+featuretype+"_DefaultStyle</option>");
		builder.append("</select><button id=\"applystyle\"/>Apply Style</button><table width=\"100%\" align=\"center\" id=\"queryres\" class=\"description\" border=\"1\">");
		Boolean first = true;
		JSONArray features = geojson.getJSONArray("features");
		Map<Integer,String> propToTableCol=new TreeMap<Integer,String>();
		for (int i = 0; i < features.length(); i++) {
			if (first) {
				builder.append("<thead><tr class=\"even\">");
				if (!onlyproperty) {	
					int j=0;
					for (String key : features.getJSONObject(0).getJSONObject("properties").keySet()) {
						propToTableCol.put(j,key);
						if (key.startsWith("http") || key.startsWith("www.")) {
							if (key.contains("#")) {
								builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">"
										+ key.substring(key.lastIndexOf('#') + 1) + "</a></td>");
							} else {
								builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">"
										+ key.substring(key.lastIndexOf('/') + 1) + "</a></td>");
							}
						} else {
							builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">" + key
									+ "</a></td>");
						}
						j++;
					}

				}else {
					if (propertytype.startsWith("http")) {
						if (propertytype.contains("#")) {
							builder.append("<th align=\"center\"><a href=\"" + propertytype + "\" target=\"_blank\">"
									+ propertytype.substring(propertytype.lastIndexOf('#') + 1) + "</a></td>");
						} else {
							builder.append("<th align=\"center\"><a href=\"" + propertytype + "\" target=\"_blank\">"
									+ propertytype.substring(propertytype.lastIndexOf('/') + 1) + "</a></td>");
						}
					} else {
						builder.append("<th align=\"center\"><a href=\"" + propertytype + "\" target=\"_blank\">" + propertytype
								+ "</a></td>");
					}
				}
				builder.append("</tr></thead><tbody>");
				first = false;
			}
			if(i%2==0){
			    builder.append("<tr class=\"even\">");			    
			}else{
			    builder.append("<tr class=\"odd\">");			    
			}
			// System.out.println(builder.toString());
			for(Integer col:propToTableCol.keySet()) {
			//for (String key : features.getJSONObject(i).getJSONObject("properties").keySet()) {
				// System.out.println(key);
				String key=propToTableCol.get(col);
				if(features.getJSONObject(i).getJSONObject("properties").has(key)) {
				String value = features.getJSONObject(i).getJSONObject("properties").get(key).toString();
				if(value.startsWith("[")) {
					value=value.replace("[","").replace("]", "").replace("\"", "");
					builder.append("<td align=\"center\">");
					for(String val:value.split(",")) {
						if (val.contains("http") || value.startsWith("www.")) {
							if (val.contains("^^")) {
								builder.append("<a href=\"" + val.substring(val.lastIndexOf('^') +2) + "\" target=\"_blank\">"
										+ val.substring(0, val.lastIndexOf('^') - 1) + "</a>");
							} else if (val.contains("#")) {
								builder.append("<a href=\"" + val + "\" target=\"_blank\">"
										+ val.substring(val.lastIndexOf('#') + 1) + "</a>");
							} else {
								builder.append("<a href=\"" + val + "\" target=\"_blank\">" + val.substring(val.lastIndexOf('/')+1)
										+ "</a>");
							}
						} else {
							builder.append(value);
						}
						builder.append("<br/>");
					}
					builder.append("</td>");
				}else {
					if (value.contains("http") || value.startsWith("www.")) {
						if (value.contains("^^")) {
							builder.append("<td align=\"center\"><a href=\"" + value.substring(value.lastIndexOf('^') +2) + "\" target=\"_blank\">"
									+ value.substring(0, value.lastIndexOf('^') - 1) + "</a></td>");
						} else if (value.contains("#")) {
							builder.append("<td align=\"center\"><a href=\"" + value + "\" target=\"_blank\">"
									+ value.substring(value.lastIndexOf('#') + 1) + "</a></td>");
						} else {
							builder.append("<td align=\"center\"><a href=\"" + value + "\" target=\"_blank\">" + value.substring(value.lastIndexOf('/')+1)
									+ "</a></td>");
						}
					} else {
						builder.append("<td align=\"center\">" + value + "</td>");
					}	
				}
				
				}else {
					builder.append("<td></td>");
				}
			}
			builder.append("</tr>");
		}
		builder.append("</tbody></table></div></div>");
		// System.out.println(builder.toString());
		return builder.toString();
	}

}
