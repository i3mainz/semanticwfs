package de.hsmainz.cs.semgis.wfs.resultmetadataformatter;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.util.XSLTTransformer;

/**
 * Formats metadata according to the GeoDCAT format.
 * This class takes metadata formatted in the GMD standard and uses a XSLT stylesheet to convert the results to GeoDCAT.
 *
 */
public class DCATFormatter extends ResultMetadataFormatter  {

	/**
	 * GMDFormatter to produce the initial GMD XML.
	 */
	GMDFormatter formatter=new GMDFormatter();
	
	@Override
	public String formatter(String collectionid, String collectioncall, String collectionurl, JSONObject workingobj,String format)
			throws XMLStreamException {
		String gmd=formatter.formatter(collectionid, collectioncall, collectionurl, workingobj,format);
		String gmdrdf;
		try {
			gmdrdf = XSLTTransformer.gmdToGeoDCAT(gmd);
		if(format.contains("html")) {
			this.mimeType=MediaType.TEXT_HTML;
			return XSLTTransformer.GeoDCATToHTML(gmdrdf);
		}
		OntModel model = ModelFactory.createOntologyModel();
		model.read(IOUtils.toInputStream(gmdrdf, "UTF-8"), null, "RDF/XML");
		OutputStream output = new OutputStream() {
		    private StringBuilder string = new StringBuilder();

		    @Override
		    public void write(int b) throws IOException {
		        this.string.append((char) b );
		    }

		    //Netbeans IDE automatically overrides this toString()
		    public String toString() {
		        return this.string.toString();
		    }
		};
		if(format.contains("n3")) {
			this.mimeType="text/n3";
			model.write(output,"N3");
			return output.toString();
		}
		if(format.contains("jsonld")) {
			this.mimeType="application/ld+json";
			model.write(output,"JSON-LD");
			return output.toString();
		}
		if(format.contains("ttl")) {
			this.mimeType="text/ttl";
			model.write(output,"TTL");
			return output.toString();
		}
		if(format.contains("ntriples")) {
			this.mimeType="application/n-triples";
			model.write(output,"NTRIPLES");
			return output.toString();
		}
		this.mimeType=MediaType.APPLICATION_XML;
		return gmdrdf;
		} catch (TransformerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

}
