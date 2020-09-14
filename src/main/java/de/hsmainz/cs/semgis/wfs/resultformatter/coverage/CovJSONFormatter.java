package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.util.ReprojectionUtils;

public class CovJSONFormatter extends CoverageResultFormatter {

	public CovJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/covjson";
		this.urlformat="covjson";
		this.label="CoverageJSON";
		this.fileextension="covjson";
		this.definition="https://covjson.org";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException {	
		lastQueriedElemCount=1;
		JSONObject result=new JSONObject();
		result.put("type", "Coverage");
		JSONObject domain=new JSONObject();
		result.put("domain", domain);
		domain.put("type", "Domain");
		domain.put("domainType", "Grid");
		JSONObject axes=new JSONObject();
		domain.put("axes", axes);
		JSONObject parameters=new JSONObject();
		JSONObject parameter=new JSONObject();
		parameters.put("altitude",parameter);
		result.put("parameters",parameters);
		parameter.put("type","Parameter");
		JSONObject paramdescription=new JSONObject();
		parameter.put("description", paramdescription);
		paramdescription.put("en", "altitude");
		JSONObject unit=new JSONObject();		
		parameter.put("unit", unit);
		JSONObject unitlabel=new JSONObject();
		unitlabel.put("en", "meter");
		JSONObject unitsymbol=new JSONObject();
		unit.put("label", unitlabel);
		unit.put("symbol", unitsymbol);
		unitsymbol.put("value","meter");
		unitsymbol.put("type","http://www.opengis.net/def/uom/UCUM/");	
		JSONArray referencing=new JSONArray();
		domain.put("referencing", referencing);
		JSONObject ref=new JSONObject();
		referencing.put(ref);
		JSONArray coordinates=new JSONArray();
		coordinates.put("x");
		coordinates.put("y");
		ref.put("coordinates", coordinates);
		JSONObject system=new JSONObject();
		ref.put("system", system);
		system.put("type", "GeographicCRS");
		system.put("id", "http://www.opengis.net/def/crs/EPSG/0/"+srsName);
		JSONObject ranges=new JSONObject();
		result.put("ranges",ranges);
		JSONObject altituderange=new JSONObject();
		ranges.put("altitude",altituderange);
		altituderange.put("type","ndArray");
		altituderange.put("dataType", "float");	
		JSONArray axisNames=new JSONArray();
		altituderange.put("axisNames", axisNames);
		axisNames.put("x");
		axisNames.put("y");
		JSONArray values=new JSONArray();
		altituderange.put("values", values);
		JSONObject x=new JSONObject();
		x.put("values",new JSONArray());
		JSONObject y=new JSONObject();
		y.put("values", new JSONArray());
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
						Geometry geom=wktreader.read(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")));
						geom=ReprojectionUtils.reproject(geom, epsg, srsName);
						for(Coordinate coord:geom.getCoordinates()) {
							x.getJSONArray("values").put(coord.getX());
							y.getJSONArray("values").put(coord.getY());
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
					geom = wktreader.read("Point("+lon+" "+lat+")");
					geom=ReprojectionUtils.reproject(geom, epsg, srsName);
					for(Coordinate coord:geom.getCoordinates()) {
						x.getJSONArray("values").put(coord.getX());
						y.getJSONArray("values").put(coord.getY());
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
		axes.put("x", x);
		axes.put("y", y);
		return result.toString(2);
	}

}
