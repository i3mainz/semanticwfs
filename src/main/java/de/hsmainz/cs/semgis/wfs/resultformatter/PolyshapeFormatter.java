package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.NodeValue;

import de.hsmainz.cs.semgis.wfs.converters.AsGeoURI;
import de.hsmainz.cs.semgis.wfs.converters.AsPolyshape;

public class PolyshapeFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement) throws XMLStreamException {
		List<QuerySolution> test=ResultSetFormatter.toList(results);
    	Boolean first=true;
    	StringBuilder resultCSV=new StringBuilder();
    	StringBuilder resultCSVHeader=new StringBuilder();
	    for(QuerySolution solu:test) {
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		if(first) {
	    		    resultCSVHeader.append(name+",");
	    		}
	    		if(name.endsWith("_geom")) {
	    			AsPolyshape polyshape=new AsPolyshape();
	    			try {
	    			NodeValue val=polyshape.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),solu.getLiteral(name).getDatatype()));
	    			//JSONObject geomobj=new JSONObject(val.asNode().getLiteralValue().toString());
	    			resultCSV.append(val.asString());
	    			}catch(Exception e) {
	    				e.printStackTrace();
	    			}
	    		}else {
	    			try {
	    				Literal lit=solu.getLiteral(name);
	    				resultCSV.append(lit.getString()+",");
	    			}catch(Exception e) {
	    				resultCSV.append(solu.get(name)+",");	
	    			}  			
	    		}

	    	}
	    	if(first) {
	    		resultCSVHeader.delete(resultCSVHeader.length()-1, resultCSVHeader.length());
		    	resultCSVHeader.append(System.lineSeparator());
	    		first=false;
	    	}else {
	    		resultCSV.delete(resultCSV.length()-1, resultCSV.length());
		    	resultCSV.append(System.lineSeparator());
	    	}   	
	    }
	    return resultCSVHeader.toString()+resultCSV;
	}

}
