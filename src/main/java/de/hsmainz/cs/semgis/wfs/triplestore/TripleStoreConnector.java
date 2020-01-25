package de.hsmainz.cs.semgis.wfs.triplestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
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
import org.json.JSONObject;


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
	
	public static String getBoundingBoxFromTripleStoreData(String triplestore,String queryString) {
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore, query);
		ResultSet results = qexec.execSelect();
		Double minx=Double.MAX_VALUE,maxx=Double.MIN_VALUE,miny=Double.MAX_VALUE,maxy=Double.MIN_VALUE;
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			String wktLiteral=solu.get("the_geom").toString().substring(0,solu.get("the_geom").toString().indexOf("^^"));
			wktLiteral=wktLiteral.replace("(","").replace(")","").replace(","," ").trim();
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
		}
		return minx+" "+miny+" "+maxx+" "+maxy;
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
		if(featureTypes.containsKey(featuretype)) {
			return featureTypes.get(featuretype);
		}
		Map<String,String> result=new TreeMap<>();
		System.out.println(prefixCollection+queryString);
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query+" LIMIT 1");
		ResultSet results = qexec.execSelect();
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
		Integer attcount=0;
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
					result.put("http://www.opengis.net/ont/geosparql#asWKT",solu.getLiteral(varname).toString());
					//geomLiteral=solu.getLiteral(varname).toString();
				}else {
					try {
						Literal lit=solu.getLiteral(varname);
						result.put(varname, lit.getDatatypeURI());
					}catch(Exception e) {
						result.put(varname, solu.get(varname).toString());	
					}  
				}

			}
			if(!rel.isEmpty() && !val.isEmpty()) {
				result.put(rel, val);
				rel="";
				val="";
			}
			attcount++;
		}
		System.out.println(result);
		workingobj.put("attcount",attcount);
			return result;
	}

	
	public static void main(String[] args) {
		System.out.println(CQLfilterStringToSPARQLQuery("abc=4 AND DISJOINT(the_geom, POLYGON((-90 40, -90 45, -60 45, -60 40, -90 40)))","namedplace"));
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
	
	public static String CQLfilterStringToSPARQLQuery(String filter,String featuretype) {
		if(filter.isEmpty())
			return filter;
		StringBuilder additionaltriples=new StringBuilder();
		StringBuilder builder=new StringBuilder();
		builder.append("FILTER(");
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
		return builder.toString();
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
			queryString=queryString.replace("WHERE{","WHERE{ BIND( <"+workingobj.getString("namespace")+resourceids+"> AS ?"+workingobj.getString("indvar")+") "+System.lineSeparator());
		}else if(!resourceids.isEmpty() && resourceids.contains(",")) {
			StringBuilder toreplace=new StringBuilder();
			toreplace.append("WHERE{ VALUES ?"+workingobj.getString("indvar")+"{ ");
			for(String uri:resourceids.split(",")) {
				toreplace.append("<"+workingobj.getString("namespace")+uri+"> "); 
			}
			toreplace.append("} "+System.lineSeparator());
			queryString=queryString.replace("WHERE{",toreplace.toString());	
		}
		queryString+="?"+workingobj.getString("indvar")+" <"+propertyValue+"> ?member ."+System.lineSeparator();
		queryString+=CQLfilterStringToSPARQLQuery(filter,featuretype);
		queryString+="}"+System.lineSeparator();
		if(!resultType.equalsIgnoreCase("hits"))
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
	
	
	public static String executeQuery(String queryString,String queryurl,String output,String count,String offset,String startingElement,String featuretype,String resourceids,JSONObject workingobj,String filter,String resultType,String srsName) throws XMLStreamException {
		System.out.println(resultType);
		if(resultType.equalsIgnoreCase("hits")) {
			queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) "+queryString.substring(queryString.indexOf("WHERE"));
			//queryString=" SELECT (COUNT(DISTINCT ?"+featuretype.toLowerCase()+") AS ?count) WHERE{ ?"+featuretype.toLowerCase()+" ?abc ?def .}"+System.lineSeparator();
		}else {
			queryString+=" SELECT ?"+featuretype.toLowerCase()+" ?member WHERE{"+System.lineSeparator();
		}
		if(!resourceids.isEmpty() && !resourceids.contains(",")) {
			queryString=queryString.replace("WHERE{","WHERE{ BIND( <"+workingobj.getString("namespace")+resourceids+"> AS ?"+workingobj.getString("indvar")+") ");
		}else if(!resourceids.isEmpty() && resourceids.contains(",")) {
			StringBuilder toreplace=new StringBuilder();
			toreplace.append("WHERE{ VALUES ?"+workingobj.getString("indvar")+"{ ");
			for(String uri:resourceids.split(",")) {
				toreplace.append("<"+workingobj.getString("namespace")+uri+"> "); 
			}
			toreplace.append("}");
			queryString=queryString.replace("WHERE{",toreplace.toString());	
		}
		queryString=queryString.substring(0,queryString.lastIndexOf('}'))+CQLfilterStringToSPARQLQuery(filter,featuretype)+"}";
		if(!resultType.equalsIgnoreCase("hits"))
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

