package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.main.api.sax.EXIResult;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class RDFEXIFormatter extends ResultFormatter {

	/**
	 * Constructor for this class.
	 */
	public RDFEXIFormatter() {
		this.mimeType="text/rdf+xml+exi";
		this.exposedType="text/rdf+xml+exi";
		this.urlformat="rdfexi";
		this.label="RDF/EXI (RDF)";
		this.fileextension="rdf";
		this.definition="https://www.w3.org/TR/exi-c14n/";
		this.constructQuery=true;
	}
	
	@Override
	public String formatter(Model results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		return null;
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException, IOException {
		ResultFormatter format = resultMap.get("ttl");
		String ttl=format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,srsName,indvar,
				epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out);
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		OntModel model=ModelFactory.createOntologyModel();
		InputStream result = new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8));
		model.read(result, null, "TTL");
		System.out.println("RDF Formatter!!!!");
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
		model.write(bOutput,"RDF/XML");
        String xmlstring = new String(bOutput.toByteArray());
		EXIFactory exiFactory = DefaultEXIFactory.newInstance();
		try {
			bOutput = new ByteArrayOutputStream(12);
			EXIResult exiResult = new EXIResult(exiFactory);		
			exiResult.setOutputStream(bOutput);
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler( exiResult.getHandler() );
			xmlReader.parse(new InputSource(new StringReader(xmlstring)));
			bOutput.flush();
			return new String(bOutput.toByteArray(),"UTF-8");
		} catch (EXIException | IOException | SAXException e) {
			e.printStackTrace();
			return "";
		}
	}

}
