package de.hsmainz.cs.semgis.importer.connectors.resultformatter;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.locationtech.jts.geom.Coordinate;

public class GPXFormatter extends ResultFormatter {

	@Override
	public String formatter(ResultSet results) throws XMLStreamException {
			String out2= 
				"  <trk>" + 
				"    <name>Example gpx</name>"; 
			String out3="    <trkseg>";
		String out4=" </trkseg></trk></gpx>";
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
	    		}else {
	    			StringBuilder trackpoints=new StringBuilder();
	                for(Coordinate coord:geom.getCoordinates()) {
	                	trackpoints.append("<trkpt lat='"+coord.x+"' lon='"+coord.y+"'>");
	                	if(!Double.isNaN(coord.getZ()))
	                		trackpoints.append("<ele>"+coord.getZ()+"</ele>");
	                	trackpoints.append("</trkpt>");
	                }
	    		}
	    	}
	    	gpxout.append("</trk>");
	    }
	    gpxout.append("</gpx>");
		return gpxout.toString();
	}

}
