package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class PostgreSQLFormatter extends VectorResultFormatter {

	WKBWriter writer=new WKBWriter();
	
	public PostgreSQLFormatter() {
		this.mimeType="text/psql";
		this.exposedType="text/psql";
		this.urlformat="psql";
		this.label="PostgreSQL (SQL)";
		this.fileextension="psql";
		this.definition="https://www.iso.org/standard/40114.html";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		out.write("SET standard_conforming_strings = OFF;"+System.lineSeparator());
		out.write("DROP TABLE IF EXISTS \"public\".\""+featuretype+"\" CASCADE;"+System.lineSeparator());		
		out.write("DELETE FROM geometry_columns WHERE f_table_name = 'test2' AND f_table_schema = 'public';"+System.lineSeparator());
		out.write("BEGIN;"+System.lineSeparator());
		out.write("CREATE TABLE \"public\".\""+featuretype+"\" ( \"ogc_fid\" SERIAL, CONSTRAINT \""+featuretype+"_pk\" PRIMARY KEY (\"ogc_fid\") );"+System.lineSeparator());
		out.write("SELECT AddGeometryColumn('public','"+featuretype+"','wkb_geometry',"+epsg+",'POLYGON',2);"+System.lineSeparator());
		out.write("CREATE INDEX \""+featuretype+"_wkb_geometry_geom_idx\" ON \"public\".\""+featuretype+"\" USING GIST (\"wkb_geometry\");"+System.lineSeparator());
		Map<String,String> valmap=new TreeMap<String,String>();
		String lat="",lon="",lastInd="";
		Geometry geom=null;
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			Iterator<String> varnames=solu.varNames();
			if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
				if(lastQueriedElemCount==1) {
					for(String key:valmap.keySet()) {
						out.write("ALTER TABLE \"public\".\""+featuretype+"\" ADD COLUMN \""+key+"\" VARCHAR;"+System.lineSeparator());
					}
				}
				if(lastQueriedElemCount>0) {
				out.write("INSERT INTO \"public\".\""+featuretype+"\" VALUES (");
				out.write("'"+WKBWriter.toHex(writer.write(geom))+"',");
				Iterator<String> iter=valmap.values().iterator();
				while(iter.hasNext()) {
					out.write("'"+iter.next()+"'");
					if(iter.hasNext()) {
						out.write(",");
					}
				}
				out.write(");"+System.lineSeparator());
				}
				lastQueriedElemCount++;
			}
			while(varnames.hasNext()) {
				String name=varnames.next();
				if(name.endsWith("_geom")) {
					geom=this.parseVectorLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
							solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
				}else if (name.endsWith("_val") || name.equals("val") || name.matches("val[0-9]+$")) {
					valmap.put(name,solu.get(name).toString());
				}else if(name.equalsIgnoreCase(indvar)){
					continue;
				}else if("lat".equalsIgnoreCase(name)){
					lat=solu.get(name).toString();
				}else if("lon".equalsIgnoreCase(name)){
					lon=solu.get(name).toString();
				}else {
					valmap.put(name, solu.get(name).toString());
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
				geom=this.parseVectorLiteral("Point("+lon+" "+lat+")",WKTLiteral, epsg, srsName);
				lat="";
				lon="";
			}
			lastInd=solu.get(indvar).toString();
			if(lastQueriedElemCount%FLUSHTHRESHOLD==0)
				out.flush();
		}
		out.write("COMMIT;");
				// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Adds a key/value pair to a JSONObject, creates a JSONArray if neccessary.
	 * @param properties the JSONObject
	 * @param rel Relation to add
	 * @param val Value to add
	 */
	public void addKeyVal(JSONObject properties,String rel,String val) {
		if(properties.has(rel)) {			
			try {
				properties.getJSONArray(rel).put(val);
			}catch(JSONException e) {
				String oldval=properties.getString(rel);
				properties.put(rel,new JSONArray());
				properties.getJSONArray(rel).put(oldval);
				properties.getJSONArray(rel).put(val);
			}
		}else {
			properties.put(rel, val);
		}
	}

	
}
