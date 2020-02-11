package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

public class SLDFormatter extends ResultStyleFormatter {

	@Override
	public String formatter(ResultSet results) throws XMLStreamException {
		StringWriter strwriter = new StringWriter();
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
			writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
			writer.writeStartDocument();
			writer.writeStartElement("sf:FeatureCollection");
			writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
			writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
			writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			writer.writeAttribute("xmlns:sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
			writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
			return exposedType;
	}

}
