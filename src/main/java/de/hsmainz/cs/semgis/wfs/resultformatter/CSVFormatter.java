package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

public class CSVFormatter extends WFSResultFormatter {

	public CSVFormatter() {
		this.mimeType="text/csv";
		this.exposedType="text/csv";
	}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn,Boolean onlyproperty) {
    	Boolean first=true;
    	StringBuilder resultCSV=new StringBuilder();
    	StringBuilder resultCSVHeader=new StringBuilder();
		String rel="",val="",lastInd="";
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
	    	Iterator<String> varnames = solu.varNames();
	    	String curfeaturetype="";
	    	if(solu.get(featuretype.toLowerCase())!=null) {
				curfeaturetype=solu.get(featuretype.toLowerCase()).toString();
				if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
					curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
				}
				if(!solu.get(featuretype.toLowerCase()).toString().equals(lastInd) || lastInd.isEmpty()) {
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
	    		}else if(name.equalsIgnoreCase(featuretype)){
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
			lastInd=solu.get(featuretype.toLowerCase()).toString();
	    	//resultCSV.delete(resultCSV.length()-1,resultCSV.length());
	    }
	    return resultCSVHeader.toString()+resultCSV;
	}

}
