package de.hsmainz.cs.semgis.wfs.converters;

import java.math.BigInteger;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase3;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper; 
/**
 * Returns a Geometry in X3D xml node element format: ISO-IEC-19776-1.2-X3DEncodings-XML
 *
 */
public class AsX3D extends FunctionBase3 {

	String header="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
	"<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.2//EN\""+
	  "http://www.web3d.org/specifications/x3d-3.2.dtd\"><Scene><Shape>";
	String footer="</Shape></Scene></X3D>";
	
	
	
	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2, NodeValue v3) {
		try {
            GeometryWrapper geometry = GeometryWrapper.extract(v1);
            Geometry geom = geometry.getXYGeometry();
            StringBuilder builder=new StringBuilder();
            builder.append("<IndexedFaceSet coordIndex=\"");
            Integer index=0;
            for(Coordinate coord:geom.getCoordinates()) {
            	builder.append(index.toString());
            	index++;
            }
            builder.append("</IndexedFaceSet>");
            builder.append("<Coordinate point=\"");
            for(Coordinate coord:geom.getCoordinates()) {
            	builder.append(coord.getX()+" "+coord.getY()+" "+coord.getZ()+" ");
            }
            builder.append("\"/>");
            BigInteger maximaldecimaldigits=v2.getInteger();
            BigInteger options=v3.getInteger();
    		return NodeValue.makeString(header+builder.toString()+footer);
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }

	}

}

