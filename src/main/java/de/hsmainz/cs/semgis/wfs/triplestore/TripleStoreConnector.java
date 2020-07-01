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
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.GeoJSONFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.ResultStyleFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
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
		if(queryString.contains("the_geom")) {
			queryString=" SELECT ?the_geom "+queryString.substring(queryString.indexOf("WHERE"));
		}else if(queryString.contains("lat") && queryString.contains("lon")) {
			queryString=" SELECT (MIN(?lat) AS ?minlat) (MAX(?lat) AS ?maxlat) (MIN(?lon) AS ?minlon) (MAX(?lon) AS ?maxlon) "+queryString.substring(queryString.indexOf("WHERE"));
		}
		try {
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore, query+" LIMIT 5000");
		ResultSet results = qexec.execSelect();
		Integer outercounter=0;
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			if(solu.get("the_geom")==null && solu.get("minlat")==null && solu.get("minlon")==null) {
				continue;
			}
			if(solu.get("minlon")!=null) {
				if(solu.get("minlon")!=null) {
					minx=Double.valueOf(solu.get("minlon").toString().substring(0,solu.get("minlon").toString().indexOf("^^")));
				}
				if(solu.get("minlat")!=null) {
					miny=Double.valueOf(solu.get("minlat").toString().substring(0,solu.get("minlat").toString().indexOf("^^")));
				}
				if(solu.get("maxlon")!=null) {
					maxx=Double.valueOf(solu.get("maxlon").toString().substring(0,solu.get("maxlon").toString().indexOf("^^")));
				}
				if(solu.get("maxlat")!=null) {
					maxy=Double.valueOf(solu.get("maxlat").toString().substring(0,solu.get("maxlat").toString().indexOf("^^")));
				}
				return new Double[] {minx,miny,maxx,maxy};
			}
			if(solu.get("the_geom")!=null) {
			String wktLiteral=solu.get("the_geom").toString();
			System.out.println(solu.get("the_geom"));
			if(!wktLiteral.contains("(")) {
				continue;
			}
			wktLiteral=wktLiteral.substring(0,wktLiteral.indexOf("^^"));
			wktLiteral=wktLiteral.substring(wktLiteral.indexOf('(')).replace("(","").replace(")","").replace(","," ").trim();
			Integer counter=0;
			for(String coord:wktLiteral.split(" ")) {
				if(coord.isEmpty())
					continue;
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
			}else if(solu.get("lat")!=null && solu.get("lon")!=null) {
				Double lon=Double.valueOf(solu.get("lon").toString().substring(0,solu.get("lon").toString().indexOf("^^")));
				Double lat=Double.valueOf(solu.get("lat").toString().substring(0,solu.get("lat").toString().indexOf("^^")));
				if(lon<minx) {
					minx=lon;
				}
				if(lon>maxx) {
					maxx=lon;
				}
				if(lat<miny) {
					miny=lat;
				}
				if(lat>maxy) {
					maxy=lat;
				}
			}
			outercounter++;
		}
		System.out.println(outercounter/2);
		qexec.close();
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
		GeoJSONFormatter form=new GeoJSONFormatter();
		String res=form.formatter(rs, "", "item", "", "", false, false, "","item","",null,null,null,false,false);
		qe.close();
		return res;
	}
	
	public static String getTemporalExtentFromTripleStoreData(String triplestore,String queryString) {
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore, query);
		ResultSet results = qexec.execSelect();
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			String wktLiteral=solu.get("the_geom").toString();
			
		}
		qexec.close();
		return "";
	}
	
	public static String getMetaData(String queryString,String queryurl,String featuretype,JSONObject workingobject) {
		queryString="SELECT ?pointstyle ?linestringstyle ?polygonstyle WHERE { <"+featuretype+"> semgis:hasStyle ?style . ?style semgis:hasPointStyle ?pointstyle . ?style semgis:hasPointStyle ?linestringstyle . ?style semgis:hasPointStyle ?polygonstyle . }";
		Query query = QueryFactory.create(prefixCollection+queryString+" LIMIT 1");
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query);	
		ResultSet results = qexec.execSelect();
		qexec.close();
		return "";
	}
	
	public static String getStyleNames(String baseurl,JSONObject workingobject,String format) {
		String queryString="SELECT ?style ?styleLabel ?pointstyle ?linestringstyle ?polygonstyle WHERE { <"+workingobject.getString("class")+"> owl:equivalentClass ?equivclass ."+System.lineSeparator()+" ?equivclass owl:intersectionOf ?intersect ."+System.lineSeparator()+" ?intersect rdf:rest ?rest."+System.lineSeparator()+" ?rest rdf:first ?first ."+System.lineSeparator()+" ?first owl:allValuesFrom ?styleclass ."+System.lineSeparator()+" ?style rdf:type ?styleclass."+System.lineSeparator()+" OPTIONAL{?style rdfs:label ?styleLabel .} }";
		Query query = QueryFactory.create(prefixCollection+queryString+" LIMIT 1");
		System.out.println(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(workingobject.getString("triplestore"), query);	
		ResultSet results = qexec.execSelect();
		String result="";
		if("json".equals(format)) {
			JSONObject resultjson=new JSONObject();
			resultjson.put("styles",new JSONArray());
			while(results.hasNext()) {
				JSONObject style=new JSONObject();
				QuerySolution curresult = results.next();
				style.put("uri",curresult.get("style"));
				style.put("name",curresult.get("style").toString().substring(curresult.get("style").toString().indexOf("#")+1));
				resultjson.getJSONArray("styles").put(style);
			}
			result=resultjson.toString(2);
		}else if("html".equals(format)) {
			result="";
			result+="<table border=1><tr><th>Stylename</th><th>Styletest</th></tr>";
			while(results.hasNext()) {
				QuerySolution curresult = results.next();
				result+="<tr><td><a href=\""+curresult.get("style")+"\">"+curresult.get("style").toString()
				.substring(curresult.get("style").toString().indexOf("#")+1)+"</a></td><td><a href=\""
				+baseurl+"/collections/"+workingobject.getString("name")+"/items?f=html&style="+curresult.get("style").toString()
				.substring(curresult.get("style").toString().indexOf("#")+1)+"\">View</a></tr>";
			}
			result+="</table>";
		}else {
			result="<?xml version=\"1.0\"?><styles>"+System.lineSeparator();
			while(results.hasNext()) {
				QuerySolution curresult = results.next();
				result+="<style uri=\""+curresult.get("style")+"\">"+curresult.get("style").toString().substring(curresult.get("style").toString().indexOf("#")+1)+"</style>"+System.lineSeparator();
			}
			result+="</styles>"+System.lineSeparator();
		}
		qexec.close();
		return result;
	}
	
	public static StyleObject getStyle(String featuretype,String stylename,String triplestore,String namespace) {
		if(WebService.styleCache.containsKey(triplestore) && WebService.styleCache.get(triplestore).containsKey(stylename)) {
			return WebService.styleCache.get(triplestore).get(stylename);
		}
		String queryString="SELECT ?style ?styleLabel ?pointStyle ?pointImage ?hatch ?linestringStyle ?linestringImage ?linestringImageStyle ?polygonStyle ?polygonImage WHERE { "
	    +" OPTIONAL {<"+namespace+stylename+"> rdfs:label ?styleLabel .}"+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:linestringImage ?linestringImage .} "+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:linestringImageStyle ?linestringImageStyle .} "+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:hatch ?hatch .}"+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:image ?pointImage .}"+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:imageStyle ?pointStyle .}"+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:linestringStyle ?linestringStyle .}"+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:polygonStyle ?polygonStyle .}"+System.lineSeparator()
		+" OPTIONAL {<"+namespace+stylename+"> semgis:polygonImage ?polygonImage .}"+System.lineSeparator()+" }";
		System.out.println(queryString);
		Query query = QueryFactory.create(prefixCollection+queryString+" LIMIT 1");
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore, query);	
		ResultSet results = qexec.execSelect();
		System.out.println("Style Query finished!");
		StyleObject result=null;
		if(results.hasNext()) {
			result=new StyleObject();
			QuerySolution solu=results.next();
			result.styleName=stylename;
			result.pointStyle=solu.get("pointStyle")!=null?solu.get("pointStyle").toString():null;
			result.pointImage=solu.get("pointImage")!=null?solu.get("pointImage").toString():null;
			result.lineStringImage=solu.get("linestringImage")!=null?solu.get("linestringImage").toString():null;
			result.lineStringImageStyle=solu.get("linestringImageStyle")!=null?solu.get("linestringImageStyle").toString():null;
			result.lineStringStyle=solu.get("linestringStyle")!=null?solu.get("linestringStyle").toString():null;
			result.polygonStyle=solu.get("polygonStyle")!=null?solu.get("polygonStyle").toString():null;
			result.polygonImage=solu.get("polygonImage")!=null?solu.get("polygonImage").toString():null;
			result.hatch=solu.get("hatch")!=null?solu.get("hatch").toString():null;
			if(!WebService.styleCache.containsKey(triplestore)) {
				WebService.styleCache.put(triplestore,new TreeMap<>());
			}
			WebService.styleCache.get(triplestore).put(stylename,result);			
		}
		qexec.close();
		return result;
	}
	
	public static List<String> getClassesFromOntology(String triplestoreurl){
		List<String> result=new LinkedList<String>();
		Query query = QueryFactory.create(prefixCollection+" SELECT ?class WHERE { ?class rdf:type ?class . ?class geo:hasGeometry ?geom . } ");
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestoreurl, query);
		ResultSet resformat=qexec.execSelect();
		while(resformat.hasNext()) {
			result.add(resformat.next().get("class").toString());
		}
		qexec.close();
		return result;
	}

	public static List<String> getPropertiesByClass(String triplestoreurl,String classs){
		List<String> result=new LinkedList<String>();
		Query query = QueryFactory.create(prefixCollection+" SELECT ?rel WHERE { <"+classs+"> ?rel ?val . } ");
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestoreurl, query);
		ResultSet resformat=qexec.execSelect();
		while(resformat.hasNext()) {
			result.add(resformat.next().get("rel").toString());
		}
		qexec.close();
		return result;
	}
	
	
	public static Map<String,String> getFeatureTypeInformation(String queryString,String queryurl,
			String featuretype,JSONObject workingobj){
		System.out.println("Getting FeatureType Information for "+featuretype+"...");
		if(featureTypes.containsKey(featuretype.toLowerCase())) {
			return featureTypes.get(featuretype.toLowerCase());
		}
		String indvar="item";
		if(workingobj.has("indvar")) {
			indvar=workingobj.getString("indvar");
		}
		Map<String,String> result=new TreeMap<>();
		Map<String,String> nscache=new TreeMap<>();
		System.out.println(prefixCollection+queryString+" LIMIT 1");
		//if(workingobj.has("useorderby") && workingobj.getBoolean("useorderby"))
			//queryString+=" ORDER BY ?"+indvar+System.lineSeparator();
		Query query;
		if(queryString.contains("?rel") && queryString.contains("?val")) {
			query = QueryFactory.create(prefixCollection+queryString+" LIMIT 100");
		}else {
			query = QueryFactory.create(prefixCollection+queryString+" LIMIT 1");
		}
		System.out.println("Starting query execution... "+queryurl);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query);
		qexec.setTimeout(1, TimeUnit.MINUTES);
		ResultSet results = qexec.execSelect();
		System.out.println("Hello");
		if(!results.hasNext()) {
			System.out.println("No results!?!");
			qexec.close();
			return null;
		}
		System.out.println("Executed query... getting results");
		QuerySolution solu=results.next();
		Iterator<String> varnames=solu.varNames();
		System.out.println(solu.get(indvar));
		if(solu.get(indvar)!=null) {
			result=new TreeMap<>();
			queryString=queryString.replace("WHERE{","WHERE{ BIND( <"+solu.get(indvar)+"> AS ?"+indvar+") ");
			System.out.println(prefixCollection+queryString);
			qexec.close();
			query = QueryFactory.create(prefixCollection+queryString);
			qexec = QueryExecutionFactory.sparqlService(queryurl, query);
			results = qexec.execSelect();
		}
		Map<String,String> rel = new TreeMap<String,String>();
		Map<String,String> val=new TreeMap<String,String>();
		String lat="",lon="",lastInd="";
		List<String> latlist=new LinkedList<String>();
		List<String> lonlist=new LinkedList<String>();
		Integer attcount=0,nscounter=0;
		result.put("namespaces","");
		while(results.hasNext()) {
			solu=results.next();
			varnames=solu.varNames();
			if(lastInd.isEmpty()) {
				if(!latlist.isEmpty() && !lonlist.isEmpty()) {
					if(latlist.size()==1 && lonlist.size()==1) {
						result.put("http://www.opengis.net/ont/geosparql#asWKT","Point("+lonlist.get(0)+" "+latlist.get(0)+")");
					}else if(latlist.get(latlist.size()-1).equals(latlist.get(0)) && lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
						StringBuilder builder=new StringBuilder();
						for(int i=0;i<latlist.size();i++) {
							builder.append(lonlist.get(i)+" "+latlist.get(i)+",");
						}
						builder.delete(builder.length()-1,builder.length());
						result.put("http://www.opengis.net/ont/geosparql#asWKT","Polygon(("+builder.toString()+"))");
					}else if(!latlist.get(latlist.size()-1).equals(latlist.get(0)) || !lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
						StringBuilder builder=new StringBuilder();
						for(int i=0;i<latlist.size();i++) {
							builder.append(lonlist.get(i)+" "+latlist.get(i)+",");
						}
						builder.delete(builder.length()-1,builder.length());
						result.put("http://www.opengis.net/ont/geosparql#asWKT","LineString(("+builder.toString()+"))");
					}
					latlist.clear();
					lonlist.clear();
				}
			}else if(!lastInd.equals(solu.get(indvar).toString())) {
				break;
			}
			while(varnames.hasNext()) {
				String varname=varnames.next();
				if(varname.endsWith("_rel") || varname.equals("rel") || varname.matches("rel[0-9]+$")) {
					rel.put(varname,solu.get(varname).toString());
				}else if(varname.endsWith("_val") || varname.equals("val") || varname.matches("val[0-9]+$")){
					val.put(varname,solu.get(varname).toString());
				}else if(varname.equals("lat")) {
					lat=solu.get(varname).toString();
				}else if(varname.equals("lon")){
					lon=solu.get(varname).toString();
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
						e.printStackTrace();
						result.put(varname, varval);	
					}  
				}

			}
			if(!lat.isEmpty() && !lon.isEmpty()) {
				String ns=null;
				if(lat.contains("^^")) {
					ns=lat.substring(lat.lastIndexOf("^^")+2);
				}else if(lat.contains("http") && lat.contains("#")) {
					ns=lat.substring(0,lat.lastIndexOf('#')+1);
				}else if(lat.contains("http") && lat.contains("/")) {
					ns=lat.substring(0,lat.lastIndexOf('/')+1);
				}
				if(ns!=null && !ns.isEmpty() && !nscache.containsKey(ns)) {
					nscache.put(ns,"ns"+nscounter++);
				}	
				if(lat.contains("^^") && lon.contains("^^")) {
					latlist.add(lat.substring(0,lat.indexOf("^^")));
					lonlist.add(lon.substring(0,lon.indexOf("^^")));
				}
				lat="";
				lon="";
			}
			if(!rel.isEmpty() && !val.isEmpty()) {
				String ns=null;
				int i=0;
				String attname="",vall="",rell="";
				Iterator<String> valit=val.values().iterator();
				while(valit.hasNext()) {
					vall=valit.next();
				}
				for(String rl:rel.values()) {
					if(rl.contains("http") && rl.contains("#")) {
						ns=rl.substring(0,rl.lastIndexOf('#')+1);
						attname+=rl.substring(rl.lastIndexOf('#')+1);
					}else if(rl.contains("http") && rl.contains("/")) {
						ns=rl.substring(0,rl.lastIndexOf('/')+1);
						attname+=rl.substring(rl.lastIndexOf('/')+1);
					}
					rell=rl;
					if(ns!=null && !ns.isEmpty() && !nscache.containsKey(ns)) {
						nscache.put(ns,"ns"+nscounter++);
					}
					if(i<(rel.size()-1)) {
						attname+=".";
					}
					i++;
				}	
				result.put(rell, vall);
				rel.clear();
				val.clear();
			}
			attcount++;
		}
		if(!latlist.isEmpty() && !lonlist.isEmpty()) {
			if(latlist.size()==1 && lonlist.size()==1) {
				result.put("http://www.opengis.net/ont/geosparql#asWKT","Point("+lonlist.get(0)+" "+latlist.get(0)+")");
			}else if(latlist.get(latlist.size()-1).equals(latlist.get(0)) && lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
				StringBuilder builder=new StringBuilder();
				for(int i=0;i<latlist.size();i++) {
					builder.append(lonlist.get(i)+" "+latlist.get(i)+",");
				}
				builder.delete(builder.length()-1,builder.length());
				result.put("http://www.opengis.net/ont/geosparql#asWKT","Polygon(("+builder.toString()+"))");
			}else if(!latlist.get(latlist.size()-1).equals(latlist.get(0)) || !lonlist.get(lonlist.size()-1).equals(lonlist.get(0))) {
				StringBuilder builder=new StringBuilder();
				for(int i=0;i<latlist.size();i++) {
					builder.append(lonlist.get(i)+" "+latlist.get(i)+",");
				}
				builder.delete(builder.length()-1,builder.length());
				result.put("http://www.opengis.net/ont/geosparql#asWKT","LineString(("+builder.toString()+"))");
			}
			latlist.clear();
			lonlist.clear();
		}
		WebService.nameSpaceCache.put(featuretype.toLowerCase(),nscache);
		System.out.println("NamespaceCache: "+WebService.nameSpaceCache);
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
	
	public static String CQLfilterStringToSPARQLQuery(String filter,String bbox,String curquery,String queryurl,String featuretype,String indvar) {
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
					additionaltriples.append("?"+indvar+" <"+getPropertyFromMapping(featuretype, propname)+"> ?"+propname+" ."+System.lineSeparator());
					builder.append("regex(str(?"+propname+"),\""+filterex.substring(filterex.indexOf("LIKE")+4).trim()+"\",\"i\") ");
				}else if(spatialFunctions.matcher(filterex).matches()){
					String prefix=filterex.substring(0,filterex.indexOf(',')+1).trim();			
					builder.append("geo:sf"+prefix+" \""+filterex.substring(filterex.indexOf(',')+1,filterex.length()-1).trim()+"\"^^geo:wktLiteral)");
				}else if(binaryOperators.matcher(filterex).matches()) {
					String[] splitted=filterex.split("<|>|=|<=|>=|<>");
					if(filterex.contains("=")) {
						String propname=filterex.substring(0,filterex.indexOf('=')).toLowerCase();
						additionaltriples.append("?"+indvar+" <"+getPropertyFromMapping(featuretype, propname)+"> ?"+propname+" ."+System.lineSeparator());
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
		String queryString="",indvar="item";
		if(workingobj.has("indvar")) {
			indvar=workingobj.getString("indvar");
		}
		if(resultType.equalsIgnoreCase("hits")) {
			queryString=" SELECT (COUNT(DISTINCT ?"+indvar+") AS ?count) "+queryString.substring(queryString.indexOf("WHERE"));
			//queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) WHERE { ?"+featuretype.toLowerCase()+" ?abc ?def .} "+System.lineSeparator();
		}else {
			queryString+=" SELECT ?"+featuretype.toLowerCase()+" ?member WHERE{"+System.lineSeparator();
		}
		if(!resourceids.isEmpty() && !resourceids.contains(",")) {
			queryString=queryString.replace("WHERE{","WHERE{"+System.lineSeparator()+" BIND( <"+workingobj.getString("namespace")+resourceids+"> AS ?"+indvar+") "+System.lineSeparator());
		}else if(!resourceids.isEmpty() && resourceids.contains(",")) {
			StringBuilder toreplace=new StringBuilder();
			toreplace.append("WHERE{ "+System.lineSeparator()+"VALUES ?"+indvar+"{ ");
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
		queryString=CQLfilterStringToSPARQLQuery(filter,"",queryString,queryurl,featuretype,indvar);
		queryString+="}"+System.lineSeparator();
		if(!resultType.equalsIgnoreCase("hits") && workingobj.has("useorderby") && workingobj.getBoolean("useorderby"))
			queryString+=" ORDER BY ?"+indvar+System.lineSeparator();
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
		String res=resformat.formatter(results,startingElement,featuretype.toLowerCase(),propertyValue,
				(workingobj.has("typeColumn")?workingobj.get("typeColumn").toString():""),true,false,
				srsName,(workingobj.has("indvar")?workingobj.getString("indvar"):"item"),
				(workingobj.has("targetCRS")?workingobj.getString("targetCRS"):""),null,null,null,false,false);
		qexec.close();
		if(resformat.lastQueriedElemCount==0) {
			return "";
		}
		return res;
	}
	
	
	public static String executeQuery(String queryString,String queryurl,String output,String count,
			String offset,String startingElement,String featuretype,String resourceids,JSONObject workingobj,
			String filter,String resultType,String srsName,String bbox,String mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException {
		if(invertXY==null) {
			invertXY=false;
		}
		System.out.println(resultType);
		System.out.println(mapstyle);
		StyleObject style=null;
		if(!mapstyle.isEmpty()) {
			style=TripleStoreConnector.getStyle(featuretype, mapstyle, workingobj.getString("triplestore"),workingobj.getString("namespace"));
			System.out.println(style);
		}
		String indvar="item";
		if(workingobj.has("indvar")) {
			indvar=workingobj.getString("indvar");
		}
		queryString=queryString.replace(" ."," ."+System.lineSeparator());
		System.out.println(queryString);
		if(resultType.equalsIgnoreCase("hits")) {
			queryString=" SELECT (COUNT(DISTINCT ?"+indvar+") AS ?count) "+queryString.substring(queryString.indexOf("WHERE"));
			//queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) WHERE{ ?"+featuretype.toLowerCase()+" ?abc ?def .}"+System.lineSeparator();
		}else {
			queryString+=" SELECT ?"+indvar+" ?member WHERE{"+System.lineSeparator();
		}
		if(!resourceids.isEmpty() && !resourceids.contains(",")) {
			queryString=queryString.replace("WHERE{","WHERE{"+System.lineSeparator()+" BIND( <"+workingobj.getString("namespace")+resourceids+"> AS ?"+indvar+") ");
		}else if(!resourceids.isEmpty() && resourceids.contains(",")) {
			StringBuilder toreplace=new StringBuilder();
			toreplace.append("WHERE{ "+System.lineSeparator()+" VALUES ?"+indvar+"{ ");
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
		queryString=CQLfilterStringToSPARQLQuery(filter,bbox,queryString,queryurl,featuretype,indvar)+"}";
		if(!resultType.equalsIgnoreCase("hits") && workingobj.has("useorderby") && workingobj.getBoolean("useorderby"))
			queryString+=System.lineSeparator()+"ORDER BY ?"+indvar+System.lineSeparator();
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
				String res=results.next().getLiteral("count").getString();
				qexec.close();
				return res;
			}
		}
		String res=resformat.formatter(results,startingElement,featuretype.toLowerCase(),"",
				(workingobj.has("typeColumn")?workingobj.get("typeColumn").toString():""),false,false,
				srsName,indvar,
				(workingobj.has("targetCRS")?workingobj.getString("targetCRS"):""),null,null,style,alternativeFormat,invertXY);
		qexec.close();
		if(resformat.lastQueriedElemCount==0) {
			return "";
		}
		return res;
	}

}

