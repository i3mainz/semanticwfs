package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * Formats styles according to the styled layer description definition.
 *
 */
public class SLDFormatter extends ResultStyleFormatter {

	@Override
	public String formatter(ResultSet results,String featuretype) throws XMLStreamException {
		StringWriter strwriter = new StringWriter();
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
			writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
			writer.writeStartDocument();
			writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
			writer.writeAttribute("xmlns:sld", "http://www.opengis.net/sld");
			writer.writeAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
			writer.writeAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
			writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml");
			writer.writeAttribute("xmlns:xsi","http://www.w3.org/1999/xlink");
			writer.writeAttribute("version","1.0.0");
			writer.writeStartElement("sld:UserStyle");
			writer.writeStartElement("sld:Name");
			writer.writeCharacters(featuretype+" Style");
			writer.writeEndElement();
			writer.writeStartElement("sld:Title");
			writer.writeCharacters(featuretype+" Style");
			writer.writeEndElement();
			writer.writeStartElement("sld:Abstract");
			writer.writeCharacters("");
			writer.writeEndElement();
			writer.writeStartElement("sld:IsDefault");
			writer.writeCharacters("1");
			writer.writeEndElement();
			writer.writeStartElement("sld:FeatureTypeStyle");
			writer.writeStartElement("sld:Name");
			writer.writeEndElement();
			writer.writeStartElement("sld:Title");
			writer.writeEndElement();
			writer.writeStartElement("sld:Abstract");
			writer.writeEndElement();
			writer.writeStartElement("sld:FeatureTypeName");
			writer.writeCharacters(featuretype);
			writer.writeEndElement();
			writer.writeStartElement("sld:SemanticTypeIdentifier");
			writer.writeCharacters("generic:geometry");
			writer.writeEndElement();
			writer.writeStartElement("sld:Rule");
			writer.writeStartElement("sld:PolygonSymbolizer");
			cssLiteralToXML(writer, "");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			return exposedType;
	}
	
	/**
	 * Formats a cssliteral to an XML representation as needed by SLD.
	 * @param writer the XMLWriter to use
	 * @param cssString a String containing CSS definitions
	 */
	public void cssLiteralToXML(XMLStreamWriter writer,String cssString) {
		Map<String,Map<String,String>> treemap=new TreeMap<>();
		treemap.put("Fill",new TreeMap<>());
		treemap.put("Stroke",new TreeMap<>());
		if(cssString.contains(";")) {
			for(String statement:cssString.split(";")) {
				String[] split=statement.split(":");
				if(split[0].contains("stroke")) {
					treemap.get("Stroke").put(split[0],split[1]);
				}else if(split[0].contains("fill")) {
					treemap.get("Fill").put(split[0],split[1]);
				}
			}
			for(String key:treemap.keySet()) {
				try {
					writer.writeStartElement("sld:"+key);
					for(String keyy:treemap.get(key).keySet()) {
						writer.writeStartElement("sld:CssParameter");
						writer.writeAttribute("name", keyy);
						writer.writeStartElement("ogc:Literal");
						writer.writeCharacters(treemap.get(key).get(keyy));
						writer.writeEndElement();
						writer.writeEndElement();
					}
					writer.writeEndElement();
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	public String formatGeometry(String geometrytype,StyleObject style) {
		// TODO Auto-generated method stub
		return null;
	}

}
