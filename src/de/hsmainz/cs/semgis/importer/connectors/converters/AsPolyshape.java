package de.hsmainz.cs.semgis.importer.connectors.converters;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper;
import io.github.galbiston.geosparql_jena.implementation.datatype.PolyshapeDatatype;

public class AsPolyshape extends FunctionBase1 {

	@Override
	public NodeValue exec(NodeValue v) {
		try {
            GeometryWrapper geom = GeometryWrapper.extract(v);
            String geomstr=PolyshapeDatatype.INSTANCE.unparse(geom);
            return NodeValue.makeString(geomstr);
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
	}

}
