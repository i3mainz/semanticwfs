package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.query.ResultSet;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.CoverageResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GMLCOVFormatter extends CoverageResultFormatter {

	public GMLCOVFormatter() {
		this.mimeType="application/xml";
		this.exposedType="application/gmlcov";
		this.urlformat="gmlcov";
		this.label="GMLCOV";
		this.fileextension="gmlcov";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,
			Boolean onlyproperty,Boolean onlyhits,String srsName,
			String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,
			Boolean alternativeFormat,Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
		writer.writeStartDocument();
		writer.writeAttribute("version","1.0");
		writer.writeStartElement("RectifiedGridCoverage");
		writer.writeNamespace("gmlcov", "http://www.opengis.net/gmlcov/1.0");
		writer.writeNamespace("gml", "http://www.opengis.net/gml/3.2");

		writer.writeStartElement("name");
		writer.writeCharacters(featuretype);
		writer.writeEndElement();
		// TODO Auto-generated method stub
		return null;
	}

}
