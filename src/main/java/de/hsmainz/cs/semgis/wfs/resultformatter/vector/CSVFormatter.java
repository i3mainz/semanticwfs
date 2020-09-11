package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

import de.hsmainz.cs.semgis.wfs.resultformatter.WFSResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to CSV.
 */
public class CSVFormatter extends WFSResultFormatter {

	public CSVFormatter() {
		this.mimeType="text/csv";
		this.exposedType="text/csv";
		this.urlformat="csv";
		this.label="Comma Separated Values (CSV)";
		this.fileextension="csv";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) {
    	Boolean first=true;
    	StringBuilder resultCSV=new StringBuilder();
    	StringBuilder resultCSVHeader=new StringBuilder();
		String rel="",val="",lastInd="";
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
	    	Iterator<String> varnames = solu.varNames();
	    	String curfeaturetype="";
	    	if(solu.get(indvar)!=null) {
				curfeaturetype=solu.get(indvar).toString();
				if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
					curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
				}
				if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
					lastQueriedElemCount++;
					if(resultCSV.length()>0) {
						resultCSV.delete(resultCSV.length()-1, resultCSV.length());
						resultCSV.append(System.lineSeparator());
					}
					if(!lastInd.isEmpty() && first) {
						resultCSVHeader.delete(resultCSVHeader.length()-1, resultCSVHeader.length());
						resultCSVHeader.append(System.lineSeparator());
						first=false;
					}
				}
			}
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		System.out.println("Name: "+name);
	    		if(name.endsWith("_geom")) {
	    			try {
	    				Literal lit=solu.getLiteral(name);
	    				resultCSV.append(lit.getString()+",");
	    			}catch(Exception e) {
	    				resultCSV.append(solu.get(name)+",");	
	    			}
	    			if(first) {
	    				resultCSVHeader.append("the_geom,");
	    			}
	    		}else if(name.equalsIgnoreCase(indvar)){
					continue;
				}else if("rel".equalsIgnoreCase(name) || name.contains("_rel")){
					rel=solu.get(name).toString();
		    		if(first) {
		    		    resultCSVHeader.append(solu.get(name).toString()+",");
		    		}
				}else if("val".equalsIgnoreCase(name) || name.contains("_val")){
					val=solu.get(name).toString();
				}else {
		    		if(first) {
		    		    resultCSVHeader.append(name+",");
		    		}
	    			try {
	    				Literal lit=solu.getLiteral(name);
	    				resultCSV.append(lit.getString()+",");
	    			}catch(Exception e) {
	    				resultCSV.append(solu.get(name)+",");	
	    			}  			
	    		}
	    	}
	    	if(!rel.isEmpty() && !val.isEmpty()) {
				if(!rel.equals("http://www.opengis.net/ont/geosparql#hasGeometry") && !rel.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					resultCSV.append(val+",");
				}		
				rel="";
				val="";
			}else {
				lastQueriedElemCount++;
		    		resultCSV.delete(resultCSV.length()-1, resultCSV.length());
			    	resultCSV.append(System.lineSeparator());
			}
	    	
	    	System.out.println(resultCSV.toString());
			lastInd=solu.get(indvar).toString();
	    	//resultCSV.delete(resultCSV.length()-1,resultCSV.length());
	    }
	    return resultCSVHeader.toString()+resultCSV;
	}

}
