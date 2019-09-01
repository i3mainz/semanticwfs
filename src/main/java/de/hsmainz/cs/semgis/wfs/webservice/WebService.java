package de.hsmainz.cs.semgis.wfs.webservice;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import org.json.JSONException;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.triplestore.TripleStoreConnector;



@Path("/")
public class WebService {

	JSONObject triplestoreconf=new JSONObject();
	
	JSONObject wfsconf=new JSONObject();
	
	public WebService() throws IOException {
		String text2 = new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8);
	    wfsconf = new JSONObject(text2);
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/service")
	public String entryPoint(@QueryParam("SERVICE") String service, @QueryParam("REQUEST") String request,@QueryParam("VERSION") String version) {
		if(service.equalsIgnoreCase("WFS")) {
			if("getCapabilities".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilities();
				} catch (XMLStreamException e) {
					return "";
				}
			}
			if("describeFeatureType".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilities();
				} catch (XMLStreamException e) {
					return "";
				}
			}
			if("getFeature".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilities();
				} catch (XMLStreamException e) {
					return "";
				}
			}
		}
		return "";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/hello")
    public String helloWorld() {
		return "HelloWorld";
	}
	
	public String SERVICETYPEVERSION="2.0.0";
	
	public String SERVERURL="http://localhost:8080/RESTfulExample/rest/service?";
	
	
	public String constructCapabilities() throws XMLStreamException {
		String serviceType="WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter xmlwriter=factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer=new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("WFS_Capabilities");
		writer.writeDefaultNamespace("http://www.opengis.net/wfs/2.0");
		writer.writeAttribute("version", "2.0.0");
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs/2.0");
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes/2.0");		
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink","http://www.w3.org/1999/xlink");
		 		//ServiceInformation
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "ServiceIdentification");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "ServiceType");
		writer.writeAttribute("codeSpace", "OGC");
		writer.writeCharacters(serviceType);
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","ServiceTypeVersion");
		writer.writeCharacters(SERVICETYPEVERSION);
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Title");
		writer.writeCharacters(wfsconf.has("title")?wfsconf.getString("title"):"");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Fees");
		writer.writeCharacters("NONE");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Abstract");
		writer.writeCharacters(wfsconf.has("abstract")?wfsconf.getString("abstract"):"");
		writer.writeEndElement();
		writer.writeEndElement();
		//ServiceProvider
		//Capabilities
		//writer.writeStartElement("http://www.opengis.net/wfs/2.0","Capability");
		//writer.writeStartElement("http://www.opengis.net/wfs/2.0","Request");
		//writer.writeStartElement("http://www.opengis.net/wfs/2.0","GetCapabilities");
		//OperationsMetadata
		writer.writeStartElement("http://www.opengis.net/ows/1.1","OperationsMetadata");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Operation");
		writer.writeAttribute("name", "GetCapabilities");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DCP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","HTTP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", SERVERURL);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Parameter");
		writer.writeAttribute("name", "AcceptVersions");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","AllowedValues");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Value");
		writer.writeCharacters("2.0.0");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Operation");
		writer.writeAttribute("name", "GetFeature");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DCP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","HTTP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", SERVERURL);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Parameter");
		writer.writeAttribute("name", "AcceptVersions");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","AllowedValues");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Value");
		writer.writeCharacters("2.0.0");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0","FeatureTypeList");
		for(int i=0;i<this.wfsconf.getJSONArray("datasets").length();i++) {
			describeFeatureType(writer, this.wfsconf.getJSONArray("datasets").getJSONObject(i));
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","Filter_Capabilities");
		describeSpatialCapabilities(writer);
		writer.writeEndElement();
		writer.writeEndElement();
		//writer.writeEndElement();
		//writer.writeEndElement();
		//writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return strwriter.toString();
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/service/getCapabilities")
	public String getCapabilities() throws XMLStreamException {
		return constructCapabilities();
	}
	
	public void describeFeatureType(XMLStreamWriter writer,JSONObject featuretype) throws XMLStreamException {
		writer.writeStartElement("http://www.opengis.net/wfs/2.0","FeatureType");
		writer.writeStartElement("http://www.opengis.net/wfs/2.0","Name");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0","Title");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0","DefaultCRS");
		writer.writeCharacters("urn:ogc:def:crs:EPSG::4326");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0","OutputFormats");
		writer.writeStartElement("http://www.opengis.net/wfs/2.0","Format");
		writer.writeCharacters("application/vnd.geo+json");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "WGS84BoundingBox");
		writer.writeAttribute("dimensions", "2");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","lowerCorner");
		writer.writeCharacters("11.2299229840604 51.2165647648912");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","upperCorner");
		writer.writeCharacters("14.8566506458591 53.5637800901802");
		
		writer.writeEndElement();
		
		writer.writeEndElement();
		writer.writeEndElement();
		//writer.writeStartElement("sequence");
		/*Map<String,String> mapping=TripleStoreConnector.getFeatureTypeInformation(featuretype.getString("query"), featuretype.getString("triplestore"), featuretype.getString("name"));
		for(String elem:mapping.keySet()) {
			writer.writeStartElement("element");
			writer.writeAttribute("name", elem);
			writer.writeAttribute("type", mapping.get(elem));
			writer.writeEndElement();
		}*/
		//writer.writeEndElement();
		//writer.writeStartElement(localName);
		//writer.writeAttribute(localName, value);
	}
	
	public void describeSpatialCapabilities(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialCapabilities");
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperands");
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperand");
		writer.writeAttribute("name","gml:Box");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperand");
		writer.writeAttribute("name", "gml:Envelope");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperand");
		writer.writeAttribute("name", "gml:Point");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperand");
		writer.writeAttribute("name", "gml:LineString");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperand");
		writer.writeAttribute("name", "gml:Curve");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperand");
		writer.writeAttribute("name", "gml:Polygon");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperators");
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","BBOX");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Intersects");
		writer.writeEndElement();		
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Contains");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Crosses");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Touches");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Within");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Overlaps");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Disjoint");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","Equals");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialOperator");
		writer.writeAttribute("name","DWithin");
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/service/describeFeatureType")
	public String describeFeatureType(@QueryParam("typename") String typename) throws XMLStreamException {
		String serviceType="WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter xmlwriter=factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer=new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("schema");
		JSONObject workingobj=null;
		for(int i=0;i<this.wfsconf.getJSONArray("datasets").length();i++) {
			JSONObject curobj=this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if(curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj=curobj;
				break;
			}
		}
		if(workingobj!=null)
			this.describeFeatureType(writer, workingobj);
		writer.writeEndElement();
		writer.writeEndDocument();
		return strwriter.toString();	
	}
	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/getFeature")
	public String getFeature(@QueryParam("typename") String typename, @QueryParam("outputFormat") String output,@QueryParam("count") String count) throws JSONException, XMLStreamException {
		JSONObject workingobj=null;
		for(int i=0;i<this.wfsconf.getJSONArray("datasets").length();i++) {
			JSONObject curobj=this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if(curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj=curobj;
				break;
			}
		}
		return TripleStoreConnector.executeQuery(workingobj.getString("query"), workingobj.getString("triplestore"), output,count);	
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/getGmlObject")
	public String getGmlObject() {
		return null;	
	}
	
	public String transaction() {
		return null;	
	}
	
	public String lockFeature() {
		return null;	
	}
	
	
}
