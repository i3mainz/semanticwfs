package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.locationtech.jts.geom.Geometry;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to CSV.
 */
public class CSVFormatter extends VectorResultFormatter {

	public CSVFormatter() {
		this.mimeType="text/csv";
		this.exposedType="text/csv";
		this.urlformat="csv";
		this.label="Comma Separated Values (CSV)";
		this.fileextension="csv";
		this.definition="https://tools.ietf.org/html/rfc4180";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,
			StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY,Boolean coverage,Writer out) throws IOException {
    	Boolean first=true;
    	StringBuilder resultCSV=new StringBuilder();
    	//StringBuilder resultCSVHeader=new StringBuilder();
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
						out.write(resultCSV.toString());
						out.flush();
						resultCSV.delete(0, resultCSV.length());
					}
					if(!lastInd.isEmpty() && first) {
						//resultCSVHeader.delete(resultCSVHeader.length()-1, resultCSVHeader.length());
						out.write(System.lineSeparator());
						//resultCSVHeader.append(System.lineSeparator());
						first=false;
					}
				}
			}
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		System.out.println("Name: "+name);
	    		if(name.endsWith("_geom")) {
	    			Geometry geom=this.parseVectorLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
							solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
					if(geom!=null)
						resultCSV.append(geom.toText()+",");
					else
						resultCSV.append(solu.get(name)+",");
	    			if(first) {
	    				out.write("the_geom,");
	    				//resultCSVHeader.append("the_geom,");
	    			}
	    		}else if(name.equalsIgnoreCase(indvar)){
					continue;
				}else if("rel".equalsIgnoreCase(name) || name.contains("_rel")){
					rel=solu.get(name).toString();
		    		if(first) {
		    			out.write(solu.get(name).toString()+",");
		    		    //resultCSVHeader.append(solu.get(name).toString()+",");
		    		}
				}else if("val".equalsIgnoreCase(name) || name.contains("_val")){
					val=solu.get(name).toString();
				}else {
		    		if(first) {
		    			out.write(name+",");
		    		    //resultCSVHeader.append(name+",");
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
				out.write(resultCSV.toString());
				out.flush();
				resultCSV.delete(0, resultCSV.length());
			}
	    	
	    	System.out.println(resultCSV.toString());
			lastInd=solu.get(indvar).toString();
	    	//resultCSV.delete(resultCSV.length()-1,resultCSV.length());
	    }
	    return "";
	}

}
