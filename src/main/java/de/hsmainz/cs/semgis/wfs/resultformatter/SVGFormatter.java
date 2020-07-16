package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.webservice.WebService;

public class SVGFormatter extends ResultFormatter {

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,Boolean alternativeRepresentation,Boolean invertXY)
			throws XMLStreamException {
		
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		writer.writeStartDocument();
		writer.writeStartElement("svg");
		writer.writeAttribute("version", "1.1");
		writer.writeAttribute("baseProfile", "full");
		writer.writeAttribute("width", "800mm");
		writer.writeAttribute("height", "600mm");
		writer.writeAttribute("viewBox", "-400 -300 800 600");
		
		/*<svg xmlns="http://www.w3.org/2000/svg"
			    xmlns:xlink="http://www.w3.org/1999/xlink"
			    version="1.1" baseProfile="full"
			    width="800mm" height="600mm"
			    viewBox="-400 -300 800 600">*/
		System.out.println(WebService.nameSpaceCache);
		System.out.println(featuretype.toLowerCase());
		for(String ns:WebService.nameSpaceCache.get(featuretype.toLowerCase()).keySet()) {
			writer.setPrefix(WebService.nameSpaceCache.get(featuretype.toLowerCase()).get(ns),ns);
		}
		
		// TODO Auto-generated method stub
		return null;
	}

}
