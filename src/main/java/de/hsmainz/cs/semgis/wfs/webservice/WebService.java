package de.hsmainz.cs.semgis.wfs.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.jersey.api.NotFoundException;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.HTMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.triplestore.TripleStoreConnector;
import de.hsmainz.cs.semgis.wfs.util.Tuple;

@Path("/")
public class WebService {

	static JSONObject triplestoreconf = new JSONObject();

	static JSONObject wfsconf = null;
	
	public static Map<String,Map<String,String>> featureTypeCache=new TreeMap<>();
	
	public static Map<String,Map<String,String>> nameSpaceCache=new TreeMap<>();
	
	public static Map<String,Tuple<Date,String>> hitCache=new TreeMap<>();
	
	public static long milliesInDays=24 * 60 * 60 * 1000;

	String htmlHead="<html><head><link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.css\"\r\n" + 
			"   integrity=\"sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ==\"\r\n" + 
			"   crossorigin=\"\"/>\r\n" + 
			"<script src=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.js\"\r\n" + 
			"   integrity=\"sha512-GffPMF3RvMeYyc1LWMHtK8EbPv0iNZ8/oTtHPx9/cc2ILxQ+u905qIwdpULaqDkyBKgOaB57QTMg7ztg8Jm2Og==\"\r\n" + 
			"   crossorigin=\"\"></script><link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\"/><script src=\"https://code.jquery.com/jquery-3.4.1.min.js\"></script><script src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script><script src='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/Leaflet.fullscreen.min.js'></script>\r\n" + 
			"<link href='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/leaflet.fullscreen.css' rel='stylesheet' /></head>";
	
	public WebService() throws IOException {
		if(wfsconf==null) {
			String text2 = new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8);
			wfsconf = new JSONObject(text2);
		}
	}
	
	public Response createExceptionResponse(Exception e,String format) {
		if(format==null || format.equals("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.writeStartElement("ExceptionReport");
				writer.setDefaultNamespace("http://www.opengis.net/ogc");
				writer.writeNamespace("xsi", "https://www.w3.org/2001/XMLSchema-instance");

					writer.writeStartElement("Exception");
				
				writer.writeAttribute("locator", e.getStackTrace()[0].getClassName());
				writer.writeStartElement("ExceptionText");
				writer.writeCharacters(e.getMessage());
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				} catch (XMLStreamException e1 ) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return Response.ok("").type(MediaType.APPLICATION_XML).build();
				}
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
		}
		return Response.ok("").type(MediaType.APPLICATION_XML).build();
	}

	@POST
	@Produces(MediaType.TEXT_XML)
	@Path("/post/wfs")
	public Response entryPointPOST(@DefaultValue("WFS") @QueryParam("SERVICE") String service,
			@DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@DefaultValue("2.0.0") @QueryParam("VERSION") String version,
			@DefaultValue("") @QueryParam("TYPENAME") String typename,
			@DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@DefaultValue("") @QueryParam("SRSNAME") String srsName,
			@DefaultValue("gml") @QueryParam("EXCEPTIONS") String exceptions,
			@DefaultValue("") @QueryParam("BBOX") String bbox,
			@DefaultValue("") @QueryParam("VALUEREFERENCE") String propertyname,
			@DefaultValue("ASC") @QueryParam("SORTBY") String sortBy,
			@DefaultValue("results") @QueryParam("RESULTTYPE") String resultType,
			@DefaultValue("") @QueryParam("RESOURCEID") String resourceids,
			@DefaultValue("") @QueryParam("GMLOBJECTID") String gmlobjectid,
			@DefaultValue("0") @QueryParam("STARTINDEX") String startindex,
			@DefaultValue("") @QueryParam("FILTER") String filter,
			@DefaultValue("") @QueryParam("FILTERLANGUAGE") String filterLanguage,
			@DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output,
			@DefaultValue("5") @QueryParam("COUNT") String count) {
	    return entryPoint(service,request,version,typename,typenames,srsName,exceptions,bbox,propertyname,sortBy,resultType,resourceids,gmlobjectid,startindex,filter,filterLanguage,output,count);
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs")
	public Response entryPoint( @DefaultValue("WFS") @QueryParam("SERVICE") String service,
			@DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@DefaultValue("2.0.0") @QueryParam("VERSION") String version,
			@DefaultValue("") @QueryParam("TYPENAME") String typename,
			@DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@DefaultValue("") @QueryParam("SRSNAME") String srsName,
			@DefaultValue("gml") @QueryParam("EXCEPTIONS") String exceptions,
			@DefaultValue("") @QueryParam("BBOX") String bbox,
			@DefaultValue("") @QueryParam("VALUEREFERENCE") String propertyname,
			@DefaultValue("ASC") @QueryParam("SORTBY") String sortBy,
			@DefaultValue("results") @QueryParam("RESULTTYPE") String resultType,
			@DefaultValue("") @QueryParam("RESOURCEID") String resourceids,
			@DefaultValue("") @QueryParam("GMLOBJECTID") String gmlobjectid,
			@DefaultValue("0") @QueryParam("STARTINDEX") String startindex,
			@DefaultValue("") @QueryParam("FILTER") String filter,
			@DefaultValue("") @QueryParam("FILTERLANGUAGE") String filterLanguage,
			@DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output,
			@DefaultValue("5") @QueryParam("COUNT") String count) {
			System.out.println("Request: "+request);
			System.out.println("ResultType: "+resultType);
			if(typename.isEmpty() && !typenames.isEmpty()) {
				typename=typenames;
			}
			if (service.equalsIgnoreCase("WFS")) {
				if ("getCapabilities".equalsIgnoreCase(request)) {
					try {
						return this.constructCapabilities(version,version.substring(0,version.lastIndexOf('.')));
					} catch (XMLStreamException e) {
						e.printStackTrace();
						return Response.ok("").type(MediaType.TEXT_PLAIN).build();
					}
				}
				if ("describeFeatureType".equalsIgnoreCase(request)) {
					try {
						return this.describeFeatureType(typename, version);
					} catch (XMLStreamException e) {
						e.printStackTrace();
						return Response.ok("").type(MediaType.TEXT_PLAIN).build();
					}
				}
				if ("getFeature".equalsIgnoreCase(request)) {
					try {
						return this.getFeature(typename, output, count,startindex,srsName,sortBy,version,resourceids,filter,filterLanguage,resultType);
					} catch (XMLStreamException e) {
						e.printStackTrace();
						return Response.ok("").type(MediaType.TEXT_PLAIN).build();
					}
				}
				if ("getPropertyValue".equalsIgnoreCase(request)) {
					return this.getPropertyValue(typename, propertyname, output,resourceids,filter,count,resultType);
				}
				if ("getGmlObject".equalsIgnoreCase(request)) {
					return this.getGmlObject(typename, gmlobjectid,"4",output);
				}
			}
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections")
	public Response collections(@DefaultValue("json") @QueryParam("f") String format) {
		System.out.println(format);
		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONArray collections = new JSONArray();
			JSONObject link=new JSONObject();
			link.put("rel", "self");
			link.put("title", "This document");
			link.put("type", "application/json");
			link.put("href", wfsconf.get("baseurl")+"/collections?f=json");
			links.put(link);
			link=new JSONObject();
			link.put("rel", "alternate");
			link.put("title", "This document as XML");
			link.put("type", "text/xml");
			link.put("href", wfsconf.get("baseurl")+"/collections?f=gml");
			links.put(link);
			link=new JSONObject();
			link.put("rel", "alternate");
			link.put("title", "This document as HTML");
			link.put("type", "text/html");
			link.put("href", wfsconf.get("baseurl")+"/collections?f=html");
			links.put(link);
			link=new JSONObject();
			link.put("rel", "describedBy");
			link.put("title", "XML Schema for this dataset");
			link.put("type", "application/xml");
			link.put("href", "http://www.acme.com/3.0/wfs/collections/schema");
			links.put(link);
			result.put("links", links);
			result.put("collections", collections);
			for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
				JSONObject coll = new JSONObject();
				JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
				coll.put("id", curobj.getString("name"));
				coll.put("title", curobj.getString("name"));
				JSONObject extent=new JSONObject();
				JSONObject spatial=new JSONObject();
				spatial.put("crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84");
				coll.put("extent", extent);
				extent.put("spatial",spatial);
				JSONArray colinks = new JSONArray();
				for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
					link = new JSONObject();
					if(formatter.exposedType.contains("geo+json")) {
						link.put("rel", "self");
					}else {
						link.put("rel", "alternate");
					}
					link.put("href", wfsconf.getString("baseurl") + "/collections/" + curobj.getString("name") + "/items/" + "?f="+formatter.exposedType);
					link.put("type", formatter.exposedType);
					link.put("title", curobj.getString("name"));
					colinks.put(link);
				}
				coll.put("links", colinks);
				collections.put(coll);
			}
			return Response.ok(result.toString(2)).type(ResultFormatter.getFormatter(format).mimeType).build();
		} else if (format!=null && format.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.setDefaultNamespace("http://www.opengis.net/ogcapi-features-1/1.0");
				writer.setPrefix("atom", "http://www.w3.org/2005/Atom");
				writer.writeStartElement("Collections");
				writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("xmlns:sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
				writer.writeAttribute("service", "OGCAPI-FEATURES");
				writer.writeAttribute("version", "1.0.0");
				writer.writeStartElement("Title");
				writer.writeCharacters(wfsconf.getString("servicetitle"));
				writer.writeEndElement();
				writer.writeStartElement("Description");
				writer.writeCharacters("");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "self");
				writer.writeAttribute("title", "This document");
				writer.writeAttribute("type", "text/xml");
				writer.writeAttribute("href", wfsconf.get("baseurl")+"/collections?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.get("baseurl")+"/collections?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as HTML");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.get("baseurl")+"/collections?f=html");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "describedBy");
				writer.writeAttribute("title", "XML Schema for this dataset");
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", "http://www.acme.com/3.0/wfs/collections/schema");
				writer.writeEndElement();
				for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
					JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
					writer.writeStartElement("Collection");
					writer.writeStartElement("Id");
					writer.writeCharacters(curobj.getString("name"));
					writer.writeEndElement();
					writer.writeStartElement("Title");
					writer.writeCharacters(curobj.getString("name"));
					writer.writeEndElement();
					for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
						writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
						writer.writeAttribute("rel", "items");
						writer.writeAttribute("title", curobj.getString("name"));
						writer.writeAttribute("type", formatter.exposedType);
						writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
								+ curobj.getString("name") + "/items?f="+formatter.exposedType);
						writer.writeEndElement();
					}
					writer.writeEndElement();
					writer.writeStartElement("Extent");
					writer.writeStartElement("Spatial");
					writer.writeAttribute("crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84");
					writer.writeEndElement();
					writer.writeEndElement();
				}
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}
		} else if (format == null || format.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append(htmlHead);
			builder.append("<h1 align=center>");
			builder.append("FeatureCollection View");
			builder.append("</h1>");
			builder.append("<table width=100% border=1><tr><th>Collection</th><th>Decription</th><th>Schema</th><th>Formats</th></tr>");
			for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
				JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
				builder.append("<tr><td align=center><a href=\""+wfsconf.getString("baseurl")+"/collections/"+wfsconf.getJSONArray("datasets").getJSONObject(i).get("name")+"?f=html\">"+wfsconf.getJSONArray("datasets").getJSONObject(i).get("name")+"</a></td><td align=center>");
				if(wfsconf.getJSONArray("datasets").getJSONObject(i).has("description")) {
					builder.append(wfsconf.getJSONArray("datasets").getJSONObject(i).get("description"));
				}
				builder.append("</td><td align=center>");
				if(wfsconf.getJSONArray("datasets").getJSONObject(i).has("schema")) {
					builder.append("<a href=\""+wfsconf.getJSONArray("datasets").getJSONObject(i).get("schema")+"\" target=\"_blank\">[Schema]</a>");
				}else {
					builder.append("<a href=\""+wfsconf.getString("baseurl") + "/collections/"+ curobj.getString("name") + "/schema\" target=\"_blank\">[Schema]</a>");
				}
				builder.append("</td><td align=center>");
				Integer counter=0;
				for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
					if(counter%4==0) {
						builder.append("<br>");
					}
					builder.append("<a href=\""+wfsconf.getString("baseurl") + "/collections/"+ curobj.getString("name") + "/items?f="+formatter.exposedType+"\">["+formatter.exposedType.toUpperCase()+"]</a>&nbsp;&nbsp;");
					counter++;
				}
				builder.append("</td></tr>");
			}
			builder.append("</table>");
			builder.append("<table width=100%><tr><td><a href=\""+wfsconf.getString("baseurl")+"/?f=html\">Back to LandingPage</a></td><td align=right>This page in <a href=\""+wfsconf.getString("baseurl") + "/collections?f=gml\">[GML]</a> <a href=\""+wfsconf.getString("baseurl") + "/collections?f=json\">[JSON]</a></body></html>");
			return Response.ok(builder.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
		}else {
			throw new NotFoundException();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/items/{featureid}")
	public Response getFeatureById(@PathParam("collectionid") String collectionid,
			@PathParam("featureid") String featureid, 
			@DefaultValue("json") @QueryParam("f") String format) {
		System.out.println(collectionid+" - "+featureid);
		if (collectionid == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(collectionid)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj ==null) {
			throw new NotFoundException();
		}
		if(!workingobj.has("attcount") && !workingobj.getString("query").contains("?rel") && !workingobj.getString("query").contains("?val")) {
			workingobj.put("attcount", 1);
		}else if(!workingobj.has("attcount")) {
			featureTypeCache.put(collectionid.toLowerCase(),TripleStoreConnector
					.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
			  workingobj.getString("name"),workingobj));		
		}
		String query=workingobj.getString("query");
		String res = "";
		try {
			res = TripleStoreConnector.executeQuery(query, workingobj.getString("triplestore"),
					format, "0","0","sf:featureMember",collectionid,featureid,workingobj,"","","");
			System.out.println(res);
			if(res==null || res.isEmpty()) {
				throw new NotFoundException();
			}
		} catch (JSONException | XMLStreamException e1) {
			// TODO Auto-generated catch block
			return this.createExceptionResponse(e1, "");
		}

		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			result.put("links", links);
			for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
				JSONObject link = new JSONObject();
				if(formatter.exposedType.contains("geojson")) {
					link.put("rel", "self");
				}else {
					link.put("rel", "alternate");
				}
				link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items/" + featureid
						+ "?f="+formatter.exposedType);
				link.put("type", formatter.exposedType);
				link.put("title", featureid);
				links.put(link);
			}
			result.put("id", featureid);
			JSONObject jsonresult=new JSONObject(res);
			JSONObject features = jsonresult.getJSONArray("features").getJSONObject(0);
			if(jsonresult.has("@context")) {
				result.put("@context",jsonresult.getJSONObject("@context"));
			}
			result.put("type", "Feature");
			result.put("links", links);
			result.put("timeStamp", System.currentTimeMillis());
			result.put("numberMatched", features.length());
			result.put("numberReturned", features.length());
			result.put("geometry", features.getJSONObject("geometry"));
			result.put("properties", features.getJSONObject("properties"));
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (format!=null && format.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.setDefaultNamespace("http://www.opengis.net/ogcapi-features-1/1.0");
				writer.setPrefix("atom", "http://www.w3.org/2005/Atom");
				writer.writeStartElement("sf:Feature");
				writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("xmlns:sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
				writer.writeAttribute("service", "OGCAPI-FEATURES");
				writer.writeAttribute("version", "1.0.0");
				writer.writeStartElement("Title");
				writer.writeCharacters("");
				writer.writeEndElement();
				writer.writeStartElement("Description");
				writer.writeCharacters("");
				writer.writeEndElement();
				for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					if(formatter.exposedType.contains("json")) {
						writer.writeAttribute("rel", "self");
					}else {
						writer.writeAttribute("rel", "alternate");
					}
					writer.writeAttribute("title", workingobj.getString("name"));
					writer.writeAttribute("type", formatter.exposedType);
					writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
							+ workingobj.getString("name") + "/items/+" + featureid + "?f="+formatter.exposedType);
					writer.writeEndElement();
				}
				strwriter.append(res);
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}
		} else if (format == null || format.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append(htmlHead);
			builder.append("<body><h1 align=\"center\">");
			builder.append(featureid);
			builder.append("</h1>");
			builder.append("<ul>");
			builder.append(res);
			builder.append("</ul>");
			builder.append("<table width=100%><tr><td><a href=\""+wfsconf.getString("baseurl")+"/collections/"+collectionid+"?f=html\">Back to "+collectionid+" Collection</a></td><td align=right>This page in <a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+"/items/"+featureid+"?f=gml\">[GML]</a> <a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+"/items/"+featureid+"?f=json\">[JSON]</a></body></html>");
			builder.append("</body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		}else {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/")
	public Response landingPage(@DefaultValue("html") @QueryParam("f") String format) {
		if (format == null || format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONObject link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"?f=json");
			link.put("rel", "self");
			link.put("type", "application/json");
			link.put("title", "This document");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"?f=gml");
			link.put("rel", "alternate");
			link.put("type", "application/xml");
			link.put("title", "This document as XML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"?f=html");
			link.put("rel", "alternate");
			link.put("type", "text/html");
			link.put("title", "This document as HTML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"/conformance?f=html");
			link.put("rel", "conformance");
			link.put("type", "text/html");
			link.put("title", "Conformance Declaration as HTML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"/conformance?f=gml");
			link.put("rel", "conformance");
			link.put("type", "application/xml");
			link.put("title", "Conformance Declaration as XML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"/conformance?f=json");
			link.put("rel", "conformance");
			link.put("type", "application/json");
			link.put("title", "Conformance Declaration as JSON");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"/collections?f=json");
			link.put("rel", "data");
			link.put("type", "application/json");
			link.put("title", "Collections Metadata as JSON");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"/collections?f=gml");
			link.put("rel", "data");
			link.put("type", "application/xml");
			link.put("title", "Collections Metadata as XML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl")+"/collections?f=html");
			link.put("rel", "data");
			link.put("type", "text/html");
			link.put("title", "Collections Metadata as HTML");
			links.put(link);
			result.put("title", wfsconf.getString("servicetitle"));
			result.put("description", wfsconf.getString("servicedescription"));
			result.put("links", links);
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (format.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.setDefaultNamespace("http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeStartElement("LandingPage");
				writer.setPrefix("atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("xmlns:sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
				writer.writeAttribute("service", "OGCAPI-FEATURES");
				writer.writeAttribute("version", "1.0.0");
				writer.writeStartElement("Title");
				writer.writeCharacters(wfsconf.getString("servicetitle"));
				writer.writeEndElement();
				writer.writeStartElement("Description");
				writer.writeCharacters(wfsconf.getString("servicedescription"));
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "self");
				writer.writeAttribute("title", "This document");
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as HTML");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/?f=html");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "conformance");
				writer.writeAttribute("title", "Conformance Declaration as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/conformance?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "conformance");
				writer.writeAttribute("title", "Conformance Declaration as XML");
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/conformance?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "conformance");
				writer.writeAttribute("title", "Conformance Declaration as HTML");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/conformance?f=html");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "data");
				writer.writeAttribute("title", "Collections Metadata as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/collections?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "data");
				writer.writeAttribute("title", "Collections Metadata as XML");
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/collections?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "data");
				writer.writeAttribute("title", "Collections Metadata as HTML");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", wfsconf.getString("baseurl")+"/collections?f=html");
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				return this.createExceptionResponse(e, "");
			}
		}else if (format.contains("html")) {
			StringBuilder builder=new StringBuilder();
			builder.append(htmlHead);
			builder.append("<body><h1 align=\"center\">LandingPage: "+wfsconf.getString("servicetitle"));
			builder.append("</h1><p>"+wfsconf.getString("servicedescription")+"</p><ul>");
			builder.append("<li>LandingPage in <a href=\""+wfsconf.getString("baseurl")+"/?f=html\">[HTML]</a>");
			builder.append(" <a href=\""+wfsconf.getString("baseurl")+"/?f=gml\">[XML]</a>");
			builder.append(" <a href=\""+wfsconf.getString("baseurl")+"/?f=json\">[JSON]</a></li>");
			builder.append("<li>Conformance Declaration in <a href=\""+wfsconf.getString("baseurl")+"/conformance?f=html\">[HTML]</a>");
			builder.append(" <a href=\""+wfsconf.getString("baseurl")+"/conformance?f=gml\">[XML]</a>");
			builder.append(" <a href=\""+wfsconf.getString("baseurl")+"/conformance?f=json\">[JSON]</a></li>");
			builder.append("<li>Collections Metadata in <a href=\""+wfsconf.getString("baseurl")+"/collections?f=html\">[HTML]</a>");
			builder.append(" <a href=\""+wfsconf.getString("baseurl")+"/collections?f=gml\">[XML]</a>");
			builder.append(" <a href=\""+wfsconf.getString("baseurl")+"/collections?f=json\">[JSON]</a></li></ul>");
			builder.append("This homepage also exposes a WFS 1.0.0, 1.1.0, 2.0.0 compatible Webservice:<ul>");
			builder.append("<li>GetCapabilities WFS 1.0.0 ");
			builder.append("<a href=\""+wfsconf.getString("baseurl")+"/wfs?REQUEST=getCapabilities&VERSION=1.0.0\">[XML]</a><br/>");
			builder.append("</li><li>GetCapabilities WFS 1.1.0 ");
			builder.append("<a href=\""+wfsconf.getString("baseurl")+"/wfs?REQUEST=getCapabilities&VERSION=1.1.0\">[XML]</a>");
			builder.append("</li><li>GetCapabilities WFS 2.0.0 ");
			builder.append("<a href=\""+wfsconf.getString("baseurl")+"/wfs?REQUEST=getCapabilities&VERSION=2.0.0\">[XML]</a>");
			builder.append("</li></ul></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		}else {
			throw new NotFoundException();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}")
	public Response collectionInformation(@PathParam("collectionid") String collectionid,
			@DefaultValue("json") @QueryParam("f") String format, @QueryParam("limit") String limit,
			 @DefaultValue("0") @QueryParam("offset") String offset,
			@QueryParam("bbox") String bbox) {
		if (collectionid == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(collectionid)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj == null) {
			throw new NotFoundException();
		}
		if(!featureTypeCache.containsKey(collectionid.toLowerCase())) {
			featureTypeCache.put(collectionid.toLowerCase(),TripleStoreConnector
					.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
			  workingobj.getString("name"),workingobj)); 
		}
		Map<String,String> mapping=featureTypeCache.get(collectionid.toLowerCase());
		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			result.put("id", workingobj.getString("name"));
			result.put("title", workingobj.getString("name"));
			result.put("description", "");
			JSONObject extent=new JSONObject();
			result.put("extent", extent);
			JSONObject spatial=new JSONObject();
			extent.put("spatial",spatial);
			spatial.put("crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84");
			//extent.put("temporal",new JSONObject());
			JSONArray links=new JSONArray();
			for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
				JSONObject link = new JSONObject();
				if(formatter.exposedType.contains("geojson")) {
					link.put("rel", "self");
				}else {
					link.put("rel", "alternate");
				}
				link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items/"
						+ "?f="+formatter.exposedType);
				link.put("type", formatter.exposedType);
				link.put("title", collectionid);
				links.put(link);
			}
			JSONObject link=new JSONObject();
			link.put("rel", "describedBy");
			link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/schema/");
			link.put("type", "application/xml");
			link.put("title", collectionid+" Schema");
			links.put(link);
			result.put("links",links);
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (format!=null && format.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.setDefaultNamespace("http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeStartElement("Collection");
				writer.setPrefix("atom","http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("xmlns:sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
				writer.writeAttribute("service", "OGCAPI-FEATURES");
				writer.writeAttribute("version", "1.0.0");
				writer.writeStartElement("Id");
				writer.writeCharacters(collectionid);
				writer.writeEndElement();
				writer.writeStartElement("Title");
				writer.writeCharacters(workingobj.getString("description"));
				writer.writeEndElement();
				for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					if(formatter.exposedType.contains("geojson")) {
						writer.writeAttribute("rel", "self");
					}else {
						writer.writeAttribute("rel", "alternate");
					}
					writer.writeAttribute("title", workingobj.getString("name"));
					writer.writeAttribute("type", formatter.exposedType);
					writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
							+ workingobj.getString("name") + "/items?f="+formatter.exposedType);
					writer.writeEndElement();
				}
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "describedBy");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name") + "/schema/");
				writer.writeEndElement();
				writer.writeStartElement("Extent");
				writer.writeStartElement("Spatial");
				writer.writeAttribute("crs","http://www.opengis.net/def/crs/OGC/1.3/CRS84");
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}
		}else if(format == null || format.contains("html")){
			StringBuilder builder=new StringBuilder();
			StringBuilder builder2=new StringBuilder();
			JSONObject geojson=new JSONObject();
			JSONObject geometry=new JSONObject();
			JSONObject properties=new JSONObject();
			geojson.put("type","Feature");
			geojson.put("id",collectionid);
			geojson.put("geometry",geometry);
			geojson.put("properties",properties);
			builder.append(htmlHead);
			builder.append("<body><h1 align=\"center\">");
			builder.append((workingobj.getString("description")!=null?workingobj.getString("description"):collectionid));
			builder.append("</h1>");
			builder.append("<table width=100%><tr><td width=\"100%\" rowspan=2>");
			builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\""+(workingobj.has("typeColumn")?workingobj.getString("typeColumn"):"")+"\"; var markercollection=[];");
			builder2.append(((HTMLFormatter)ResultFormatter.getFormatter("html")).htmlHeader);
			builder2.append("</ul></td><td>Contents:<table border=\"1\"><tr><th>Value</th><th>Type</th>");
			for(String elem:mapping.keySet()) {
				if(!elem.equals("http://www.opengis.net/ont/geosparql#hasGeometry") &&
						!elem.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				if(elem.contains("http")) {
					if(elem.contains("#")) {
						builder2.append("<tr><td align=center><a href=\""+elem+"\">"+elem.substring(elem.lastIndexOf('#')+1)+"</a> ");
					}else {
						builder2.append("<tr><td align=center><a href=\""+elem+"\">"+elem.substring(elem.lastIndexOf('/')+1)+"</a> ");
					}
				}else {
					builder2.append("<tr><td align=center>"+elem);
				}
				if(mapping.get(elem).contains("^^")) {
					String type=mapping.get(elem).substring(mapping.get(elem).lastIndexOf("^^")+2);
					builder2.append("</td><td align=center><a href=\""+type+"\">"+type.substring(type.lastIndexOf('#')+1)+"</a></td></tr>");
				}else {
					if((mapping.get(elem).contains("http") || mapping.get(elem).contains("file:/") ) && mapping.get(elem).contains("#")) {
						builder2.append("</td><td align=center><a href=\""+mapping.get(elem)+"\">"+mapping.get(elem).substring(mapping.get(elem).lastIndexOf('#')+1)+"</a></td></tr>");						
					}else if((mapping.get(elem).contains("http") || mapping.get(elem).contains("file:/") ) && mapping.get(elem).contains("/")) {
						builder2.append("</td><td align=center><a href=\""+mapping.get(elem)+"\">"+mapping.get(elem).substring(mapping.get(elem).lastIndexOf('/')+1)+"</a></td></tr>");						
					}else {
						builder2.append("</td><td align=center><a href=\""+mapping.get(elem)+"\">"+mapping.get(elem)+"</a></td></tr>");
					}
				}
				if(elem.contains("http://www.opengis.net/ont/geosparql#asWKT")) {
					geometry.put("type",mapping.get(elem).substring(0,mapping.get(elem).indexOf('(')));
					String coords=mapping.get(elem).substring(mapping.get(elem).indexOf('(')+1,mapping.get(elem).indexOf(')'));
					JSONArray arr=new JSONArray();
					geometry.put("coordinates",arr);
					for(String coord:coords.split(" ")) {
						arr.put(Double.valueOf(coord));
					}
				}
				properties.put(elem,mapping.get(elem));
				}
			}
			builder2.append("</table>");
			builder.append("var geojson="+geojson.toString()+"</script>");
			builder.append(builder2.toString());
			builder.append("</td></tr><tr><td>Serializations:<ul>");
			for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
				builder.append("<li><a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name") + "/items?limit=5&f="+formatter.exposedType+"\">["+formatter.exposedType.toUpperCase()+"]</a></li>");
			}
			builder.append("</td></tr></table>");
			builder.append("<table width=100%><tr><td><a href=\""+wfsconf.getString("baseurl")+"/collections/"+workingobj.getString("name")+"/items?f=html&limit=1&offset="+(Integer.valueOf(offset)!=0?(Integer.valueOf(offset)-1)+"":"0")+"\">[Previous]</a></td><td align=right><a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+"/items?f=html&limit=1&offset="+(Integer.valueOf(offset)+1)+"\">[Next]</a></body></html>");
			builder.append("<table width=100%><tr><td><a href=\""+wfsconf.getString("baseurl")+"/collections?f=html\">Back to Collections</a></td><td align=right>This page in <a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+"?f=gml\">[GML]</a> <a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+"?f=json\">[JSON]</a></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else {
			throw new NotFoundException();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Path("/collections/{collectionid}/schema")
	public Response getSchema(@PathParam("collectionid") String collectionid) {
		try {
			return this.describeFeatureType(collectionid, "2.0.0");
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new NotFoundException();
		}
	}
	
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/items")
	public Response collectionItems(@PathParam("collectionid") String collectionid,
			@DefaultValue("json") @QueryParam("f") String format, 
			@DefaultValue("5") @QueryParam("limit") String limit,
			@DefaultValue("0") @QueryParam("offset") String offset,
			@DefaultValue("") @QueryParam("bbox") String bbox,
			@DefaultValue("") @QueryParam("datetime") String datetime) {
		System.out.println("Limit: "+limit);
		if (collectionid == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(collectionid)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj == null) {
			throw new NotFoundException();
		}
		if(!workingobj.has("attcount") && !workingobj.getString("query").contains("?rel") && !workingobj.getString("query").contains("?val")) {
			workingobj.put("attcount", 1);
		}else if(!workingobj.has("attcount")) {
			featureTypeCache.put(collectionid.toLowerCase(),TripleStoreConnector
					.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
			  workingobj.getString("name"),workingobj));		
		}
		System.out.println("Attcount: "+workingobj.getInt("attcount"));
		System.out.println(limit);
		try {
			String res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
						workingobj.getString("triplestore"), format,""+(Integer.valueOf(limit)*workingobj.getInt("attcount")),
						""+(Integer.valueOf(offset)*workingobj.getInt("attcount")),"sf:featureMember",collectionid,"",workingobj,"","","");
			System.out.println(res);
			if(res==null || res.isEmpty()) {
				throw new NotFoundException();
			}
			//System.out.println(res);
			if (format != null && format.contains("json")) {
				JSONObject result = new JSONObject();
				JSONArray links = new JSONArray();
				for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
					JSONObject link = new JSONObject();
					if(formatter.exposedType.contains("geojson")) {
						link.put("rel", "self");
					}else {
						link.put("rel", "alternate");
					}
					link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items?f="+formatter.exposedType);
					link.put("type", formatter.exposedType);
					link.put("title", collectionid);
					links.put(link);
				}
				if(ResultFormatter.getFormatter(format).mimeType.contains("jsonld")) {
					return Response.ok(res).type("text/plain").build();
				}
				JSONObject jsonresult=new JSONObject(res);
				JSONArray features = jsonresult.getJSONArray("features");
				if(jsonresult.has("@context")) {
					result.put("@context",jsonresult.getJSONObject("@context"));
				}
				result.put("type", "FeatureCollection");
				result.put("links", links);
				result.put("timeStamp", System.currentTimeMillis());
				result.put("numberMatched", features.length());
				result.put("numberReturned", features.length());
				result.put("features", features);
				return Response.ok(result.toString(2)).type(ResultFormatter.getFormatter(format).mimeType).build();
			} else if (format != null && format.contains("gml")) {
				StringWriter strwriter = new StringWriter();
				XMLOutputFactory output = XMLOutputFactory.newInstance();
				XMLStreamWriter writer;
				try {
					writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
					writer.writeStartDocument();
					writer.writeStartElement("sf:FeatureCollection");
					writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
					writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
					writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
					writer.writeAttribute("xmlns:sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
					writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
					writer.setDefaultNamespace("http://www.opengis.net/ogcapi-features-1/1.0");
					writer.setPrefix("atom", "http://www.w3.org/2005/Atom");
					writer.writeAttribute("service", "OGCAPI-FEATURES");
					writer.writeAttribute("version", "1.0.0");
					writer.writeStartElement("gml:boundedBy");
					writer.writeEndElement();
					writer.writeStartElement("Title");
					writer.writeCharacters(collectionid);
					writer.writeEndElement();
					writer.writeStartElement("Description");
					writer.writeCharacters(collectionid);
					writer.writeEndElement();
					for(ResultFormatter formatter:ResultFormatter.resultMap.values()) {
						writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
						if(formatter.exposedType.contains("json")) {
							writer.writeAttribute("rel", "self");
						}else {
							writer.writeAttribute("rel", "alternate");
						}
						writer.writeAttribute("title", workingobj.getString("name"));
						writer.writeAttribute("type", formatter.exposedType);
						writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
								+ workingobj.getString("name") + "/items?f="+formatter.exposedType);
						writer.writeEndElement();
					}
					strwriter.append(res);
					writer.writeEndElement();
					writer.writeEndDocument();
					writer.flush();
					return Response.ok(strwriter.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
				} catch (XMLStreamException e) {
					e.printStackTrace();
					// TODO Auto-generated catch block
					return this.createExceptionResponse(e, "");
				}
			} else if (format == null || format.contains("html")) {
				StringBuilder builder = new StringBuilder();
				builder.append(htmlHead);
				builder.append("<body><h1 align=\"center\">");
				builder.append(collectionid);
				builder.append("</h1>");
				builder.append(res);
				builder.append("<script>$( document ).ready(function() {$('#queryres').DataTable();});</script>");
				builder.append("<table width=100%><tr><td><a href=\""+wfsconf.getString("baseurl")+"/collections/"+collectionid+"?f=html\">Back to "+collectionid+" Collection</a></td><td align=right>This page in <a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+"/items?f=gml\">[GML]</a> <a href=\""+wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+"/items?f=json\">[JSON]</a></body></html>");
				builder.append("</body></html>");
				return Response.ok(builder.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
			}else {				
				return Response.ok(res).type(ResultFormatter.getFormatter(format).mimeType).build();
			}
		} catch (JSONException | XMLStreamException e1) {
			e1.printStackTrace();
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/conformance")
	public Response conformance(@DefaultValue("json") @QueryParam("f") String format) {
		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray conforms = new JSONArray();
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0");
			result.put("conformsTo", conforms);
			return Response.ok(result.toString(2)).type(ResultFormatter.getFormatter(format).mimeType).build();
		} else if(format!=null && format.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.setDefaultNamespace("http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeStartElement("ConformsTo");
				writer.writeAttribute("service", "OGCAPI-FEATURES");
				writer.writeAttribute("version", "1.0.0");
				writer.writeNamespace(null, "http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeNamespace("atom", "http://www.w3.org/2005/Atom");
				writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("xsi:schemaLocation",
						"http://www.opengis.net/ogcapi-features-1/1.0../../xml/core.xsd");
				writer.writeStartElement("atom:link");
				writer.writeAttribute("href", "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core");
				writer.writeEndElement();
				writer.writeStartElement("atom:link");
				writer.writeAttribute("href", "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30");
				writer.writeEndElement();
				writer.writeStartElement("atom:link");
				writer.writeAttribute("href", "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html");
				writer.writeEndElement();
				writer.writeStartElement("atom:link");
				writer.writeAttribute("href", "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson");
				writer.writeEndElement();
				writer.writeStartElement("atom:link");
				writer.writeAttribute("href", "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0");
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
			} catch (XMLStreamException e) {
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}
		}else if(format == null || format.contains("html")) {
			StringBuilder builder=new StringBuilder();
			builder.append("<html><head></head><body><h1>Conformance</h1><ul>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core\">Core</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30\">Oas30</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html\">HTML</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson\">GeoJSON</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0\">GMLSf0</a></li>");
			builder.append("</ul><a href=\""+wfsconf.getString("baseurl")+"/?f=html\">Back to LandingPage</a></body></html>");
			return Response.ok(builder.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
		}else {
			throw new NotFoundException();
		}

	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/hello")
	public String helloWorld() {
		return "HelloWorld";
	}

	public String SERVICETYPEVERSION = "2.0.0";

	public String SERVERURL = "http://localhost:8080/WFSGeoSPARQL/rest/wfs?";

	public Response constructCapabilitiesWFS10(String version,String versionnamespace) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter = new StringWriter();
		XMLStreamWriter xmlwriter = factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("WFS_Capabilities");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.writeDefaultNamespace("http://www.opengis.net/wfs");
		writer.writeAttribute("version", version);
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs");
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
		writer.writeStartElement("Service");
		writer.writeStartElement("Name");
		writer.writeCharacters(wfsconf.has("servicetitle") ? wfsconf.getString("servicetitle") : "");
		writer.writeEndElement();
		writer.writeStartElement("Title");
		writer.writeCharacters(wfsconf.has("servicetitle") ? wfsconf.getString("servicetitle") : "");
		writer.writeEndElement();
		writer.writeStartElement("Abstract");
		writer.writeCharacters(wfsconf.has("abstract") ? wfsconf.getString("abstract") : "");
		writer.writeEndElement();
		writer.writeStartElement("Fees");
		writer.writeCharacters(wfsconf.has("fees") ? wfsconf.getString("fees") : "none");
		writer.writeEndElement();
		writer.writeStartElement("OnlineResource");
		writer.writeCharacters(wfsconf.has("baseurl") ? wfsconf.getString("baseurl") : "");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("Capability");
		writer.writeStartElement("Request");
		writer.writeStartElement("GetCapabilities");
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("GetGmlObject");
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("GetPropertyValue");
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("DescribeFeatureType");
		writer.writeStartElement("SchemaDescriptionLanguage");
		writer.writeStartElement("XMLSCHEMA");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("GetFeature");
		writer.writeStartElement("ResultFormat");
		for(ResultFormatter format:ResultFormatter.resultMap.values()) {
			if(!format.exposedType.isEmpty()) {
				if(format.exposedType.contains("/")) {
					writer.writeStartElement(format.exposedType.substring(format.exposedType.lastIndexOf('/')+1).replace("+","").toUpperCase());
					writer.writeEndElement();
				}else {
					writer.writeStartElement(format.exposedType.toUpperCase());
					writer.writeEndElement();
				}
			}
		}
		writer.writeEndElement();
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("FeatureTypeList");
		writer.writeStartElement("Operations");
		writer.writeStartElement("Query");
		writer.writeEndElement();
		writer.writeEndElement();
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			describeFeatureTypeWFS10(writer, wfsconf.getJSONArray("datasets").getJSONObject(i),versionnamespace,version);
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ogc", "Filter_Capabilities");
		describeSpatialCapabilitiesWFS10(writer,versionnamespace,"http://www.opengis.net/ogc");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
	}
	
	public Response constructCapabilities(String version,String versionnamespace) throws XMLStreamException {
		String serviceType = "WFS";
		String owsns="http://www.opengis.net/ows/1.1";
		if("1.0.0".equals(version))
			return constructCapabilitiesWFS10(version, versionnamespace);
		if("1.0.0".equals(version) || "1.1.0".equals(version)) {
			versionnamespace="";
		}else {
			versionnamespace="/"+versionnamespace;
		}
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter = new StringWriter();
		XMLStreamWriter xmlwriter = factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("WFS_Capabilities");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs"+versionnamespace);
		writer.writeDefaultNamespace("http://www.opengis.net/wfs"+versionnamespace);
		writer.writeAttribute("version", version);
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs"+versionnamespace);
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes/"+versionnamespace);
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
		// ServiceInformation
		writer.writeStartElement(owsns, "ServiceIdentification");
		writer.writeStartElement(owsns, "ServiceType");
		writer.writeAttribute("codeSpace", "OGC");
		writer.writeCharacters(serviceType);
		writer.writeEndElement();
		writer.writeStartElement(owsns, "ServiceTypeVersion");
		writer.writeCharacters(version);
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Title");
		writer.writeCharacters(wfsconf.has("servicetitle") ? wfsconf.getString("servicetitle") : "");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Fees");
		writer.writeCharacters(wfsconf.has("fees") ? wfsconf.getString("fees") : "none");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Abstract");
		writer.writeCharacters(wfsconf.has("abstract") ? wfsconf.getString("abstract") : "");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "OperationsMetadata");
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "GetCapabilities");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Parameter");
		writer.writeAttribute("name", "AcceptVersions");
		writer.writeStartElement(owsns, "AllowedValues");
		writer.writeStartElement(owsns, "Value");
		writer.writeCharacters(version);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Operation");
		writer.writeAttribute("name","GetPropertyValue");
		writer.writeStartElement(owsns,"DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Operation");
		writer.writeAttribute("name","DescribeFeatureType");
		writer.writeStartElement(owsns,"DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Operation");
		writer.writeAttribute("name","GetGmlObject");
		writer.writeStartElement(owsns,"DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "GetFeature");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl")+"/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Parameter");
		writer.writeAttribute("name", "AcceptVersions");
		writer.writeStartElement(owsns, "AllowedValues");
		writer.writeStartElement(owsns, "Value");
		writer.writeCharacters(version);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "FeatureTypeList");
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "Operations");
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "Operation");
		writer.writeCharacters("Query");
		writer.writeEndElement();
		writer.writeEndElement();
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			describeFeatureType(writer, wfsconf.getJSONArray("datasets").getJSONObject(i),versionnamespace,version);
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "Filter_Capabilities");
		describeSpatialCapabilities(writer,versionnamespace,"http://www.opengis.net/fes/"+versionnamespace);
		describeScalarCapabilities(writer, versionnamespace, "http://www.opengis.net/fes/"+versionnamespace);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs/getCapabilities")
	public Response getCapabilities(@DefaultValue("2.0.0") @QueryParam("version") String version) throws XMLStreamException {
		if(!version.equals("2.0.0") && !version.equals("1.1.0"))
			version="2.0.0";
		return constructCapabilities(version,version.substring(0,version.lastIndexOf('.')));
	}

	public void describeFeatureTypeWFS10(XMLStreamWriter writer, JSONObject featuretype,String versionnamespace,String version) throws XMLStreamException {
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.writeStartElement("http://www.opengis.net/wfs", "FeatureType");
		writer.writeStartElement("http://www.opengis.net/wfs", "Name");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs", "Title");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs", "SRS");
		writer.writeCharacters("urn:ogc:def:crs:EPSG::4326");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs", "OutputFormats");
		for(ResultFormatter format:ResultFormatter.resultMap.values()) {
			if(!format.exposedType.isEmpty()) {
				writer.writeStartElement("http://www.opengis.net/wfs", "Format");
				writer.writeCharacters(format.exposedType);
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "LatLongBoundingBox");
		writer.writeAttribute("minx","11.2299229840604");
		writer.writeAttribute("miny","51.2165647648912");
		writer.writeAttribute("maxx","14.8566506458591");
		writer.writeAttribute("maxy","53.5637800901802");
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	
	public void describeFeatureType(XMLStreamWriter writer, JSONObject featuretype,String versionnamespace,String version) throws XMLStreamException {
		if("1.0.0".equals(version) || "1.1.0".equals(version)) {
			versionnamespace="";
		}else {
			versionnamespace="/"+versionnamespace;
			versionnamespace=versionnamespace.replace("//","/");
		}
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "FeatureType");
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "Name");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "Title");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "DefaultCRS");
		writer.writeCharacters("urn:ogc:def:crs:EPSG::4326");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "OutputFormats");
		for(ResultFormatter format:ResultFormatter.resultMap.values()) {
			if(!format.exposedType.isEmpty()) {
				writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "Format");
				writer.writeCharacters(format.exposedType);
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "WGS84BoundingBox");
		writer.writeAttribute("dimensions", "2");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "lowerCorner");
		writer.writeCharacters("11.2299229840604 51.2165647648912");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "upperCorner");
		writer.writeCharacters("14.8566506458591 53.5637800901802");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	public void describeSpatialCapabilities(XMLStreamWriter writer,String versionnamespace,String namespace) throws XMLStreamException {
		writer.writeStartElement(namespace, "SpatialCapabilities");
		writer.writeStartElement(namespace, "GeometryOperands");
		writer.writeStartElement(namespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Box");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Envelope");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Point");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:LineString");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Curve");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Polygon");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperators");
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "BBOX");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Intersects");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Contains");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Crosses");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Touches");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Within");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Overlaps");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Disjoint");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "Equals");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "SpatialOperator");
		writer.writeAttribute("name", "DWithin");
		writer.writeEndElement();
		writer.writeEndElement();
	}

	public void describeSpatialCapabilitiesWFS10(XMLStreamWriter writer,String versionnamespace,String namespace) throws XMLStreamException {
		writer.writeStartElement(namespace, "Spatial_Capabilities");
		writer.writeStartElement(namespace, "Spatial_Operators");
		writer.writeStartElement(namespace, "BBOX");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "Intersects");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "Contains");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "Crosses");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "Touches");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "Within");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "Overlaps");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "Disjoint");
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Equals");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "DWithin");
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	public void describeScalarCapabilities(XMLStreamWriter writer,String versionnamespace,String namespace) throws XMLStreamException {
		writer.writeStartElement(namespace, "Scalar_Capabilities");
		writer.writeStartElement(namespace, "LogicalOperators");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperators");
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("LessThan");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("GreaterThan");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("LessThanEqualTo");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("GreaterThanEqualTo");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("EqualTo");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("NotEqualTo");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("Like");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ComparisonOperator");
		writer.writeCharacters("Between");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "ArithmeticOperators");
		writer.writeEndElement();
		writer.writeEndElement();
	}
	

	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs/describeFeatureType")
	public Response describeFeatureType(@QueryParam("typename") String typename,
			@DefaultValue("version") @QueryParam("version")String version) throws XMLStreamException {
		if(typename==null)
			throw new NotFoundException();
		JSONObject workingobj = null;
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj == null)
			throw new NotFoundException();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter = new StringWriter();
		XMLStreamWriter xmlwriter = factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlwriter);
		String versionnamespace=version.substring(0,version.lastIndexOf('.'));
		if("1.0.0".equals(version) || "1.1.0".equals(version)) {
			versionnamespace="";
		}else {
			versionnamespace="/"+versionnamespace;
			versionnamespace=versionnamespace.replace("//","/");
		}
		writer.writeStartDocument();
		writer.writeStartElement("schema");
		//writer.writeAttribute("targetNamespace",(workingobj.has("namespace")?workingobj.getString("namespace"):wfsconf.getString("baseurl")));
		writer.writeDefaultNamespace("http://www.w3.org/2001/XMLSchema");
		writer.writeNamespace("app",(workingobj.has("namespace")?workingobj.getString("namespace"):wfsconf.getString("baseurl")));
		writer.setPrefix("app",(workingobj.has("namespace")?workingobj.getString("namespace"):wfsconf.getString("baseurl")));
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs"+versionnamespace);
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes/"+versionnamespace);
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
		writer.writeStartElement("element");
		writer.writeAttribute("name",typename);
		writer.writeAttribute("type",typename+"Type");
		writer.writeAttribute("substitutionGroup","gml:_Feature");
		writer.writeEndElement();
		writer.writeStartElement("complexType");
		writer.writeAttribute("name",typename+"Type");
		writer.writeStartElement("complexContent");
		writer.writeStartElement("extension");
		writer.writeAttribute("base", "gml:AbstractFeatureType");
		writer.writeStartElement("all");
		if(!featureTypeCache.containsKey(typename.toLowerCase())) {
			featureTypeCache.put(typename.toLowerCase(),TripleStoreConnector
				.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
		  workingobj.getString("name"),workingobj));
		}
		writer.writeStartElement("element");
		writer.writeAttribute("name","the_geom");
		writer.writeAttribute("minOccurs","0");
		writer.writeAttribute("type","gml:"+(workingobj.has("geometrytype")?workingobj.getString("geometrytype"):"Geometry")+"PropertyType");
		writer.writeEndElement();
		Map<String,String>mapping=featureTypeCache.get(typename.toLowerCase());
		for(String elem:mapping.keySet()) {
			if(elem.equals("namespaces"))
				continue;
			writer.writeStartElement("element"); 
			if(elem.startsWith("http") && elem.contains("#")) {
				writer.writeAttribute("name", elem.substring(elem.lastIndexOf('#')+1));
			}else if(elem.startsWith("http") && elem.contains("/")) {
				writer.writeAttribute("name", elem.substring(elem.lastIndexOf('/')+1));
			}else {
				writer.writeAttribute("name", elem);
			}
			if(mapping.get(elem).contains("^^")) {
				writer.writeAttribute("type", mapping.get(elem).substring(mapping.get(elem).lastIndexOf("^^")+2)); 
			}else if(mapping.get(elem).startsWith("http") || mapping.get(elem).startsWith("file:/")){
				writer.writeAttribute("type","anyURI");
			}else {
				writer.writeAttribute("type","string"); 	
			}
			writer.writeAttribute("minOccurs", "0"); 
			writer.writeEndElement(); 
		}
		// writer.writeEndElement();
		//this.describeFeatureType(writer, workingobj,version.substring(0,version.lastIndexOf('.')),version);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/getFeature")
	public Response getFeature(@QueryParam("typename") String typename, 
			@DefaultValue("json") @QueryParam("outputFormat") String output,
			@DefaultValue("5") @QueryParam("count") String count,
			@DefaultValue("0") @QueryParam("startindex") String startindex,
			@DefaultValue("") @QueryParam("srsName") String srsName,
			@DefaultValue("ASC") @QueryParam("sortBy") String sortBy,
			@DefaultValue("2.0.0") @QueryParam("version") String version, 
			@DefaultValue("") @QueryParam("resourceid") String resourceids, 
			@DefaultValue("") @QueryParam("filter") String filter, 
			@DefaultValue("CQL") @QueryParam("filterLanguage") String filterLanguage,
			@DefaultValue("results") @QueryParam("resultType") String resultType) throws JSONException, XMLStreamException {
		System.out.println(typename);	
		if (typename == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj = curobj;
				break;
			}
		}
		if(workingobj==null)
			throw new NotFoundException();
		String res = "";
		System.out.println(hitCache);
		if(resultType.equalsIgnoreCase("hits") 
				&& hitCache.containsKey(typename.toLowerCase()) 
				&& (hitCache.get(typename.toLowerCase()).getOne().getTime()+ milliesInDays)
				> System.currentTimeMillis()) {
			res=hitCache.get(typename.toLowerCase()).getTwo();
		}else {
			if(!workingobj.has("attcount") && !workingobj.getString("query").contains("?rel") 
					&& !workingobj.getString("query").contains("?val")) {
				featureTypeCache.put(typename.toLowerCase(),TripleStoreConnector
						.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
				  workingobj.getString("name"),workingobj));
				workingobj.put("attcount", 1);
			}else if(!workingobj.has("attcount")) {
				featureTypeCache.put(typename.toLowerCase(),TripleStoreConnector
						.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
				  workingobj.getString("name"),workingobj));		
			}
		try {
			res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
					workingobj.getString("triplestore"),
					output, ""+(Integer.valueOf(count)*workingobj.getInt("attcount")),""
					+(Integer.valueOf(startindex)*workingobj.getInt("attcount")),
					"gml:featureMember",typename,resourceids,workingobj,filter,resultType,srsName);
			System.out.println(res);
			if(res.isEmpty()) {
				throw new NotFoundException();
			}
			if(resultType.equalsIgnoreCase("hits")) {
				hitCache.put(typename.toLowerCase(),new Tuple<Date,String>(new Date(System.currentTimeMillis()),res));
			}
		} catch (JSONException | XMLStreamException e1) {
			e1.printStackTrace();
			return this.createExceptionResponse(e1, "");
		}
		}
		System.out.println(output);
		if(output.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory outputf = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(outputf.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.writeStartElement("wfs:FeatureCollection");
				writer.writeDefaultNamespace((wfsconf.getString("baseurl")+"/").replace("//","/"));
				writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
				writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
				writer.writeNamespace("gml", "http://www.opengis.net/gml");
				writer.writeNamespace("wfs","http://www.opengis.net/wfs");
				writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
				writer.setPrefix("gml", "http://www.opengis.net/gml");
				writer.setPrefix("wfs", "http://www.opengis.net/wfs");
				
				if(resultType.equalsIgnoreCase("hits")) {
					writer.writeAttribute("numberOfFeatures", res);
				}else {
					for(String ns:nameSpaceCache.get(typename.toLowerCase()).keySet()) {
						writer.setPrefix(nameSpaceCache.get(typename.toLowerCase()).get(ns),ns);
						writer.writeNamespace(nameSpaceCache.get(typename.toLowerCase()).get(ns),ns);
					}
					writer.writeCharacters("");
					writer.flush();
					strwriter.write(res);
				}
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			}catch(Exception e) {
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}
		}else if(output.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONObject jsonresult=new JSONObject(res);
			JSONArray features = jsonresult.getJSONArray("features");
			if(jsonresult.has("@context")) {
				result.put("@context",jsonresult.getJSONObject("@context"));
			}			
			result.put("type", "FeatureCollection");
			result.put("links", links);
			result.put("timeStamp", System.currentTimeMillis());
			if(resultType.equalsIgnoreCase("hits")) {
				result.put("numberMatched", res);
			}else {
				result.put("numberMatched", features.length());
			}
			result.put("numberReturned", features.length());
			result.put("features", features);
			System.out.println("EXPORT JSON: "+result.toString());
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		}else if(output.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append(htmlHead);
			builder.append("<body><h1 align=\"center\">");
			builder.append(typename);
			builder.append("</h1>");
			builder.append(res);
			builder.append("<script>$( document ).ready(function() {$('#queryres').DataTable();});</script></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else if(output.contains("csv") || output.contains("geouri") || output.contains("geohash")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}else if(output.contains("gpx")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/getGmlObject")
	public Response getGmlObject(@QueryParam("typename") String typename,
			@QueryParam("GmlObjectId") String gmlobjectid,
			@DefaultValue("4") @QueryParam("traverseXlinkDepth") String traverseXlinkDepth,
			@DefaultValue("gml") @QueryParam("outputFormat") String output) {
		try {
			return this.getFeature(typename,output,"1","0","","ASC","2.0.0", gmlobjectid,"","CQL","");
		} catch (JSONException | XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return this.createExceptionResponse(e, "");
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/getPropertyValue")
	public Response getPropertyValue(@QueryParam("typename") String typename,
			@QueryParam("valuereference") String propertyname,
			@DefaultValue("json") @QueryParam("outputFormat") String output,
			@DefaultValue("") @QueryParam("resourceids") String resourceids,
			@DefaultValue("") @QueryParam("filter") String filter
			,@DefaultValue("0") @QueryParam("count") String count
			,@DefaultValue("results") @QueryParam("resultType") String resultType) {
		System.out.println(typename);	
		System.out.println(propertyname);
		if (typename == null || propertyname==null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj = curobj;
				break;
			}
		}
		if(workingobj==null)
			throw new NotFoundException();
		if(!propertyname.startsWith("http")) {
			if(!featureTypeCache.containsKey(typename.toLowerCase())) {
				featureTypeCache.put(typename.toLowerCase(),TripleStoreConnector
					.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
						workingobj.getString("name"),workingobj));
			}
			for(String key:featureTypeCache.get(typename.toLowerCase()).keySet()) {
				if(key.contains(propertyname)) {
					propertyname=key;
					break;
				}
			}
		}
		String res = ""; 
		try {
			res = TripleStoreConnector.executePropertyValueQuery(
					workingobj.getString("triplestore"),
					output,propertyname, "gml:featureMember",typename,resourceids,workingobj,
					filter,count,resultType,"");
			System.out.println(res);
		} catch (JSONException | XMLStreamException e1) {
			e1.printStackTrace();
			return this.createExceptionResponse(e1, "");
		}
		if(resultType.equalsIgnoreCase("hits")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}			
		if(output!=null && output.contains("json")) {
			return Response.ok(res).type(MediaType.APPLICATION_JSON).build();
		}else if(output!=null && output.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory outputf = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(outputf.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.writeStartElement("ValueCollection");
				writer.writeDefaultNamespace("http://www.opengis.net/wfs/2.0");
				writer.writeCharacters("");
				writer.flush();
				strwriter.write(res);
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
			}catch(Exception e) {
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}	
			return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
		}else if(output!=null && output.contains("html")) {
			StringBuilder builder=new StringBuilder();
			builder.append("<html><head></head><body><h1 align=center>PropertyRequest: "+typename+"["+propertyname+"]</h1>");
			builder.append(res);
			builder.append("<a href=\""+wfsconf.getString("baseurl")+"/?f=html\">Back to LandingPage</a></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		}else if(output!=null) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/addFeatureType")
	public String addFeatureType(@QueryParam("query") String sparqlQuery, 
			@QueryParam("typename")String name, 
			@QueryParam("namespace") String namespace,
			@QueryParam("triplestore") String triplestore,
			@QueryParam("username") String username,
			@QueryParam("password") String password) {
		JSONArray datasets=wfsconf.getJSONArray("datasets");
		JSONObject toadd=new JSONObject();
		toadd.put("name",name);
		toadd.put("namespace",namespace);
		toadd.put("triplestore",triplestore);
		toadd.put("query",sparqlQuery);
		datasets.put(toadd);
		return null;
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/saveFeatureTypes")
	public String saveFeatureTypes(@QueryParam("featjson") String featureTypesJSON, 
			@QueryParam("username") String username,
			@QueryParam("password") String password) {
		return null;
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/featureTypes")
	public String getFeatureTypes() {
		return wfsconf.toString(2);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/prefixes")
	public String prefixes() {
		return TripleStoreConnector.prefixCollection;
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/addPrefixes")
	public String addPrefixes(@QueryParam("query") String sparqlQuery, 
			@QueryParam("typename")String name, 
			@QueryParam("namespace") String namespace, 
			@QueryParam("triplestore") String triplestore) {	
		return wfsconf.toString(2);
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/queryConfigs")
	public Response queryConfigs() {
		JSONObject res=new JSONObject();
		for(int i=0;i<wfsconf.getJSONArray("datasets").length();i++){
			JSONObject curjson=wfsconf.getJSONArray("datasets").getJSONObject(i);
			JSONObject instance=new JSONObject();
			if(!res.has(curjson.getString("triplestore"))) {
				res.put(curjson.getString("triplestore"),new JSONArray());
			}
			res.getJSONArray(curjson.getString("triplestore")).put(instance);
			instance.put("triplestore",curjson.getString("triplestore"));
			instance.put("query",curjson.getString("query"));
			instance.put("name",curjson.getString("name"));
		}
		return Response.ok(res.toString(2)).type(MediaType.APPLICATION_JSON).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/queryservice")
    public String queryService(@QueryParam("query") String query,
    		@QueryParam("endpoint") String endpoint) { 
		final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir); 
		return TripleStoreConnector.executeQuery(query,endpoint,false);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/queryservicegeojson")
    public String queryService(@QueryParam("query") String query,@QueryParam("endpoint") String endpoint,
    		@QueryParam("geojson") String geojson) { 
		final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir); 
		return TripleStoreConnector.executeQuery(query,endpoint,true);
	}
	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/transaction")
	public String transaction() {
		return null;
	}

	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/lockFeature")
	public String lockFeature() {
		return null;
	}

}
