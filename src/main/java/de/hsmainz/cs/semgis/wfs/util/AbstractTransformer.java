package de.hsmainz.cs.semgis.wfs.util;

import java.io.FileNotFoundException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * An abstract class which describes a transformation process from a XSD schema to an OWL representation.
 */
public abstract class AbstractTransformer {
	
	public abstract void transform(String source,String destination,String xsldoc, String baseURI, String superClass,
			String superClass2,String commentlang) throws FileNotFoundException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException;

}
