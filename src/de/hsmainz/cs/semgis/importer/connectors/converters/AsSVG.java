package de.hsmainz.cs.semgis.importer.connectors.converters;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.locationtech.jtstest.util.io.SVGWriter;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper; 
/**
 * Returns a Geometry in SVG path data given a geometry or geography object.
 *
 */
public class AsSVG extends FunctionBase1 {

	@Override
	public NodeValue exec(NodeValue arg0) {
		try {
            GeometryWrapper geometry = GeometryWrapper.extract(arg0);
            SVGWriter writer=new SVGWriter();
            String result=writer.write(geometry.getParsingGeometry());
            return NodeValue.makeString(result.toString());
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
        
	}

}
