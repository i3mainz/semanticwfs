package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import java.util.Map;
import java.util.TreeMap;

public class KMLStyleFormatter extends ResultStyleFormatter {

	public Map<String,Map<String,String>> tagMap;
	
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
	
	public Map<String,String> cssLiteralToMap(String cssString,XMLStreamWriter writer) throws XMLStreamException {
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
				cssLiteralToKML(cssLiteralToMap(styleobj.pointStyle, writer),"Point",writer);
				writer.writeStartElement("Icon");
				writer.writeStartElement("href");
				writer.writeCharacters(styleobj.pointImage);
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();
			}
			if(geometrytype.contains("LineString") && styleobj.lineStringStyle!=null && !styleobj.lineStringStyle.trim().isEmpty()) {
				cssLiteralToKML(cssLiteralToMap(styleobj.lineStringStyle, writer),"Point",writer);
			}
			if(geometrytype.contains("Polygon")  && styleobj.polygonStyle!=null && !styleobj.polygonStyle.trim().isEmpty()) {
				cssLiteralToKML(cssLiteralToMap(styleobj.polygonStyle, writer),"Point",writer);
			}			
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strwriter.toString();
	}

}
