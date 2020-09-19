package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.locationtech.jts.io.WKBWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.undercouch.bson4jackson.BsonFactory;

public class BSONFormatter extends VectorResultFormatter {

	public BSONFormatter() {
		this.mimeType="application/bson";
		this.exposedType="application/bson";
		this.urlformat="bson";
		this.label="Binary JSON (BSON)";
		this.fileextension="bson";
		this.definition="http://bsonspec.org";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY) throws XMLStreamException {
		ResultFormatter format = resultMap.get("geojson");
		String geojson=
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY);
		lastQueriedElemCount=format.lastQueriedElemCount;
		JsonNode jsonNodeTree;
		try {
			ObjectMapper mapper=new ObjectMapper(new BsonFactory());
			jsonNodeTree=mapper.readTree(geojson);
			return WKBWriter.bytesToHex(mapper.writeValueAsBytes(jsonNodeTree));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

}
