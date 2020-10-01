package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class OSMLinkFormatter extends VectorResultFormatter {

	WKTReader reader=new WKTReader();
	
	/**
	 * Constructor for this class.
	 */
	public OSMLinkFormatter() {
		this.mimeType="application/osmlink";
		this.exposedType="application/osmlink";
		this.urlformat="osmlink";
		this.label="OpenStreetMap Link";
		this.fileextension="";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException, IOException {
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
					Geometry geom=this.parseVectorLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
							solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
					if(geom!=null) {
						Envelope env = geom.getEnvelopeInternal();
			        	out.write("http://www.openstreetmap.org/?");
			        	out.write("minlon="+env.getMinY());
			        	out.write("&minlat="+env.getMinX());
			        	out.write("&maxlon="+env.getMaxY());
			        	out.write("&maxlat="+env.getMaxX());
			        	Coordinate centre = env.centre();
			        	out.write("&mlat="+centre.x);
			        	out.write("&mlon="+centre.y);          
			        	out.write(System.lineSeparator());
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
				out.write("http://www.openstreetmap.org/?");
				out.write("&mlat="+lat);
				out.write("&mlon="+lon);  
				out.write(System.lineSeparator());
				lat="";
				lon="";
			}
			lastInd=solu.get(indvar).toString();
		}
		return "";
	}

}
