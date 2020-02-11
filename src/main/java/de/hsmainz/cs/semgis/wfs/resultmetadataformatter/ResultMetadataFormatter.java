package de.hsmainz.cs.semgis.wfs.resultmetadataformatter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

public abstract class ResultMetadataFormatter {

	public static Map<String,ResultMetadataFormatter> resultMap=new TreeMap<String, ResultMetadataFormatter>();
	
	public Integer lastQueriedElemCount=0;
	
	public String mimeType="text/plain";
	
	public String exposedType="application/vnd.geo+json";
	
	public static ResultMetadataFormatter getFormatter(String formatString) {
		formatString=formatString.toLowerCase();
		if(resultMap.containsKey(formatString)) {
			return resultMap.get(formatString);
		}
		formatString=formatString.replace("+","");
		if(formatString.contains("mapcss")) {
			return resultMap.get("mapcss");
		}
		if(formatString.contains("geojsoncss")) {
			return resultMap.get("geojsoncss");
		}
		if(formatString.contains("sld")) {
			return resultMap.get("sld");
		}
		return null;
	}
	
	static { 
		resultMap.put("gmd", new GMDFormatter());
	}
	
	/*public convertCoordinates() {
		ProjCoordinate coord = new ProjCoordinate(5439627.33, 5661628.09);
        System.out.println(coord);

        ProjCoordinate target = new ProjCoordinate();
        CRSFactory crsFactory = new CRSFactory();
        CoordinateTransformFactory f = new CoordinateTransformFactory();
        CoordinateTransform t;
	}*/
	
	public abstract String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,String typeColumn,
			Boolean onlyproperty,Boolean onlyhits,String srsName,String indvar,String epsg,List<String> eligiblenamespaces,List<String> noteligiblenamespaces) throws XMLStreamException;

	public String formatHeader() {
		return "";
	}

	public String formatFooter() {
		return "";
	}
	
}
