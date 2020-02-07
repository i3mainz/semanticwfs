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

	public String htmlHeader = "";

	public HTMLFormatter() {
		super();
		try {
			this.htmlHeader = readFile("htmltemplate.txt", StandardCharsets.UTF_8);
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
			String typeColumn, Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,String epsg) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg));
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		// System.out.println(geojson);
		StringBuilder builder = new StringBuilder();
		if (!onlyproperty) {
			builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\"" + typeColumn
					+ "\"; var markercollection=[];var epsg=\""+epsg+"\" var geojson=" + geojson.toString() + "</script>");
			builder.append(htmlHeader);
		}
		builder.append("<table width=\"100%\" align=\"center\" id=\"queryres\" class=\"tablesorter\" border=\"1\">");
		Boolean first = true;
		JSONArray features = geojson.getJSONArray("features");

		for (int i = 0; i < features.length(); i++) {
			if (first) {
				builder.append("<thead><tr>");
				if (!onlyproperty) {					
					for (String key : features.getJSONObject(0).getJSONObject("properties").keySet()) {
						if (key.contains("http")) {
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
					}

				}else {
					if (propertytype.contains("http")) {
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
				builder.append("</tr></thead>");
				first = false;
			}
			builder.append("<tbody><tr>");
			// System.out.println(builder.toString());
			for (String key : features.getJSONObject(i).getJSONObject("properties").keySet()) {
				// System.out.println(key);
				String value = features.getJSONObject(i).getJSONObject("properties").get(key).toString();
				if (value.contains("http")) {
					if (value.contains("^^")) {
						builder.append("<td align=\"center\"><a href=\"" + value + "\" target=\"_blank\">"
								+ value.substring(0, value.lastIndexOf('^') - 1) + "</a></td>");
					} else if (value.contains("#")) {
						builder.append("<td align=\"center\"><a href=\"" + value + "\" target=\"_blank\">"
								+ value.substring(value.lastIndexOf('#') + 1) + "</a></td>");
					} else {
						builder.append("<td align=\"center\"><a href=\"" + value + "\" target=\"_blank\">" + value
								+ "</a></td>");
					}
				} else {
					builder.append("<td align=\"center\">" + value + "</td>");
				}
			}
			builder.append("</tr>");
		}
		builder.append("</tbody></table>");
		// System.out.println(builder.toString());
		return builder.toString();
	}

}
