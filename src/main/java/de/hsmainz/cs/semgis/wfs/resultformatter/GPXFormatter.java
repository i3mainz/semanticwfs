package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class GPXFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results,Integer offset,String startingElement,String featuretype,String typeColumn) throws XMLStreamException {
		StringBuilder gpxout=new StringBuilder();
		gpxout.append("<?xml version='1.0' encoding='UTF-8' standalone='no' ?><gpx version='1.0'><name>Example gpx</name>");
		List<QuerySolution> test=ResultSetFormatter.toList(results);
	    for(QuerySolution solu:test) {
	    	gpxout.append("<trk>");
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		if(!name.endsWith("_geom")) {
	    			gpxout.append("<"+name+">");
	    			gpxout.append(solu.get(name));
	    			gpxout.append("</"+name+">");
	    		}else {/*
	    			AsGPX gpx=new AsGPX();
	    			NodeValue val=gpx.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),solu.getLiteral(name).getDatatype()));
	    			String res=val.asString();
	    			gpxout.append(res);*/
	    		}
	    	}
	    	gpxout.append("</trk>");
	    }
	    gpxout.append("</gpx>");
		return gpxout.toString();
	}

}
