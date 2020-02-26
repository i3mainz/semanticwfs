package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

public class StyleObject {

	public String pointStyle;
	
	public String pointImage;
	
	public String lineStringStyle;

	public String lineStringImage;
	
	public String polygonStyle;

	public String polygonImage;

	public String hatch;

	public String lineStringImageStyle;
	
	public String styleName;

	@Override
	public String toString() {
		return "StyleObject [pointStyle=" + pointStyle + ", pointImage=" + pointImage + ", lineStringStyle="
				+ lineStringStyle + ", lineStringImage=" + lineStringImage + ", polygonStyle=" + polygonStyle
				+ ", polygonImage=" + polygonImage + ", hatch=" + hatch + "]";
	}
	
	public String toJSON() {
		JSONObject result=new JSONObject();
		result.put("pointStyle",pointStyle.replace("\"","").replace("\\",""));
		result.put("pointImage",pointImage.replace("\"","").replace("\\",""));
		result.put("lineStringStyle",lineStringStyle.replace("\"","").replace("\\",""));
		result.put("lineStringImage",lineStringImage.replace("\"","").replace("\\",""));
		result.put("lineStringImageStyle",lineStringImageStyle.replace("\"","").replace("\\",""));
		result.put("polygonStyle",polygonStyle.replace("\"","").replace("\\",""));
		result.put("polygonImage",polygonImage.replace("\"","").replace("\\",""));
		result.put("hatch",hatch.replace("\"","").replace("\\",""));
		result.put("styleName",styleName);
		return result.toString(2);
	}
	
	public String toXML() {
		StringWriter strwriter = new StringWriter();
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.writeStartElement("style");
				writer.writeStartElement(styleName);
				writer.writeStartElement("pointStyle");
				writer.writeCharacters(pointStyle);
				writer.writeEndElement();
				writer.writeStartElement("pointImage");
				writer.writeCharacters(pointImage);
				writer.writeEndElement();
				writer.writeStartElement("lineStringStyle");
				writer.writeCharacters(lineStringStyle);
				writer.writeEndElement();
				writer.writeStartElement("lineStringImage");
				writer.writeCharacters(lineStringImage);
				writer.writeEndElement();
				writer.writeStartElement("lineStringImageStyle");
				writer.writeCharacters(lineStringImageStyle);
				writer.writeEndElement();
				writer.writeStartElement("polygonStyle");
				writer.writeCharacters(polygonStyle);
				writer.writeEndElement();
				writer.writeStartElement("polygonImage");
				writer.writeCharacters(polygonImage);
				writer.writeEndElement();
				writer.writeStartElement("hatch");
				writer.writeCharacters(hatch);
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return strwriter.toString();
	}
	
	
	
	
}
