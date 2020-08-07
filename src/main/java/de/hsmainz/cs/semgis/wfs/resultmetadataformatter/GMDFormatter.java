package de.hsmainz.cs.semgis.wfs.resultmetadataformatter;

import java.io.StringWriter;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * Formats metadata to the GMD XML format.
 *
 */
public class GMDFormatter extends ResultMetadataFormatter {

	/**
	 * Constructor for this class.
	 */
	public GMDFormatter() {
		this.mimeType=MediaType.APPLICATION_XML;
	}
	
	@Override
	public String formatter(String collectionid, String collectioncall,String collectionurl,JSONObject workingobj,String format) throws XMLStreamException {
		StringWriter strwriter = new StringWriter();
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
		try {
			if ("true".equals(collectioncall)) {
				writer = this.xmlwriter;
			} else {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
			}
			writer.setPrefix("gmd", "http://www.isotc211.org/2005/gmd");
			writer.setPrefix("gmx", "http://www.isotc211.org/2005/gmx");
			writer.setPrefix("gco", "http://www.isotc211.org/2005/gco");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "MD_Metadata");
			writer.writeNamespace("gmd", "http://www.isotc211.org/2005/gmd");
			writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			writer.writeNamespace("gco", "http://www.isotc211.org/2005/gco");
			writer.writeNamespace("gmx", "http://www.isotc211.org/2005/gmx");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "characterSet");
			writer.writeStartElement("MD_CharacterSetCode");
			writer.writeAttribute("codelist",
					"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_CharacterSetCode");
			writer.writeAttribute("codeListValue", "utf8");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "language");
			writer.writeStartElement("MD_LanguageCode");
			writer.writeAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#LanguageCode");
			writer.writeAttribute("codeListValue", "ger");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "topicCategory");
			writer.writeStartElement("MD_TopicCategoryCode");
			writer.writeAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#TopicCategoryCode");
			writer.writeCharacters(collectionid);
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "spatialRepresentationType");
			writer.writeStartElement("MD_SpatialRepresentationTypeCode");
			writer.writeAttribute("codeListValue", "vector");
			writer.writeAttribute("codeList",
					"http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "extent");
			writer.writeStartElement("EX_Extent");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "geographicElement");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "EX_GeographicBoundingBox");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "datasetURI");
			writer.writeStartElement("http://www.isotc211.org/2005/gco", "CharacterString");
			writer.writeCharacters(collectionurl);
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "contact");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "CI_ResponsibleParty");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "individualName");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "organisationName");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "positionName");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "contactInfo");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "CI_Contact");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "phone");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "address");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "onlineResource");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "role");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "metadataStandardName");
			writer.writeStartElement("http://www.isotc211.org/2005/gco", "CharacterString");
			writer.writeCharacters("ISO 19115:2003/19139");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "metadataStandardVersion");
			writer.writeStartElement("http://www.isotc211.org/2005/gco", "CharacterString");
			writer.writeCharacters("1.0");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "status");
			writer.writeStartElement("MD_ProgressCode");
			writer.writeAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#MD_ProgressCode");
			writer.writeAttribute("codeListValue", "onGoing");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "identificationInfo");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "MD_DataIdentification");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "abstract");
			writer.writeStartElement("http://www.isotc211.org/2005/gco", "CharacterString");
			writer.writeCharacters(workingobj.getString("description"));
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "distributionInfo");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "MD_Distribution");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "distributionFormat");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "distributor");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "transferOptions");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd","MD_DigitalTransferOptions");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd","online");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd","CI_OnlineResource");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd","linkage");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd","URL");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd","function");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd","CI_OnLineFunctionCode");
			writer.writeAttribute("codeList","CI_OnLineFunctionCode");
			writer.writeAttribute("codeListValue","information");
			writer.writeCharacters("information");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "dataQualityInfo");
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "referenceSysteminfo");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "MD_ReferenceSystem");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "referenceSystemIdentifier");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "RS_Identifier");
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "code");
			writer.writeStartElement("http://www.isotc211.org/2005/gco", "CharacterString");
			writer.writeCharacters(
					workingobj.getString("targetCRS").substring(workingobj.getString("targetCRS").indexOf(':') + 1));
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "codeSpace");
			writer.writeStartElement("http://www.isotc211.org/2005/gco", "CharacterString");
			writer.writeCharacters("urn:ogc:def:crs:EPSG");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("http://www.isotc211.org/2005/gmd", "version");
			writer.writeStartElement("http://www.isotc211.org/2005/gco", "CharacterString");
			writer.writeCharacters("6.11.2");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			if (!"true".equals(collectioncall)) {
				writer.writeEndDocument();
			}
			writer.flush();
			return strwriter.toString();
		} catch (XMLStreamException e) {
			return null;
		}
	}

}
