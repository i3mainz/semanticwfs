package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;


public class GPXFormatter extends WFSResultFormatter {

	public GPXFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/gpx";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar) throws XMLStreamException {
		StringBuilder gpxout=new StringBuilder();
		gpxout.append("<?xml version='1.0' encoding='UTF-8' standalone='no' ?><gpx version='1.0'><name>"+featuretype+"</name>");
	    while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
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
