package de.hsmainz.cs.semgis.wfs.resultformatter.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.WCSResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class CovJSONFormatter extends WCSResultFormatter {

	public CovJSONFormatter() {
		this.mimeType="application/json";
		this.exposedType="application/covjson";
		this.urlformat="covjson";
		this.label="CoverageJSON";
		this.fileextension="covjson";
	}
	
	@Override
	public String formatter(ResultSet results,String startingElement,
			String featuretype,String propertytype,
			String typeColumn,Boolean onlyproperty,Boolean onlyhits,
			String srsName,String indvar,String epsg,List<String> eligiblenamespaces,
			List<String> noteligiblenamespaces,StyleObject mapstyle,Boolean alternativeFormat,Boolean invertXY) throws XMLStreamException {
		
		JSONObject result=new JSONObject();
		result.put("type", "Coverage");
		JSONObject domain=new JSONObject();
		result.put("domain", domain);
		domain.put("type", "Domain");
		domain.put("domainType", "Grid");
		JSONObject axes=new JSONObject();
		domain.put("axes", axes);
		JSONObject parameters=new JSONObject();
		JSONObject parameter=new JSONObject();
		parameters.put("altitude",parameter);
		result.put("parameters",parameters);
		parameter.put("type","Parameter");
		JSONObject paramdescription=new JSONObject();
		parameter.put("description", paramdescription);
		paramdescription.put("en", "altitude");
		JSONObject unit=new JSONObject();		
		parameter.put("unit", unit);
		JSONObject unitlabel=new JSONObject();
		unitlabel.put("en", "meter");
		JSONObject unitsymbol=new JSONObject();
		unit.put("label", unitlabel);
		unit.put("symbol", unitsymbol);
		unitsymbol.put("value","meter");
		unitsymbol.put("type","http://www.opengis.net/def/uom/UCUM/");	
		JSONArray referencing=new JSONArray();
		domain.put("referencing", referencing);
		JSONObject ref=new JSONObject();
		referencing.put(ref);
		JSONArray coordinates=new JSONArray();
		coordinates.put("x");
		coordinates.put("y");
		ref.put("coordinates", coordinates);
		JSONObject system=new JSONObject();
		ref.put("system", system);
		system.put("type", "GeographicCRS");
		system.put("id", "http://www.opengis.net/def/crs/EPSG/0/"+srsName);
		JSONObject ranges=new JSONObject();
		result.put("ranges",ranges);
		JSONObject altituderange=new JSONObject();
		ranges.put("altitude",altituderange);
		altituderange.put("type","ndArray");
		altituderange.put("dataType", "float");	
		JSONArray axisNames=new JSONArray();
		altituderange.put("axisNames", axisNames);
		axisNames.put("x");
		axisNames.put("y");
		JSONArray values=new JSONArray();
		altituderange.put("values", values);
		Integer numlines=1;
		JSONObject x=new JSONObject();
		x.put("values",new JSONArray());
		JSONObject y=new JSONObject();
		y.put("values", new JSONArray());
		Double maxx=Double.MIN_VALUE,maxy=Double.MIN_VALUE,maxz=Double.MIN_VALUE,minx=Double.MAX_VALUE,miny=Double.MAX_VALUE,minz=Double.MAX_VALUE;
		File file=null;
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String firstline;
			firstline = reader.readLine();
			while((firstline=reader.readLine())!=null) {
				String[] splitted=firstline.split(" ");
				for(String spl:splitted) {
					/*Double x=Double.valueOf(splitted[0]);
					if(x<minx) {
						minx=x;
					}
					if(x>maxx) {
						maxx=x;
					}
					Double y=Double.valueOf(splitted[1]);
					if(y<miny) {
						miny=y;
					}
					if(y>maxy) {
						maxy=y;
					}*/
					x.getJSONArray("values").put(Double.valueOf(splitted[0]));
					y.getJSONArray("values").put(Double.valueOf(splitted[1]));
					values.put(Double.valueOf(splitted[2]));
				}
				numlines++;
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*JSONObject x=new JSONObject();
		x.put("num", numlines);
		x.put("min",minx);
		x.put("max", maxx);
		JSONObject y=new JSONObject();
		y.put("num", numlines);
		y.put("min",miny);
		y.put("max", maxy);
		JSONObject z=new JSONObject();
		z.put("num", numlines);
		z.put("min",minz);
		z.put("max", maxz);*/
		axes.put("x", x);
		axes.put("y", y);
		System.out.println(result.toString(2));
		return result.toString();
	}

}
