package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.util.ReprojectionUtils;

public class XYZASCIIFormatter extends ResultFormatter {

	public XYZASCIIFormatter() {
		this.mimeType="text/xyz";
		this.exposedType="text/xyz";
		this.urlformat="xyz";
		this.label="XYZ ASCII Format (XYZ)";
		this.fileextension="xyz";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY) throws XMLStreamException {
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
					try {
						Geometry geom=reader.read(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")));
						geom=ReprojectionUtils.reproject(geom, epsg, srsName);
						for(Coordinate coord:geom.getCoordinates()) {
							builder.append(coord.getX()+" "+coord.getY());
							if(!Double.isNaN(coord.getZ())) {
								builder.append(" "+coord.getZ()+System.lineSeparator());								
							}else {
								builder.append(System.lineSeparator());
							}
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
				Geometry geom;
				try {
					geom = reader.read("Point("+lon+" "+lat+")");
					geom=ReprojectionUtils.reproject(geom, epsg, srsName);
					for(Coordinate coord:geom.getCoordinates()) {
						builder.append(coord.getX()+" "+coord.getY()+" "+coord.getZ()+System.lineSeparator());
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lat="";
				lon="";
			}
			lastInd=solu.get(indvar).toString();
		}
		return builder.toString();
	}

}
