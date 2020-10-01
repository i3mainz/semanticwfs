package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

/**
 * Formats a query result to SVG.
 */
public class SVGFormatter extends ResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public SVGFormatter() {
		this.mimeType="image/svg+xml";
		this.exposedType="image/svg+xml";
		this.urlformat="svg";
		this.label="Scalable Vector Graphics (SVG)";
		this.fileextension="svg";
		this.definition="https://www.w3.org/TR/SVG11/";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeRepresentation,Boolean invertXY, Boolean coverage,Writer out)
			throws XMLStreamException, JSONException, IOException {
		ResultFormatter format = resultMap.get("geojson");
		JSONObject geojson = new JSONObject(
				format.formatter(results,startingElement, featuretype,propertytype, typeColumn, 
						onlyproperty,onlyhits,srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,
						mapstyle,alternativeRepresentation,invertXY,coverage,out));
		lastQueriedElemCount=format.lastQueriedElemCount;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=null;
		if(out!=null) {
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out));
		}else {
			writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		}
		writer.writeStartDocument();
		writer.writeStartElement("svg");
		writer.writeAttribute("version", "1.1");
		writer.writeAttribute("baseProfile", "full");
		writer.writeAttribute("width", "800mm");
		writer.writeAttribute("height", "600mm");
		writer.writeAttribute("viewBox", "-400 -300 800 600");
		for(int i=0;i<geojson.getJSONArray("features").length();i++) {
			JSONObject feature=geojson.getJSONArray("features").getJSONObject(i);
			JSONObject geometry=feature.getJSONObject("geometry");
			if(geometry.getString("type").equalsIgnoreCase("Point")) {
				writer.writeStartElement("circle");
				writer.writeAttribute("stroke", "black");
				writer.writeAttribute("stroke-width","3");
				writer.writeAttribute("r", "10");
				writer.writeAttribute("cy", geometry.getJSONArray("coordinates").getDouble(1)+"");
				writer.writeAttribute("cx", geometry.getJSONArray("coordinates").getDouble(0)+"");
				writer.writeEndElement();
			}else {
				writer.writeStartElement("polyline");
				writer.writeAttribute("stroke", "black");
				writer.writeAttribute("stroke-width","3");
				String points="";
				for(int j=0;j<geometry.getJSONArray("coordinates").length()-1;j+=2) {
					points+=geometry.getJSONArray("coordinates").getDouble(j)+","+geometry.getJSONArray("coordinates").getDouble(j+1);
				}
				writer.writeAttribute("points", points);
				writer.writeEndElement();
			}

		}
		/*<svg xmlns="http://www.w3.org/2000/svg"
			    xmlns:xlink="http://www.w3.org/1999/xlink"
			    version="1.1" baseProfile="full"
			    width="800mm" height="600mm"
			    viewBox="-400 -300 800 600">*/
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		return strwriter.toString();
	}

}
