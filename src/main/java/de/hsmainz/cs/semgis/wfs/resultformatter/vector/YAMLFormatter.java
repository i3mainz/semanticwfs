package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class YAMLFormatter extends ResultFormatter {

	
	/**
	 * Constructor for this class.
	 */
	public YAMLFormatter() {
		this.mimeType="application/yaml";
		this.exposedType="text/vnd.yaml";
		this.urlformat="yaml";
		this.label="YAML Ain't Markup Language (YAML)";
		this.fileextension="yaml";
		this.definition="https://yaml.org";
	}
		
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException, IOException {
		ResultFormatter format = resultMap.get("geojson");
		String geojson=
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,
						epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out);
		lastQueriedElemCount=format.lastQueriedElemCount;
		JsonNode jsonNodeTree;
		try {
			jsonNodeTree = new ObjectMapper().readTree(geojson);
	        new YAMLMapper().writeValue(out,jsonNodeTree);
	        return "";
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

}
