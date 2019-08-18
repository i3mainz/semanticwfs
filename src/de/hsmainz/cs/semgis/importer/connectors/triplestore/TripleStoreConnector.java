package de.hsmainz.cs.semgis.importer.connectors.triplestore;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public abstract class TripleStoreConnector {

	protected static final String prefixCollection=	"PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#> "+		
			"PREFIX skos:  <http://www.w3.org/2004/02/skos/core#> "+
			"PREFIX lemon: <http://www.lemon-model.net/lemon#> "+
			"PREFIX dbo:<http://dbpedia.org/ontology/> "+
			"PREFIX geom: <http://geovocab.org/geometry#> "+
			"PREFIX ogc: <http://www.opengis.net/ont/geosparql#> "+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+
			"PREFIX bn-lemon: <http://babelnet.org/model/babelnet#> "+
		 	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
		 	"PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> "+
			"PREFIX dbpedia: <http://dbpedia.org/resource/> "+
			"Prefix lgdr:<http://linkedgeodata.org/triplify/> "+
			"Prefix lgdo:<http://linkedgeodata.org/ontology/> "+
		 	"PREFIX owl:<http://www.w3.org/2002/07/owl#> "+
			"PREFIX omgeo:<http://www.ontotext.com/owlim/geo#> "+
		 	"PREFIX ff:<http://factforge.net> "+
			"PREFIX om:<http://www.ontotext.com/owlim> "+
		 	"PREFIX wd:<http://www.wikidata.org/entity/> "+
		 	"PREFIX bd: <http://www.bigdata.com/rdf#> "+
		 	"PREFIX wdt: <http://www.wikidata.org/prop/direct/> "+
			"PREFIX wikibase: <http://wikiba.se/ontology#> "+
		 	"PREFIX geo-pos:<http://w3.org/2003/01/geo/wgs84_pos#> ";
			

	protected String queryurl;	
	
	protected static TripleStoreConnector instance;

	protected String queryurl2; 
	
	public Set<String> prefix;
	
	protected Boolean babelnetCompatible;
	
	protected Boolean matchLabels;
	
	protected Double defaultRadius;
		
	public Boolean getMatchLabels() {
		return matchLabels;
	}

	protected Boolean geospatialComaptible;
	
	public Boolean getBabelnetCompatible() {
		return babelnetCompatible;
	}

	public Set<String> matchConceptsByLabel(String resourceString){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
		classOfResourceQuery.append( "?class rdf:type owl:Class . "
				+ "?class rdfs:label ?label ."
				+ "FILTER(regex(STR(?label),\"^"+resourceString+"$\"))\n}");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("Class? "+solu.get("?class"));
			result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}
	
	public Set<String> getConceptsGeoGraphicallyNearTo(Geometry geometry,CoordinateReferenceSystem res) {
		try {
			geometry=convertToWGS84(geometry, res);
		} catch (MismatchedDimensionException | FactoryException
				| TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(geometry instanceof Point){
			return this.getConceptsGeoGraphicallyNearTo(((Point)geometry).getX(), ((Point)geometry).getY());
		}
		return this.getConceptsGeoGraphicallyNearTo(geometry.getCentroid().getX(), geometry.getCentroid().getY());
	}
	
	public static Geometry convertToWGS84(Geometry geom,CoordinateReferenceSystem geomcrs) throws MismatchedDimensionException, TransformException, FactoryException{
		CoordinateReferenceSystem wgs84 = CRS.forCode("EPSG:4326");
		//CoordinateReferenceSystem geomcrs = CRS.decode("EPSG:"+geom.getGeometryType());
		return JTS.transform( geom, wgs84);
	}
	
	public static Envelope convertToWGS84(Envelope geom,CoordinateReferenceSystem geomcrs) throws FactoryException, NoSuchAuthorityCodeException, MismatchedDimensionException, TransformException{
		CoordinateReferenceSystem wgs84 = CRS.forCode("EPSG:4326");
		//CoordinateReferenceSystem geomcrs = geom.getCoordinateReferenceSystem();
		
		MathTransform transform = (MathTransform) CRS.findOperation(geomcrs, wgs84,null);
		return JTS.transform((com.vividsolutions.jts.geom.Envelope) geom, transform);
	}
	
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Geometry geometry,CoordinateReferenceSystem res,Double bufferSize,String indid,Boolean dummy) {

		try {
			if(res!=null && !res.equals(CRS.forCode("EPSG:4326"))){
				geometry=convertToWGS84(geometry,res);
			}
		} catch (MismatchedDimensionException | FactoryException
				| TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(geometry instanceof Point){
			return this.getConceptsGeoGraphicallyNearTo(((Point)geometry).getX(), ((Point)geometry).getY(),bufferSize,indid,dummy);
		}

		return this.getConceptsGeoGraphicallyNearTo(		geometry.getCoordinate().x, 		geometry.getCoordinate().y,bufferSize,indid,dummy);
	}
	
	public Set<String> getConceptsGeoGraphicallyNearTo(Geometry geometry,CoordinateReferenceSystem res,Double bufferSize) {

		try {
			if(!res.equals(CRS.forCode("EPSG:4326"))){
				geometry=convertToWGS84(geometry,res);
			}
		} catch (MismatchedDimensionException | FactoryException
				| TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(geometry instanceof Point){
			return this.getConceptsGeoGraphicallyNearTo(((Point)geometry).getX(), ((Point)geometry).getY(),bufferSize);
		}

		return this.getConceptsGeoGraphicallyNearTo(		geometry.getCoordinate().x, 		geometry.getCoordinate().y,bufferSize);
	}
	
	public Set<String> getConceptsGeoGraphicallyNearTo(GridCoverage2D geometry,CoordinateReferenceSystem res) {
		try {
			Geometry geom=convertToWGS84((Geometry) geometry.getEnvelope(),res);
			return this.getConceptsGeoGraphicallyNearTo(geom.getCentroid().getY(), geom.getCentroid().getY());

		} catch (MismatchedDimensionException | FactoryException
				| TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.getConceptsGeoGraphicallyNearTo(geometry.getEnvelope2D().getCenterX(), geometry.getEnvelope2D().getCenterY());
	}
	
	public Set<String> getConceptsGeoGraphicallyNearTo(GridCoverage2D geometry,CoordinateReferenceSystem res,Double bufferSize) {
		try {
			Geometry geom=convertToWGS84((Geometry) geometry.getEnvelope(),res);
			return this.getConceptsGeoGraphicallyNearTo(geom.getCentroid().getY(), geom.getCentroid().getY());

		} catch (MismatchedDimensionException | FactoryException
				| TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.getConceptsGeoGraphicallyNearTo(geometry.getEnvelope2D().getCenterX(), geometry.getEnvelope2D().getCenterY(),bufferSize);
	}
	
	public Set<String> matchPropertiesByLabel(String resourceString){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
		classOfResourceQuery.append( "?class rdf:type owl:Property . "
				+ "?class rdfs:label ?label ."
				+ "FILTER(regex(STR(?label),\"^"+resourceString+"$\"))\n}");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("Class? "+solu.get("?class"));
			result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}
	
	public Set<String> matchConceptsByLabel(String loc,String word,Boolean restrictDBPedia){
		return this.matchConceptsByLabel(word);
	}
	
	public Set<String> matchConceptsByLabel(Language loc,List<PropertyDescriptor> word,Boolean restrictDBPedia){
		return this.matchConceptsByLabel(word.iterator().next().toString());
	}
		
	public Set<String> getSuperConcepts(String originConcept){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?superclass WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("<"+originConcept+"> rdfs:subClassOf ?superclass . }");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("SuperClass? "+solu.get("?superclass"));
			result.add(solu.get("?superclass").toString());
		}
		qexec.close();
		return result;
	}
	
	public Set<String> getSuperConceptsHierarchy(String originConcept){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?superclass WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("<"+originConcept+"> rdfs:subClassOf* ?superclass . }");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("SuperClass? "+solu.get("?superclass"));
			result.add(solu.get("?superclass").toString());
		}
		qexec.close();
		return result;
	}
	
	
	public Set<String> getClassOfResource(String resourceString){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("<"+resourceString+">");
		classOfResourceQuery.append(" rdf:type ");
		classOfResourceQuery.append("?class . }");
		//System.out.println(classOfResourceQuery);
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}
	
	public Boolean isSubClassOf(String classname,String classname2) {
		//Set<String> result=new TreeSet<String>();
		//for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("ASK { "+System.lineSeparator());
			classOfResourceQuery.append("<"+classname+">");
			classOfResourceQuery.append(" rdfs:subClassOf* ");
			classOfResourceQuery.append("<"+classname2+">.   SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" . } }");
			//System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl2, query);
			Boolean results = qexec.execAsk();
			qexec.close();
			return results;
		//}
		//return result;
	}
	
	public Set<String> getIndividualsOfClass(String loc,String resourceString){
		StringBuilder individualOfClassQuery=new StringBuilder();
		individualOfClassQuery.append(prefixCollection);
		individualOfClassQuery.append(" SELECT DISTINCT ?individual WHERE { "+System.lineSeparator());
		individualOfClassQuery.append("?individual");
		individualOfClassQuery.append(" rdf:type ");
		individualOfClassQuery.append(resourceString);
		individualOfClassQuery.append(" . }");
		//System.out.println(individualOfClassQuery.toString());
		Query query = QueryFactory.create(individualOfClassQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			result.add(solu.get("?individual").toString());
		}
		qexec.close();
		return result;
	}
	
	public Set<String> getExactResource(String resourceString){
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+URLEncoder.encode(resourceString)+">");
			classOfResourceQuery.append(" a ");
			classOfResourceQuery.append("?class . }");
			//System.out.println(classOfResourceQuery);
			
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			try{
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				result.add(pref+resourceString);
			}
			qexec.close();
			}catch(Exception e){
				System.out.println(classOfResourceQuery);
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public Set<String> getExactResourceOfType(String resourceString,String classs){
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("ASK { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+resourceString+">");
			classOfResourceQuery.append(" a ");
			classOfResourceQuery.append("<"+classs+"> .}");
			//System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			Boolean resu = qexec.execAsk();
			if(resu)
				result.add(pref+resourceString);
			qexec.close();
		}
		return result;
	}
	
	public Set<String> getClassesWithGeometries(){
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT ?class WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("?ind rdf:type ?class .");
			classOfResourceQuery.append("?ind geo:hasGeometry ?geom . ");
			classOfResourceQuery.append("}");
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				result.add(solu.get("?class").toString());
			}
			qexec.close();
		}
		return result;
	}
	
	public Map<String,Integer> getGeometryTypesForClass(String classs){
		Map<String,Integer> result=new TreeMap<String,Integer>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT ?class WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("?ind rdf:type <"+classs+"> .");
			classOfResourceQuery.append("?ind geo:hasGeometry ?geom . ");
			classOfResourceQuery.append("?geom geo:asWKT ?geomwkt . ");
			classOfResourceQuery.append("}");
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				WKTReader wktreader=new WKTReader();
				if(solu.get("?geomwkt")!=null){
					try{
						Geometry geom=(Geometry)wktreader.read(solu.get("?geomwkt").toString());
						String geomStringtoAdd=geom.getGeometryType();
						if(geom.isRectangle()){
							geomStringtoAdd+="Rectangle";
						}
						if(geom.isSimple()){
							geomStringtoAdd+="Simple";
						}
						if(geom.isValid()){
							geomStringtoAdd+="Valid";
						}
						if(geom instanceof LineString){
							if(((LineString)geom).isClosed()){
								geomStringtoAdd+="Closed";
							}
						}
						if(!result.containsKey(geomStringtoAdd)){
							result.put(geomStringtoAdd, 0);
						}
						result.put(geomStringtoAdd,result.get(geomStringtoAdd)+1);
					}catch(Exception e){
						
					}
				}

			}
			qexec.close();
		}
		return result;
	}
	
	public Set<ClassResLabResult> getExactResourceOfType(String resourceString,String classs,String language){
		Set<ClassResLabResult> result=new TreeSet<ClassResLabResult>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("ASK { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+resourceString+">");
			classOfResourceQuery.append(" a ");
			classOfResourceQuery.append("<"+classs+"> .}");
			//System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			Boolean resu = qexec.execAsk();
			if(resu)
				result.add(new ClassResLabResult(classs,"",pref+resourceString,"",""));
			qexec.close();
		}
		return result;
	}
	
	public Set<String> getExactConcept(String resourceString){
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+UriEncoder.encode(resourceString)+">");
			classOfResourceQuery.append(" a ");
			classOfResourceQuery.append("?class . }");
			System.out.println(classOfResourceQuery);
			try{
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl2, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				result.add(resourceString);
			}
			qexec.close();
			}catch(QueryParseException e){
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public Set<String> getExactConcept(String loc,String word,Boolean restrictDBPedia){
		return this.getExactConcept(loc);
	}
	
	public Set<String> getExactConcept(Language loc,String word,Boolean restrictDBPedia,
			Map<String,Map<String,Set<Integer>>> categories,Map<Integer,Map<String,Set<String>>> contoCat,Integer colid){
		return this.getExactConcept(loc.toString(), word, restrictDBPedia);
	}
	
	public Set<String> getRelatedConcept(String loc,String word,Boolean restrictDBPedia){
		return null;
	}
	
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon){
		return this.getConceptsGeoGraphicallyNearTo(lat, lon, this.defaultRadius);
	}
	
	public abstract Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon,Double radius);

	public abstract Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Double lat, Double lon,Double radius,String indid,Boolean dummy);

	public abstract Set<ClassResLabResult> localityQuery(String resourceString, GoogleMapping mapping, String language);

	public Set<String> getLabelForClass(String classname) {
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT DISTINCT ?labelLabel WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+classname+">");
			classOfResourceQuery.append(" rdfs:label ");
			classOfResourceQuery.append("?label. }");
			System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl2, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				result.add(solu.get("?label").toString());
			}
			qexec.close();
		}
		return result;
	}

	/*private Integer graphRecursion(String concept,Set<String> super1,Integer depth){
		Set<String> cursupcon=this.getSuperConcepts(concept);
		for(String con:cursupcon){
			if(super1.contains(con)){
				return depth;
			}
		}
		for(String con:cursupcon){
			Integer result=graphRecursion(con, super1, depth+1);
		}

	}*/
	
	
	public Boolean shareCommonSuperConcepts(String uri, String uri2) {
		Set<String> super1=this.getSuperConceptsHierarchy(uri);
		Set<String> super2=this.getSuperConceptsHierarchy(uri2);
		super1.retainAll(super2);
		return super1.isEmpty();
		/*Boolean found=false;
		Integer distance=1;
		Set<String> cursupcon=this.getSuperConcepts(uri);
		while(!found){

			for(String con:cursupcon){
				if(super1.contains(con)){
					found=true;
					break;
				}
			}
			distance++;
			for(String con:cursupson){
				
			}
			cursupson=this.getSuperConcepts(con);
		}*/
	}

	public Map<String, StatParameter> getGeometryStatsForClass(String classs) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Geometry geom) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<ClassResLabResult> getClassForGeometryId(Long geometryid, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Double> getCommonAttributesOfClass(String classs) {
		// TODO Auto-generated method stub
		
	}




}
