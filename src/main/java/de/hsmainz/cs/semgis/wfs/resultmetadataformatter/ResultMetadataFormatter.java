package de.hsmainz.cs.semgis.wfs.resultmetadataformatter;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONObject;

public abstract class ResultMetadataFormatter {

	public static Map<String,ResultMetadataFormatter> resultMap=new TreeMap<String, ResultMetadataFormatter>();
	
	public Integer lastQueriedElemCount=0;
	
	public String mimeType="text/plain";
	
	public String exposedType="application/vnd.geo+json";
	
	public XMLStreamWriter xmlwriter;
	
	public static ResultMetadataFormatter getFormatter(String formatString) {
		formatString=formatString.toLowerCase();
		if(resultMap.containsKey(formatString)) {
			return resultMap.get(formatString);
		}
		formatString=formatString.replace("+","");
		if(formatString.contains("dcat")) {
			return resultMap.get("geodcat");
		}
		return resultMap.get("gmd");
	}
	
	static { 
		resultMap.put("gmd", new GMDFormatter());
		resultMap.put("geodcat", new DCATFormatter());
	}
	
	/*public convertCoordinates() {
		ProjCoordinate coord = new ProjCoordinate(5439627.33, 5661628.09);
        System.out.println(coord);

        ProjCoordinate target = new ProjCoordinate();
        CRSFactory crsFactory = new CRSFactory();
        CoordinateTransformFactory f = new CoordinateTransformFactory();
        CoordinateTransform t;
	}*/
	
	public abstract String formatter(String collectionid, String collectioncall,String collectionurl,JSONObject workingobj,String format) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
