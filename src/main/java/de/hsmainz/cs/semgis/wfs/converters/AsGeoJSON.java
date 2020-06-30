package de.hsmainz.cs.semgis.wfs.converters;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.wololo.geojson.GeoJSON;
import org.wololo.jts2geojson.GeoJSONWriter;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper;

/**
 * Return the geometry as a GeoJSON element.
 *
 */
public class AsGeoJSON extends FunctionBase1 {
	
	@Override
	public NodeValue exec(NodeValue arg0) {
        try {
            GeometryWrapper geometry = GeometryWrapper.extract(arg0);
            GeoJSONWriter writer = new GeoJSONWriter();
            GeoJSON json = writer.write(geometry.getParsingGeometry());
            String jsonstring = json.toString();
            return NodeValue.makeString(jsonstring);
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
	}

}
