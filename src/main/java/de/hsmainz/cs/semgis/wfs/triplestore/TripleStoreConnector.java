package de.hsmainz.cs.semgis.wfs.triplestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.NodeValue;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.converters.AsGeoJSON;
import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.webservice.WebService;


public abstract class TripleStoreConnector {

	public static String prefixCollection="";
	
	static {
		try {
			BufferedReader reader=new BufferedReader(new FileReader(new File("prefixes.txt")));
			String line;
			while((line=reader.readLine())!=null){
				prefixCollection+=line+System.lineSeparator();
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static Pattern spatialFunctions=Pattern.compile(".*(equals|disjoint|intersects|touches|crosses|within|contains|overlaps|dwithin).*",Pattern.CASE_INSENSITIVE);
	
	static Pattern binaryOperators=Pattern.compile(".*(<|>|=|<=|>=|<>).*",Pattern.CASE_INSENSITIVE);
	
	protected static Map<String,Map<String,String>> featureTypes=new TreeMap<>();
	
	public static Double[] getBoundingBoxFromTripleStoreData(String triplestore,String queryString) {
		Double minx=Double.MAX_VALUE,maxx=Double.MIN_VALUE,miny=Double.MAX_VALUE,maxy=Double.MIN_VALUE;
		queryString=" SELECT ?the_geom "+queryString.substring(queryString.indexOf("WHERE"));
		try {
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore, query);
		ResultSet results = qexec.execSelect();
		Integer outercounter=0;
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			if(solu.get("the_geom")==null) {
				continue;
			}
			String wktLiteral=solu.get("the_geom").toString();
			System.out.println(solu.get("the_geom"));
			if(!wktLiteral.contains("(")) {
				continue;
			}
			wktLiteral=wktLiteral.substring(0,wktLiteral.indexOf("^^"));
			wktLiteral=wktLiteral.substring(wktLiteral.indexOf('(')).replace("(","").replace(")","").replace(","," ").trim();
			Integer counter=0;
			for(String coord:wktLiteral.split(" ")) {
				Double curcoord=Double.valueOf(coord);
				if(counter%2==0) {
					if(curcoord<minx) {
						minx=curcoord;
					}
					if(curcoord>maxx) {
						maxx=curcoord;
					}
				}else {
					if(curcoord<miny) {
						miny=curcoord;
					}
					if(curcoord>maxy) {
						maxy=curcoord;
					}
				}
				counter++;
			}
			outercounter++;
		}
		System.out.println(outercounter/2);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new Double[] {minx,miny,maxx,maxy};
	}
	
	public static void main(String[] args) {
		//System.out.println(CQLfilterStringToSPARQLQuery("abc=4 AND DISJOINT(the_geom, POLYGON((-90 40, -90 45, -60 45, -60 40, -90 40)))","namedplace"));
		System.out.println(getBoundingBoxFromTripleStoreData("https://query.wikidata.org/sparql", "SELECT ?wikidatacity ?wikidatacityLabel ?the_geom WHERE{ ?wikidatacity wdt:P31 wd:Q515 . ?wikidatacity wdt:P625 ?the_geom . SERVICE wikibase:label { bd:serviceParam wikibase:language '[AUTO_LANGUAGE],en'. } }"));
	}
	
	static Integer resultSetSize=0;
	
	public static String executeQuery(String query, String endpoint, Boolean geojsonout) {
		query = prefixCollection + query;
		System.out.println(query);
		Query queryjena = QueryFactory.create(query);
		//"http://localhost:8080/rdf4j-server/repositories/pleiades"
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, queryjena);
			ResultSet rs = qe.execSelect();
			JSONArray geojsonresults = new JSONArray();
			List<JSONArray> allfeatures = new LinkedList<JSONArray>();
			JSONObject result = new JSONObject();
			JSONArray obj = new JSONArray();
			Boolean first = true;
			String geomvarname="";
			String relationName = "";
			Integer counter=0;
			Boolean newobject=true;
			JSONObject jsonobj = new JSONObject();
			JSONObject properties = new JSONObject();
			List<JSONObject> geoms = new LinkedList<JSONObject>();
			System.out.println(resultSetSize);
			String lastgeom="",rel="",val="",lat="",lon="";
			int geomvars=0;
			while (rs.hasNext()) {
				QuerySolution solu=rs.next();
				counter++;
				if(!first) {
					if(!geomvarname.isEmpty() && solu.get(geomvarname)!=null && solu.get(geomvarname).toString().equals(lastgeom)) {
						newobject=false;
					}else {
						newobject=true;
					}
					if(newobject) {
						//System.out.println("Geomvars: "+geomvars);
						int geomcounter = 0;
						for (JSONObject geom : geoms) {
							JSONObject geojsonobj = new JSONObject();
							geojsonobj.put("type", "Feature");
							geojsonobj.put("properties", properties);
							geojsonobj.put("geometry", geom);
							allfeatures.get(geomcounter % geomvars).put(geojsonobj);
							geomcounter++;
						}
					}
				}
				if(newobject) {
					geomvars = 0;
					jsonobj = new JSONObject();
					properties = new JSONObject();
					geoms = new LinkedList<JSONObject>();
				}
				//System.out.println(counter);
				Iterator<String> varnames = solu.varNames();
				while (varnames.hasNext()) {

					String name = varnames.next();
					if (name.endsWith("_geom")) {
						System.out.println("Geomvar: "+name);
						geomvars++;
						geomvarname=name;
						if (first) {
							JSONObject geojsonresult = new JSONObject();
							geojsonresult.put("type", "FeatureCollection");
							geojsonresult.put("name", name);
							JSONArray features = new JSONArray();
							allfeatures.add(features);
							geojsonresults.put(geojsonresult);
							geojsonresults.getJSONObject(geojsonresults.length() - 1).put("features", features);
						}
						if(newobject) {
							AsGeoJSON geojson = new AsGeoJSON();
							lastgeom=solu.get(name).toString();
							try {
								NodeValue vall = geojson.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),
									solu.getLiteral(name).getDatatype()));
								JSONObject geomobj = new JSONObject(vall.asNode().getLiteralValue().toString());
								geoms.add(geomobj);
							} catch (Exception e) {
								e.printStackTrace();
							}
							properties.put(name, solu.get(name));
						}else{
							properties.put(name, lastgeom);
						}
					} 
					
					if (name.endsWith("_rel") || name.equals("rel")) {
						relationName = solu.get(name).toString();
						rel = solu.get(name).toString();
					}else if (name.endsWith("_val") || name.equals("val")) {
						val = solu.get(name).toString();
					}else if (name.equals("lat")) {
						lat = solu.get(name).toString();
					}else if (name.equals("lon")) {
						lon = solu.get(name).toString();
					} else {
						if (!relationName.isEmpty()) {
							// System.out.println("Putting property: "+relationName+" - "+solu.get(name));
							properties.put(relationName, solu.get(name));
						} else {
							properties.put(name, solu.get(name));
						}
					}
					//System.out.println(relationName);
					//System.out.println(name);
					//System.out.println(solu.get(name));
					if(!geojsonout) {
						jsonobj.put(name, solu.get(name));
					}
				}
				if (!rel.isEmpty() && !val.isEmpty()) {
					if(!rel.equals("http://www.opengis.net/ont/geosparql#hasGeometry") &&
							!rel.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
						properties.put(rel, val);
					}
					rel = "";
					val = "";
				}
				if(!lat.isEmpty() && !lon.isEmpty()) {
					System.out.println("LatLon: "+lat+","+lon);
					if(lat.contains("^^")) {
						lat=lat.substring(0,lat.indexOf("^^"));
					}
					if(lon.contains("^^")) {
						lon=lon.substring(0,lon.indexOf("^^"));
					}
					JSONObject geomobj=new JSONObject("{\"type\":\"Point\",\"coordinates\":["+lon+","+lat+"]}");
					geoms.add(geomobj);
					properties.put("lat", lat);
					properties.put("lon",lon);
					lat="";
					lon="";
				}
				first = false;
				if(!geojsonout) {
					obj.put(jsonobj);
				}
					first = false;
			}
			qe.close();
			result.put("geojson", geojsonresults);
			result.put("data", obj);
			result.put("size", counter);
			resultSetSize=counter;
			if (geojsonout) {
				return geojsonresults.toString();
			}
			return result.toString();
	}
	
	public static String getTemporalExtentFromTripleStoreData(String triplestore,String queryString) {
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore, query);
		ResultSet results = qexec.execSelect();
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			String wktLiteral=solu.get("the_geom").toString();
			
		}
		return "";
	}
	
	public static Map<String,String> getFeatureTypeInformation(String queryString,String queryurl,String featuretype,JSONObject workingobj){
		if(featureTypes.containsKey(featuretype.toLowerCase())) {
			return featureTypes.get(featuretype.toLowerCase());
		}
		Map<String,String> result=new TreeMap<>();
		Map<String,String> nscache=new TreeMap<>();
		System.out.println(prefixCollection+queryString);
		if(workingobj.has("useorderby") && workingobj.getBoolean("useorderby"))
			queryString+=" ORDER BY ?"+featuretype.toLowerCase()+System.lineSeparator();
		Query query = QueryFactory.create(prefixCollection+queryString+" LIMIT 1");
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query);
		ResultSet results = qexec.execSelect();
		if(!results.hasNext()) {
			return null;
		}
		QuerySolution solu=results.next();
		Iterator<String> varnames=solu.varNames();
		System.out.println(solu.get(featuretype.toLowerCase()));
		if(solu.get(featuretype.toLowerCase())!=null) {
			result=new TreeMap<>();
			queryString=queryString.replace("WHERE{","WHERE{ BIND( <"+solu.get(featuretype.toLowerCase())+"> AS ?"+featuretype.toLowerCase()+") ");
			System.out.println(prefixCollection+queryString);
			query = QueryFactory.create(prefixCollection+queryString);
			qexec = QueryExecutionFactory.sparqlService(queryurl, query);
			results = qexec.execSelect();
		}
		String rel="",val="";
		Integer attcount=0,nscounter=0;
		result.put("namespaces","");
		while(results.hasNext()) {
			solu=results.next();
			varnames=solu.varNames();
			while(varnames.hasNext()) {
				String varname=varnames.next();
				if(varname.equals("rel")) {
					rel=solu.get(varname).toString();
				}else if(varname.equals("val")){
					val=solu.get(varname).toString();
				}else if(varname.contains("_geom")){
					if(!nscache.containsKey("http://www.opengis.net/ont/geosparql#"))
							nscache.put("http://www.opengis.net/ont/geosparql#","ns"+nscounter++);
					result.put("http://www.opengis.net/ont/geosparql#asWKT",solu.getLiteral(varname).toString());
					//geomLiteral=solu.getLiteral(varname).toString();
				}else {
					String ns=null;
					String varval=solu.get(varname).toString();
					if(varval.contains("http") && varval.contains("#")) {
						ns=varval.substring(0,varval.lastIndexOf('#')+1);
					}else if(varval.contains("http") && varval.contains("/")) {
						ns=varval.substring(0,varval.lastIndexOf('/')+1);
					}
					if(ns!=null && !ns.isEmpty() && !nscache.containsKey(ns)) {
						nscache.put(ns,"ns"+nscounter++);
					}
					try {
						Literal lit=solu.getLiteral(varname);
						result.put(varname, lit.getDatatypeURI());
					}catch(Exception e) {
						result.put(varname, varval);	
					}  
				}

			}
			if(!rel.isEmpty() && !val.isEmpty()) {
				String ns=null;
				if(rel.contains("http") && rel.contains("#")) {
					ns=rel.substring(0,rel.lastIndexOf('#')+1);
				}else if(rel.contains("http") && rel.contains("/")) {
					ns=rel.substring(0,rel.lastIndexOf('/')+1);
				}
				if(ns!=null && !ns.isEmpty() && !nscache.containsKey(ns)) {
					nscache.put(ns,"ns"+nscounter++);
				}
				result.put(rel, val);
				rel="";
				val="";
			}
			attcount++;
		}
		WebService.nameSpaceCache.put(featuretype.toLowerCase(),nscache);
		System.out.println(result);
		workingobj.put("attcount",attcount);
		qexec.close();
			return result;
	}
	
	public static String getPropertyFromMapping(String typename,String propertyname) {
		for(String key:WebService.featureTypeCache.get(typename.toLowerCase()).keySet()) {
			if(key.contains(propertyname)) {
				propertyname=key;
				return key;
			}
		}
		return null;
	}
	
	public static String CQLfilterStringToSPARQLQuery(String filter,String bbox,String curquery,String queryurl,String featuretype) {
		if(filter.isEmpty() && bbox.isEmpty())
			return curquery;
		StringBuilder additionaltriples=new StringBuilder();
		StringBuilder builder=new StringBuilder();
		System.out.println("Curquery: "+curquery);
		if(!bbox.isEmpty()) {
			String[] bboxcoords=bbox.split(",");
			if(queryurl.contains("wikidata")) {
				String newcurquery="";
				String the_geomline="";
				for(String line:curquery.split(System.lineSeparator())) {
					if(line.contains("the_geom") && !line.contains("SELECT")) {
						the_geomline=line;					
					}else {
						newcurquery+=line+System.lineSeparator();
					}
				}
				curquery=newcurquery;
				builder.append("SERVICE wikibase:box { "+the_geomline+System.lineSeparator()+"  bd:serviceParam wikibase:cornerSouthWest \"POINT("+bboxcoords[1]+","+bboxcoords[0]+")\"^^geo:wktLiteral . "+System.lineSeparator()+"bd:serviceParam wikibase:cornerNorthEast \"POINT("+bboxcoords[3]+","+bboxcoords[2]+")\"^^geo:wktLiteral . "+System.lineSeparator()+" }"+System.lineSeparator());
				builder.append("FILTER(");
			}else {
				builder.append("FILTER(");
				builder.append(" geof:sfIntersects(\"POLYGON(("+bboxcoords[1]+" "+bboxcoords[0]+","+bboxcoords[1]+" "+bboxcoords[2]+","+bboxcoords[3]+" "+bboxcoords[2]+","+bboxcoords[3]+" "+bboxcoords[0]+","+bboxcoords[1]+" "+bboxcoords[0]+"))\"^^geo:wktLiteral,?the_geom) ");
			}
		}else {
			builder.append("FILTER(");
		}
		if(filter.isEmpty()) {
			builder.append(")");		
			return curquery+builder.toString().replace("FILTER()","")+System.lineSeparator();
		}
		if(filter.contains("AND")) {
			Boolean containedbetween=false;
			String betweenleftoperand="";
			String betweenrightoperand="";
			for(String filterex:filter.split("AND")) {
				System.out.println(filterex);
				if(filterex.contains("BETWEEN")) {
					containedbetween=true;
					betweenleftoperand=filterex.substring(0,filterex.indexOf("BETWEEN"));
					betweenrightoperand=filterex.substring(filterex.indexOf("BETWEEN")+8);
				}else if(!filterex.contains("BETWEEN") && containedbetween) {
					containedbetween=false;
					String propname=filterex.substring(0,filterex.indexOf("LIKE")).trim();
					additionaltriples.append("?"+betweenleftoperand+" <"+getPropertyFromMapping(featuretype, propname)+"> ?"+propname+" ."+System.lineSeparator());
					builder.append("?"+betweenleftoperand+" > "+betweenrightoperand+" && ?"+betweenleftoperand+" < "+filterex.trim()+" ");
				} if(filterex.equalsIgnoreCase("LIKE")){
					String propname=filterex.substring(0,filterex.indexOf("LIKE")).trim();
					additionaltriples.append("?"+featuretype.toLowerCase()+" <"+getPropertyFromMapping(featuretype, propname)+"> ?"+propname+" ."+System.lineSeparator());
					builder.append("regex(str(?"+propname+"),\""+filterex.substring(filterex.indexOf("LIKE")+4).trim()+"\",\"i\") ");
				}else if(spatialFunctions.matcher(filterex).matches()){
					String prefix=filterex.substring(0,filterex.indexOf(',')+1).trim();			
					builder.append("geo:sf"+prefix+" \""+filterex.substring(filterex.indexOf(',')+1,filterex.length()-1).trim()+"\"^^geo:wktLiteral)");
				}else if(binaryOperators.matcher(filterex).matches()) {
					String[] splitted=filterex.split("<|>|=|<=|>=|<>");
					if(filterex.contains("=")) {
						String propname=filterex.substring(0,filterex.indexOf('=')).toLowerCase();
						additionaltriples.append("?"+featuretype.toLowerCase()+" <"+getPropertyFromMapping(featuretype, propname)+"> ?"+propname+" ."+System.lineSeparator());
						builder.append("?"+filterex.toLowerCase().trim()+" ");
					}else {
						//builder.append("?"+featuretype.toLowerCase()+" "+getPropertyFromMapping(featuretype, )
					}
					
					//builder.append("?"+featuretype.toLowerCase()+" ?abc "+WebService+". "+System.lineSeparator());
					builder.append("?"+filterex);
				}
				builder.append(" && ");
			}
			builder.delete(builder.length()-4,builder.length());
		}else if(filter.contains("=")){
			builder.append("?"+filter.substring(0,filter.lastIndexOf('=')+1));
			String suffix=filter.substring(filter.lastIndexOf('=')+1);
			if(suffix.matches("[0-9\\.]+")) {
				builder.append(suffix+" ");
			}else {
				builder.append("\""+suffix+"\"");
			}
		}
		builder.append(")"+System.lineSeparator());
		return curquery+builder.toString();
	}
	
	public static String executePropertyValueQuery(String queryurl,String output,String propertyValue,
			String startingElement,String featuretype,
			String resourceids,JSONObject workingobj,String filter,String count,String resultType,String srsName) throws XMLStreamException {
		String queryString="";
		if(resultType.equalsIgnoreCase("hits")) {
			queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) "+queryString.substring(queryString.indexOf("WHERE"));
			//queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) WHERE { ?"+featuretype.toLowerCase()+" ?abc ?def .} "+System.lineSeparator();
		}else {
			queryString+=" SELECT ?"+featuretype.toLowerCase()+" ?member WHERE{"+System.lineSeparator();
		}
		if(!resourceids.isEmpty() && !resourceids.contains(",")) {
			queryString=queryString.replace("WHERE{","WHERE{"+System.lineSeparator()+" BIND( <"+workingobj.getString("namespace")+resourceids+"> AS ?"+workingobj.getString("indvar")+") "+System.lineSeparator());
		}else if(!resourceids.isEmpty() && resourceids.contains(",")) {
			StringBuilder toreplace=new StringBuilder();
			toreplace.append("WHERE{ "+System.lineSeparator()+"VALUES ?"+workingobj.getString("indvar")+"{ ");
			for(String uri:resourceids.split(",")) {
				toreplace.append("<"+workingobj.getString("namespace")+uri+"> "); 
			}
			toreplace.append("} "+System.lineSeparator());
			queryString=queryString.replace("WHERE{",System.lineSeparator()+toreplace.toString());	
		}else {
			queryString=queryString.replace("WHERE{","WHERE{"+System.lineSeparator());
		}
		queryString+="?"+workingobj.getString("indvar")+" <"+propertyValue+"> ?member ."+System.lineSeparator();
		queryString=queryString.substring(0,queryString.lastIndexOf('}'));
		queryString=CQLfilterStringToSPARQLQuery(filter,"",queryString,queryurl,featuretype);
		queryString+="}"+System.lineSeparator();
		if(!resultType.equalsIgnoreCase("hits") && workingobj.has("useorderby") && workingobj.getBoolean("useorderby"))
			queryString+=" ORDER BY ?"+featuretype.toLowerCase()+System.lineSeparator();
		Integer limit=Integer.valueOf(count);
		if(limit>0 && !resultType.equalsIgnoreCase("hits"))
			queryString+=" LIMIT "+limit;
		System.out.println(prefixCollection+queryString);
		System.out.println(resourceids);
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query);
		ResultFormatter resformat=ResultFormatter.getFormatter(output);
		if(resformat==null) {
			return null;
		}
		ResultSet results = qexec.execSelect();
		if(resultType.equalsIgnoreCase("hits")) {
			if(results.hasNext()) {
				return results.next().getLiteral("count").getString();
			}
		}
		String res=resformat.formatter(results,startingElement,featuretype,propertyValue,(workingobj.has("typeColumn")?workingobj.get("typeColumn").toString():""),true,false,srsName);
		qexec.close();
		if(resformat.lastQueriedElemCount==0) {
			return "";
		}
		return res;
	}
	
	
	public static String executeQuery(String queryString,String queryurl,String output,String count,
			String offset,String startingElement,String featuretype,String resourceids,JSONObject workingobj,
			String filter,String resultType,String srsName,String bbox) throws XMLStreamException {
		System.out.println(resultType);
		queryString=queryString.replace(".","."+System.lineSeparator());
		System.out.println(queryString);
		if(resultType.equalsIgnoreCase("hits")) {
			queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) "+queryString.substring(queryString.indexOf("WHERE"));
			//queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) WHERE{ ?"+featuretype.toLowerCase()+" ?abc ?def .}"+System.lineSeparator();
		}else {
			queryString+=" SELECT ?"+featuretype.toLowerCase()+" ?member WHERE{"+System.lineSeparator();
		}
		if(!resourceids.isEmpty() && !resourceids.contains(",")) {
			queryString=queryString.replace("WHERE{","WHERE{"+System.lineSeparator()+" BIND( <"+workingobj.getString("namespace")+resourceids+"> AS ?"+workingobj.getString("indvar")+") ");
		}else if(!resourceids.isEmpty() && resourceids.contains(",")) {
			StringBuilder toreplace=new StringBuilder();
			toreplace.append("WHERE{ "+System.lineSeparator()+" VALUES ?"+workingobj.getString("indvar")+"{ ");
			for(String uri:resourceids.split(",")) {
				toreplace.append("<"+workingobj.getString("namespace")+uri+"> "); 
			}
			toreplace.append("}"+System.lineSeparator());
			queryString=queryString.replace("WHERE{",System.lineSeparator()+toreplace.toString());	
		}else {
			queryString=queryString.replace("WHERE{","WHERE{"+System.lineSeparator());
		}
		System.out.println("PreCurQuery: "+queryString);
		queryString=queryString.substring(0,queryString.lastIndexOf('}'));
		queryString=CQLfilterStringToSPARQLQuery(filter,bbox,queryString,queryurl,featuretype)+"}";
		if(!resultType.equalsIgnoreCase("hits") && workingobj.has("useorderby") && workingobj.getBoolean("useorderby"))
			queryString+=System.lineSeparator()+"ORDER BY ?"+featuretype.toLowerCase()+System.lineSeparator();
		Integer limit=Integer.valueOf(count);
		Integer offsetval=Integer.valueOf(offset);
		System.out.println("Count: "+count);
		if(limit>=1 && !resultType.equalsIgnoreCase("hits")) {
			queryString+=" LIMIT "+count+System.lineSeparator();
		}
		if(offsetval>0) {
			queryString+=" OFFSET "+offsetval+System.lineSeparator();
		}
		System.out.println(prefixCollection+queryString);
		System.out.println(resourceids);
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query);
		ResultFormatter resformat=ResultFormatter.getFormatter(output);
		if(resformat==null) {
			return null;
		}
		ResultSet results = qexec.execSelect();
		if(resultType.equalsIgnoreCase("hits")) {
			if(results.hasNext()) {
				return results.next().getLiteral("count").getString();
			}
		}
		String res=resformat.formatter(results,startingElement,featuretype,"",(workingobj.has("typeColumn")?workingobj.get("typeColumn").toString():""),false,false,srsName);
		qexec.close();
		if(resformat.lastQueriedElemCount==0) {
			return "";
		}
		return res;
	}

}

