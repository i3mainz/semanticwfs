package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.locationtech.jts.geom.Geometry;

import com.github.jferard.fastods.AnonymousOdsFileWriter;
import com.github.jferard.fastods.OdsDocument;
import com.github.jferard.fastods.OdsFactory;
import com.github.jferard.fastods.Table;
import com.github.jferard.fastods.TableRowImpl;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class ODSFormatter extends ResultFormatter {

	public ODSFormatter() {
		this.urlformat = "ods";
		this.label = "Open Document Spreadsheet (ODS)";
		this.mimeType = "application/vnd.oasis.opendocument.spreadsheet";
		this.exposedType = "application/vnd.oasis.opendocument.spreadsheet";
		this.fileextension = "ods";
		this.definition = "https://www.oasis-open.org/committees/tc_home.php";
	}

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException {
		final OdsFactory odsFactory = OdsFactory.create(Logger.getLogger("odsformatter"), Locale.US);
		final AnonymousOdsFileWriter writer = odsFactory.createWriter();
		final OdsDocument wb = writer.document();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Table sheet;
		try {
			sheet = wb.addTable(featuretype);
			Boolean first = true;
			String rel = "", val = "", lastInd = "";
			Integer RowNum = 1, colNum = 0;
			TableRowImpl firstRow = sheet.getRow(0);
			TableRowImpl currentRow = sheet.getRow(RowNum);
			while (results.hasNext()) {
				QuerySolution solu = results.next();
				Iterator<String> varnames = solu.varNames();
				String curfeaturetype = "";
				if (solu.get(indvar) != null) {
					curfeaturetype = solu.get(indvar).toString();
					if (curfeaturetype.contains("http") && curfeaturetype.contains("#")) {
						curfeaturetype = curfeaturetype.substring(curfeaturetype.lastIndexOf('#') + 1);
					}
					if (!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
						lastQueriedElemCount++;
						currentRow = sheet.getRow(++RowNum);
						colNum = 0;
					}
				}
				while (varnames.hasNext()) {
					String name = varnames.next();
					System.out.println("Name: " + name);
					if (name.endsWith("_geom")) {
						Geometry geom = this.parseVectorLiteral(
								solu.get(name).toString().substring(0, solu.get(name).toString().indexOf("^^")),
								solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^") + 2), epsg,
								srsName);
						if (geom != null)
							currentRow.getOrCreateCell(colNum++).setStringValue(geom.toText());
						else
							currentRow.getOrCreateCell(colNum++).setStringValue(solu.get(name).toString());
						if (first) {
							firstRow.getOrCreateCell(colNum++).setStringValue("the_geom");
						}
					} else if (name.equalsIgnoreCase(indvar)) {
						continue;
					} else if ("rel".equalsIgnoreCase(name) || name.contains("_rel")) {
						rel = solu.get(name).toString();
						if (first) {
							firstRow.getOrCreateCell(colNum++).setStringValue(solu.get(name).toString());
						}
					} else if ("val".equalsIgnoreCase(name) || name.contains("_val")) {
						val = solu.get(name).toString();
					} else {
						if (first) {
							firstRow.getOrCreateCell(colNum++).setStringValue(name);
						}
						try {
							Literal lit = solu.getLiteral(name);
							currentRow.getOrCreateCell(colNum++).setStringValue(lit.getString());
						} catch (Exception e) {
							currentRow.getOrCreateCell(colNum++).setStringValue(solu.get(name).toString());
						}
					}
				}
				if (!rel.isEmpty() && !val.isEmpty()) {
					if (!rel.equals("http://www.opengis.net/ont/geosparql#hasGeometry")
							&& !rel.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
						currentRow.getOrCreateCell(colNum++).setStringValue(val);
					}
					rel = "";
					val = "";
				} else {
					lastQueriedElemCount++;
					currentRow = sheet.getRow(++RowNum);
					colNum = 0;
				}
				lastInd = solu.get(indvar).toString();
			}
			// Write the output to a file

			writer.save(baos);
			baos.close();
			return new String(baos.toByteArray(), "UTF-8");
		} catch (IOException e) {
			try {
				baos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return "";
		}
	}

}
