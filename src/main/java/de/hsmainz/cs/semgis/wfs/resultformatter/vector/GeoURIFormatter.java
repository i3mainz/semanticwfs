package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.locationtech.jts.geom.Geometry;

import de.hsmainz.cs.semgis.wfs.resultformatter.WFSResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.util.ReprojectionUtils;

/**
 * Formats a query result to GeoURI.
 * Only geometries will be serialized, attributes will be ignored.
 *
 */
public class GeoURIFormatter extends WFSResultFormatter {

	public GeoURIFormatter() {
		this.mimeType="text/plain";
		this.exposedType="text/geouri";
		this.urlformat="geouri";
		this.label="GeoURI";
		this.fileextension="txt";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) {
		List<QuerySolution> test=ResultSetFormatter.toList(results);
    	StringBuilder resultCSV=new StringBuilder();
    	String lastInd="";
	    for(QuerySolution solu:test) {
	    	Iterator<String> varnames = solu.varNames();
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		String curfeaturetype="";
		    	if(solu.get(indvar)!=null) {
					curfeaturetype=solu.get(indvar).toString();
					if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
						curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
					}
					if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
						lastQueriedElemCount++;
					}
				}
	    		if(name.endsWith("_geom")) {
	    			try {
						Geometry geom=ReprojectionUtils.reproject(reader.read(solu.getLiteral(name).getString()), epsg,srsName);
						String uricode="";
						if(!srsName.isEmpty()) {
							if(srsName.startsWith("http")) {
								uricode="EPSG:"+srsName.substring(srsName.lastIndexOf("/")+1);
							}else {
								uricode=srsName;
							}
						}else {
							uricode=epsg;
						}
		                if("POINT".equalsIgnoreCase(geom.getGeometryType())) {
		                	resultCSV.append("geo:"+geom.getCoordinate().x+","+geom.getCoordinate().y+";crs="+uricode+System.lineSeparator());
		                }else {
		                	resultCSV.append("geo:"+geom.getCentroid().getCoordinate().x+","+geom.getCentroid().getCoordinate().y+";crs="+uricode+System.lineSeparator());
		                }
	    			}catch(Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	    	}	
	    }
	    return resultCSV.toString();
	}

}
