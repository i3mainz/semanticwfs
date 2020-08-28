package de.hsmainz.cs.semgis.wfs.util;

import java.io.FileNotFoundException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * An abstract class which describes a transformation process from a XSD schema to an OWL representation.
 */
public abstract class AbstractTransformer {
	
	/**
	 * Performs a transformation from a source XSD schema to a destination OWL representation.
	 * @param source The path to the XSD schema
	 * @param destination The destination 
	 * @param xsldoc The XSLT transformation stylesheet
	 * @param baseURI The baseURI 
	 * @param superClass
	 * @param superClass2
	 * @param commentlang
	 * @throws FileNotFoundException
	 * @throws TransformerConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public abstract void transform(String source,String destination,String xsldoc, String baseURI, String superClass,
			String superClass2,String commentlang) throws FileNotFoundException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException;

}
