package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class TGFFormatter extends ResultFormatter {

	public TGFFormatter() {
		this.urlformat="tgf";
		this.label="Trivial Graph Format (TGF)";
		this.mimeType="text/tgf";
		this.exposedType="text/tgf";
		this.fileextension="tgf";
		this.definition="https://docs.yworks.com/yfiles/doc/developers-guide/tgf.html";
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
        Map<String,Integer> uriToNodeId=new HashMap<String,Integer>();
        Integer nodecounter=0;
        Integer resnodeid=0;
        StringBuilder edgebuilder=new StringBuilder();
        for(Resource res:resources) {
        	if(!res.isURIResource())
				continue;
        	if(uriToNodeId.containsKey(res.getURI())) {
        		resnodeid=uriToNodeId.get(res.getURI());
        	}else {
            	resnodeid=nodecounter++;
            	uriToNodeId.put(res.getURI(),resnodeid);
        	}
        	out.write(resnodeid+" "+res.getURI()+System.lineSeparator());
        	StmtIterator propiter = res.listProperties();
        	while(propiter.hasNext()) {
        		Statement curst=propiter.next();
        		if(curst.getObject().isURIResource()) {
        			Integer objnodeid=0;
        			if(uriToNodeId.containsKey(curst.getObject().asResource().getURI())) {
        				objnodeid=uriToNodeId.get(curst.getObject().asResource().getURI());
        			}else {
        				objnodeid=nodecounter;
        				uriToNodeId.put(curst.getObject().asResource().getURI(),objnodeid);
        			}
        			out.write(objnodeid+" "+curst.getObject().asResource().getURI()+System.lineSeparator());
        			edgebuilder.append(resnodeid+" "+objnodeid+" "+curst.getPredicate().getURI()+System.lineSeparator());
        		}else if(curst.getObject().isLiteral()) {
        			out.write(nodecounter+" "+curst.getObject().asLiteral().getValue()+" ("+curst.getObject().asLiteral().getDatatypeURI()+")"+System.lineSeparator());
        			edgebuilder.append(resnodeid+" "+nodecounter+++" "+curst.getPredicate().getURI()+System.lineSeparator());
        		}
        	}
        }
        out.write("#"+System.lineSeparator());
        out.write(edgebuilder.toString());
        out.close();
        return "";
        
	}
	

}
