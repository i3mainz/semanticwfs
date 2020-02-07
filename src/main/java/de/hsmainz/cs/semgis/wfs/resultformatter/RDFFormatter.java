package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONObject;

public class RDFFormatter extends ResultFormatter {

	public RDFFormatter() {
		this.mimeType="text/ttl";
		this.exposedType="text/ttl";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,String epsg) throws XMLStreamException {
		StringBuilder builder=new StringBuilder();
		String rel="",val="",lastInd="",geomLiteral="",lat="",lon="";
		builder.append("<http://www.opengis.net/ont/geosparql#Geometry> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#Feature> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#SpatialObject> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#"+featuretype+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#Feature> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.opengis.net/ont/geosparql#SpatialObject> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#Geometry> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.opengis.net/ont/geosparql#SpatialObject> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#hasGeometry> <http://www.w3.org/2000/01/rdf-schema#type> <http://www.w3.org/2002/07/owl#ObjectProperty> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#asWKT> <http://www.w3.org/2000/01/rdf-schema#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> ."+System.lineSeparator());
		builder.append("<http://www.opengis.net/ont/geosparql#"+featuretype+"> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2002/07/owl#Feature> ."+System.lineSeparator());
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			Iterator<String> varnames=solu.varNames();
			if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
				lastQueriedElemCount++;
			}
			while(varnames.hasNext()) {
				String name=varnames.next();
				if(name.endsWith("_geom")) {
					geomLiteral="\""+solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^"))+"\"^^<"+solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2)+"> .";
				}else if(name.equalsIgnoreCase(indvar)){
					continue;
				}else if("rel".equalsIgnoreCase(name) || name.contains("_rel")){
					rel=solu.get(name).toString();
				}else if("val".equalsIgnoreCase(name) || name.contains("_val")){
					val=solu.get(name).toString();
				}else if("lat".equalsIgnoreCase(name)){
					lat=solu.get(name).toString();
				}else if("lon".equalsIgnoreCase(name)){
					lon=solu.get(name).toString();
				}else {
					if(val.startsWith("http") || val.startsWith("file:/")) {
						builder.append("<"+solu.get(indvar)+"> <"+name+"> <"+solu.get(name).toString()+"> ."+System.lineSeparator());		
					}else if(val.contains("^^")) {
						builder.append("<"+solu.get(indvar)+"> <"+name+"> \""+solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^"))+"\"^^<"+solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2)+"> ."+System.lineSeparator());
					}else {
						builder.append("<"+solu.get(indvar)+"> <"+name+"> \""+solu.get(name).toString()+"\"^^<http://www.w3.org/2001/XMLSchema#string> ."+System.lineSeparator());
					}
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
				geomLiteral="\"Point("+lon+" "+lat+")\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> .";
				builder.append("<"+solu.get(indvar)+"_geom> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.opengis.net/ont/geosparql#Geometry> ."+System.lineSeparator());
				builder.append("<"+solu.get(indvar)+"> <http://www.opengis.net/ont/geosparql#hasGeometry> <"+solu.get(indvar)+"_geom> ."+System.lineSeparator());
				builder.append("<"+solu.get(indvar)+"_geom> <http://www.opengis.net/ont/geosparql#asWKT> "+geomLiteral+System.lineSeparator());
				lat="";
				lon="";
			}
			if(!rel.isEmpty() && !val.isEmpty()) {
				if(!geomLiteral.isEmpty()) {
					builder.append("<"+val+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.opengis.net/ont/geosparql#Geometry> ."+System.lineSeparator());
					builder.append("<"+solu.get(indvar)+"> <http://www.opengis.net/ont/geosparql#hasGeometry> <"+val+"> ."+System.lineSeparator());
					builder.append("<"+val+"> <http://www.opengis.net/ont/geosparql#asWKT> "+geomLiteral+System.lineSeparator());
				}else if(val.startsWith("http") || val.startsWith("file:/")) {
					builder.append("<"+solu.get(indvar)+"> <"+rel+"> <"+val+"> ."+System.lineSeparator());		
				}else if(val.contains("^^")) {
					builder.append("<"+solu.get(indvar)+"> <"+rel+"> \""+val.substring(0,val.indexOf("^^"))+"\"^^<"+val.substring(val.indexOf("^^")+2)+"> ."+System.lineSeparator());
				}else {
					builder.append("<"+solu.get(indvar)+"> <"+rel+"> \""+val+"\"^^<http://www.w3.org/2001/XMLSchema#string> ."+System.lineSeparator());
				}
				rel="";
				val="";
				geomLiteral="";
			}
			lastInd=solu.get(indvar).toString();
		}
		return builder.toString();
	}

}
