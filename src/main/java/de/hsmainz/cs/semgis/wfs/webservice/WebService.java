package de.hsmainz.cs.semgis.wfs.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	@Path("/service")
	public String entryPoint(@QueryParam("SERVICE") String service, @QueryParam("REQUEST") String request,
			@QueryParam("VERSION") String version) {
		if (service.equalsIgnoreCase("WFS")) {
			if ("getCapabilities".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilities();
				} catch (XMLStreamException e) {
					return "";
				}
			}
			if ("describeFeatureType".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilities();
				} catch (XMLStreamException e) {
					return "";
				}
			}
			if ("getFeature".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilities();
				} catch (XMLStreamException e) {
					return "";
				}
			}
		}
		return "";
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections")
	public Response collections(@DefaultValue("json") @QueryParam("f") String format) {
		System.out.println(format);
		if (format == null || format.contains("json")) {
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
		} else if (format.contains("gml")) {
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
		} else if (format.contains("html")) {
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
			@PathParam("featureid") String featureid, @DefaultValue("json") @QueryParam("f") String format) {
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
		String res = "";
		try {
			res = TripleStoreConnector.executeQuery(workingobj.getString("query"), workingobj.getString("triplestore"),
					format, "1");
			System.out.println(res);
		} catch (JSONException | XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (format == null || format.contains("json")) {
			JSONObject result = new JSONObject();
			result.put("type", "Feature");
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
			result.put("id", 0);
			result.put("geometry", new JSONObject());
			result.put("properties", new JSONObject());
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
				writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
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
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				e.printStackTrace();
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
		} else if (format.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append("<html><head></head><body>");
			builder.append("<h1>");
			builder.append(featureid);
			builder.append("</h1>");
			builder.append("<ul>");
			builder.append(res);
			builder.append("</ul></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
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
		if (format == null || format.contains("json")) {
			JSONObject result = new JSONObject();
			result.put("id", workingobj.getString("name"));
			result.put("title", workingobj.getString("name"));
			result.put("description", "");
			result.put("extent", new JSONObject());
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (format.contains("gml")) {
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
		} else {
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/items")
	public Response collectionItems(@PathParam("collectionid") String collectionid,
			@DefaultValue("json") @QueryParam("f") String format, @DefaultValue("-1") @QueryParam("limit") String limit,
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
			if(limit.equals("-1")) {
				res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
						workingobj.getString("triplestore"), format);
			}else {
				res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
						workingobj.getString("triplestore"), format,limit);
			}
			System.out.println(res);
			if (format == null || format.contains("json")) {
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
			} else if (format.contains("gml")) {
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
					writer.writeStartElement("sf:featureMember");
					writer.writeEndElement();
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
					e.printStackTrace();
					// TODO Auto-generated catch block
					return Response.ok("").type(MediaType.TEXT_PLAIN).build();
				}
			} else if (format.contains("html")) {
				StringBuilder builder = new StringBuilder();
				builder.append("<html><head><link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\"/><script src=\"https://code.jquery.com/jquery-3.4.1.min.js\"></script><script src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script></head><body>");
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
		if (format == null || format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray conforms = new JSONArray();
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson");
			conforms.put("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0");
			result.put("conformsTo", conforms);
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else {
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
		}

	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/hello")
	public String helloWorld() {
		return "HelloWorld";
	}

	public String SERVICETYPEVERSION = "2.0.0";

	public String SERVERURL = "http://localhost:8080/RESTfulExample/rest/service?";

	public String constructCapabilities() throws XMLStreamException {
		String serviceType = "WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter = new StringWriter();
		XMLStreamWriter xmlwriter = factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("WFS_Capabilities");
		writer.writeDefaultNamespace("http://www.opengis.net/wfs/2.0");
		writer.writeAttribute("version", "2.0.0");
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs/2.0");
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes/2.0");
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
		// ServiceInformation
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "ServiceIdentification");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "ServiceType");
		writer.writeAttribute("codeSpace", "OGC");
		writer.writeCharacters(serviceType);
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "ServiceTypeVersion");
		writer.writeCharacters(SERVICETYPEVERSION);
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Title");
		writer.writeCharacters(wfsconf.has("title") ? wfsconf.getString("title") : "");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Fees");
		writer.writeCharacters("NONE");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Abstract");
		writer.writeCharacters(wfsconf.has("abstract") ? wfsconf.getString("abstract") : "");
		writer.writeEndElement();
		writer.writeEndElement();
		// ServiceProvider
		// Capabilities
		// writer.writeStartElement("http://www.opengis.net/wfs/2.0","Capability");
		// writer.writeStartElement("http://www.opengis.net/wfs/2.0","Request");
		// writer.writeStartElement("http://www.opengis.net/wfs/2.0","GetCapabilities");
		// OperationsMetadata
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "OperationsMetadata");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Operation");
		writer.writeAttribute("name", "GetCapabilities");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "DCP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "HTTP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", SERVERURL);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Parameter");
		writer.writeAttribute("name", "AcceptVersions");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "AllowedValues");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Value");
		writer.writeCharacters("2.0.0");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Operation");
		writer.writeAttribute("name", "GetFeature");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "DCP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "HTTP");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", SERVERURL);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Parameter");
		writer.writeAttribute("name", "AcceptVersions");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "AllowedValues");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "Value");
		writer.writeCharacters("2.0.0");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0", "FeatureTypeList");
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			describeFeatureType(writer, this.wfsconf.getJSONArray("datasets").getJSONObject(i));
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "Filter_Capabilities");
		describeSpatialCapabilities(writer);
		writer.writeEndElement();
		writer.writeEndElement();
		// writer.writeEndElement();
		// writer.writeEndElement();
		// writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return strwriter.toString();
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/service/getCapabilities")
	public String getCapabilities() throws XMLStreamException {
		return constructCapabilities();
	}

	public void describeFeatureType(XMLStreamWriter writer, JSONObject featuretype) throws XMLStreamException {
		writer.writeStartElement("http://www.opengis.net/wfs/2.0", "FeatureType");
		writer.writeStartElement("http://www.opengis.net/wfs/2.0", "Name");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0", "Title");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0", "DefaultCRS");
		writer.writeCharacters("urn:ogc:def:crs:EPSG::4326");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs/2.0", "OutputFormats");
		writer.writeStartElement("http://www.opengis.net/wfs/2.0", "Format");
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

	public void describeSpatialCapabilities(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialCapabilities");
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "GeometryOperands");
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "GeometryOperand");
		writer.writeAttribute("name", "gml:Box");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "GeometryOperand");
		writer.writeAttribute("name", "gml:Envelope");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "GeometryOperand");
		writer.writeAttribute("name", "gml:Point");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "GeometryOperand");
		writer.writeAttribute("name", "gml:LineString");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "GeometryOperand");
		writer.writeAttribute("name", "gml:Curve");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "GeometryOperand");
		writer.writeAttribute("name", "gml:Polygon");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperators");
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "BBOX");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Intersects");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Contains");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Crosses");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Touches");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Within");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Overlaps");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Disjoint");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "Equals");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes/2.0", "SpatialOperator");
		writer.writeAttribute("name", "DWithin");
		writer.writeEndElement();
		writer.writeEndElement();
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/service/describeFeatureType")
	public String describeFeatureType(@QueryParam("typename") String typename) throws XMLStreamException {
		String serviceType = "WFS";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter = new StringWriter();
		XMLStreamWriter xmlwriter = factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("schema");
		JSONObject workingobj = null;
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj = curobj;
				break;
			}
		}
		if (workingobj != null)
			this.describeFeatureType(writer, workingobj);
		writer.writeEndElement();
		writer.writeEndDocument();
		return strwriter.toString();
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/getFeature")
	public String getFeature(@QueryParam("typename") String typename, @QueryParam("outputFormat") String output,
			@QueryParam("count") String count) throws JSONException, XMLStreamException {
		JSONObject workingobj = null;
		for (int i = 0; i < this.wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curobj = this.wfsconf.getJSONArray("datasets").getJSONObject(i);
			if (curobj.getString("name").equalsIgnoreCase(typename)) {
				workingobj = curobj;
				break;
			}
		}
		return TripleStoreConnector.executeQuery(workingobj.getString("query"), workingobj.getString("triplestore"),
				output, count);
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/getGmlObject")
	public String getGmlObject() {
		return null;
	}

	public String transaction() {
		return null;
	}

	public String lockFeature() {
		return null;
	}

}
