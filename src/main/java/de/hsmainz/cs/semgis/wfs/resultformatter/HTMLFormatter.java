package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
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
	
	
	public String keyPathToLabel(String keyPath) {
		if(!keyPath.contains(";")) {
			if (keyPath.contains("#")) {
				return keyPath.substring(keyPath.lastIndexOf('#') + 1);
			} else {
				return keyPath.substring(keyPath.lastIndexOf('/') + 1);
			}
		}
		String result="";
		String[] splitted=keyPath.split(";");
		int i=0;
		for(i=0;i<splitted.length;i++) {	
			if (splitted[i].contains("#")) {
				result+=splitted[i].substring(splitted[i].lastIndexOf('#') + 1);
			} else {
				result+=splitted[i].substring(splitted[i].lastIndexOf('/') + 1);
			}
			if(i<splitted.length-1) {
				result+=".";
			}
		}
		return result;
	}
	
	public void collectColumns(StringBuilder builder,JSONObject properties,List<String> propToTableCol,String keyPath,Integer index) {
		for (String key : properties.keySet()) {
			Boolean subcols=false;
			if(keyPath.isEmpty()) {
				try {
					collectColumns(builder,properties.getJSONObject(key),propToTableCol,key,index);
					subcols=true;
				}catch(Exception e) {
					
				}
			}else {
				try {
					collectColumns(builder,properties.getJSONObject(key),propToTableCol,keyPath+";"+key,index);
					subcols=true;
				}catch(Exception e) {
					
				}
			}
			if(!subcols) {
				String label="";
			if(keyPath.isEmpty()) {
				label=key;
				propToTableCol.add(key);
			}else {
				propToTableCol.add(keyPath+";"+key);
				label=keyPath+";"+key;
			}	
			index++;
			if (key.startsWith("http") || key.startsWith("www.")) {
				if (key.contains("#")) {
					builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">"+
					keyPathToLabel(label) + "</a></td>");
				} else {
					builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">"
							+ keyPathToLabel(label) + "</a></td>");
				}
			} else {
				builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">" + keyPathToLabel(label)
						+ "</a></td>");
			}
			}	
		}
		
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype,
			String propertytype, 
			String typeColumn, Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY));
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		// System.out.println(geojson);
		StringBuilder builder = new StringBuilder();
		if (!onlyproperty) {
			builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\"" + typeColumn
					+ "\"; var markercollection=[];var epsg=\""+epsg+"\"; var invertXY="+invertXY+"; var geojson=" + geojson.toString());
			builder.append("</script>");
			builder.append(htmlHeader);
		}	
		builder.append("</div><div class=\"row\"><div class=\"left col-sm-12\"><select id=\"styles\">");
		builder.append("<option value=\""+featuretype+"_DefaultStyle\">"+featuretype+"_DefaultStyle</option>");
		builder.append("</select><button id=\"applystyle\"/>Apply Style</button><table width=\"100%\" align=\"center\" id=\"queryres\" class=\"description\" border=\"1\"></table>");
		/*
		Boolean first = true;
		JSONArray features = geojson.getJSONArray("features");
		List<String> propToTableCol=new LinkedList<String>();
		for (int i = 0; i < features.length(); i++) {
			if (first) {
				builder.append("<thead><tr class=\"even\"><th align=\"center\">FeatureID</th>");
				if (!onlyproperty) {
					collectColumns(builder, features.getJSONObject(0).getJSONObject("properties"), propToTableCol, "",0);
					System.out.println(propToTableCol);
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
			builder.append("<td align=\"center\"><a href=\"" + features.getJSONObject(i).get("id") + "\" target=\"_blank\">");
			String vall=features.getJSONObject(i).get("id").toString();
			if (vall.contains("http")) {
				if (vall.contains("^^")) {
					builder.append("<a href=\"" + vall.substring(vall.lastIndexOf('^') +1) + "\" target=\"_blank\">"
							+ vall.substring(0, vall.lastIndexOf('^') - 1) + "</a>");
				} else if (vall.contains("#")) {
					if(alternativeFormat) {
						builder.append("<a href=\"" +WebService.wfsconf.get("baseurl")+"/collections/"+featuretype+"/items/"+ vall.substring(vall.lastIndexOf('#')+1)+ "\">"
								+  vall.substring(vall.lastIndexOf('#')+1) + "</a>");
					}else {
						builder.append("<a href=\"" + vall + "\" target=\"_blank\">"
								+ vall.substring(vall.lastIndexOf('#') + 1) + "</a>");
					}
					
				} else {
					if(alternativeFormat) {
						builder.append("<a href=\"" +WebService.wfsconf.get("baseurl")+"/collections/"+featuretype+"/items/"+ vall.substring(vall.lastIndexOf('/')+1)+ "\">"
								+  vall.substring(vall.lastIndexOf('/')+1) + "</a>");
					}else {
						builder.append("<a href=\"" + vall + "\" target=\"_blank\">" + vall.substring(vall.lastIndexOf('/')+1)
							+ "</a>");
					}
				}
			} else {
				builder.append(vall);
			}
			builder.append("</td>");
			// System.out.println(builder.toString());
			for(String key:propToTableCol) {
			//for (String key : features.getJSONObject(i).getJSONObject("properties").keySet()) {
				// System.out.println(key);
				//String key=propToTableCol.get(col);
				String value=null;
				String curvalue;
				JSONObject curobj=features.getJSONObject(i).getJSONObject("properties");
				System.out.println(key);
				if(key.contains(";")) {
					String[] splitted=key.split(";");
					System.out.println("Splitted key: "+Arrays.toString(splitted));
					for(String spl:splitted) {
						if(!spl.isEmpty()) {
							try {
								curobj=curobj.getJSONObject(spl);
								System.out.println(curobj.toString());
							}catch(Exception e) {
								value=curobj.get(spl).toString();
								System.out.println("Got Value: "+value);
							}
						}
					}
				}else {
					if(features.getJSONObject(i).getJSONObject("properties").has(key)) {
						value = features.getJSONObject(i).getJSONObject("properties").get(key).toString();
					}
				}
				//System.out.println(key+" - "+value);
				if(value!=null) {
				//String value = features.getJSONObject(i).getJSONObject("properties").get(key).toString();
				if(value.startsWith("[")) {
					value=value.replace("[","").replace("]", "").replace("\"", "");
					builder.append("<td align=\"center\">");
					for(String val:value.split(",")) {
						if(value.startsWith("www.")) {
							value="http://"+value;
						}
						if (val.contains("http")) {
							if (val.contains("^^")) {
								builder.append("<a href=\"" + val.substring(val.lastIndexOf('^') +1) + "\" target=\"_blank\">"
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
					if(value.startsWith("www.")) {
						value="http://"+value;
					}
					if (value.contains("http")) {
						if (value.contains("^^")) {
							builder.append("<td align=\"center\"><a href=\"" + value.substring(value.lastIndexOf('^') +1) + "\" target=\"_blank\">"
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
		*/
		// System.out.println(builder.toString());
		return builder.toString();
	}

}
