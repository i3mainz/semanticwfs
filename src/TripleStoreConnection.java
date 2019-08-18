import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Path;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public class TripleStoreConnection {

	JSONObject triplestoreconf=new JSONObject();
	
	JSONObject wfsconf=new JSONObject();
	
	public TripleStoreConnection() throws IOException {
		String text = new String(Files.readAllBytes(Paths.get("triplestoreconf.json")), StandardCharsets.UTF_8);
	    triplestoreconf = new JSONObject(text);
		String text2 = new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8);
	    wfsconf = new JSONObject(text2);
	}
	
	public String SERVICETYPEVERSION="2.0.0";
	
	public String SERVERURL="2.0.0";
	
	@Path("getCapabilities")
	public String getCapabilities() throws XMLStreamException {
		String serviceType="WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=factory.createXMLStreamWriter(strwriter);
		writer.writeStartDocument();
		//ServiceInformation
		writer.writeStartElement("ServiceIdentification");
		writer.writeStartElement("ServiceType");
		writer.writeCharacters(serviceType);
		writer.writeEndElement();
		writer.writeStartElement("ServiceTypeVersion");
		writer.writeCharacters(SERVICETYPEVERSION);
		writer.writeEndElement();
		writer.writeStartElement("Title");
		writer.writeCharacters(wfsconf.getString("title")!=null?wfsconf.getString("title"):"");
		writer.writeEndElement();
		writer.writeStartElement("Fees");
		writer.writeCharacters("NONE");
		writer.writeEndElement();
		writer.writeStartElement("Abstract");
		writer.writeCharacters(wfsconf.getString("abstract")!=null?wfsconf.getString("abstract"):"");
		writer.writeEndElement();
		writer.writeEndElement();
		//ServiceProvider
		//Capabilities
		writer.writeStartElement("Capability");
		writer.writeStartElement("Request");
		writer.writeStartElement("GetCapabilities");
		//OperationsMetadata
		writer.writeStartElement("OperationsMetadata");
		writer.writeStartElement("Operation");
		writer.writeAttribute("name", "GetCapabilities");
		writer.writeStartElement("DCP");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("http://www.w3.org/1999/xlink", "href", SERVERURL);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		return null;	
	}
	
	@Path("describeFeatureType")
	public String describeFeatureType() {
		return null;	
	}
	
	@Path("getFeature")
	public String getFeature() {
		return null;	
	}
	
	@Path("getGmlObject")
	public String getGmlObject() {
		return null;	
	}
	
	@Path("transaction")
	public String transaction() {
		return null;	
	}
	
	@Path("lockFeature")
	public String lockFeature() {
		return null;	
	}
	
	
	
}
