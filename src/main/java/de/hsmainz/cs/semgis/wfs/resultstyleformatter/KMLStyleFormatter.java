package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

public class KMLStyleFormatter extends ResultStyleFormatter {

	@Override
	public String formatter(ResultSet results, String featuretype) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

	public void cssLiteralToKML(String cssString,XMLStreamWriter writer) throws XMLStreamException {
		if(cssString==null)
			return;
		if(cssString.contains(";")) {
			for(String statement:cssString.split(";")) {
				String[] split=statement.split(":");
				writer.writeStartElement(split[0]);
				writer.writeCharacters(split[1]);
				writer.writeEndElement();
			}
		}else {
			String[] split=cssString.split(":");
			writer.writeStartElement(split[0]);
			writer.writeCharacters(split[1]);
			writer.writeEndElement();
		}
	}
	
	@Override
	public String formatGeometry(String geometrytype, StyleObject styleobj) {
		if(styleobj==null)
			return "";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		try {
			XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
			writer.writeStartElement(geometrytype+"Style");
			writer.writeCharacters("");
			writer.flush();
			if(geometrytype.contains("Point")) {
				cssLiteralToKML(styleobj.pointStyle,writer);
			}
			if(geometrytype.contains("LineString")) {
				cssLiteralToKML(styleobj.lineStringStyle,writer);
			}
			if(geometrytype.contains("Polygon")) {
				cssLiteralToKML(styleobj.polygonStyle,writer);
			}
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strwriter.toString();
	}

}
