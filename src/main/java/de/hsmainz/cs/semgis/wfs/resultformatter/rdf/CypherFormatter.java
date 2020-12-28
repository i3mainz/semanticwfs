package de.hsmainz.cs.semgis.wfs.resultformatter.rdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

public class CypherFormatter extends ResultFormatter {

	public CypherFormatter() {
		this.urlformat="cypher";
		this.label="Cypher Neo4J (Cypher)";
		this.mimeType="text/cypher";
		this.exposedType="text/cypher";
		this.fileextension="cypher";
		this.definition="https://neo4j.com/developer/cypher/";
		this.constructQuery=false;
	}
	
	Map<String,String> namespaceToPrefix=new TreeMap<String,String>();
	
	Integer namespacecounter=0;

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage, Writer out)
			throws XMLStreamException, IOException {
		namespacecounter=0;
		namespaceToPrefix.clear();
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
        
        StringBuilder literalresult=new StringBuilder();
        StringBuilder resourceresult=new StringBuilder();
        resourceresult.append("CREATE ");
        for(Resource res:resources) {
        	if(!res.isURIResource() || res.getLocalName()==null || res.getLocalName().isEmpty())
        		continue;
			String resprefix=res.getModel().getNsURIPrefix(res.getNameSpace());
			if(resprefix==null) {
				if(namespaceToPrefix.containsKey(res.getNameSpace())) {
					resprefix=namespaceToPrefix.get(res.getNameSpace());
				}else {
					resprefix="ns"+namespacecounter++;
					namespaceToPrefix.put(res.getNameSpace(),resprefix);
				}
			}else {
				resprefix=resprefix.replace("-", "_");
			}
        	literalresult.append("CREATE (").append(resprefix+"_"+res.getLocalName().replace(".","_").replace("-", "_")).append(" { ");
        	if(res.getURI()!=null && !res.getURI().isEmpty()) {
        		literalresult.append("_id:'"+resprefix+"_"+res.getLocalName().replace(".","_").replace("-", "_")+"', ");
        		literalresult.append("_uri:'"+res.getURI()+"', ");
        	}
        	StmtIterator propiter = res.listProperties();
        	while(propiter.hasNext()) {
        		Statement curst=propiter.next();
    			String subprefix=curst.getSubject().asResource().getModel().getNsURIPrefix(curst.getSubject().asResource().getNameSpace());
    			if(subprefix==null) {
    				if(namespaceToPrefix.containsKey(curst.getSubject().getNameSpace())) {
    					subprefix=namespaceToPrefix.get(curst.getSubject().getNameSpace());
    				}else {
    					subprefix="ns"+namespacecounter++;
    					namespaceToPrefix.put(curst.getSubject().getNameSpace(),subprefix);
    				}
    			}else {
    				subprefix=subprefix.replace("-", "_");
    			}
    			String predprefix=curst.getPredicate().asResource().getModel().getNsURIPrefix(curst.getPredicate().asResource().getNameSpace());
    			if(predprefix==null) {
    				if(namespaceToPrefix.containsKey(curst.getPredicate().getNameSpace())) {
    					predprefix=namespaceToPrefix.get(curst.getPredicate().getNameSpace());
    				}else {
    					predprefix="ns"+namespacecounter++;
    					namespaceToPrefix.put(curst.getPredicate().getNameSpace(),predprefix);
    				}
    			}else {
    				predprefix=predprefix.replace("-", "_");
    			}
    			if(curst.getObject().isURIResource() && curst.getObject().asResource().getLocalName()!=null && !curst.getObject().asResource().getLocalName().isEmpty()) {
        			String objprefix=curst.getObject().asResource().getModel().getNsURIPrefix(curst.getObject().asResource().getNameSpace());
        			if(objprefix==null) {
        				if(namespaceToPrefix.containsKey(curst.getObject().asResource().getNameSpace())) {
        					objprefix=namespaceToPrefix.get(curst.getObject().asResource().getNameSpace());
        				}else {
        					objprefix="ns"+namespacecounter++;
        					namespaceToPrefix.put(curst.getObject().asResource().getNameSpace(),objprefix);
        				}
        			}else {
        				objprefix=objprefix.replace("-", "_");
        			}
                	resourceresult.append("("+subprefix+"_"+curst.getSubject().getLocalName().replace(".","_").replace("-", "_")+")-[:"+predprefix+"_"+curst.getPredicate().getLocalName().replace(".","_").replace("-", "_")+"]->("+objprefix+"_"+curst.getObject().asResource().getLocalName().replace(".","_").replace("-", "_")+"),\n");	
        		}else if(curst.getObject().isLiteral()){
                	literalresult.append(predprefix+"_"+curst.getPredicate().getLocalName().replace(".","_").replace("-", "_")+":'"+curst.getObject().asLiteral().getValue().toString()+"', ");        			
        		}
        	}
        	if(!literalresult.toString().endsWith("{"))
        		literalresult.delete(literalresult.length()-2, literalresult.length());
        	literalresult.append(" })\n");
        }
    	if(!resourceresult.toString().isEmpty())
    		resourceresult.delete(resourceresult.length()-2, resourceresult.length());
    	out.write(literalresult.toString()+System.lineSeparator()+resourceresult.toString());
		out.close();
		return "";
	}

}
