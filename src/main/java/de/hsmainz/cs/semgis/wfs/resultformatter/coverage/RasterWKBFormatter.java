package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.apache.sis.coverage.grid.GridCoverage;
import org.geotoolkit.coverage.wkb.WKBRasterWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.util.CovJSONCoverage;

public class RasterWKBFormatter extends CoverageResultFormatter {

	WKBRasterWriter wkbrastwriter=new WKBRasterWriter();
	
	public RasterWKBFormatter() {
		this.mimeType="application/rasterwkb";
		this.exposedType="application/rasterwkb";
		this.urlformat="rastwkb";
		this.label="RasterWKB (WKB)";
		this.fileextension="wkb";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage) throws XMLStreamException {
		
		// TODO Auto-generated method stub
		return null;
	}

}
