package de.hsmainz.cs.semgis.wfs.resultformatter;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.mibcxb.topojson.McTopoJSON;

public class TopoJSONFormatter extends WFSResultFormatter {

	@Override
	public String formatter(ResultSet results) throws XMLStreamException {
		McTopoJSON topo=new McTopoJSON();
		topo.getHandler().
		String topol=topo.encode(null);
		topol.
		// TODO Auto-generated method stub
		return null;
	}

}
