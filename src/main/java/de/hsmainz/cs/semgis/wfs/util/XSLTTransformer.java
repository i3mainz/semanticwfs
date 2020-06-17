package de.hsmainz.cs.semgis.wfs.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class XSLTTransformer {
	
	public static Boolean validateAgainstXSDSchema(String inputStr,String schemapath) throws SAXException, IOException {
	            SchemaFactory factory = 
	                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	            Schema schema = factory.newSchema(new File(schemapath));
	            Validator validator = schema.newValidator();
	            InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(inputStr.getBytes(StandardCharsets.UTF_8)));
	            try {
	            	validator.validate(new StreamSource(input));
	            	return true;
	            } catch (SAXException e) {
	            	e.printStackTrace();
	            	return false;
	            }
	}

	public static String gmdToGeoDCAT(String source) throws TransformerException  {
		System.out.println("Source: " + source);
		InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
		TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
		Source xmlSource = new StreamSource(input);
		Source xslSource = new StreamSource(new File("gmd2geodcat.xsl"));
		StringWriter outWriter = new StringWriter();
		StreamResult fOutresult = new StreamResult( outWriter );
		Transformer transformer = tFactory.newTransformer(xslSource);
		transformer.transform(xmlSource, fOutresult);
		return outWriter.toString();
	}
	
	public static String GeoDCATToHTML(String source) throws TransformerException  {
		System.out.println("Source: " + source);
		InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
		TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
		Source xmlSource = new StreamSource(input);
		Source xslSource = new StreamSource(new File("dcat-ap-rdf2rdfa.xsl"));
		StringWriter outWriter = new StringWriter();
		StreamResult fOutresult = new StreamResult( outWriter );
		Transformer transformer = tFactory.newTransformer(xslSource);
		transformer.transform(xmlSource, fOutresult);
		return outWriter.toString();
	}

}

