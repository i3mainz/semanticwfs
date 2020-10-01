package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.geotoolkit.coverage.wkb.WKBRasterWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class RasterWKBFormatter extends CoverageResultFormatter {

	WKBRasterWriter wkbrastwriter=new WKBRasterWriter();
	
	public RasterWKBFormatter() {
		this.mimeType="application/rasterwkb";
		this.exposedType="application/rasterwkb";
		this.urlformat="rasterwkb";
		this.label="RasterWKB (WKB)";
		this.fileextension="wkb";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException {
		StringBuilder builder=new StringBuilder();
		String lastInd="",lat="",lon="";
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			Iterator<String> varnames=solu.varNames();
			if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
				lastQueriedElemCount++;
			}
			while(varnames.hasNext()) {
				String name=varnames.next();
				if(name.endsWith("_geom")) {
					Object obj=this.parseLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
							solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
					if(obj instanceof Geometry) {
						Geometry geom=(Geometry)obj;
						for(Coordinate coord:geom.getCoordinates()) {
							builder.append(coord.getX()+" "+coord.getY());
							if(!Double.isNaN(coord.getZ())) {
								builder.append(" "+coord.getZ()+System.lineSeparator());								
							}else {
								builder.append(System.lineSeparator());
							}
						}						
					}else if(obj!=null) {
						//this.wkbrastwriter.write(coverage);
					}
				}else if(name.equalsIgnoreCase(indvar)){
					continue;
				}else if("lat".equalsIgnoreCase(name)){
					lat=solu.get(name).toString();
				}else if("lon".equalsIgnoreCase(name)){
					lon=solu.get(name).toString();
				}
			}
			if(!lat.isEmpty() && !lon.isEmpty()) {
				System.out.println("LatLon: "+lat+","+lon);
				if(lat.contains("^^")) {
					lat=lat.substring(0,lat.indexOf("^^"));
				}
				if(lon.contains("^^")) {
					lon=lon.substring(0,lon.indexOf("^^"));
				}
				Geometry geom=this.parseVectorLiteral("Point("+lon+" "+lat+")", VectorResultFormatter.WKTLiteral, epsg, srsName);
				if(geom!=null)
					builder.append(geom.toText()+System.lineSeparator());
				lat="";
				lon="";
			}
			lastInd=solu.get(indvar).toString();
		}
		return builder.toString();
	}

}
