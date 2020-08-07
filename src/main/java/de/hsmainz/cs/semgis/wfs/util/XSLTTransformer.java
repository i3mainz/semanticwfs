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

/**
 * A class performing XSLT transformations on XML files and XSD schema validations.
 *
 */
public class XSLTTransformer {
	
	/**
	 * Validates an XML file given as a String against an XSD schema given by a filepath to it on the harddisk.
	 * @param inputStr The XML document to verify
	 * @param schemapath The path to the XSD schema
	 * @return True if the validation was successful, False otherwise
	 * @throws SAXException on parsing errors
	 * @throws IOException on reading errors
	 */
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

	/**
	 * Converts a GMD representation to GeoDCAT using the GDM2GeoDCAT XSLT file.
	 * @param source A String representing a GMD XML document
	 * @return A GeoDCAT representation as XML
	 * @throws TransformerException when transformation errors occur
	 */
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
	
	/**
	 * Performs a transformation from GeoDCAT XML to GeoDCAT in HTML.
	 * @param source the GeoDCAT XML representation as String
	 * @return the HTML representation as a String
	 * @throws TransformerException when transformation errors occur
	 */
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

