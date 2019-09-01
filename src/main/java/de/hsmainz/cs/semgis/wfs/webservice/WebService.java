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



@Path("/service")
public class WebService {

	JSONObject triplestoreconf=new JSONObject();
	
	JSONObject wfsconf=new JSONObject();
	
	public WebService() throws IOException {
		String text2 = new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8);
	    wfsconf = new JSONObject(text2);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/hello")
    public String helloWorld() {
		return "HelloWorld";
	}
	
	public String SERVICETYPEVERSION="2.0.0";
	
	public String SERVERURL="2.0.0";
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/getCapabilities")
	public String getCapabilities() throws XMLStreamException {
		String serviceType="WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter xmlwriter=factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer=new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("WFS_Capabilities");
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs/2.0");
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes/2.0");		
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink","http://www.w3.org/1999/xlink");
		 		//ServiceInformation
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "ServiceIdentification");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "ServiceType");
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
		writer.writeStartElement("Capability");
		writer.writeStartElement("Request");
		writer.writeStartElement("GetCapabilities");
		//OperationsMetadata
		writer.writeStartElement("http://www.opengis.net/ows/1.1","OperationsMetadata");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Operation");
		writer.writeAttribute("name", "GetCapabilities");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DCP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","HTTP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Get");
		//writer.writeAttribute("http://www.w3.org/1999/xlink", "href", SERVERURL);
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
		writer.writeEndElement();
		writer.writeStartElement("FeatureTypeList");
		for(int i=0;i<this.wfsconf.getJSONArray("datasets").length();i++) {
			describeFeatureType(writer, this.wfsconf.getJSONArray("datasets").getJSONObject(i));
		}
		writer.writeEndElement();
		describeSpatialCapabilities(writer);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return strwriter.toString();	
	}
	
	public void describeFeatureType(XMLStreamWriter writer,JSONObject featuretype) throws XMLStreamException {
		writer.writeStartElement("FeatureType");
		writer.writeStartElement("Name");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("Title");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("DefaultCRS");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("sequence");
		Map<String,String> mapping=TripleStoreConnector.getFeatureTypeInformation(featuretype.getString("query"), featuretype.getString("triplestore"), featuretype.getString("name"));
		for(String elem:mapping.keySet()) {
			writer.writeStartElement("element");
			writer.writeAttribute("name", elem);
			writer.writeAttribute("type", mapping.get(elem));
			writer.writeEndElement();
		}
		writer.writeEndElement();
		//writer.writeStartElement(localName);
		//writer.writeAttribute(localName, value);
	}
	
	public void describeSpatialCapabilities(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("http://www.opengis.net/fes/2.0","SpatialCapabilities");
		writer.writeStartElement("http://www.opengis.net/fes/2.0","GeometryOperands");
		writer.writeAttribute("gml", "http://www.opengis.net/gml");
		writer.writeAttribute("gml32", "http://www.opengis.net/gml");
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
		writer.writeEndElement();
	}
	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/describeFeatureType")
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
	@Path("/getFeature")
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
	@Path("/getGmlObject")
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
