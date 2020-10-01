package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class LatLonTextFormatter extends VectorResultFormatter {

	WKTReader reader=new WKTReader();
	
	public LatLonTextFormatter() {
		this.mimeType="text/latlon";
		this.exposedType="text/latlon";
		this.urlformat="latlon";
		this.label="LatLonText";
		this.fileextension="txt";
	}
	
	/**
	 * Converts a decimal number to a latitude longitude representation.
	 * @param D the decimal number
	 * @param lng indicates if a latitude or longitude number should be returned
	 * @return The number in lat/lon representation
	 */
	public String convertDecimalToLatLonText(Double D, Boolean lng){
	    String dir;
		if(D<0) {
			if(lng) {
				dir="W";
			}else {
				dir="S";
			}
		}else {
			if(lng) {
				dir="E";
			}else {
				dir="N";
			}
		}
		Double deg=D<0?-D:D;
		Double min=D%1*60;
		Double sec=(D*60%1*6000)/100;
		return deg+"°"+min+"'"+sec+"\""+dir;
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, IOException {
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
					if(geom!=null)
						out.write(convertDecimalToLatLonText(geom.getCentroid().getCoordinate().x,false)+" "+convertDecimalToLatLonText(geom.getCentroid().getCoordinate().getY(),true)+System.lineSeparator());
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
				Geometry geom=this.parseVectorLiteral("Point("+lon+" "+lat+")",WKTLiteral, epsg, srsName);
				if(geom!=null)
					out.write(convertDecimalToLatLonText(geom.getCoordinate().x,false)+" "+convertDecimalToLatLonText(geom.getCoordinate().getY(),true)+System.lineSeparator());
				lat="";
				lon="";
			}
			lastInd=solu.get(indvar).toString();
		}
		return "";
	}
	
}
