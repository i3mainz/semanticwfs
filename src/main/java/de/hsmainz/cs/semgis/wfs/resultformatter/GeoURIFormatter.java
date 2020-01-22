package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.expr.NodeValue;

import de.hsmainz.cs.semgis.wfs.converters.AsGeoURI;

public class GeoURIFormatter extends WFSResultFormatter {

	public GeoURIFormatter() {
		this.mimeType="text/plain";
		this.exposedType="text/plain";
	}
	
	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn) {
		List<QuerySolution> test=ResultSetFormatter.toList(results);
    	Boolean first=true;
    	StringBuilder resultCSV=new StringBuilder();
    	StringBuilder resultCSVHeader=new StringBuilder();
    	String lastInd="";
	    for(QuerySolution solu:test) {
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		String curfeaturetype="";
		    	if(solu.get(featuretype.toLowerCase())!=null) {
					curfeaturetype=solu.get(featuretype.toLowerCase()).toString();
					if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
						curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
					}
					if(!solu.get(featuretype.toLowerCase()).toString().equals(lastInd) || lastInd.isEmpty()) {
						lastQueriedElemCount++;
					}
				}
	    		if(first) {
	    		    resultCSVHeader.append(name+",");
	    		}
	    		if(name.endsWith("_geom")) {
	    			AsGeoURI geojson=new AsGeoURI();
	    			try {
	    			NodeValue val=geojson.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),solu.getLiteral(name).getDatatype()));
	    			//JSONObject geomobj=new JSONObject(val.asNode().getLiteralValue().toString());
	    			resultCSV.append(val.asString()+System.lineSeparator());
	    			}catch(Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	    	}	
	    }
	    return resultCSV.toString();
	}

}
