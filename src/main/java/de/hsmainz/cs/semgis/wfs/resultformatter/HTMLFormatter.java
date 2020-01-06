package de.hsmainz.cs.semgis.wfs.resultformatter;

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class HTMLFormatter extends ResultFormatter {

	@Override
	public String formatter(ResultSet results) throws XMLStreamException {
		StringBuilder builder=new StringBuilder();
		builder.append("<table width=\"100%\" align=\"center\" id=\"queryres\" class=\"tablesorter\" border=\"1\"><tr>");
		Boolean first=true;
		while(results.hasNext()) {
			QuerySolution solu=results.next();
			if(first) {
				Iterator<String> iter=solu.varNames();
				builder.append("<thead><tr>");
				while(iter.hasNext()) {
					builder.append("<th>"+iter.next()+"</th>");
				}
				builder.append("</tr></thead><tbody>");
				first=false;
			}
			Iterator<String> iter=solu.varNames();
			while(iter.hasNext()) {
				builder.append("<td align=\"center\">"+solu.get(iter.next())+"</td>");
			}
			builder.append("</tr>");
	    }
		builder.append("</tbody></table>");
		return builder.toString();
	}

}
