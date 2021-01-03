package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GEXFFormatter extends ResultFormatter {

	public GEXFFormatter() {
		this.urlformat="gexf";
		this.label="Graph Exchange XML Format";
		this.mimeType="text/gexf";
		this.exposedType="text/gexf";
		this.fileextension="gexf";
		this.definition="https://gephi.org/gexf/format/";
		this.constructQuery=false;
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		ResultFormatter format = resultMap.get("ttl");
		String ttl=format.formatter(results,startingElement, featuretype,propertytype, typeColumn, onlyproperty,onlyhits,
				srsName,indvar,epsg,eligiblenamespaces,noteligiblenamespaces,mapstyle,alternativeFormat,invertXY,coverage,out);
		this.lastQueriedElemCount = format.lastQueriedElemCount;
		OntModel model=ModelFactory.createOntologyModel();
		InputStream result = new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8));
		model.read(result, null, "TTL");
        Set<Resource> resources = new HashSet<>();
        model.listStatements().toList()
                .forEach( statement -> {
                    resources.add(statement.getSubject());
                    RDFNode object = statement.getObject();
                    if (object.isResource()) {
                        resources.add(object.asResource());
                    }
        });
        Set<String> uriToNodeId = new HashSet<String>();
		Integer literalcounter = 0, edgecounter = 0;
		StringWriter strwriter = new StringWriter();
		StringBuilder builder=new StringBuilder();
		builder.append("<edges>"+System.lineSeparator());
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		try {
			XMLStreamWriter writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
			writer.writeStartDocument();
			writer.writeStartElement("gexf");
			writer.writeAttribute("xmlns", "http://www.gexf.net/1.2draft");
			writer.writeAttribute("version", "1.2");
			writer.writeStartElement("graph");
			writer.writeAttribute("mode", "static");
			writer.writeAttribute("defaultedgetype", "directed");
			writer.writeStartElement("nodes");
			for (Resource res : resources) {
				if (!res.isURIResource())
					continue;
				String subprefix = null;
				if (!uriToNodeId.contains(res.getURI())) {
					writer.writeStartElement("node");
					writer.writeAttribute("id", res.getURI());
					writer.writeAttribute("uri", res.getURI());
					subprefix = res.getModel().getNsURIPrefix(res.getNameSpace());
					if (subprefix != null) {
						writer.writeAttribute("label",subprefix + ":" + res.getLocalName());
					} else {
						writer.writeAttribute("label",res.getLocalName());
					}
					writer.writeEndElement();
					uriToNodeId.add(res.getURI());
				}
				StmtIterator propiter = res.listProperties();
				while (propiter.hasNext()) {
					Statement curst = propiter.next();
					if (curst.getObject().isURIResource()) {
						if (!uriToNodeId.contains(curst.getObject().asResource().getURI())) {
							writer.writeStartElement("node");
							writer.writeAttribute("id", curst.getObject().asResource().getURI());
							subprefix = res.getModel().getNsURIPrefix(curst.getObject().asResource().getNameSpace());
							if (subprefix != null) {
								writer.writeAttribute("label",subprefix + ":" + curst.getObject().asResource().getLocalName());
							} else {
								writer.writeAttribute("label",curst.getObject().asResource().getLocalName());
							}
							writer.writeAttribute("uri", curst.getObject().asResource().getURI());
							writer.writeEndElement();
							uriToNodeId.add(curst.getObject().asResource().getURI());
						}
						builder.append("<edge source=\""+curst.getSubject().getURI()+"\" target=\""+curst.getObject().asResource().getURI()+"\" label=\""+curst.getPredicate().getURI()+"\" id=\""+"e" + edgecounter+++"\"/>"+System.lineSeparator());
					} else if (curst.getObject().isLiteral()) {
						writer.writeStartElement("node");
						writer.writeAttribute("id", "literal" + literalcounter);
						writer.writeAttribute("label", curst.getObject().asLiteral().getLexicalForm()+" ("+curst.getObject().asLiteral().getDatatypeURI()+")");
						writer.writeEndElement();
						builder.append("<edge source=\""+curst.getSubject().getURI()+"\" target=\"literal" + literalcounter+"\" label=\""+curst.getPredicate().getURI()+"\" id=\""+"e" + edgecounter+++"\"/>"+System.lineSeparator());
						literalcounter++;
					}
				}
			}
			writer.writeEndElement();
			writer.flush();
			strwriter.write(builder.toString()+"</edges>"+System.lineSeparator());
			strwriter.flush();
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			strwriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.write(strwriter.toString());
		out.close();
		return null;
	}
	
	

}
