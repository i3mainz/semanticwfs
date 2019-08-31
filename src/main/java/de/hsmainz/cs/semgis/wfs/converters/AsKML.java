package de.hsmainz.cs.semgis.wfs.converters;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.locationtech.jts.io.kml.KMLWriter;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper; 
/**
 * Return the geometry as a KML element. Several variants. Default version=2, default maxdecimaldigits=15
 *
 */
public class AsKML extends FunctionBase1 {
	
	@Override
	public NodeValue exec(NodeValue arg0) {
        try {
            GeometryWrapper geometry = GeometryWrapper.extract(arg0);
            KMLWriter writer=new KMLWriter();
            String result=writer.write(geometry.getParsingGeometry());
            return NodeValue.makeString(result.toString());
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
	}

}
