package de.hsmainz.cs.semgis.importer.connectors.resultformatter;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public abstract class ResultFormatter {

	public static final Map<String,ResultFormatter> resultMap=new TreeMap<>();
	
	public String formatter(ResultSet results) {
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			
			String res=formatResult(results.next());
		}
		return "";

	}

	public String formatHeader() {
		return "";
	}
	
	public abstract String formatResult(QuerySolution result);

	public String formatFooter() {
		return "";
	}
	
}
