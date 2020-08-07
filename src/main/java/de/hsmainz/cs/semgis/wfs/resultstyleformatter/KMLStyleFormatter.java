package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import java.util.Map;
import java.util.TreeMap;

/**
 * Reads an ontological style description and formats it to KML Styles.
 *
 */
public class KMLStyleFormatter extends ResultStyleFormatter {

	/**
	 * A map which includes tags which are relevant for specific geometry types
	 */
	public Map<String,Map<String,String>> tagMap;
	
	/**
	 * Constructor for this class.
	 */
	public KMLStyleFormatter() {
		this.tagMap=new TreeMap<>();
		this.tagMap.put("Point",new TreeMap<String,String>());
		this.tagMap.put("LineString",new TreeMap<String,String>());
		this.tagMap.get("LineString").put("stroke","");
		this.tagMap.get("LineString").put("stroke-width","width");
		this.tagMap.put("Polygon",new TreeMap<String,String>());
		this.tagMap.get("Polygon").put("stroke","");
		this.tagMap.get("Polygon").put("stroke-width","width");
	}
	
	@Override
	public String formatter(ResultSet results, String featuretype) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Converts a CSS literal to a KML representation to include in a KML document.
	 * @param cssString the css String contained in the literal
	 * @return A map of style attributes to include into the KML representation. 
	 */
	public Map<String,String> cssLiteralToMap(String cssString) {
		Map<String,String> result=new TreeMap<String,String>();
		if(cssString.contains(";")) {
			for(String statement:cssString.split(";")) {
				String[] split=statement.split(":");
				if(!split[1].trim().isEmpty()) {
					String key=split[0].replace("\\\"","").trim();
					result.put(key,split[1].replace("\\\"","").replace("^","").replace("http","").replace("true","1").trim());
				}
			}
		}else if(cssString.contains(",")) {
			for(String statement:cssString.split(",")) {
				String[] split=statement.split(":");
				if(!split[1].trim().isEmpty()) {					
					String key=split[0].replace("\\\"","").trim();
					result.put(key,split[1].replace("\\\"","").replace("^","").replace("http","").replace("true","1").trim());
				}
			}
		}else {
			String[] split=cssString.split(":");
			if(!split[1].trim().isEmpty()) {
				String key=split[0].replace("\\\"","").trim();
				result.put(key,split[1].replace("\\\"","").replace("^","").replace("http","").replace("true","1").trim());
			}
		}
		return result;
	}

	/**
	 * Converts a CSS literal which has been converted to a map to KML and appends it to the XML produced by the writer.
	 * @param cssMap the map of css statements
	 * @param geomtype the geometry type to which the CSS style is applied
	 * @param writer the XMLStreamWriter to write the results with
	 * @throws XMLStreamException when a XML writing error occurs
	 */
	public void cssLiteralToKML(Map<String,String> cssMap,String geomtype,XMLStreamWriter writer) throws XMLStreamException {
		if(geomtype.equals("Polygon")) {
			writer.writeStartElement("PolyStyle");
			if(cssMap.containsKey("color")) {
				writer.writeStartElement("color");
				writer.writeCharacters(cssMap.get("color"));
				writer.writeEndElement();
				writer.writeStartElement("colorMode");
				writer.writeCharacters("normal");
				writer.writeEndElement();
			}
			if(cssMap.containsKey("fill")) {
				writer.writeStartElement("fill");
				writer.writeCharacters(cssMap.get("fill").replace("\\\"","").replace("^","").replace("http","").replace("true","1").replace("false","0").trim());
				writer.writeEndElement();
			}
			if(cssMap.containsKey("stroke")) {
				writer.writeStartElement("outline");
				writer.writeCharacters(cssMap.get("stroke").replace("\\\"","").replace("^","").replace("http","").replace("true","1").replace("false","0").trim());
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}else if(geomtype.equals("LineString")) {
			writer.writeStartElement("LineStyle");
			if(cssMap.containsKey("color")) {
				writer.writeStartElement("color");
				writer.writeCharacters(cssMap.get("color"));
				writer.writeEndElement();
				writer.writeStartElement("colorMode");
				writer.writeCharacters("normal");
				writer.writeEndElement();
			}
			if(cssMap.containsKey("stroke-width")) {
				writer.writeStartElement("width");
				writer.writeCharacters(cssMap.get("stroke-width"));
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}else if(geomtype.equals("Point")) {
			if(cssMap.containsKey("color")) {
				writer.writeStartElement("color");
				writer.writeCharacters(cssMap.get("color"));
				writer.writeEndElement();
				writer.writeStartElement("colorMode");
				writer.writeCharacters("normal");
				writer.writeEndElement();
			}
			if(cssMap.containsKey("stroke-width")) {
				writer.writeStartElement("width");
				writer.writeCharacters(cssMap.get("stroke-width"));
				writer.writeEndElement();
			}
		}
		
	}
	
	@Override
	public String formatGeometry(String geometrytype, StyleObject styleobj) {
		if(styleobj==null)
			return "";
		System.out.println(geometrytype);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		try {
			XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));

			writer.flush();
			if(geometrytype.contains("Point") && styleobj.pointStyle!=null && !styleobj.pointStyle.trim().isEmpty()) {
				writer.writeStartElement("IconStyle");
				writer.writeCharacters("");		
				cssLiteralToKML(cssLiteralToMap(styleobj.pointStyle),"Point",writer);
				writer.writeStartElement("Icon");
				writer.writeStartElement("href");
				writer.writeCharacters(styleobj.pointImage);
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();
			}
			if(geometrytype.contains("LineString") && styleobj.lineStringStyle!=null && !styleobj.lineStringStyle.trim().isEmpty()) {
				cssLiteralToKML(cssLiteralToMap(styleobj.lineStringStyle),"Point",writer);
			}
			if(geometrytype.contains("Polygon")  && styleobj.polygonStyle!=null && !styleobj.polygonStyle.trim().isEmpty()) {
				cssLiteralToKML(cssLiteralToMap(styleobj.polygonStyle),"Point",writer);
			}			
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strwriter.toString();
	}

}
