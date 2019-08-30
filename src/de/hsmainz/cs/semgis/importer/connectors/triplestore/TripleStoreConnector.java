package de.hsmainz.cs.semgis.importer.connectors.triplestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.importer.connectors.resultformatter.ResultFormatter;


public abstract class TripleStoreConnector {

	protected static String prefixCollection;
	
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

	
	public static String executeQuery(String queryString,String queryurl,String output,String count) throws XMLStreamException {
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query+" LIMIT "+count);
		ResultFormatter resformat=ResultFormatter.resultMap.get(output);
		ResultSet results = qexec.execSelect();
		String res=resformat.formatter(results);
		qexec.close();
		return res;
	}


	public static String executeQuery(String queryString,String queryurl,String output) throws XMLStreamException {
		Query query = QueryFactory.create(prefixCollection+queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(queryurl, query);
		ResultFormatter resformat=ResultFormatter.resultMap.get(output);
		ResultSet results = qexec.execSelect();
		String res=resformat.formatter(results);
		qexec.close();
		return res;
	}
	
	




}
