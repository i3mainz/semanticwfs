package de.hsmainz.cs.semgis.wfs.util;

import java.io.FileNotFoundException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

public abstract class AbstractTransformer {
	
	public abstract void transform(String source,String destination,String xsldoc, String baseURI, String superClass,
			String superClass2,String commentlang) throws FileNotFoundException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException;

}
