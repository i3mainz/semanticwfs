package de.hsmainz.cs.semgis.importer.connectors.webservice;
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

import de.hsmainz.cs.semgis.importer.connectors.triplestore.TripleStoreConnector;

public class Webservice {

	JSONObject triplestoreconf=new JSONObject();
	
	JSONObject wfsconf=new JSONObject();
	
	public Webservice() throws IOException {
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
		writer.writeStartElement("Parameter");
		writer.writeAttribute("name", "AcceptVersions");
		writer.writeStartElement("AllowedValues");
		writer.writeStartElement("Value");
		writer.writeCharacters("2.0.0");
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
		return null;	
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
		writer.writeStartElement(localName);
		writer.writeAttribute(localName, value);
	}
	
	public void describeSpatialCapabilities(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("SpatialCapabilities");
		writer.writeStartElement("GeometryOperands");
		writer.writeAttribute("gml", "http://www.opengis.net/gml");
		writer.writeAttribute("gml32", "http://www.opengis.net/gml");
		writer.writeStartElement("GeometryOperand");
		writer.writeAttribute("name","gml:Box");
		writer.writeEndElement();
		writer.writeStartElement("GeometryOperand");
		writer.writeAttribute("name", "gml:Envelope");
		writer.writeEndElement();
		writer.writeStartElement("GeometryOperand");
		writer.writeAttribute("name", "gml:Point");
		writer.writeEndElement();
		writer.writeStartElement("GeometryOperand");
		writer.writeAttribute("name", "gml:LineString");
		writer.writeEndElement();
		writer.writeStartElement("GeometryOperand");
		writer.writeAttribute("name", "gml:Curve");
		writer.writeEndElement();
		writer.writeStartElement("GeometryOperand");
		writer.writeAttribute("name", "gml:Polygon");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("SpatialOperators");
		writer.writeStartElement("SpatialOperator");
		writer.writeAttribute("name","BBOX");
		writer.writeEndElement();
		writer.writeStartElement("SpatialOperator");
		writer.writeAttribute("name","Intersects");
		writer.writeEndElement();		
		writer.writeStartElement("SpatialOperator");
		writer.writeAttribute("name","Contains");
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	
	@Path("describeFeatureType")
	public String describeFeatureType(String typename) throws XMLStreamException {
		String serviceType="WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter=new StringWriter();
		XMLStreamWriter writer=factory.createXMLStreamWriter(strwriter);
		writer.writeStartDocument();
		
		return null;	
	}
	
	@Path("getFeature")
	public String getFeature(String typename,String output,String count) {
		JSONObject workingobj=null;
		for(int i=0;i<this.wfsconf.getJSONArray("datasets").length();i++) {
			JSONObject curobj=this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if(curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj=curobj;
				break;
			}
		}
		return TripleStoreConnector.executeQuery(workingobj.getString("query"), workingobj.getString("triplestore"), output);	
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
