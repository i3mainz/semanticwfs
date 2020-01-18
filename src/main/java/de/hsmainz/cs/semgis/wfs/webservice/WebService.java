package de.hsmainz.cs.semgis.wfs.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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

import de.hsmainz.cs.semgis.wfs.triplestore.TripleStoreConnector;

@Path("/")
public class WebService {

	JSONObject triplestoreconf = new JSONObject();

	JSONObject wfsconf = new JSONObject();

	public WebService() throws IOException {
		String text2 = new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8);
		wfsconf = new JSONObject(text2);
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs")
	public Response entryPoint( @DefaultValue("WFS") @QueryParam("SERVICE") String service,
			@DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@DefaultValue("2.0.0") @QueryParam("VERSION") String version,
			@DefaultValue("") @QueryParam("TYPENAME") String typename,
			@DefaultValue("") @QueryParam("SRSNAME") String srsname,
			@DefaultValue("") @QueryParam("BBOX") String bbox,
			@DefaultValue("") @QueryParam("SORTBY") String sortby,
			@DefaultValue("0") @QueryParam("STARTINDEX") String startindex,
			@DefaultValue("") @QueryParam("FILTER") String filter,
			@DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output,
			@DefaultValue("5") @QueryParam("COUNT") String count) {
			System.out.println("Request: "+request);
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
						return this.getFeature(typename, output, count,version);
					} catch (XMLStreamException e) {
						e.printStackTrace();
						return Response.ok("").type(MediaType.TEXT_PLAIN).build();
					}
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
			result.put("links", links);
			result.put("collections", collections);
			for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
				JSONObject coll = new JSONObject();
				JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
				coll.put("id", curobj.getString("name"));
				coll.put("title", curobj.getString("name"));
				coll.put("extent", new JSONObject());
				JSONArray colinks = new JSONArray();
				JSONObject link = new JSONObject();
				link.put("rel", "items");
				link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + curobj.getString("name")
						+ "/items?f=json");
				link.put("type", "application/json");
				link.put("title", curobj.getString("name"));
				colinks.put(link);
				link = new JSONObject();
				link.put("rel", "alternate");
				link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + curobj.getString("name")
						+ "/items?f=html");
				link.put("type", "text/html");
				link.put("title", curobj.getString("name"));
				colinks.put(link);
				coll.put("links", colinks);
				collections.put(coll);
			}
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
				writer.writeStartElement("Collections");
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
				for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
					JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
					writer.writeStartElement("Collection");
					writer.writeStartElement("Id");
					writer.writeCharacters(curobj.getString("name"));
					writer.writeEndElement();
					writer.writeStartElement("Title");
					writer.writeCharacters(curobj.getString("name"));
					writer.writeEndElement();
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					writer.writeAttribute("rel", "items");
					writer.writeAttribute("title", curobj.getString("name"));
					writer.writeAttribute("type", "application/geo+json");
					writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
							+ curobj.getString("name") + "/items?f=json");
					writer.writeEndElement();
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					writer.writeAttribute("rel", "items");
					writer.writeAttribute("title", curobj.getString("name"));
					writer.writeAttribute("type",
							"application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
					writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
							+ curobj.getString("name")
							+ "/items?f=application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
					writer.writeEndElement();
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					writer.writeAttribute("rel", "alternate");
					writer.writeAttribute("title", curobj.getString("name"));
					writer.writeAttribute("type", "text/html");
					writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
							+ curobj.getString("name") + "/items?f=text/html");
					writer.writeEndElement();
					writer.writeEndElement();
				}
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.TEXT_XML).build();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
		} else if (format == null || format.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append("<html><head></head><body>");
			builder.append("<h1>");
			builder.append("FeatureCollection View");
			builder.append("</h1>");
			builder.append("<ul>");
			for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
				JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
				builder.append("<li>" + curobj.getString("name"));
				builder.append(" <a href=\"" + this.wfsconf.getString("baseurl") + "/collections/"
						+ curobj.getString("name") + "/items?f=json\">[JSON]</a>");
				builder.append(" <a href=\"" + this.wfsconf.getString("baseurl") + "/collections/"
						+ curobj.getString("name") + "/items?f=gml\">[GML]</a>");
				builder.append(" <a href=\"" + this.wfsconf.getString("baseurl") + "/collections/"
						+ curobj.getString("name") + "/items?f=text/html\">[HTML]</a></li>");
			}
			builder.append("</ul></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else {
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/items/{featureid}")
	public Response getFeatureById(@PathParam("collectionid") String collectionid,
			@PathParam("featureid") String featureid, 
			@DefaultValue("json") @QueryParam("f") String format) {
		System.out.println("Featureid");
		if (collectionid == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(collectionid)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj == null) {
			throw new NotFoundException();
		}
		String query=workingobj.getString("query");
		query=query.replace("WHERE{","WHERE{ BIND( <"+workingobj.getString("namespace")+featureid+"> AS ?"+workingobj.getString("indvar")+") ");
		System.out.println("?"+workingobj.getString("indvar")+" - "+"<"+workingobj.getString("namespace")+featureid+">");
		System.out.println(query);
		String res = "";
		try {
			res = TripleStoreConnector.executeQuery(query, workingobj.getString("triplestore"),
					format, "1","0","sf:featureMember");
			System.out.println(res);
		} catch (JSONException | XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			result.put("links", links);
			JSONObject link = new JSONObject();
			link.put("rel", "self");
			link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items/" + featureid
					+ "?f=json");
			link.put("type", "application/geo+json");
			link.put("title", featureid);
			links.put(link);
			link = new JSONObject();
			link.put("rel", "alternate");
			link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items/" + featureid
					+ "?f=html");
			link.put("type", "text/html");
			link.put("title", featureid);
			links.put(link);
			link = new JSONObject();
			link.put("rel", "collection");
			link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items/?f=json");
			link.put("type", "application/json");
			link.put("title", featureid);
			links.put(link);
			result.put("id", featureid);
			JSONObject features = new JSONObject(res).getJSONArray("features").getJSONObject(0);
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
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "self");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type", "application/geo+json");
				writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name") + "/items/+" + featureid + "?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type",
						"application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
				writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name") + "/items/+" + featureid
						+ "?f=application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name") + "/items/+" + featureid + "?f=text/html");
				writer.writeEndElement();
				strwriter.append(res);
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				e.printStackTrace();
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
		} else if (format == null || format.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append("<html><head><link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.css\"/><link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\"/><script src=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.js\"></script><script src=\"https://code.jquery.com/jquery-3.4.1.min.js\"></script><script src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script></head><body>");
			builder.append("<h1 align=\"center\">");
			builder.append(featureid);
			builder.append("</h1>");
			builder.append("<ul>");
			builder.append(res);
			builder.append("</ul></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		}else if (format == null || format.contains("geouri")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}else if (format == null || format.contains("csv")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		} else {
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/landingPage")
	public Response landingPage(@DefaultValue("json") @QueryParam("f") String format) {
		if (format == null || format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONObject link = new JSONObject();
			link.put("href", this.wfsconf.getString("baseurl"));
			link.put("rel", "self");
			link.put("type", "application/json");
			link.put("title", "this document");
			links.put(link);
			result.put("title", this.wfsconf.getString("servicetitle"));
			result.put("description", this.wfsconf.getString("servicedescription"));
			result.put("links", links);
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else {
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
				writer.writeCharacters(this.wfsconf.getString("servicetitle"));
				writer.writeEndElement();
				writer.writeStartElement("Description");
				writer.writeCharacters(this.wfsconf.getString("servicedescription"));
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "self");
				writer.writeAttribute("title", this.wfsconf.getString("servicetitle"));
				writer.writeAttribute("type", "application/geo+json");
				writer.writeAttribute("href", this.wfsconf.getString("baseurl"));
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				return Response.ok(strwriter.toString()).type(MediaType.TEXT_PLAIN).build();
			}

		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}")
	public Response collectionInformation(@PathParam("collectionid") String collectionid,
			@DefaultValue("json") @QueryParam("f") String format, @QueryParam("limit") String limit,
			@QueryParam("bbox") String bbox) {
		if (collectionid == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(collectionid)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj == null) {
			throw new NotFoundException();
		}
		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			result.put("id", workingobj.getString("name"));
			result.put("title", workingobj.getString("name"));
			result.put("description", "");
			result.put("extent", new JSONObject());
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
				writer.writeStartElement("gml:boundedBy");
				writer.writeEndElement();
				writer.writeStartElement("sf:featureMember");
				writer.writeEndElement();
				writer.writeStartElement("Title");
				writer.writeCharacters(workingobj.getString("name"));
				writer.writeEndElement();
				writer.writeStartElement("Description");
				writer.writeCharacters("");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "self");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type", "application/geo+json");
				writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name") + "/items?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type",
						"application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
				writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name")
						+ "/items?f=application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name") + "/items?f=text/html");
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
		}else if(format == null || format.contains("html")){
			StringBuilder builder=new StringBuilder();
			builder.append("<html><head><link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\"/><script src=\"https://code.jquery.com/jquery-3.4.1.min.js\"></script><script src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script></head><body>");
			builder.append("<h1 align=\"center\">");
			builder.append(collectionid);
			builder.append("</h1>Serializations:<ul>");
			builder.append("<li><a href=\""+this.wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name") + "/items?f=json"+"\">[GeoJSON]</a></li>");
			builder.append("<li><a href=\""+this.wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+ "/items?f=application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0"+"\">[GML]</a></li>");
			builder.append("</ul></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else {
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/items")
	public Response collectionItems(@PathParam("collectionid") String collectionid,
			@DefaultValue("json") @QueryParam("f") String format, @DefaultValue("-1") @QueryParam("limit") String limit,
			@DefaultValue("0") @QueryParam("offset") String offset,
			@QueryParam("bbox") String bbox, @QueryParam("datetime") String datetime) {
		if (collectionid == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(collectionid)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj == null) {
			throw new NotFoundException();
		}
		System.out.println(limit);
		try {
			String res;
			if(limit.equals("-1") && offset.equals("0")) {
				res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
						workingobj.getString("triplestore"), format,"sf:featureMember");
			}else {
				res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
						workingobj.getString("triplestore"), format,limit,offset,"sf:featureMember");
			}
			System.out.println(res);
			if (format != null && format.contains("json")) {
				JSONObject result = new JSONObject();
				JSONArray links = new JSONArray();
				JSONObject link = new JSONObject();
				link.put("rel", "self");
				link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items?f=json");
				link.put("type", "application/geo+json");
				link.put("title", collectionid);
				links.put(link);
				link = new JSONObject();
				link.put("rel", "alternate");
				link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items?f=html");
				link.put("type", "text/html");
				link.put("title", collectionid);
				links.put(link);
				link = new JSONObject();
				link.put("rel", "collection");
				link.put("href", this.wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items?f=json");
				link.put("type", "application/json");
				link.put("title", collectionid);
				links.put(link);
				JSONArray features = new JSONObject(res).getJSONArray("features");
				result.put("type", "FeatureCollection");
				result.put("links", links);
				result.put("timeStamp", System.currentTimeMillis());
				result.put("numberMatched", features.length());
				result.put("numberReturned", features.length());
				result.put("features", features);
				return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
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
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					writer.writeAttribute("rel", "self");
					writer.writeAttribute("title", workingobj.getString("name"));
					writer.writeAttribute("type", "application/geo+json");
					writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name") + "/items/?f=json");
					writer.writeEndElement();
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					writer.writeAttribute("rel", "alternate");
					writer.writeAttribute("title", workingobj.getString("name"));
					writer.writeAttribute("type","application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
					writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name")+ "/items?f=application/gml+xml;version=3.2;profile=http://www.opengis.net/def/profile/ogc/2.0/gml-sf0");
					writer.writeEndElement();
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					writer.writeAttribute("rel", "alternate");
					writer.writeAttribute("title", workingobj.getString("name"));
					writer.writeAttribute("type", "text/html");
					writer.writeAttribute("href", this.wfsconf.getString("baseurl") + "/collections/"+ workingobj.getString("name") + "/items?f=text/html");
					writer.writeEndElement();
					strwriter.append(res);
					writer.writeEndElement();
					writer.writeEndDocument();
					writer.flush();
					return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
				} catch (XMLStreamException e) {
					e.printStackTrace();
					// TODO Auto-generated catch block
					return Response.ok("").type(MediaType.TEXT_PLAIN).build();
				}
			} else if (format == null || format.contains("html")) {
				StringBuilder builder = new StringBuilder();
				builder.append("<html><head><link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.css\"/><link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\"/><script src=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.js\"></script><script src=\"https://code.jquery.com/jquery-3.4.1.min.js\"></script><script src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script></head><body>");
				builder.append("<h1 align=\"center\">");
				builder.append(collectionid);
				builder.append("</h1>");
				builder.append(res);
				builder.append("<script>$( document ).ready(function() {$('#queryres').DataTable();});</script></body></html>");
				return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
			} else {
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
		} catch (JSONException | XMLStreamException e1) {
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
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
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
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				e.printStackTrace();
				// TODO Auto-generated catch block
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
		}else if(format == null || format.contains("html")) {
			StringBuilder builder=new StringBuilder();
			builder.append("<html><head></head><body><h1>Conformance</h1><ul>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core\">Core</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30\">Oas30</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html\">HTML</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson\">GeoJSON</a></li>");
			builder.append("<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0\">GMLSf0</a></li>");
			builder.append("</ul></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		}else {
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
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

	public Response constructCapabilities(String version,String versionnamespace) throws XMLStreamException {
		String serviceType = "WFS";
		String owsns="http://www.opengis.net/ows/1.1";
		if("1.0.0".equals(version) || "1.1.0".equals(version)) {
			versionnamespace="";
			if("1.0.0".equals(version))
				owsns="http://www.opengis.net/wfs"+versionnamespace;
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
		writer.writeCharacters(wfsconf.has("title") ? wfsconf.getString("title") : "");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Fees");
		writer.writeCharacters("NONE");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Abstract");
		writer.writeCharacters(wfsconf.has("abstract") ? wfsconf.getString("abstract") : "");
		writer.writeEndElement();
		writer.writeEndElement();
		// ServiceProvider
		// Capabilities
		// writer.writeStartElement("http://www.opengis.net/wfs/2.0","Capability");
		// writer.writeStartElement("http://www.opengis.net/wfs/2.0","Request");
		// writer.writeStartElement("http://www.opengis.net/wfs/2.0","GetCapabilities");
		// OperationsMetadata
		writer.writeStartElement(owsns, "OperationsMetadata");
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "GetCapabilities");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", SERVERURL);
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
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "GetFeature");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", SERVERURL);
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
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			describeFeatureType(writer, this.wfsconf.getJSONArray("datasets").getJSONObject(i),versionnamespace,version);
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "Filter_Capabilities");
		describeSpatialCapabilities(writer,versionnamespace);
		writer.writeEndElement();
		writer.writeEndElement();
		// writer.writeEndElement();
		// writer.writeEndElement();
		// writer.writeEndElement();
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
		writer.writeStartElement("http://www.opengis.net/wfs"+versionnamespace, "Format");
		writer.writeCharacters("application/vnd.geo+json");
		writer.writeEndElement();
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
		// writer.writeStartElement("sequence");
		/*
		 * Map<String,String>
		 * mapping=TripleStoreConnector.getFeatureTypeInformation(featuretype.getString(
		 * "query"), featuretype.getString("triplestore"),
		 * featuretype.getString("name")); for(String elem:mapping.keySet()) {
		 * writer.writeStartElement("element"); writer.writeAttribute("name", elem);
		 * writer.writeAttribute("type", mapping.get(elem)); writer.writeEndElement(); }
		 */
		// writer.writeEndElement();
		// writer.writeStartElement(localName);
		// writer.writeAttribute(localName, value);
	}

	public void describeSpatialCapabilities(XMLStreamWriter writer,String versionnamespace) throws XMLStreamException {
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialCapabilities");
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "GeometryOperands");
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Box");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Envelope");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Point");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:LineString");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Curve");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "GeometryOperand");
		writer.writeAttribute("name", "gml:Polygon");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperators");
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "BBOX");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Intersects");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Contains");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Crosses");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Touches");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Within");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Overlaps");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Disjoint");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "Equals");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/"+versionnamespace, "SpatialOperator");
		writer.writeAttribute("name", "DWithin");
		writer.writeEndElement();
		writer.writeEndElement();
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs/describeFeatureType")
	public Response describeFeatureType(@QueryParam("typename") String typename,@DefaultValue("version") @QueryParam("version")String version) throws XMLStreamException {
		if(typename==null)
			throw new NotFoundException();
		JSONObject workingobj = null;
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
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
		writer.writeDefaultNamespace("http://www.w3.org/2001/XMLSchema");
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
		writer.writeStartElement("sequence");
		Map<String,String>mapping=TripleStoreConnector
				.getFeatureTypeInformation(workingobj.getString("query"), workingobj.getString("triplestore"),
		  workingobj.getString("name")); 
		for(String elem:mapping.keySet()) {
			writer.writeStartElement("element"); writer.writeAttribute("name", elem);
			writer.writeAttribute("type", mapping.get(elem)); 
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
			@DefaultValue("2.0.0") @QueryParam("version") String version) throws JSONException, XMLStreamException {
		System.out.println(typename);	
		if (typename == null) {
			throw new NotFoundException();
		}
		JSONObject workingobj = null;
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj = curobj;
				break;
			}
		}
		if(workingobj==null)
			throw new NotFoundException();
		String res = "";
		try {
			res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
					workingobj.getString("triplestore"),
					output, "1","0","gml:featureMember");
			System.out.println(res);
		} catch (JSONException | XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
				writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
				writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
				writer.writeNamespace("gml", "http://www.opengis.net/gml");
				writer.writeNamespace("wfs","http://www.opengis.net/wfs");
				writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
				writer.setPrefix("gml", "http://www.opengis.net/gml");
				writer.setPrefix("wfs", "http://www.opengis.net/wfs");
				writer.writeCharacters("");
				writer.flush();
				strwriter.write(res);
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			}catch(Exception e) {
				e.printStackTrace();
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
		}else if(output.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONArray features = new JSONObject(res).getJSONArray("features");
			result.put("type", "FeatureCollection");
			result.put("links", links);
			result.put("timeStamp", System.currentTimeMillis());
			result.put("numberMatched", features.length());
			result.put("numberReturned", features.length());
			result.put("features", features);
			System.out.println("EXPORT JSON: "+result.toString());
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		}else if(output.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append("<html><head><link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.css\"/><link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\"/><script src=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.js\"></script><script src=\"https://code.jquery.com/jquery-3.4.1.min.js\"></script><script src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script></head><body>");
			builder.append("<h1 align=\"center\">");
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
	public String getGmlObject() {
		return null;
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/getPropertyValue")
	public String getPropertyValue() {
		return null;
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/addFeatureType")
	public String getGmlObject(@QueryParam("query") String sparqlQuery, 
			@QueryParam("typename")String name, 
			@QueryParam("namespace") String namespace,
			@QueryParam("triplestore") String triplestore,
			@QueryParam("username") String username,
			@QueryParam("password") String password) {
		return null;
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/saveFeatureTypes")
	public String getGmlObject(@QueryParam("featjson") String featureTypesJSON, 
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

	public String transaction() {
		return null;
	}

	public String lockFeature() {
		return null;
	}

}
