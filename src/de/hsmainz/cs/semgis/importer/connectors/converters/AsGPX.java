package de.hsmainz.cs.semgis.importer.connectors.converters;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper;

public class AsGPX extends FunctionBase1 {

	String out=		"<?xml version='1.0' encoding='UTF-8' standalone='no' ?>" + 
			"<gpx version='1.0'>"+
			"<name>Example gpx</name>";
		String out2= 
			"  <trk>" + 
			"    <name>Example gpx</name>" + 
			"    <trkseg>";
	String out3=" </trkseg></trk></gpx>";
	
	
	@Override
	public NodeValue exec(NodeValue v) {
		 try {
	            GeometryWrapper geometry = GeometryWrapper.extract(v);
	            Geometry geom=geometry.getXYGeometry();
	            StringBuilder trackpoints=new StringBuilder();
	            for(Coordinate coord:geom.getCoordinates()) {
	            	trackpoints.append("<trkpt lat='"+coord.x+"' lon='"+coord.y+"'>");
	            	if(!Double.isNaN(coord.getZ()))
	            		trackpoints.append("<ele>"+coord.getZ()+"</ele>");
	            	trackpoints.append("</trkpt>");
	            }
	            return NodeValue.makeString(out+out2+trackpoints+out3);
	        } catch (DatatypeFormatException ex) {
	            throw new ExprEvalException(ex.getMessage(), ex);
	        }
		
		

	}

}
