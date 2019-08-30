package de.hsmainz.cs.semgis.importer.connectors.converters;

import java.math.BigInteger;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.sis.index.GeoHashCoder;
import org.locationtech.jts.algorithm.Angle;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper;

/**
 * Return a GeoHash representation of the geometry
 *
 */
public class AsGeoHash extends FunctionBase2 {

	GeoHashCoder coder=new GeoHashCoder();
	
	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		GeometryWrapper geom1 = GeometryWrapper.extract(v1);
		BigInteger maxchars=v2.getInteger();
		if(geom1.getGeometryType().equalsIgnoreCase("Point")) {
			String geohash = coder.encode(Angle.toDegrees(geom1.getXYGeometry().getCoordinate().getX()), Angle.toDegrees(geom1.getXYGeometry().getCoordinate().getY()));
			return NodeValue.makeString(geohash);
		}
		throw new RuntimeException("Input geometry needs to be a Point");
	}

}
