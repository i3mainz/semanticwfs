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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GraphMLFormatter extends ResultFormatter {

	public GraphMLFormatter() {
		this.urlformat="graphml";
		this.label="GraphML";
		this.mimeType="text/graphml";
		this.exposedType="text/graphml";
		this.fileextension="graphml";
		this.definition="http://graphml.graphdrawing.org";
		this.constructQuery=false;
	}
	
	
	public String getColorForResource(OntModel model, Resource res,String defaultColor) {
		Property scof=model.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		Property spof=model.createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		Property rdftype=model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		if(res.getURI().contains("http://www.w3.org/2000/01/rdf-schema#")) {
			return "#F08080";
		}else if(res.listProperties(spof).hasNext()){
			return "#F0F8FF";	
		}else if(res.listProperties(scof).hasNext()){
			return "#ffa500";
		}else if(res.listProperties(rdftype).hasNext()) {
			StmtIterator it=res.listProperties(rdftype);
			while(it.hasNext()) {
				Statement st=it.next();
				if(st.getObject().isURIResource() && st.getObject().asResource().getURI().equals("http://www.w3.org/2002/07/owl#Class")) {
					return "#ffa500";
				}
				if(st.getObject().isURIResource() && st.getObject().asResource().getURI().equals("http://www.w3.org/2002/07/owl#Property")) {
					return "#F0F8FF";
				}
			}
		}
		return defaultColor;
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
		Integer typecounter = 0, langcounter = 0, valcounter = 0;
		StringWriter strwriter = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		try {
			XMLStreamWriter writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(strwriter));
			writer.writeStartDocument();
			writer.writeStartElement("graphml");
			writer.writeAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
			writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			writer.writeAttribute("xmlns:y", "http://www.yworks.com/xml/graphml");
			writer.writeAttribute("xmlns:schemaLocation",
					"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
			writer.writeStartElement("key");
			writer.writeAttribute("for", "node");
			writer.writeAttribute("id", "nodekey");
			writer.writeAttribute("yfiles.type", "nodegraphics");
			writer.writeEndElement();
			writer.writeStartElement("key");
			writer.writeAttribute("for", "edge");
			writer.writeAttribute("id", "edgekey");
			writer.writeAttribute("yfiles.type", "edgegraphics");
			writer.writeEndElement();
			writer.writeStartElement("graph");
			writer.writeAttribute("id", "G");
			writer.writeAttribute("edgedefault", "undirected");
			for (Resource res : resources) {
				if (!res.isURIResource())
					continue;
				String subprefix = null;
				if (!uriToNodeId.contains(res.getURI())) {
					writer.writeStartElement("node");
					writer.writeAttribute("id", res.getURI());
					writer.writeAttribute("uri", res.getURI());
					writer.writeStartElement("data");
					writer.writeAttribute("key", "nodekey");
					literalcounter++;
					writer.writeStartElement("y:ShapeNode");
					writer.writeStartElement("y:Shape");
					writer.writeAttribute("shape", "ellipse");
					writer.writeEndElement();
					writer.writeStartElement("y:Fill");
					writer.writeAttribute("color", this.getColorForResource(model, res, "#800080"));
					writer.writeAttribute("transparent", "false");
					writer.writeEndElement();
					writer.writeStartElement("y:NodeLabel");
					writer.writeAttribute("alignment", "center");
					writer.writeAttribute("autoSizePolicy", "content");
					writer.writeAttribute("fontSize", "12");
					writer.writeAttribute("fontStyle", "plain");
					writer.writeAttribute("hasText", "true");
					writer.writeAttribute("visible", "true");
					writer.writeAttribute("width", "4.0");
					subprefix = res.getModel().getNsURIPrefix(res.getNameSpace());
					if (subprefix != null) {
						writer.writeCharacters(subprefix + ":" + res.getLocalName());
					} else {
						writer.writeCharacters(res.getLocalName());
					}
					writer.writeEndElement();
					writer.writeEndElement();
					writer.writeEndElement();
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
							writer.writeAttribute("value", curst.getObject().asResource().getLocalName());
							writer.writeAttribute("uri", curst.getObject().asResource().getURI());
							writer.writeStartElement("data");
							writer.writeAttribute("key", "nodekey");
							literalcounter++;
							writer.writeStartElement("y:ShapeNode");
							writer.writeStartElement("y:Shape");
							writer.writeAttribute("shape", "ellipse");
							writer.writeEndElement();
							writer.writeStartElement("y:Fill");
							writer.writeAttribute("color", this.getColorForResource(model, curst.getObject().asResource(), "#800080"));
							writer.writeAttribute("transparent", "false");
							writer.writeEndElement();
							writer.writeStartElement("y:NodeLabel");
							writer.writeAttribute("alignment", "center");
							writer.writeAttribute("autoSizePolicy", "content");
							writer.writeAttribute("fontSize", "12");
							writer.writeAttribute("fontStyle", "plain");
							writer.writeAttribute("hasText", "true");
							writer.writeAttribute("visible", "true");
							writer.writeAttribute("width", "4.0");
							subprefix = curst.getObject().asResource().getModel()
									.getNsURIPrefix(curst.getObject().asResource().getNameSpace());
							if (subprefix != null) {
								writer.writeCharacters(subprefix + ":" + curst.getObject().asResource().getLocalName());
							} else {
								writer.writeCharacters(curst.getObject().asResource().getLocalName());
							}
							writer.writeEndElement();
							writer.writeEndElement();
							writer.writeEndElement();
							writer.writeEndElement();
							uriToNodeId.add(curst.getObject().asResource().getURI());
						}
						writer.writeStartElement("edge");
						writer.writeAttribute("id", "e" + edgecounter++);
						writer.writeAttribute("uri", curst.getPredicate().getURI());
						writer.writeAttribute("source", curst.getSubject().getURI());
						writer.writeAttribute("target", curst.getObject().asResource().getURI());
						writer.writeEndElement();
					} else if (curst.getObject().isLiteral()) {
						writer.writeStartElement("node");
						writer.writeAttribute("id", "literal" + literalcounter);
						if (!curst.getObject().asLiteral().getValue().toString().isEmpty()) {
							writer.writeStartElement("data");
							writer.writeAttribute("key", "nodekey");
							writer.writeStartElement("y:ShapeNode");
							writer.writeStartElement("y:Shape");
							writer.writeAttribute("shape", "ellipse");
							writer.writeEndElement();
							writer.writeStartElement("y:Fill");
							if (curst.getPredicate().getURI() != null && curst.getPredicate().getURI()
									.startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
								writer.writeAttribute("color", "#F08080");
							} else {
								writer.writeAttribute("color", "#008000");
							}
							writer.writeAttribute("transparent", "false");
							writer.writeEndElement();
							writer.writeStartElement("y:NodeLabel");
							writer.writeAttribute("alignment", "center");
							writer.writeAttribute("autoSizePolicy", "content");
							writer.writeAttribute("fontSize", "12");
							writer.writeAttribute("fontStyle", "plain");
							writer.writeAttribute("hasText", "true");
							writer.writeAttribute("visible", "true");
							writer.writeAttribute("width", "4.0");
							writer.writeCharacters(curst.getObject().asLiteral().getLexicalForm() + " (xsd:"
									+ curst.getObject().asLiteral().getDatatype().getURI().substring(
											curst.getObject().asLiteral().getDatatype().getURI().lastIndexOf('#') + 1)
									+ ")");
							writer.writeEndElement();
							writer.writeEndElement();
							writer.writeEndElement();
							valcounter++;
						}
						writer.writeStartElement("data");
						writer.writeAttribute("key", "type" + typecounter);
						typecounter++;
						writer.writeCharacters(curst.getObject().asLiteral().getDatatypeURI());
						writer.writeEndElement();
						if (curst.getObject().asLiteral().getLanguage() != null) {
							writer.writeStartElement("data");
							writer.writeAttribute("key", "lang" + langcounter);
							langcounter++;
							writer.writeCharacters(curst.getObject().asLiteral().getLanguage());
							writer.writeEndElement();
						}
						writer.writeEndElement();
						writer.writeStartElement("edge");
						writer.writeAttribute("id", "e" + edgecounter++);
						writer.writeAttribute("uri", curst.getPredicate().getURI());
						writer.writeAttribute("source", curst.getSubject().getURI());
						writer.writeAttribute("target", "literal" + literalcounter);
						writer.writeStartElement("data");
						writer.writeAttribute("key", "edgekey");
						literalcounter++;
						writer.writeStartElement("y:PolyLineEdge");
						writer.writeStartElement("y:EdgeLabel");
						writer.writeAttribute("alignment", "center");
						writer.writeAttribute("configuration", "AutoFlippingLabel");
						writer.writeAttribute("fontSize", "12");
						writer.writeAttribute("fontStyle", "plain");
						writer.writeAttribute("hasText", "true");
						writer.writeAttribute("visible", "true");
						writer.writeAttribute("width", "4.0");
						subprefix = curst.getPredicate().asResource().getModel()
								.getNsURIPrefix(curst.getPredicate().asResource().getNameSpace());
						if (subprefix != null) {
							writer.writeCharacters(subprefix + ":" + curst.getPredicate().getLocalName());
						} else {
							writer.writeCharacters(curst.getPredicate().getLocalName());
						}
						writer.writeEndElement();
						writer.writeEndElement();
						writer.writeEndElement();
						writer.writeEndElement();
						literalcounter++;
					}
				}
			}
			writer.writeEndElement();
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
