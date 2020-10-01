package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.webservice.WebService;

/**
 * Formats a query result to HTML.
 */
public class HTMLFormatter extends ResultFormatter {

	/** HTMLHeader for export 1. */
	public String htmlHeader = "";
	/** HTMLHeader for export 2. */
	public String htmlHeader2 = "";
	/** HTMLCovHeader for export 2. */
	public String htmlcovHeader = "";
	/**
	 * Constructor for this class. Reads HTML header from given HTML template files
	 */
	public HTMLFormatter() {
		super();
		try {
			this.htmlHeader = readFile("htmltemplate.txt", StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.htmlcovHeader = readFile("htmlcovtemplate.txt", StandardCharsets.UTF_8);
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
		this.urlformat = "html";
		this.label = "HTML";
		this.fileextension = "html";
		this.definition = "https://html.spec.whatwg.org";
	}

	/**
	 * Utility method to read a file.
	 * 
	 * @param path     The file path
	 * @param encoding the file encoding
	 * @return A string which includes the contents of the file
	 * @throws IOException on error
	 */
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	/**
	 * Shortens a given URI or a list of URIs to a label or a list of labels.
	 * 
	 * @param keyPath The String to shorten
	 * @return The label
	 */
	public String keyPathToLabel(String keyPath) {
		if (!keyPath.contains(";")) {
			if (keyPath.contains("#")) {
				return keyPath.substring(keyPath.lastIndexOf('#') + 1);
			} else {
				return keyPath.substring(keyPath.lastIndexOf('/') + 1);
			}
		}
		String result = "";
		String[] splitted = keyPath.split(";");
		int i = 0;
		for (i = 0; i < splitted.length; i++) {
			if (splitted[i].contains("#")) {
				result += splitted[i].substring(splitted[i].lastIndexOf('#') + 1);
			} else {
				result += splitted[i].substring(splitted[i].lastIndexOf('/') + 1);
			}
			if (i < splitted.length - 1) {
				result += ".";
			}
		}
		return result;
	}

	/**
	 * Reads columns recursively and prints the results in a given format.
	 * 
	 * @param builder        The StringBuilder for collecting the result
	 * @param properties     Properties as given by the GeoJSON input
	 * @param propToTableCol List of properties which are mapped to the result table
	 * @param keyPath        The property path to reconstruct the property chain in
	 *                       case of recursion
	 * @param index          The index in the table list
	 */
	public void collectColumns(StringBuilder builder, JSONObject properties, List<String> propToTableCol,
			String keyPath, Integer index) {
		for (String key : properties.keySet()) {
			Boolean subcols = false;
			if (keyPath.isEmpty()) {
				try {
					collectColumns(builder, properties.getJSONObject(key), propToTableCol, key, index);
					subcols = true;
				} catch (Exception e) {

				}
			} else {
				try {
					collectColumns(builder, properties.getJSONObject(key), propToTableCol, keyPath + ";" + key, index);
					subcols = true;
				} catch (Exception e) {

				}
			}
			if (!subcols) {
				String label = "";
				if (keyPath.isEmpty()) {
					label = key;
					propToTableCol.add(key);
				} else {
					propToTableCol.add(keyPath + ";" + key);
					label = keyPath + ";" + key;
				}
				index++;
				if (key.startsWith("http") || key.startsWith("www.")) {
					if (key.contains("#")) {
						builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">"
								+ keyPathToLabel(label) + "</a></td>");
					} else {
						builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">"
								+ keyPathToLabel(label) + "</a></td>");
					}
				} else {
					builder.append("<th align=\"center\"><a href=\"" + key + "\" target=\"_blank\">"
							+ keyPathToLabel(label) + "</a></td>");
				}
			}
		}

	}

	public Boolean getVectorOrCoverageRepresentationForHTML(QuerySolution first) {
		Iterator<String> it = first.varNames();
		while (it.hasNext()) {
			String name = it.next();
			System.out.println(name+" - "+first.get(name).toString());
			if (name.contains("_geom")) {
				if (vectorLiteralMap.contains(first.getLiteral(name).getDatatypeURI())) {
					System.out.println("Assessment Result True: "+first.getLiteral(name).getDatatypeURI());
					return true;
				}
				if (coverageLiteralMap.contains(first.getLiteral(name).getDatatypeURI())) {
					System.out.println("Assessment Result False: "+first.getLiteral(name).getDatatypeURI());
					return false;
				}
			}
		}
		System.out.println("Assessment Result Null: ");
		return null;
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype,
			String propertytype, 
			String typeColumn, Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,
			Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, JSONException, IOException {
		StringBuilder builder = new StringBuilder();
		if(coverage) {
			ResultFormatter format = resultMap.get("covjson");
			JSONObject covjson = new JSONObject(
					format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,"",
							indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out));
			this.lastQueriedElemCount = format.lastQueriedElemCount;
			builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\"" + typeColumn
					+ "\"; var markercollection=[];var epsg=\""+epsg+"\"; var invertXY="+invertXY+"; var cov=" + covjson.toString()+"; "+"var parameters=[");
			Iterator<String> it=covjson.getJSONObject("parameters").keySet().iterator();
			while(it.hasNext()) {
				builder.append("\""+it.next()+"\"");
				if(it.hasNext())
					builder.append(",");
			}
			builder.append("]"+System.lineSeparator());
			builder.append("</script>");
			builder.append(htmlcovHeader);
		}else {
		ResultFormatter format = resultMap.get("geojson");
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,"",
						indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out));
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		// System.out.println(geojson);

		if (!onlyproperty) {
			builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\"" + typeColumn
					+ "\"; var markercollection=[];var epsg=\""+epsg+"\"; var invertXY="+invertXY+"; var geojson=" + geojson.toString());
			builder.append("</script>");
			builder.append(htmlHeader);
		}	
		builder.append("</div><div class=\"row\"><div class=\"left col-sm-12\"><select id=\"styles\">");
		builder.append("<option value=\""+featuretype+"_DefaultStyle\">"+featuretype+"_DefaultStyle</option>");
		builder.append("</select><button id=\"applystyle\"/>Apply Style</button><table width=\"100%\" align=\"center\" id=\"queryres\" class=\"description\" border=\"1\">");
		Boolean first = true;
		JSONArray features = geojson.getJSONArray("features");
		List<String> propToTableCol=new LinkedList<String>();
		for (int i = 0; i < features.length(); i++) {
			if (first) {
				builder.append("<thead><tr class=\"even\"><th align=\"center\">FeatureID</th>");
				if (!onlyproperty) {
					collectColumns(builder, features.getJSONObject(0).getJSONObject("properties"), propToTableCol, "",0);
					//System.out.println(propToTableCol);
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
				JSONObject curobj=features.getJSONObject(i).getJSONObject("properties");
				//System.out.println(key);
				if(key.contains(";")) {
					String[] splitted=key.split(";");
					//System.out.println("Splitted key: "+Arrays.toString(splitted));
					for(String spl:splitted) {
						if(!spl.isEmpty()) {
							try {
								curobj=curobj.getJSONObject(spl);
								//System.out.println(curobj.toString());
							}catch(Exception e) {
								value=curobj.get(spl).toString();
								//System.out.println("Got Value: "+value);
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
							} else if (val.contains("@")) {
								builder.append("<a href=\"mailto:" + val + "\" target=\"_blank\">"
										+ val + "</a>");
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
						} else if (value.contains("@")) {
							builder.append("<a href=\"mailto:" + value + "\" target=\"_blank\">"
									+ value + "</a>");
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
		}
		// System.out.println(builder.toString());
		return builder.toString();
	}

}
