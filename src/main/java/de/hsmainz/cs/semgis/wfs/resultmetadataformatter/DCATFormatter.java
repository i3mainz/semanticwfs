package de.hsmainz.cs.semgis.wfs.resultmetadataformatter;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.util.XSLTTransformer;

public class DCATFormatter extends ResultMetadataFormatter  {

	GMDFormatter formatter=new GMDFormatter();
	
	@Override
	public String formatter(String collectionid, String collectioncall, String collectionurl, JSONObject workingobj)
			throws XMLStreamException {
		String gmd=formatter.formatter(collectionid, collectioncall, collectionurl, workingobj);
		try {
			return XSLTTransformer.gmdToGeoDCAT(gmd);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

}
