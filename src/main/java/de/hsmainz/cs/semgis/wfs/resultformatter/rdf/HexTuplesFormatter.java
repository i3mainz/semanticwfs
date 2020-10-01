package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.locationtech.jts.geom.Geometry;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class HexTuplesFormatter extends ResultFormatter {

	public HexTuplesFormatter() {
		this.mimeType="application/hex+x-ndjson";
		this.exposedType="application/hex+x-ndjson";
		this.urlformat="hextuples";
		this.label="HexTuples Format (RDF)";
		this.fileextension="ndjson";
		this.definition="https://github.com/ontola/hextuples";
		this.constructQuery=false;
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException, IOException {
		StringBuilder builder=new StringBuilder();
		List<String> rel=new LinkedList<>();
		List<String> val=new LinkedList<>();
		String lastInd="",geomLiteral="",lat="",lon="";
		out.write("[\"http://www.opengis.net/ont/geosparql#Geometry\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \"http://www.w3.org/2002/07/owl#Class\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"] "+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#Feature\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \"http://www.w3.org/2002/07/owl#Class\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#SpatialObject\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \"http://www.w3.org/2002/07/owl#Class\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#"+featuretype+"\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \"http://www.w3.org/2002/07/owl#Class\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#Feature\", \"http://www.w3.org/2000/01/rdf-schema#subClassOf\", \"http://www.opengis.net/ont/geosparql#SpatialObject\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#Geometry\", \"http://www.w3.org/2000/01/rdf-schema#subClassOf\", \"http://www.opengis.net/ont/geosparql#SpatialObject\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#hasGeometry\", \"http://www.w3.org/2000/01/rdf-schema#type\", \"http://www.w3.org/2002/07/owl#ObjectProperty\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#asWKT\", \"http://www.w3.org/2000/01/rdf-schema#type\", \"http://www.w3.org/2002/07/owl#DatatypeProperty\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
		out.write("[\"http://www.opengis.net/ont/geosparql#"+featuretype+"\", \"http://www.w3.org/2000/01/rdf-schema#subClassOf\", \"http://www.w3.org/2002/07/owl#Feature\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
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
						Geometry geom=(Geometry) obj;
						geomLiteral="\""+geom.toText()+"\", \""+VectorResultFormatter.WKTLiteral+"\", \"\", \"\" ]";
					}
				}else if(name.equalsIgnoreCase(indvar)){
					continue;
				}else if("rel".equalsIgnoreCase(name) || name.contains("_rel") || name.matches("rel[0-9]+$")){
					rel.add(solu.get(name).toString());
				}else if("val".equalsIgnoreCase(name) || name.contains("_val") || name.matches("val[0-9]+$")){
					val.add(solu.get(name).toString());
				}else if("lat".equalsIgnoreCase(name)){
					lat=solu.get(name).toString();
				}else if("lon".equalsIgnoreCase(name)){
					lon=solu.get(name).toString();
				}else {
					if(val.get(val.size()-1).startsWith("http") || val.get(val.size()-1).startsWith("file:/")) {
						out.write("[\""+solu.get(indvar)+"\", \""+name+"\", \""+solu.get(name).toString()+"\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\", \"\", \"\" ]"+System.lineSeparator());		
					}else if(val.get(val.size()-1).contains("^^")) {
						out.write("[\""+solu.get(indvar)+"\", \""+name+"\", \""+solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^"))+"\",\""+solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2)+"\", \"\", \"\" ]"+System.lineSeparator());
					}else {
						out.write("[\""+solu.get(indvar)+"\", \""+name+"\", \""+solu.get(name).toString()+"\",\"http://www.w3.org/2001/XMLSchema#string\", \"\", \"\" ]"+System.lineSeparator());
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
				Geometry geom=this.parseVectorLiteral("Point("+lon+" "+lat+")",VectorResultFormatter.WKTLiteral, epsg, srsName);
				if(geom!=null) {
					geomLiteral="\""+geom.toText()+"\", \"http://www.opengis.net/ont/geosparql#wktLiteral\", \"\", \"\" ]";
					out.write("[\""+solu.get(indvar)+"_geom\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \"http://www.opengis.net/ont/geosparql#Geometry\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\"]"+System.lineSeparator());
					out.write("[\""+solu.get(indvar)+"\", \"http://www.opengis.net/ont/geosparql#hasGeometry\", \""+solu.get(indvar)+"_geom\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\", \"\", \"\" ]"+System.lineSeparator());
					out.write("[\""+solu.get(indvar)+"_geom\", \"http://www.opengis.net/ont/geosparql#asWKT\", "+geomLiteral+System.lineSeparator());
				}
				lat="";
				lon="";
			}
			if(!rel.isEmpty() && !val.isEmpty()) {
				if(!geomLiteral.isEmpty()) {
					out.write("[\""+val.get(val.size()-1)+"\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \"http://www.opengis.net/ont/geosparql#Geometry\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\" ]"+System.lineSeparator());
					out.write("[\""+solu.get(indvar)+"\", \"http://www.opengis.net/ont/geosparql#hasGeometry\", \""+val.get(val.size()-1)+"\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\",\"\", \"\" ]"+System.lineSeparator());
					out.write("[\""+val.get(val.size()-1)+"\", \"http://www.opengis.net/ont/geosparql#asWKT\", \""+geomLiteral+System.lineSeparator());
				}else {
					addKeyValList(solu.get(indvar).toString(), rel, val, out);
				}
				rel.clear();
				val.clear();
				geomLiteral="";
			}
			lastInd=solu.get(indvar).toString();
		}
		return builder.toString();
	}
	
	
	public void addKeyValList(String item,Collection<String> rell,Collection<String> vall,Writer out) throws IOException {
		Iterator<String> reliter=rell.iterator();
		Iterator<String> valiter=vall.iterator();
		while(reliter.hasNext()) {
			String rel=reliter.next();
			String val=valiter.next();
			rel=rel.replace("[","").replace("]", "");
			val=val.replace("[","").replace("]", "");
			item=item.replace("[","").replace("]", "");
			if(item.startsWith("http")) {
			if(val.startsWith("http") || val.startsWith("file:/")) {
				out.write("[\""+item+"\", \""+rel+"\", \""+val+"\", \"http://www.w3.org/1999/02/22-rdf-syntax-ns#namedNode\", \"\", \"\" ]"+System.lineSeparator());		
			}else if(val.contains("^^")) {
				out.write("[\""+item+"\", \""+rel+"\", \""+val.substring(0,val.indexOf("^^"))+"\", \""+val.substring(val.indexOf("^^")+2)+"\", \"\", \"\" ]"+System.lineSeparator());
			}else {
				out.write("[\""+item+"\", \""+rel+"\", \""+val+"\", \"http://www.w3.org/2001/XMLSchema#string\", \"\", \"\" ]"+System.lineSeparator());
			}
			item=val;
			}
		}

	}	

}
