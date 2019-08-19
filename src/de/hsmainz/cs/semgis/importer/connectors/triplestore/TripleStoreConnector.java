package de.hsmainz.cs.semgis.importer.connectors.triplestore;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.locationtech.jts.geom.Geometry;

import de.hsmainz.cs.semgis.importer.connectors.resultformatter.ResultFormatter;


public abstract class TripleStoreConnector {

	protected static final String prefixCollection=	"PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#> "+		
			"PREFIX skos:  <http://www.w3.org/2004/02/skos/core#> "+
			"PREFIX lemon: <http://www.lemon-model.net/lemon#> "+
			"PREFIX dbo:<http://dbpedia.org/ontology/> "+
			"PREFIX geom: <http://geovocab.org/geometry#> "+
			"PREFIX ogc: <http://www.opengis.net/ont/geosparql#> "+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+
			"PREFIX bn-lemon: <http://babelnet.org/model/babelnet#> "+
		 	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
		 	"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> "+
			"PREFIX dbpedia: <http://dbpedia.org/resource/> "+
			"Prefix lgdr:<http://linkedgeodata.org/triplify/> "+
			"Prefix lgdo:<http://linkedgeodata.org/ontology/> "+
		 	"PREFIX owl:<http://www.w3.org/2002/07/owl#> "+
			"PREFIX omgeo:<http://www.ontotext.com/owlim/geo#> "+
		 	"PREFIX ff:<http://factforge.net> "+
			"PREFIX om:<http://www.ontotext.com/owlim> "+
		 	"PREFIX wd:<http://www.wikidata.org/entity/> "+
		 	"PREFIX bd: <http://www.bigdata.com/rdf#> "+
		 	"PREFIX wdt: <http://www.wikidata.org/prop/direct/> "+
			"PREFIX wikibase: <http://wikiba.se/ontology#> "+
		 	"PREFIX geo-pos:<http://w3.org/2003/01/geo/wgs84_pos#> ";
			

	protected String queryurl;	
	
	protected static TripleStoreConnector instance;

	protected String queryurl2; 
	
	public Set<String> prefix;
	
	protected Boolean babelnetCompatible;
	
	protected Boolean matchLabels;
	
	protected Double defaultRadius;
		
	public Boolean getMatchLabels() {
		return matchLabels;
	}

	protected Boolean geospatialComaptible;
	
	public Boolean getBabelnetCompatible() {
		return babelnetCompatible;
	}

	
	public static String executeQuery(String queryString,String queryurl,String output,String count) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query+" LIMIT "+count);
		ResultFormatter resformat=ResultFormatter.resultMap.get(output);
		ResultSet results = qexec.execSelect();
		String res=resformat.formatter(results);
		qexec.close();
		return res;
	}


	public static String executeQuery(String queryString,String queryurl,String output) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query);
		ResultFormatter resformat=ResultFormatter.resultMap.get(output);
		ResultSet results = qexec.execSelect();
		String res=resformat.formatter(results);
		qexec.close();
		return res;
	}
	
	




}
