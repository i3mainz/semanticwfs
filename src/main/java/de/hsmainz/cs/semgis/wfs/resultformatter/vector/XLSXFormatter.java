package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.locationtech.jts.geom.Geometry;

import de.hsmainz.cs.semgis.wfs.resultformatter.VectorResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class XLSXFormatter extends VectorResultFormatter {


	
	/**
	 * Constructor for this class.
	 */
	public XLSXFormatter() {
		this.mimeType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		this.exposedType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		this.urlformat="xlsx";
		this.label="Excel Sheet (XLSX)";
		this.fileextension="xlsx";
		this.definition="http://www.openoffice.org/sc/excelfileformat.pdf";
	}
	
	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY, Boolean coverage,Writer out) throws XMLStreamException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet(featuretype);
    	Boolean first=true;
		String rel="",val="",lastInd="";
		Integer RowNum=1,colNum=0;
		Row firstRow=sheet.createRow(0);
		Row currentRow=sheet.createRow(RowNum);
	    while(results.hasNext()) {
	    	QuerySolution solu=results.next();
	    	Iterator<String> varnames = solu.varNames();
	    	String curfeaturetype="";
	    	if(solu.get(indvar)!=null) {
				curfeaturetype=solu.get(indvar).toString();
				if(curfeaturetype.contains("http") && curfeaturetype.contains("#")){
					curfeaturetype=curfeaturetype.substring(curfeaturetype.lastIndexOf('#')+1);
				}
				if(!solu.get(indvar).toString().equals(lastInd) || lastInd.isEmpty()) {
					lastQueriedElemCount++;
				    currentRow=sheet.createRow(++RowNum);
				    colNum=0;
				}
			}
	    	while(varnames.hasNext()) {
	    		String name=varnames.next();
	    		System.out.println("Name: "+name);
	    		if(name.endsWith("_geom")) {
	    			Geometry geom=this.parseVectorLiteral(solu.get(name).toString().substring(0,solu.get(name).toString().indexOf("^^")),
							solu.get(name).toString().substring(solu.get(name).toString().indexOf("^^")+2), epsg, srsName);
					if(geom!=null)
						currentRow.createCell(colNum++).setCellValue(geom.toText());
					else
						currentRow.createCell(colNum++).setCellValue(solu.get(name).toString());
	    			if(first) {
	    				firstRow.createCell(colNum++).setCellValue("the_geom");
	    			}
	    		}else if(name.equalsIgnoreCase(indvar)){
					continue;
				}else if("rel".equalsIgnoreCase(name) || name.contains("_rel")){
					rel=solu.get(name).toString();
		    		if(first) {
		    			firstRow.createCell(colNum++).setCellValue(solu.get(name).toString());
		    		}
				}else if("val".equalsIgnoreCase(name) || name.contains("_val")){
					val=solu.get(name).toString();
				}else {
		    		if(first) {
		    			firstRow.createCell(colNum++).setCellValue(name);
		    		}
	    			try {
	    				Literal lit=solu.getLiteral(name);
	    				currentRow.createCell(colNum++).setCellValue(lit.getString());
	    			}catch(Exception e) {
	    				currentRow.createCell(colNum++).setCellValue(solu.get(name).toString());
	    			}  			
	    		}
	    	}
	    	if(!rel.isEmpty() && !val.isEmpty()) {
				if(!rel.equals("http://www.opengis.net/ont/geosparql#hasGeometry") && !rel.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
    				currentRow.createCell(colNum++).setCellValue(val);
				}		
				rel="";
				val="";
			}else {
				lastQueriedElemCount++;
			    currentRow=sheet.createRow(++RowNum);
			    colNum=0;
			}
			lastInd=solu.get(indvar).toString();
	    }
		// Write the output to a file
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
			wb.close();
			return new String( baos.toByteArray(), "UTF-8" );
		} catch (IOException e) {
			try {
				wb.close();
				baos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return "";
		}
	}

}
