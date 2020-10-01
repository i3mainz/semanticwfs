package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class X3DFormatter extends CoverageResultFormatter {

	public X3DFormatter() {
		this.mimeType="text/x3d+xml";
		this.exposedType="text/x3d+xml";
		this.urlformat="x3d";
		this.label="X3D";
		this.fileextension="x3d";
		this.definition="https://www.web3d.org/standards";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException {
		return null;
		/*
		try {
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
        }*/
	}

}
