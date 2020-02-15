package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.expr.NodeValue;

import de.hsmainz.cs.semgis.wfs.converters.AsGPX;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;


public class GPXFormatter extends WFSResultFormatter {

	public GPXFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/gpx";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle) throws XMLStreamException {
		StringBuilder gpxout=new StringBuilder();
		StringBuilder attbuilder=new StringBuilder();
		gpxout.append("<?xml version='1.0' encoding='UTF-8' standalone='no' ?><gpx version='1.0'><name>"+featuretype+"</name>");
	    while(results.hasNext()) {
	    	this.lastQueriedElemCount++;
	    	QuerySolution solu=results.next();
	    	Iterator<String> varnames = solu.varNames();
	    	gpxout.append(attbuilder.toString());
	    	attbuilder.delete(0,attbuilder.length());
	    	if(lastQueriedElemCount>0) {
	    		attbuilder.append("</wpt>");
	    	}
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		if(name.equalsIgnoreCase("lat")){
	    			if(solu.get("lon")!=null) {
	    				gpxout.append("<wpt lat=\""+solu.get("lat").toString().substring(0,solu.get("lat").toString().indexOf("^^"))+"\" lon=\""+solu.get("lon").toString().substring(0,solu.get("lon").toString().indexOf("^^"))+"\">");
	    			}
	    		}else if(!name.endsWith("_geom")) {
	    			attbuilder.append("<"+name+">");
	    			attbuilder.append(solu.get(name));
	    			attbuilder.append("</"+name+">");
	    		}else {
	    			AsGPX gpx=new AsGPX();
	    			NodeValue val=gpx.exec(NodeValue.makeNode(solu.getLiteral(name).getString(),solu.getLiteral(name).getDatatype()));
	    			String res=val.asString();
	    			gpxout.append(res);
	    		}
	    	}
	    	gpxout.append("</trk>");
	    }
	    gpxout.append("</gpx>");
		return gpxout.toString();
	}

}
