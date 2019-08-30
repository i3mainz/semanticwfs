package de.hsmainz.cs.semgis.importer.connectors.converters;

import java.io.ByteArrayOutputStream;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import com.conveyal.data.geobuf.GeobufEncoder;

import geobuf.Geobuf.Data.Geometry;
import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper; import io.github.galbiston.geosparql_jena.implementation.GeometryWrapperFactory;

/**
 * Return a Geobuf representation of a set of rows.
 *
 */
public class AsGeoBuf extends FunctionBase1{

	@Override
	public NodeValue exec(NodeValue v) {
        try {
            GeometryWrapper geometry = GeometryWrapper.extract(v);
            ByteArrayOutputStream output=new ByteArrayOutputStream();
            GeobufEncoder enc=new GeobufEncoder(output,geometry.getXYGeometry().getPrecisionModel().getMaximumSignificantDigits());
            Geometry buf=enc.geomToGeobuf(geometry.getXYGeometry());
            return NodeValue.makeString(buf.toString());
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
	}

}
