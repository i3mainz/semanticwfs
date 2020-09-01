package de.hsmainz.cs.semgis.wfs.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.resultformatter.HTMLFormatter;
import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultmetadataformatter.ResultMetadataFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;
import de.hsmainz.cs.semgis.wfs.triplestore.TripleStoreConnector;
import de.hsmainz.cs.semgis.wfs.util.Tuple;
import de.hsmainz.cs.semgis.wfs.util.user.User;
import de.hsmainz.cs.semgis.wfs.util.user.UserManagementConnection;
import de.hsmainz.cs.semgis.wfs.util.user.UserType;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


/**
 * Implements OGC API Features and WFS webservice functionality.
 *
 */
@OpenAPIDefinition(
		 info = @Info(
				    title = "SemanticWFS API",
				    version = "1.0",
				    description = "The SemanticWFS API provides access to geodata, its metadata and styles which have been stored in a triple store",
				    contact = @Contact(
				      name = "Anonymous User",
				      email = "email@example.com"
				    ),
				    license = @License(
				      url = "https://www.govdata.de/dl-de/by-2-0",
				      name = "Datenlizenz Deutschland"
				    )
				  ),
				  security = {
				    @SecurityRequirement(
				      name = "none"
				    )
}
)
@Path("/")
public class WebService {

	public static JSONObject triplestoreconf = null;

	public static JSONObject wfsconf = null;
	
	public static JSONObject openapi=null;

	public static Map<String, Map<String, String>> featureTypeCache = new TreeMap<>();

	public static Map<String, Map<String, String>> nameSpaceCache = new TreeMap<>();

	public static Map<String, Tuple<Date, String>> hitCache = new TreeMap<>();

	public static Map<String, Map<String, StyleObject>> styleCache = new TreeMap<>();

	public static Map<String, Double[]> bboxCache = new TreeMap<>();

	public static long milliesInDays = 24 * 60 * 60 * 1000;

	public XMLStreamWriter xmlwriter;

	String htmlHead;

	/**
	 * Constructor for this class.
	 * Loads configuration files and performs initializations.
	 * @throws IOException on error
	 */
	public WebService() throws IOException {
		if (triplestoreconf == null) {
			triplestoreconf = new JSONObject(new String(Files.readAllBytes(Paths.get("triplestoreconf.json")), StandardCharsets.UTF_8));
			System.out.println(triplestoreconf);
		}
		if (wfsconf == null) {
			try {
				System.out.println(new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8));
				System.out.println(new JSONObject(new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8)));
			wfsconf = new JSONObject(new String(Files.readAllBytes(Paths.get("wfsconf.json")), StandardCharsets.UTF_8));
			}catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println(wfsconf);
			System.out.println("in");
		}
		System.out.println("out");
		for (Integer i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject featuretype = wfsconf.getJSONArray("datasets").getJSONObject(i);
			System.out.println("Featuretype: "+featuretype);
			if (!bboxCache.containsKey(featuretype.getString("name").toLowerCase())) {
				try {
				bboxCache.put(featuretype.getString("name").toLowerCase(),
						TripleStoreConnector.getBoundingBoxFromTripleStoreData(featuretype.getString("triplestore"),
								featuretype.getString("query")));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		htmlHead = "<html><head><link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.css\"\r\n"
				+ "   integrity=\"sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ==\"\r\n"
				+ "   crossorigin=\"\"/>\r\n" + "<script src=\"" + wfsconf.getString("baseurl")
				+ "/config/js/proj4.js\"></script>" + "<script src=\"" + wfsconf.getString("baseurl")
				+ "/config/js/prefixes.js\"></script>"
				+ " <link rel=\"stylesheet\" href=\""+ wfsconf.getString("baseurl")+"/config/css/leaflet_legend.css\" />"
				+ " <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css\" integrity=\"sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb\" crossorigin=\"anonymous\">"
				+ " <link rel=\"stylesheet\" href=\""+wfsconf.getString("baseurl")+"/config/css/style.css\">"
				+ "<script src=\"https://unpkg.com/leaflet@1.5.1/dist/leaflet.js\"\r\n"
				+ "   integrity=\"sha512-GffPMF3RvMeYyc1LWMHtK8EbPv0iNZ8/oTtHPx9/cc2ILxQ+u905qIwdpULaqDkyBKgOaB57QTMg7ztg8Jm2Og==\"\r\n"
				+ "   crossorigin=\"\"></script><link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\"/>\r\n"
				+ "<script src=\"" + wfsconf.getString("baseurl") + "/config/js/leaflet.pattern.js\"></script>\r\n"
				+ "<script src=\"" + wfsconf.getString("baseurl")
				+ "/config/js/Leaflet.geojsoncss.min.js\"></script>\r\n"
								+ "<script src=\"" + wfsconf.getString("baseurl")
				+ "/config/js/leaflet_legend.js\"></script>\r\n"
				+ "<script src=\"https://code.jquery.com/jquery-3.4.1.min.js\"></script>\r\n"
				+ "<script src=\"https://unpkg.com/leaflet.vectorgrid@latest/dist/Leaflet.VectorGrid.bundled.js\"></script>\r\n"
				+ "<script src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script>\r\n"
				+ "<script src='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/Leaflet.fullscreen.min.js'></script>\r\n"
				+ "<link href='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/leaflet.fullscreen.css' rel='stylesheet' /></head>";
	}
				
	@GET
	@Produces({MediaType.APPLICATION_JSON,"application/vnd.oai.openapi+json;version=3.0"})
	@Path("/openapi")
	public Response openapiJSON() {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet request = new HttpGet(wfsconf.get("baseurl")+"/openapi.json");
		CloseableHttpResponse response;
		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			response.close();
			httpClient.close();
			return Response.ok(result).type(MediaType.APPLICATION_JSON).build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.ok(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
	}
	
	@POST
	@Produces("application/yaml")
	@Path("/openapi")
	public Response openapiYAML() {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet request = new HttpGet(wfsconf.get("baseurl")+"/openapi.yaml");
		CloseableHttpResponse response;
		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			response.close();
			httpClient.close();
			return Response.ok(result).type("application/yaml").build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.ok(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
	}
	
	/**
	 * Generates OpenAPI definitions of the SemanticWFS services for the use with OGC API Features.
	 */
	public void generateOpenAPIDefinitions() {
		JSONObject defs=new JSONObject();
		for (Integer i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject featuretype = wfsconf.getJSONArray("datasets").getJSONObject(i);
			JSONObject curobj=new JSONObject();
			defs.put("/collections/"+featuretype.getString("name"), curobj);
			JSONObject get=new JSONObject();
			curobj.put("get", get);
			get.put("summary", featuretype.getString("description"));
			get.put("description", featuretype.getString("description"));
			get.put("operationId", featuretype.getString("name"));
			JSONObject parameters=new JSONObject();
			get.put("parameters", parameters);
		}
	}

	
	/**
	 * Generates an exception response. 
	 * @param e The exception to get the message from
	 * @param format The return format
	 * @return The exception response
	 */
	public Response createExceptionResponse(Exception e, String format) {
		if (format == null || format.equals("gml")) {
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
			} catch (XMLStreamException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return Response.ok("").type(MediaType.APPLICATION_XML).build();
			}
			return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
		}
		return Response.ok("").type(MediaType.APPLICATION_XML).build();
	}

	/**
	 * Entrypoint implementation for OGC API Features for POST requests.
	 * @param service
	 * @param request
	 * @param version
	 * @param typename
	 * @param typenames
	 * @param srsName
	 * @param exceptions
	 * @param bbox
	 * @param propertyname
	 * @param sortBy
	 * @param style
	 * @param resultType
	 * @param resourceids
	 * @param gmlobjectid
	 * @param startindex
	 * @param filter
	 * @param filterLanguage
	 * @param output
	 * @param count
	 * @return
	 */
	@POST
	@Produces(MediaType.TEXT_XML)
	@Path("/post/wfs")
	@Operation(
            summary = "Entrypoint implementation for OGC API Features for POST requests",
            description = "Entrypoint implementation for OGC API Features for POST requests")
	public Response entryPointPOST(
			@Parameter(description="Service type definition") @DefaultValue("WFS") @QueryParam("SERVICE") String service,
			@Parameter(description="Request definition") @DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@Parameter(description="Version of the web service") @DefaultValue("2.0.0") @QueryParam("VERSION") String version,
			@Parameter(description="The feature type name to be queried") @DefaultValue("") @QueryParam("TYPENAME") String typename,
			@Parameter(description="The feature type(s) name(s) to be queried") @DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@Parameter(description="The name of the CRS to be returned") @DefaultValue("") @QueryParam("SRSNAME") String srsName,
			@DefaultValue("gml") @QueryParam("EXCEPTIONS") String exceptions,
			@Parameter(description="A bounding box used for filtering results") @DefaultValue("") @QueryParam("BBOX") String bbox,
			@DefaultValue("") @QueryParam("VALUEREFERENCE") String propertyname,
			@Parameter(description="Sorting order definition") @DefaultValue("ASC") @QueryParam("SORTBY") String sortBy,
			@DefaultValue("") @QueryParam("STYLES") String style,
			@DefaultValue("results") @QueryParam("RESULTTYPE") String resultType,
			@DefaultValue("") @QueryParam("RESOURCEID") String resourceids,
			@DefaultValue("") @QueryParam("GMLOBJECTID") String gmlobjectid,
			@DefaultValue("0") @QueryParam("STARTINDEX") String startindex,
			@DefaultValue("") @QueryParam("FILTER") String filter,
			@DefaultValue("") @QueryParam("FILTERLANGUAGE") String filterLanguage,
			@DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output,
			@DefaultValue("5") @QueryParam("COUNT") String count) {
		return entryPoint(service, request, version, typename, typenames, srsName, exceptions, bbox, propertyname,
				sortBy, style, resultType, resourceids, gmlobjectid, startindex, filter, filterLanguage, output, count);
	}

	/**
	 * Returns a vector tile representation of a given feature type.
	 * @param service
	 * @param request
	 * @param version
	 * @param typename
	 * @param typenames
	 * @param output
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/vectortiles")
	public Response vectorTiles(@DefaultValue("CSW") @QueryParam("SERVICE") String service,
			@DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@DefaultValue("2.0.2") @QueryParam("VERSION") String version,
			@DefaultValue("") @QueryParam("TYPENAME") String typename,
			@DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output) {
		return null;
	}

	/**
	 * Returns a style information in a given format for a given featuretype and styleid. 
	 * @param service The service descriptor
	 * @param request The requset parameter
	 * @param typename 
	 * @param typenames
	 * @param output
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/collections/{collectionid}/style/{styleid}")
	@Operation(
            summary = "Returns a style information in a given format for a given featuretype and styleid",
            description = "Returns a style information in a given format for a given featuretype and styleid")
	public Response style(
			@Parameter(description="The service type which is addressed") @DefaultValue("WFS") @QueryParam("SERVICE") String service,
			@Parameter(description="Request type of this query") @DefaultValue("GetStyle") @QueryParam("REQUEST") String request,
			@Parameter(description="Feature type names") @DefaultValue("") @QueryParam("TYPENAME") String typename,
			@Parameter(description="Feature type names") @DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@Parameter(description="Outputformat of the style which is returned")@DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output) {
		if (output.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory outputt = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(outputt.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeAttribute("xmlns:sld", "http://www.opengis.net/sld");
				writer.writeAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
				writer.writeAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
				writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/1999/xlink");
				writer.writeAttribute("version", "1.0.0");
				writer.writeStartElement("sld:NamedLayer");
				writer.writeStartElement("sld:Name");
				writer.writeCharacters(typename);
				writer.writeEndElement();
				writer.writeStartElement("sld:StyledLayerDescriptor");
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}

	/**
	 * Gets a style for a given featuretype with a given style identifier.
	 * @param collectionid the featuretype name
	 * @param styleid the styleid
	 * @param format the format in which the style is returned
	 * @return The style as String
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/style/{styleid}")
	@Operation(
            summary = "Gets a style for a given featuretype with a given style identifier",
            description = "Gets a style for a given featuretype with a given style identifier")
	public Response getCollectionStyle(
			@Parameter(description="Feature type name")  @PathParam("collectionid") String collectionid,
			@Parameter(description="Style id") @PathParam("styleid") String styleid, 
			@Parameter(description="Return format")  @DefaultValue("html") @QueryParam("f") String format) {
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
		StyleObject obj = TripleStoreConnector.getStyle(collectionid, styleid, workingobj.getString("triplestore"),
				workingobj.getString("namespace"));
		if (obj == null) {
			throw new NotFoundException();
		}
		if (format.contains("json")) {
			return Response.ok(obj.toJSON()).type(MediaType.APPLICATION_JSON).build();
		} else if (format.contains("xml")) {
			return Response.ok(obj.toXML()).type(MediaType.APPLICATION_XML).build();
		} else if (format.contains("html")) {
			return Response.ok(obj.toHTML()).type(MediaType.TEXT_HTML).build();
		}
		return Response.ok(obj.toString()).type(MediaType.TEXT_PLAIN).build();
	}

	/**
	 * Gets a list of all styles for a given featuretype.
	 * @param collectionid the featuretype name
	 * @param f the format in which the style is returned
	 * @return The style as String
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/styles")
	@Operation(
            summary = "Gets a list of all styles for a given featuretype",
            description = "Gets a list of all styles for a given featuretype")
	public Response getCollectionStyles(
			@Parameter(description="Feature type name") @PathParam("collectionid") String collectionid,
			@Parameter(description="Return type") @DefaultValue("html") @QueryParam("f") String format) {
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
		if (format.contains("json")) {
			return Response.ok(TripleStoreConnector.getStyleNames(wfsconf.getString("baseurl"), workingobj, format))
					.type(MediaType.APPLICATION_JSON).build();
		} else if (format.contains("html")) {
			return Response.ok(TripleStoreConnector.getStyleNames(wfsconf.getString("baseurl"), workingobj, format))
					.type(MediaType.TEXT_HTML).build();
		}
		return Response.ok(TripleStoreConnector.getStyleNames(wfsconf.getString("baseurl"), workingobj, format))
				.type(MediaType.APPLICATION_XML).build();
	}

	
	/**
	 * CSW endpoint implementation.
	 * @param service The service identifier
	 * @param request The request identifier
	 * @param version The service version
	 * @param typename The feature type name to request
	 * @param typenames The feature type name to request
	 * @param output The requested return format 
	 * @return The CSW service description as XML
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/csw")
	@Operation(
            summary = "CSW endpoint implementation",
            description = "CSW endpoint implementation")
	public Response entryPointCSW(
			@Parameter(description="Service type") @DefaultValue("CSW") @QueryParam("SERVICE") String service,
			@Parameter(description="Request type") @DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@Parameter(description="Version of the webservice")  @DefaultValue("2.0.2") @QueryParam("VERSION") String version,
			@Parameter(description="Feature type names") @DefaultValue("") @QueryParam("TYPENAME") String typename,
			@Parameter(description="Feature type names") @DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@Parameter(description="Return type")  @DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output) {
		if (service.equalsIgnoreCase("CSW")) {
			if ("getCapabilities".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilitiesCSW(version, version.substring(0, version.lastIndexOf('.')));
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return Response.ok("").type(MediaType.TEXT_PLAIN).build();
				}
			}
			if ("describeRecord".equalsIgnoreCase(request)) {
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
			if ("harvest".equalsIgnoreCase(request)) {
				return Response.ok("").type(MediaType.TEXT_PLAIN).build();
			}
			if ("getRecords".equalsIgnoreCase(request)) {
				return this.getCollectionMetadata(typename, output, "false","");
			}
			if ("getRecordById".equalsIgnoreCase(request)) {
				return this.getCollectionsMetadata(output);
			}
		}
		return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}

	/**
	 * CSW endpoint implementation (POST version).
	 * @param service The service identifier
	 * @param request The request identifier
	 * @param version The service version
	 * @param typename The feature type name to request
	 * @param typenames The feature type name to request
	 * @param output The requested return format 
	 * @return The CSW service description as XML
	 */
	@POST
	@Produces(MediaType.TEXT_XML)
	@Path("/post/csw")
	@Operation(
            summary = "CSW endpoint implementation (POST version)",
            description = "CSW endpoint implementation (POST version)")
	public Response entryPointCSWPost(@DefaultValue("CSW") @QueryParam("SERVICE") String service,
			@DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@DefaultValue("2.0.2") @QueryParam("VERSION") String version,
			@DefaultValue("") @QueryParam("TYPENAME") String typename,
			@DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output) {
		return this.entryPointCSW(service, request, version, typename, typenames, output);
	}

	/**
	 * WFS endpoint implementation. 
	 * @param service The service identifier
	 * @param request The request identifier
	 * @param version The service version
	 * @param typename The feature type name to request
	 * @param typenames The feature type name to request
	 * @param srsName
	 * @param exceptions
	 * @param bbox
	 * @param propertyname
	 * @param sortBy
	 * @param style
	 * @param resultType
	 * @param resourceids
	 * @param gmlobjectid
	 * @param startindex
	 * @param filter
	 * @param filterLanguage
	 * @param output
	 * @param count
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs")
	@Operation(
            summary = "WFS endpoint implementation",
            description = "WFS endpoint implementation")
	public Response entryPoint(
			@Parameter(description="Service type") @DefaultValue("WFS") @QueryParam("SERVICE") String service,
			@Parameter(description="Request type") @DefaultValue("GetCapabilities") @QueryParam("REQUEST") String request,
			@Parameter(description="Service version")  @DefaultValue("2.0.0") @QueryParam("VERSION") String version,
			@Parameter(description="Feature type names") @DefaultValue("") @QueryParam("TYPENAME") String typename,
			@Parameter(description="Feature type names") @DefaultValue("") @QueryParam("TYPENAMES") String typenames,
			@Parameter(description="CRS type") @DefaultValue("") @QueryParam("SRSNAME") String srsName,
			@DefaultValue("gml") @QueryParam("EXCEPTIONS") String exceptions,
			@Parameter(description="Bounding box for filtering the results") @DefaultValue("") @QueryParam("BBOX") String bbox,
			@DefaultValue("") @QueryParam("VALUEREFERENCE") String propertyname,
			@Parameter(description="Sorting order")  @DefaultValue("ASC") @QueryParam("SORTBY") String sortBy,
			@DefaultValue("") @QueryParam("STYLES") String style,
			@DefaultValue("results") @QueryParam("RESULTTYPE") String resultType,
			@DefaultValue("") @QueryParam("RESOURCEID") String resourceids,
			@DefaultValue("") @QueryParam("GMLOBJECTID") String gmlobjectid,
			@DefaultValue("0") @QueryParam("STARTINDEX") String startindex,
			@Parameter(description="Filter expression") @DefaultValue("") @QueryParam("FILTER") String filter,
			@Parameter(description="Filter query language") @DefaultValue("") @QueryParam("FILTERLANGUAGE") String filterLanguage,
			@Parameter(description="Return format") @DefaultValue("gml") @QueryParam("OUTPUTFORMAT") String output,
			@Parameter(description="Maximum amount of results to return") @DefaultValue("5") @QueryParam("COUNT") String count) {
		System.out.println("Request: " + request);
		System.out.println("ResultType: " + resultType);
		if (typename.isEmpty() && !typenames.isEmpty()) {
			typename = typenames;
		}
		if (service.equalsIgnoreCase("WFS")) {
			if ("getCapabilities".equalsIgnoreCase(request)) {
				try {
					return this.constructCapabilities(version, version.substring(0, version.lastIndexOf('.')));
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
					return this.getFeature(typename, output, count, startindex, srsName, sortBy, style, version,
							resourceids, filter, filterLanguage, resultType);
				} catch (XMLStreamException e) {
					e.printStackTrace();
					return Response.ok("").type(MediaType.TEXT_PLAIN).build();
				}
			}
			if ("getPropertyValue".equalsIgnoreCase(request)) {
				return this.getPropertyValue(typename, propertyname, output, resourceids, filter, count, resultType);
			}
			if ("getGmlObject".equalsIgnoreCase(request)) {
				return this.getGmlObject(typename, gmlobjectid, "4", output);
			}
		}
		return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON})
	@Path("/collections")
	@Operation(
            summary = "Returns a list of feature types/collections",
            description = "Returns a list of feature types/collections")
	public Response collectionsJSON(
			@Parameter(description="Return format") @DefaultValue("json") @QueryParam("f") String format) {
		return collections(format);
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML})
	@Path("/collections")
	@Operation(
            summary = "Returns a list of feature types/collections",
            description = "Returns a list of feature types/collections")
	public Response collectionsXML(
			@Parameter(description="Return format") @DefaultValue("xml") @QueryParam("f") String format) {
		return collections(format);
	}

	@GET
	@Produces({ MediaType.TEXT_HTML})
	@Path("/collections")
	@Operation(
            summary = "Returns a list of feature types/collections",
            description = "Returns a list of feature types/collections")
	public Response collectionsHTML(
			@Parameter(description="Return format") @DefaultValue("html") @QueryParam("f") String format) {
		return collections(format);
	}
	
	/**
	 * Returns a list of feature types/collections. 
	 * @param format The format in which the result is returned
	 * @return The result as String
	 */
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/collections")
	@Operation(
            summary = "Returns a list of feature types/collections",
            description = "Returns a list of feature types/collections")
	public Response collections(
			@Parameter(description="Return format") @DefaultValue("html") @QueryParam("f") String format) {
		System.out.println(format);
		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONArray collections = new JSONArray();
			JSONObject link = new JSONObject();
			link.put("rel", "self");
			link.put("title", "This document");
			link.put("type", "application/json");
			link.put("href", wfsconf.get("baseurl") + "/collections?f=json");
			links.put(link);
			link = new JSONObject();
			link.put("rel", "alternate");
			link.put("title", "This document as XML");
			link.put("type", "text/xml");
			link.put("href", wfsconf.get("baseurl") + "/collections?f=gml");
			links.put(link);
			link = new JSONObject();
			link.put("rel", "alternate");
			link.put("title", "This document as HTML");
			link.put("type", "text/html");
			link.put("href", wfsconf.get("baseurl") + "/collections?f=html");
			links.put(link);
			link = new JSONObject();
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
				coll.put("name", curobj.getString("name"));
				coll.put("title", curobj.getString("name"));
				if(curobj.has("description"))
					coll.put("description", curobj.getString("description"));
				JSONObject extent = new JSONObject();
				JSONArray spatial = new JSONArray();
				JSONArray temporal=new JSONArray();
				temporal.put("1970-01-01T00:00:00Z");
				temporal.put("2020-08-22T22:10:44Z");
				JSONArray crs=new JSONArray();
				crs.put("http://www.opengis.net/def/crs/OGC/1.3/CRS84");
				coll.put("crs",crs);
				coll.put("extent", extent);
				extent.put("spatial", spatial);
				extent.put("temporal", temporal);
				JSONArray colinks = new JSONArray();
				for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
					link = new JSONObject();
					link.put("rel", "item");
					link.put("href", wfsconf.getString("baseurl") + "/collections/" + curobj.getString("name")
							+ "/items" + "?f=" + formatter.urlformat);
					link.put("type", formatter.exposedType);
					link.put("title", curobj.getString("name"));
					colinks.put(link);
				}
				link.put("rel", "self");
				link.put("title", "This document");
				link.put("type", "application/json");
				link.put("href", wfsconf.get("baseurl") + "/collections/"+curobj.getString("name")+"?f=json");
				colinks.put(link);
				link = new JSONObject();
				link.put("rel", "alternate");
				link.put("title", "This document as XML");
				link.put("type", "text/xml");
				link.put("href", wfsconf.get("baseurl") + "/collections/"+curobj.getString("name")+"?f=xml");
				colinks.put(link);
				link = new JSONObject();
				link.put("rel", "alternate");
				link.put("title", "This document as HTML");
				link.put("type", "text/html");
				link.put("href", wfsconf.get("baseurl") + "/collections/"+curobj.getString("name")+"?f=html");
				colinks.put(link);
				link = new JSONObject();
				link.put("rel", "describedBy");
				link.put("title", "XML Schema for this dataset");
				link.put("type", "application/xml");
				link.put("href", "http://www.acme.com/3.0/wfs/collections/"+curobj.getString("name")+"/schema");
				colinks.put(link);
				coll.put("links", colinks);
				collections.put(coll);
			}
			return Response.ok(result.toString(2)).type(ResultFormatter.getFormatter(format).mimeType).build();
		} else if (format != null && format.contains("gml")) {
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
				writer.writeCharacters(" ");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "self");
				writer.writeAttribute("title", "This document");
				writer.writeAttribute("type", "text/xml");
				writer.writeAttribute("href", wfsconf.get("baseurl") + "/collections?f=xml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.get("baseurl") + "/collections?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as HTML");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.get("baseurl") + "/collections?f=html");
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
					writer.writeStartElement("Description");
					if(curobj.has("description"))
						writer.writeCharacters(curobj.getString("description"));
					else {
						writer.writeCharacters("");	
					}
					writer.writeEndElement();
					for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
						writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
						writer.writeAttribute("rel", "items");
						writer.writeAttribute("title", curobj.getString("name"));
						writer.writeAttribute("type", formatter.exposedType);
						writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
								+ curobj.getString("name") + "/items?f=" + formatter.exposedType);
						writer.writeEndElement();
					}
					writer.writeEndElement();
					/*writer.writeStartElement("Extent");
					writer.writeStartElement("Spatial");
					writer.writeAttribute("crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84");
					writer.writeEndElement();
					writer.writeEndElement();*/
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
			builder.append("<header id=\"header\"><div class=\"page-header\"><h1 align=center>");
			builder.append("FeatureCollection View");
			builder.append("</h1></div></header>");
			builder.append("<div class=\"sticky row crumbs\"><div class=\"col-sm-12 col-md-10 col-md-offset-1\">");
			builder.append("<a href=\""+this.wfsconf.getString("baseurl")+"\">Landingpage</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/\">Collections</a>");
		    builder.append("</div></div>");
			builder.append(
					"<div class=\"container-fluid\" role=\"main\"><div class=\"row\"><div class=\"col-sm-12\"><table class=\"description\" id=\"collectiontable\" width=100% border=1><thead><tr><th>Collection</th><th>Description</th><th>Schema</th></tr></thead><tbody>");
			for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
				JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
				if(i%2==0){
				    builder.append("<tr class=\"even\">");
				}else{
				    builder.append("<tr class=\"odd\">");
				}
				builder.append("<td align=center><a href=\"" + wfsconf.getString("baseurl") + "/collections/"
						+ wfsconf.getJSONArray("datasets").getJSONObject(i).get("name") + "?f=html\">"
						+ wfsconf.getJSONArray("datasets").getJSONObject(i).get("name") + "</a></td><td align=center>");
				if (wfsconf.getJSONArray("datasets").getJSONObject(i).has("description")) {
					builder.append(wfsconf.getJSONArray("datasets").getJSONObject(i).get("description"));
				}
				builder.append("</td><td align=center>");
				if (wfsconf.getJSONArray("datasets").getJSONObject(i).has("schema")) {
					builder.append("<a href=\"" + wfsconf.getJSONArray("datasets").getJSONObject(i).get("schema")
							+ "\" target=\"_blank\">[Schema]</a>");
				} else {
					builder.append("<a href=\"" + wfsconf.getString("baseurl") + "/collections/"
							+ curobj.getString("name") + "/schema\" target=\"_blank\">[XML Schema]</a><br/>");
					builder.append("<a href=\"" + wfsconf.getString("baseurl") + "/collections/"
							+ curobj.getString("name") + "/schema?f=json\" target=\"_blank\">[JSON Schema]</a>");
				}
				/*builder.append("</td><td align=center>");
				Integer counter = 0;
				for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
					if (counter % 4 == 0) {
						builder.append("<br>");
					}
					builder.append("<a href=\"" + wfsconf.getString("baseurl") + "/collections/"
							+ curobj.getString("name") + "/items?f=" + formatter.exposedType + "\">["
							+ formatter.exposedType.toUpperCase() + "]</a>&nbsp;&nbsp;");
					counter++;
				}*/
				builder.append("</td></tr>");
			}
			builder.append("</tbody></table>");
			builder.append("</div></div></div><footer id=\"footer\"><table width=100%><tbody><tr><td><a href=\"" + wfsconf.getString("baseurl")
					+ "/?f=html\">Back to LandingPage</a></td><td align=right>This page in <a href=\""
					+ wfsconf.getString("baseurl") + "/collections?f=gml\">[XML]</a> <a href=\""
					+ wfsconf.getString("baseurl")
					+ "/collections?f=json\">[JSON]</a></td></tr></table></footer><script>$('#collectiontable').DataTable();</script></body></html>");
			return Response.ok(builder.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
		} else {
			throw new NotFoundException();
		}
	}

	/**
	 * Returns a feature given its feature id.
	 * @param collectionid The feature type
	 * @param featureid The feature id to return
	 * @param style The style in which to style the feature
	 * @param format The format in which to return the feature
	 * @return The feature as String in the given format
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/items/{featureid}")
	@Operation(
            summary = "Returns a feature given its feature id",
            description = "Returns a feature given its feature id with certain constraints")
	public Response getFeatureById(
			@Parameter(description="The collection id") @PathParam("collectionid") String collectionid,
			@Parameter(description="The feature id")@PathParam("featureid") String featureid, 
			@Parameter(description="The style to be applied")  @DefaultValue("") @QueryParam("style") String style,
			@Parameter(description="The format to be returned")  @DefaultValue("html") @QueryParam("f") String format) {
		System.out.println(collectionid + " - " + featureid + " - " + style);
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
		if (!workingobj.has("attcount") && !workingobj.getString("query").contains("?rel")
				&& !workingobj.getString("query").contains("?val")) {
			workingobj.put("attcount", 1);
		} else if (!workingobj.has("attcount")) {
			featureTypeCache.put(collectionid.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
		}
		String query = workingobj.getString("query");
		String res = "";
		try {
			res = TripleStoreConnector.executeQuery(query, workingobj.getString("triplestore"), format, "0", "0",
					"sf:featureMember", collectionid, featureid, workingobj, "", "", "", "", style,false,(workingobj.has("invertXY")?workingobj.getBoolean("invertXY"):false));
			System.out.println(res);
			if (res == null || res.isEmpty()) {
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
			for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
				JSONObject link = new JSONObject();
				if (formatter.exposedType.contains("geojson")) {
					link.put("rel", "self");
				} else {
					link.put("rel", "alternate");
				}
				link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items/" + featureid
						+ "?f=" + formatter.exposedType);
				link.put("type", formatter.exposedType);
				link.put("title", featureid);
				links.put(link);
			}
			result.put("id", featureid);
			JSONObject jsonresult = new JSONObject(res);
			JSONObject features = jsonresult.getJSONArray("features").getJSONObject(0);
			if (jsonresult.has("@context")) {
				result.put("@context", jsonresult.getJSONObject("@context"));
			}
			result.put("type", "Feature");
			result.put("links", links);
			result.put("timeStamp", System.currentTimeMillis());
			result.put("numberMatched", features.length());
			result.put("numberReturned", features.length());
			result.put("geometry", features.getJSONObject("geometry"));
			result.put("properties", features.getJSONObject("properties"));
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (format != null && format.contains("gml")) {
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
				for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					if (formatter.exposedType.contains("json")) {
						writer.writeAttribute("rel", "self");
					} else {
						writer.writeAttribute("rel", "alternate");
					}
					writer.writeAttribute("title", workingobj.getString("name"));
					writer.writeAttribute("type", formatter.exposedType);
					writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
							+ workingobj.getString("name") + "/items/+" + featureid + "?f=" + formatter.exposedType);
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
			builder.append("<body><header id=\"header\"><h1 align=\"center\">");
			builder.append(featureid);
			builder.append("</h1></header>");
			builder.append("<div class=\"sticky row crumbs\"><div class=\"col-sm-12 col-md-10 col-md-offset-1\">");
			builder.append("<a href=\""+this.wfsconf.getString("baseurl")+"\">Landingpage</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/\">Collections</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"\">"+workingobj.getString("name")+"</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"/items?f=html\">Items</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"/items/"+featureid+"?f=html\">"+featureid+"</a>");
		    builder.append("</div></div>");
			builder.append("<div class=\"container-fluid\" role=\"main\"><div class=\"row\">");
			builder.append(res);
			builder.append("</div></div></div></div><footer id=\"footer\"><table width=100%><tbody><tr><td><a href=\"" + wfsconf.getString("baseurl") + "/collections/"
					+ collectionid + "?f=html\">Back to " + collectionid
					+ " Collection</a></td><td align=right>This page in <a href=\"" + wfsconf.getString("baseurl")
					+ "/collections/" + workingobj.getString("name") + "/items/" + featureid
					+ "?f=gml\">[GML]</a> <a href=\"" + wfsconf.getString("baseurl") + "/collections/"
					+ workingobj.getString("name") + "/items/" + featureid + "?f=json\">[JSON]</a></td></tr></tbody></table>");
			builder.append("</footer></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}
	}
	
	/**
	 * Returns information about the queryables of ths given feature type.
	 * @param collectionid The feature type to retrieve information about
	 * @param format The format in which to retrieve the information
	 * @return The queryables as String
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/queryables")
	@Operation(
            summary = "Returns information about the queryables of ths given feature type",
            description = "Returns information about the queryables of ths given feature type")
	public Response queryables(@Parameter(description="The id of the collection to be considered") @PathParam("collectionid") String collectionid,
			@Parameter(description="The format in which the collection should be returned",example="geojson") @DefaultValue("html") @QueryParam("f") String format) {
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
		if (!featureTypeCache.containsKey(collectionid.toLowerCase())) {
			featureTypeCache.put(collectionid.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
		}
		Map<String, String> mapping = featureTypeCache.get(collectionid.toLowerCase());
		StringBuilder builder = new StringBuilder();
		StringBuilder builder2 = new StringBuilder();
		JSONObject geojson = new JSONObject();
		JSONObject geometry = new JSONObject();
		JSONObject properties = new JSONObject();
		geojson.put("type", "Feature");
		geojson.put("id", collectionid);
		geojson.put("geometry", geometry);
		geojson.put("properties", properties);
		builder.append(htmlHead);
		builder.append("<body><header id=\"header\"><h1 align=\"center\">");
		builder.append(collectionid+" Queryables");
		builder.append("</h1></header>");
		builder.append("<div class=\"sticky row crumbs\"><div class=\"col-sm-12 col-md-10 col-md-offset-1\">");
		builder.append("<a href=\""+this.wfsconf.getString("baseurl")+"\">Landingpage</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/\">Collections</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"\">"+workingobj.getString("name")+"</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"/queryables?f=html\">Queryables</a>");
	    builder.append("</div></div>");
		builder.append("<div class=\"container-fluid\" role=\"main\"><div class=\"row\">");
		builder.append("<table width=100%><tr><td width=\"100%\" rowspan=2>");
		builder.append("<script>var overlayMaps={}; var overlayControl; var typeColumn=\""
				+ (workingobj.has("typeColumn") ? workingobj.getString("typeColumn") : "")
				+ "\"; var markercollection=[]; var epsg=\""
				+ (workingobj.has("targetCRS") ? workingobj.getString("targetCRS") : "") + "\";");
		builder2.append(((HTMLFormatter) ResultFormatter.getFormatter("html")).htmlHeader2);
		
		builder2.append("</td><td>Contents:<table class=\"description\" border=\"1\"><tr><th>Value</th><th>Type</th>");
		String lon = null, lat = null;
		if (mapping != null) {
			for (String elem : mapping.keySet()) {
				if (!elem.equals("http://www.opengis.net/ont/geosparql#hasGeometry")
						&& !elem.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					if (elem.contains("http")) {
						if (elem.contains("#")) {
							builder2.append("<tr><td align=center><a href=\"" + elem + "\">"
									+ elem.substring(elem.lastIndexOf('#') + 1) + "</a> ");
						} else {
							builder2.append("<tr><td align=center><a href=\"" + elem + "\">"
									+ elem.substring(elem.lastIndexOf('/') + 1) + "</a> ");
						}
					} else {
						builder2.append("<tr><td align=center>" + elem);
					}
					if (mapping.get(elem).contains("^^")) {
						String type = mapping.get(elem).substring(mapping.get(elem).lastIndexOf("^^") + 2);
						builder2.append("</td><td align=center><a href=\"" + type + "\">"
								+ type.substring(type.lastIndexOf('#') + 1) + "</a></td></tr>");
					} else {
						if ((mapping.get(elem).contains("http") || mapping.get(elem).contains("file:/"))
								&& mapping.get(elem).contains("#")) {
							builder2.append("</td><td align=center><a href=\"" + mapping.get(elem) + "\">"
									+ mapping.get(elem).substring(mapping.get(elem).lastIndexOf('#') + 1)
									+ "</a></td></tr>");
						} else if ((mapping.get(elem).contains("http") || mapping.get(elem).contains("file:/"))
								&& mapping.get(elem).contains("/")) {
							builder2.append("</td><td align=center><a href=\"" + mapping.get(elem) + "\">"
									+ mapping.get(elem).substring(mapping.get(elem).lastIndexOf('/') + 1)
									+ "</a></td></tr>");
						} else {
							builder2.append("</td><td align=center><a href=\"" + mapping.get(elem) + "\">"
									+ mapping.get(elem) + "</a></td></tr>");
						}
					}
					if (elem.contains("http://www.opengis.net/ont/geosparql#asWKT")) {
						geometry.put("type", mapping.get(elem).substring(0, mapping.get(elem).indexOf('(')));
						String coords = mapping.get(elem).substring(mapping.get(elem).indexOf('(') + 1,
								mapping.get(elem).indexOf(')'));
						JSONArray arr = new JSONArray();
						geometry.put("coordinates", arr);
						if(!coords.contains(",")) {
							String[] commasplit = coords.split(" ");
							arr.put(Double.valueOf(commasplit[0]));
							arr.put(Double.valueOf(commasplit[1]));
						}else {								
						for (String coord : coords.split(" ")) {
							if (coord.contains(",")) {
								String[] commasplit = coord.split(",");
								arr.put(Double.valueOf(commasplit[0]));
								arr.put(Double.valueOf(commasplit[1]));
							}
						}
						}
					}
					if (elem.contains("lat")) {
						lat = mapping.get(elem);
					}
					if (elem.contains("lon")) {
						lon = mapping.get(elem);
					}
					if (lat != null && lon != null) {
						geometry.put("type", "Point");
						JSONArray arr = new JSONArray();
						geometry.put("coordinates", arr);
						arr.put(lon);
						arr.put(lat);
					}
					properties.put(elem, mapping.get(elem));
				}
			}
		}
		builder2.append("</table><br/>Styles:"
				+ TripleStoreConnector.getStyleNames(wfsconf.getString("baseurl"), workingobj, format));
		builder.append("var geojson=" + geojson.toString() + "</script>");
		builder.append(builder2.toString());
		builder.append("</td></tr></table>");
		builder.append("</div></div><footer id=\"footer\"><table width=100%><tbody><tr><td><a href=\"" + wfsconf.getString("baseurl")
		+ "/collections/"+workingobj.getString("name")+"?f=html\">Back to Collections</a></td><td align=right>This page in <a href=\""
		+ wfsconf.getString("baseurl") + "/collections/" + workingobj.getString("name")
		+ "/queryables?f=gml\">[GML]</a> <a href=\"" + wfsconf.getString("baseurl") + "/collections/"
		+ workingobj.getString("name") + "/queryables?f=geojson\">[JSON]</a></td></tr></tbody></table></footer></body></html>");
		return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
	}

	
	
	@GET
	@Produces({MediaType.TEXT_HTML })
	@Path("/")
	@Operation(
            summary = "Returns the landing page of the OGC API Features service",
            description = "Returns the landing page of the OGC API Features service")	
	public Response landingPageHTML(@Parameter(description="The format of the landingpage") @DefaultValue("html") @QueryParam("f") String format) {
		return landingPage(format);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON })
	@Path("/")
	@Operation(
            summary = "Returns the landing page of the OGC API Features service",
            description = "Returns the landing page of the OGC API Features service")	
	public Response landingPageJSON(@Parameter(description="The format of the landingpage") @DefaultValue("json") @QueryParam("f") String format) {
		return landingPage(format);
	}

	@GET
	@Produces({MediaType.APPLICATION_XML })
	@Path("/")
	@Operation(
            summary = "Returns the landing page of the OGC API Features service",
            description = "Returns the landing page of the OGC API Features service")	
	public Response landingPageXML(@Parameter(description="The format of the landingpage") @DefaultValue("xml") @QueryParam("f") String format) {
		return landingPage(format);
	}
	
	
	/**
	 * Returns the landing page of the OGC API Features service.
	 * @param format The format in which the page is to be returned
	 * @return The landing page as String
	 */
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/")
	@Operation(
            summary = "Returns the landing page of the OGC API Features service",
            description = "Returns the landing page of the OGC API Features service")
	public Response landingPage(@Parameter(description="The format of the landingpage") @DefaultValue("html") @QueryParam("f") String format) {
		if (format == null || format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONObject link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "?f=json");
			link.put("rel", "self");
			link.put("type", "application/json");
			link.put("title", "This document");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "?f=xml");
			link.put("rel", "alternate");
			link.put("type", "application/xml");
			link.put("title", "This document as XML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "?f=html");
			link.put("rel", "alternate");
			link.put("type", "text/html");
			link.put("title", "This document as HTML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/openapi");
			link.put("rel", "service-desc");
			link.put("type", "application/vnd.oai.openapi+json;version=3.0");
			link.put("title", "The API definition (JSON)");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/openapi");
			link.put("rel", "service-desc");
			link.put("type", "application/vnd.oai.openapi+yaml;version=3.0");
			link.put("title", "The API definition (YAML)");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/api/");
			link.put("rel", "service-doc");
			link.put("type", "text/html");
			link.put("title", "The API definition (HTML)");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/conformance?f=html");
			link.put("rel", "conformance");
			link.put("type", "text/html");
			link.put("title", "Conformance Declaration as HTML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/conformance?f=xml");
			link.put("rel", "conformance");
			link.put("type", "application/xml");
			link.put("title", "Conformance Declaration as XML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/conformance?f=json");
			link.put("rel", "conformance");
			link.put("type", "application/json");
			link.put("title", "Conformance Declaration as JSON");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/collections?f=json");
			link.put("rel", "data");
			link.put("type", "application/json");
			link.put("title", "Collections Metadata as JSON");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/collections?f=xml");
			link.put("rel", "data");
			link.put("type", "application/xml");
			link.put("title", "Collections Metadata as XML");
			links.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/collections?f=html");
			link.put("rel", "data");
			link.put("type", "text/html");
			link.put("title", "Collections Metadata as HTML");
			links.put(link);
			result.put("title", wfsconf.getString("servicetitle"));
			result.put("description", wfsconf.getString("servicedescription"));
			result.put("links", links);
			JSONArray collections=new JSONArray();
			result.put("collections", collections);
			for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
				JSONObject coll = new JSONObject();
				JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
				coll.put("id", curobj.getString("name"));
				coll.put("name", curobj.getString("name"));
				coll.put("title", curobj.getString("name"));
				if(curobj.has("description"))
					coll.put("description", curobj.getString("description"));
				JSONObject extent = new JSONObject();
				JSONArray spatial = new JSONArray();
				JSONArray temporal=new JSONArray();
				temporal.put("1970-01-01T00:00:00Z");
				temporal.put("2020-08-22T22:10:44Z");
				JSONArray crs = new JSONArray();
				crs.put("http://www.opengis.net/def/crs/OGC/1.3/CRS84");
				coll.put("crs", crs);
				coll.put("extent", extent);
				extent.put("spatial", spatial);
				extent.put("temporal", temporal);
				JSONArray colinks = new JSONArray();
				for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
					link = new JSONObject();
					link.put("rel", "item");
					link.put("href", wfsconf.getString("baseurl") + "/collections/" + curobj.getString("name")
							+ "/items" + "?f=" + formatter.urlformat);
					link.put("type", formatter.exposedType);
					link.put("title", curobj.getString("name"));
					colinks.put(link);
				}
				link.put("rel", "self");
				link.put("title", "This document");
				link.put("type", "application/json");
				link.put("href", wfsconf.get("baseurl") + "/collections/"+curobj.getString("name")+"?f=json");
				colinks.put(link);
				link = new JSONObject();
				link.put("rel", "alternate");
				link.put("title", "This document as XML");
				link.put("type", "application/xml");
				link.put("href", wfsconf.get("baseurl") + "/collections/"+curobj.getString("name")+"?f=gml");
				colinks.put(link);
				link = new JSONObject();
				link.put("rel", "alternate");
				link.put("title", "This document as HTML");
				link.put("type", "text/html");
				link.put("href", wfsconf.get("baseurl") + "/collections/"+curobj.getString("name")+"?f=html");
				colinks.put(link);
				link = new JSONObject();
				link.put("rel", "describedBy");
				link.put("title", "XML Schema for this dataset");
				link.put("type", "application/xml");
				link.put("href", "http://www.acme.com/3.0/wfs/collections/"+curobj.getString("name")+"/schema");
				colinks.put(link);
				coll.put("links", colinks);
				collections.put(coll);
			}
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (format.contains("xml")) {
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
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as HTML");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/?f=html");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "service-doc");
				writer.writeAttribute("title", "The API definition (HTML)");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href",  wfsconf.getString("baseurl") + "/api/");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "service-desc");
				writer.writeAttribute("title", "The API definition (YAML)");
				writer.writeAttribute("type", "application/vnd.oai.openapi+yaml;version=3.0");
				writer.writeAttribute("href",  wfsconf.getString("baseurl") + "/openapi.yaml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "service-desc");
				writer.writeAttribute("title", "The API defnition (JSON)");
				writer.writeAttribute("type", "application/vnd.oai.openapi+json;version=3.0");
				writer.writeAttribute("href",  wfsconf.getString("baseurl") + "/openapi.json");
				writer.writeEndElement();			
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "conformance");
				writer.writeAttribute("title", "Conformance Declaration as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/conformance?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "conformance");
				writer.writeAttribute("title", "Conformance Declaration as XML");
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/conformance?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "conformance");
				writer.writeAttribute("title", "Conformance Declaration as HTML");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/conformance?f=html");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "data");
				writer.writeAttribute("title", "Collections Metadata as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "data");
				writer.writeAttribute("title", "Collections Metadata as XML");
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "data");
				writer.writeAttribute("title", "Collections Metadata as HTML");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections?f=html");
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				return this.createExceptionResponse(e, "");
			}
		} else if (format.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append(htmlHead);
			builder.append("<body><header id=\"header\"><h1 align=\"center\">LandingPage: " + wfsconf.getString("servicetitle"));
			builder.append("</h1></header>");
			builder.append("<div class=\"sticky row crumbs\"><div class=\"col-sm-12 col-md-10 col-md-offset-1\">");
			builder.append("<a href=\""+this.wfsconf.getString("baseurl")+"\">Landingpage</a>");
		    builder.append("</div></div>");
			builder.append("<div class=\"container-fluid\" role=\"main\"><div class=\"row\"><div class=\"col-sm-12\"><p>"
							+ wfsconf.getString("servicedescription") + "</p><ul>");
			builder.append("<li>API Documentation in <a href=\"" + wfsconf.getString("baseurl")
					+ "/api/\">[HTML]</a>");
			builder.append(" <a href=\"" + wfsconf.getString("baseurl") + "/openapi.yaml\">[YAML]</a>");
			builder.append(" <a href=\"" + wfsconf.getString("baseurl") + "/openapi.json\">[JSON]</a></li>");
			builder.append("<li>Conformance Declaration in <a href=\"" + wfsconf.getString("baseurl")
					+ "/conformance?f=html\">[HTML]</a>");
			builder.append(" <a href=\"" + wfsconf.getString("baseurl") + "/conformance?f=gml\">[XML]</a>");
			builder.append(" <a href=\"" + wfsconf.getString("baseurl") + "/conformance?f=json\">[JSON]</a></li>");
			builder.append("<li>Collections Metadata in <a href=\"" + wfsconf.getString("baseurl")
					+ "/collections?f=html\">[HTML]</a>");
			builder.append(" <a href=\"" + wfsconf.getString("baseurl") + "/collections?f=gml\">[XML]</a>");
			builder.append(" <a href=\"" + wfsconf.getString("baseurl") + "/collections?f=json\">[JSON]</a></li></ul>");
			builder.append("This homepage also exposes a WFS 1.0.0, 1.1.0, 2.0.0 compatible Webservice:<ul>");
			builder.append("<li>GetCapabilities WFS 1.0.0 ");
			builder.append("<a href=\"" + wfsconf.getString("baseurl")
					+ "/wfs?REQUEST=getCapabilities&VERSION=1.0.0\">[XML]</a><br/>");
			builder.append("</li><li>GetCapabilities WFS 1.1.0 ");
			builder.append("<a href=\"" + wfsconf.getString("baseurl")
					+ "/wfs?REQUEST=getCapabilities&VERSION=1.1.0\">[XML]</a>");
			builder.append("</li><li>GetCapabilities WFS 2.0.0 ");
			builder.append("<a href=\"" + wfsconf.getString("baseurl")
					+ "/wfs?REQUEST=getCapabilities&VERSION=2.0.0\">[XML]</a>");
			builder.append(
					"</li></ul>Local Options:<ul><li><a href=\""+ wfsconf.getString("baseurl")+"/config/geotreeview.html\">Ontology Browser</a></li>"
							+ "<li><a href=\""+wfsconf.getString("baseurl")+"/config/queryinterface.html\">SPARQL Query Interface</a></li>"
							+ "<li><a href=\""+ wfsconf.getString("baseurl")
							+ "/config/configuration.html\">Semantic WFS Configuration</a></li><li><a href=\"../importer/\">Semantic Uplift Tools</a></li></ul></div></div></div><footer id=\"footer\"><table width=100%><tbody><tr><td><a href=\"" + wfsconf.getString("baseurl")
					+ "/?f=html\">Back to LandingPage</a></td><td align=right>This page in <a href=\""
					+ wfsconf.getString("baseurl") + "/?f=xml\">[XML]</a> <a href=\""
					+ wfsconf.getString("baseurl")
					+ "/?f=json\">[JSON]</a></td></tr></table></footer></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else {
			throw new NotFoundException();
		}
	}

	
	/**
	 * Returns the metadata of collections registered in the SemanticWFS. 
	 * @param format The metadataformat in which to return the results
	 * @return The results as String
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/metadata")
	@Operation(
            summary = "Returns the metadata of collections registered in the SemanticWFS",
            description = "Returns the metadata of collections registered in the SemanticWFS")
	public Response getCollectionsMetadata(@Parameter(description="The format of the collection page") @DefaultValue("html") @QueryParam("f") String format) {
		StringWriter strwriter = new StringWriter();
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
		try {
			writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
			writer.writeStartDocument();
			writer.setPrefix("gmd", "http://www.isotc211.org/2005/gmd");
			writer.setPrefix("gmx", "http://www.isotc211.org/2005/gmx");
			writer.setPrefix("gco", "http://www.isotc211.org/2005/gco");
			writer.setPrefix("csw", "http://www.opengis.net/cat/csw/2.0.2");
			writer.writeStartElement("http://www.opengis.net/cat/csw/2.0.2", "GetRecordsResponse");
			writer.writeNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2");
			writer.writeStartElement("http://www.opengis.net/cat/csw/2.0.2", "SearchStatus");
			writer.writeAttribute("timestamp", new Date(System.currentTimeMillis()).toGMTString());
			writer.writeEndElement();
			writer.writeStartElement("http://www.opengis.net/cat/csw/2.0.2", "SearchResults");
			writer.flush();
			this.xmlwriter = writer;
			for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
				JSONObject curobj = wfsconf.getJSONArray("datasets").getJSONObject(i);
				getCollectionMetadata(wfsconf.getJSONArray("datasets").getJSONObject(i).getString("name"), "gmd", "true","");
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return this.createExceptionResponse(e, "");
		}
	}

	/**
	 * Gets metadata of a given collection.
	 * @param collectionid The feture type of the collection
	 * @param mdformat The metadata format to use
	 * @param format The downlift format in which to return the metadata
	 * @param collectioncall indicates whether this method has been called from a higher level method
	 * @return The metadata as String
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/metadata")
	@Operation(
            summary = "Returns the metadata of a given collection",
            description = "Returns metadata of a given collection in a specified format")
	public Response getCollectionMetadata(@PathParam("collectionid") String collectionid,
			@Parameter(description="The metadata schema to be used")  @DefaultValue("gmd") @QueryParam("metadataformat") String mdformat,
			@Parameter(description="The format to downlift metadata")  @DefaultValue("html") @QueryParam("f") String format,
			@DefaultValue("false") @QueryParam("collectioncall") String collectioncall) {
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
		if (!featureTypeCache.containsKey(collectionid.toLowerCase())) {
			featureTypeCache.put(collectionid.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
		}
		String collectionurl=wfsconf.getString("baseurl") + "/collections/" + collectionid;
		try {
			return Response.ok(ResultMetadataFormatter.getFormatter(mdformat)
					.formatter(collectionid, collectioncall, collectionurl, workingobj,format))
					.type(ResultMetadataFormatter.getFormatter(mdformat).mimeType).build();
		} catch (XMLStreamException e1) {
			e1.printStackTrace();
			return this.createExceptionResponse(e1, "");
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/collections/{collectionid}")
	@Operation(
            summary = "Returns a given collection description or parts of it",
            description = "Returns a given collection in a specified format which conform to certain criteria")
	public Response collectionInformationJSON(
			@Parameter(description="The id of the collection to be considered") @PathParam("collectionid") String collectionid,
			@Parameter(description="The format in which the collection should be returned",example="geojson") @DefaultValue("json") @QueryParam("f") String format,  
			@Parameter(description="The maximum amount of features to be returned", example="10") @DefaultValue("10") @QueryParam("limit") String limit,
			@Parameter(description="An offset to be considered when returning features",example="10") @DefaultValue("0") @QueryParam("offset") String offset,
			@Parameter(description="Defines a bounding box from which to return the given features") @QueryParam("bbox") String bbox) {
		return collectionInformation(collectionid, format, limit, offset, bbox);
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Path("/collections/{collectionid}")
	@Operation(
            summary = "Returns a given collection description or parts of it",
            description = "Returns a given collection in a specified format which conform to certain criteria")
	public Response collectionInformationXML(
			@Parameter(description="The id of the collection to be considered") @PathParam("collectionid") String collectionid,
			@Parameter(description="The format in which the collection should be returned",example="geojson") @DefaultValue("xml") @QueryParam("f") String format,  
			@Parameter(description="The maximum amount of features to be returned", example="10") @DefaultValue("10") @QueryParam("limit") String limit,
			@Parameter(description="An offset to be considered when returning features",example="10") @DefaultValue("0") @QueryParam("offset") String offset,
			@Parameter(description="Defines a bounding box from which to return the given features") @QueryParam("bbox") String bbox) {
		return collectionInformation(collectionid, format, limit, offset, bbox);
	}

	@GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/collections/{collectionid}")
	@Operation(
            summary = "Returns a given collection description or parts of it",
            description = "Returns a given collection in a specified format which conform to certain criteria")
	public Response collectionInformationHTML(
			@Parameter(description="The id of the collection to be considered") @PathParam("collectionid") String collectionid,
			@Parameter(description="The format in which the collection should be returned",example="geojson") @DefaultValue("html") @QueryParam("f") String format,  
			@Parameter(description="The maximum amount of features to be returned", example="10") @DefaultValue("10") @QueryParam("limit") String limit,
			@Parameter(description="An offset to be considered when returning features",example="10") @DefaultValue("0") @QueryParam("offset") String offset,
			@Parameter(description="Defines a bounding box from which to return the given features") @QueryParam("bbox") String bbox) {
		return collectionInformation(collectionid, format, limit, offset, bbox);
	}
	
	/**
	 * Returns a given collection description or parts of it.
	 * @param collectionid The id of the collection to be considered
	 * @param format The format in which to return the collection
	 * @param limit The maximum amount of features to return
	 * @param offset The offset from which to start returning features
	 * @param bbox The boundingbox in which the returned features should fit
	 * @return The collection description as String
	 */
	@GET
	@Produces({MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}")
	@Operation(
            summary = "Returns a given collection description or parts of it",
            description = "Returns a given collection in a specified format which conform to certain criteria")
	public Response collectionInformation(
			@Parameter(description="The id of the collection to be considered") @PathParam("collectionid") String collectionid,
			@Parameter(description="The format in which the collection should be returned",example="geojson") @DefaultValue("html") @QueryParam("f") String format,  
			@Parameter(description="The maximum amount of features to be returned", example="10") @DefaultValue("10") @QueryParam("limit") String limit,
			@Parameter(description="An offset to be considered when returning features",example="10") @DefaultValue("0") @QueryParam("offset") String offset,
			@Parameter(description="Defines a bounding box from which to return the given features") @QueryParam("bbox") String bbox) {
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
		if (!featureTypeCache.containsKey(collectionid.toLowerCase())) {
			featureTypeCache.put(collectionid.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
		}
		Map<String, String> mapping = featureTypeCache.get(collectionid.toLowerCase());
		if (format != null && format.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray resultlinks=new JSONArray();
			result.put("id", workingobj.getString("name"));
			result.put("name", workingobj.getString("name"));
			result.put("title", workingobj.getString("name"));
			if(workingobj.has("description")) {
				result.put("description", workingobj.getString("description"));
			}else {
				result.put("description", "");
			}
			result.put("links",resultlinks);
			JSONObject extent = new JSONObject();
			result.put("extent", extent);
			JSONArray spatial = new JSONArray();
			JSONArray temporal=new JSONArray();
			temporal.put("1970-01-01T00:00:00Z");
			temporal.put("2020-08-22T22:10:44Z");
			extent.put("spatial", spatial);
			extent.put("temporal",temporal);
			JSONArray crs = new JSONArray();
			crs.put("http://www.opengis.net/def/crs/OGC/1.3/CRS84");
			result.put("crs", crs);
			JSONObject link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/collections/"+collectionid+"?f=json");
			link.put("rel", "self");
			link.put("type", "application/json");
			link.put("title", "This document");
			resultlinks.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/collections/"+collectionid+"?f=xml");
			link.put("rel", "alternate");
			link.put("type", "application/xml");
			link.put("title", "This document as XML");
			resultlinks.put(link);
			link = new JSONObject();
			link.put("href", wfsconf.getString("baseurl") + "/collections/"+collectionid+"?f=html");
			link.put("rel", "alternate");
			link.put("type", "text/html");
			link.put("title", "This document as HTML");
			resultlinks.put(link);
			/*JSONArray collections=new JSONArray();
			result.put("collections",collections);
			JSONObject collectionentry=new JSONObject();
			collections.put(collectionentry);
			collectionentry.put("id", workingobj.getString("name"));
			collectionentry.put("title", workingobj.getString("name"));
			collectionentry.put("description", "");
*/
			JSONArray links = resultlinks;
			for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
				link = new JSONObject();
				if (formatter.exposedType.contains("geojson")) {
					link.put("rel", "item");
				} else {
					link.put("rel", "item");
				}
				link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items" + "?f="
						+ formatter.urlformat);
				link.put("type", formatter.exposedType);
				link.put("title", collectionid);
				links.put(link);
			}
			link = new JSONObject();
			link.put("rel", "describedBy");
			link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/schema/");
			link.put("type", "application/xml");
			link.put("title", collectionid + " Schema");
			links.put(link);
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (format != null && format.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.setDefaultNamespace("http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeStartElement("Collection");
				writer.setPrefix("atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns", "http://www.opengis.net/ogcapi-features-1/1.0");
				writer.writeAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("xmlns:sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
				writer.writeAttribute("service", "OGCAPI-FEATURES");
				writer.writeAttribute("version", "1.0.0");
				writer.writeStartElement("links");
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "self");
				writer.writeAttribute("title", "This document");
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"+collectionid+"/?f=gml");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as JSON");
				writer.writeAttribute("type", "application/json");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"+collectionid+ "/?f=json");
				writer.writeEndElement();
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "alternate");
				writer.writeAttribute("title", "This document as HTML");
				writer.writeAttribute("type", "text/html");
				writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"+collectionid+ "/?f=html");
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeStartElement("collections");
				writer.writeStartElement(collectionid);
				writer.writeStartElement("Id");
				writer.writeCharacters(collectionid);
				writer.writeEndElement();
				writer.writeStartElement("Title");
				writer.writeCharacters(workingobj.getString("description"));
				writer.writeEndElement();
				for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
					writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
					if (formatter.exposedType.contains("geojson")) {
						writer.writeAttribute("rel", "item");
					} else {
						writer.writeAttribute("rel", "item");
					}
					writer.writeAttribute("title", workingobj.getString("name"));
					writer.writeAttribute("type", formatter.exposedType);
					writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
							+ workingobj.getString("name") + "/items?f=" + formatter.urlformat);
					writer.writeEndElement();
				}
				writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
				writer.writeAttribute("rel", "describedBy");
				writer.writeAttribute("title", workingobj.getString("name"));
				writer.writeAttribute("type", "application/xml");
				writer.writeAttribute("href",
						wfsconf.getString("baseurl") + "/collections/" + workingobj.getString("name") + "/schema/");
				writer.writeEndElement();
				/*writer.writeStartElement("Extent");
				writer.writeStartElement("Spatial");
				writer.writeAttribute("crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84");
				writer.writeEndElement();
				writer.writeEndElement();*/
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
		} else if (format == null || format.contains("html")) {
			System.out.println("WorkingObject: "+workingobj);
			StringBuilder builder = new StringBuilder();
			StringBuilder builder2 = new StringBuilder();
			JSONObject geojson = new JSONObject();
			JSONObject geometry = new JSONObject();
			JSONObject properties = new JSONObject();
			geojson.put("type", "Feature");
			geojson.put("id", collectionid);
			geojson.put("geometry", geometry);
			geojson.put("properties", properties);
			builder.append(htmlHead);
			builder.append("<script>function showCollections(link){window.open(link+\"?offset=\"+$('#offset').val()+\"&limit=\"+$('#limit').val()+\"&f=\"+$('#format').val(),'_blank');} var espg=\"" + (workingobj.has("targetCRS") ? workingobj.get("targetCRS") : "")
					+ "\";</script><body><header id=\"header\"><h1 align=\"center\">");
			builder.append(
					(workingobj.getString("name") != null ? workingobj.getString("name") : collectionid));
			builder.append("</h1></header>");
			builder.append("<div class=\"sticky row crumbs\"><div class=\"col-sm-12 col-md-10 col-md-offset-1\">");
			builder.append("<a href=\""+this.wfsconf.getString("baseurl")+"\">Landingpage</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/\">Collections</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"\">"+workingobj.getString("name")+"</a>");
		    builder.append("</div></div>");
			builder.append("<div class=\"container-fluid\" role=\"main\"><div class=\"row\"><section>");
			builder.append(workingobj.getString("description"));
			builder.append("<h3>Queryables</h3><ul><li>");
			builder.append("<a href=\""+wfsconf.getString("baseurl") + "/collections/"
					+ workingobj.getString("name") + "/queryables?f=html\">Queryables of "+workingobj.getString("name")+"</a></li></ul>");
			builder.append("<h3>View</h3><ul>");
			builder.append("<li><a href=\"" + wfsconf.getString("baseurl")
					+ "/collections/" + workingobj.getString("name") + "/items?f=html&limit=1&offset=" + (offset + 1)
					+ "\">First item</a></li>");
			builder.append("<li><a href=\""+wfsconf.getString("baseurl") + "/collections/"
					+ workingobj.getString("name") + "/items?f=html\">First 10 items</a></li>");
			builder.append("<li><a href=\"" + wfsconf.getString("baseurl")
			+ "/collections/" + workingobj.getString("name") + "/items?f=html&limit=100&offset=" + (offset + 1)
			+ "\">First 100 items</a></li>");
			builder.append("<li><a href=\"" + wfsconf.getString("baseurl")
			+ "/collections/" + workingobj.getString("name") + "/items?f=html&limit=1000&offset=" + (offset + 1)
			+ "\">First 1000 items</a></li>");
			builder.append("</ul><h3>Serializations</h3>Number of features:&nbsp;<input type=\"number\" min=\"1\" id=\"limit\" value=\"10\"/>&nbsp;Offset:&nbsp;<input type=\"number\" min=\"1\" id=\"offset\" value=\"0\"/>Format:<select id=\"format\">");
			for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
				builder.append("<option value=\""+formatter.urlformat+"\">"+formatter.label+"</option>");
			}
			builder.append("</select>&nbsp;<button id=\"showfeaturebutton\" onclick=\"showCollections('"+wfsconf.getString("baseurl")+"/collections/" + workingobj.getString("name") + "/items')\"/>Show</button></section></div></div></div>");
			builder.append("<footer id=\"footer\"><table width=100%><tbody><tr><td><a href=\"" + wfsconf.getString("baseurl")
					+ "/collections?f=html\">Back to Collections</a></td><td align=right>This page in <a href=\""
					+ wfsconf.getString("baseurl") + "/collections/" + workingobj.getString("name")
					+ "?f=gml\">[GML]</a> <a href=\"" + wfsconf.getString("baseurl") + "/collections/"
					+ workingobj.getString("name") + "?f=geojson\">[JSON]</a></td></tr></tbody></table></footer></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else {
			throw new NotFoundException();
		}
	}

	/**
	 * Returns a schema of a given collection. 
	 * @param collectionid The feature collection id
	 * @param format The format in which to return the collection
	 * @return The returned collection as String
	 */
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Path("/collections/{collectionid}/schema")
	@Operation(
            summary = "Returns a schema of a given collection",
            description = "Returns a schema of a given collection")
	public Response getSchema(
			@Parameter(description="The collection id for which to return the schema") @PathParam("collectionid") String collectionid,
			@Parameter(description="The format in which the schema should be returned",example="json") @DefaultValue("gml") @QueryParam("f") String format) {	
		if (format.contains("json")) {
			return this.describeFeatureTypeJSON(collectionid, "1.0.0");
		} else {
			try {
				return this.describeFeatureType(collectionid, "2.0.0");
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new NotFoundException();
			}
		}

	}

	
	/**
	 * Returns a number of features from a given featuretype.
	 * @param collectionid The featuretype to return from
	 * @param format The format of the result
	 * @param limit The maximum amount of features to return
	 * @param offset The offset from which to return features
	 * @param bbox The bounding box in which features which should be returned are contained
	 * @param style The style to apply
	 * @param bboxcrs The CRS of the boundingbox to apply
	 * @param filter A filter expression
	 * @param filterlang The filter expression language
	 * @param datetime A time constraint
	 * @return The query result as String
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	@Path("/collections/{collectionid}/items")
	@Operation(
            summary = "Returns items of a given collection",
            description = "Returns items of a given collection which conform to certain criteria")
	public Response collectionItems(@Parameter(description="The id of the collection") @PathParam("collectionid") String collectionid,
			@Parameter(description="The format of the result") @DefaultValue("html") @QueryParam("f") String format, 
			@DefaultValue("10") @QueryParam("limit") String limit,
			@Parameter(description="The offset to consider when fetching items") @DefaultValue("0") @QueryParam("offset") String offset, @DefaultValue("") @QueryParam("bbox") String bbox,
			@Parameter(description="The styling of the item when returned")  @DefaultValue("") @QueryParam("style") String style,
			@Parameter(description="The crs of a given bounding box") @DefaultValue("") @QueryParam("bbox-crs") String bboxcrs,
			@Parameter(description="A filter expression") @DefaultValue("") @QueryParam("filter") String filter,
			@Parameter(description="The language in which the filter expression is formulated") @DefaultValue("") @QueryParam("filter-lang") String filterlang,
			@Parameter(description="A temporal filter expression") @DefaultValue("") @QueryParam("datetime") String datetime) {
		System.out.println("Limit: " + limit);
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
		if (!workingobj.has("attcount") && !workingobj.getString("query").contains("?rel")
				&& !workingobj.getString("query").contains("?val")) {
			featureTypeCache.put(collectionid.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
			workingobj.put("attcount", 1);
		} else if (!workingobj.has("attcount")) {
			featureTypeCache.put(collectionid.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
		}
		if (!workingobj.has("attcount") || workingobj.getInt("attcount") == 0)
			workingobj.put("attcount", 1);
		System.out.println("Attcount: " + workingobj.getInt("attcount"));
		System.out.println(limit);
		try {
			String res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
					workingobj.getString("triplestore"), format,
					limit,
					offset, "sf:featureMember", collectionid,
					"", workingobj, filter, "", "", bbox, style,true,(workingobj.has("invertXY")?workingobj.getBoolean("invertXY"):false));
			// System.out.println(res);
			if (res == null || res.isEmpty()) {
				System.out.println("RES: "+res);
				throw new NotFoundException();
			}
			// System.out.println(res);
			if (format != null && format.contains("json") && !format.equalsIgnoreCase("rdfjson")) {
				JSONObject result = new JSONObject();
				JSONArray links = new JSONArray();
				for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
					JSONObject link = new JSONObject();
					if (formatter.exposedType.contains("geojson")) {
						link.put("rel", "self");
						JSONObject nextlink = new JSONObject();
						nextlink.put("rel", "next");
						nextlink.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items?offset="+(offset+limit)+"&limit="+limit+"&f="
								+ formatter.urlformat);
						nextlink.put("type", formatter.exposedType);
						nextlink.put("title", "next page");
						links.put(nextlink);
					} else {
						link.put("rel", "alternate");
					}
					link.put("href", wfsconf.getString("baseurl") + "/collections/" + collectionid + "/items?f="
							+ formatter.urlformat);
					link.put("type", formatter.exposedType);
					link.put("title", collectionid);
					links.put(link);
				}
				if (ResultFormatter.getFormatter(format).mimeType.contains("jsonld")) {
					return Response.ok(res).type("text/plain").build();
				}
				JSONObject jsonresult = new JSONObject(res);
				JSONArray features = jsonresult.getJSONArray("features");
				if (jsonresult.has("@context")) {
					result.put("@context", jsonresult.getJSONObject("@context"));
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
				System.out.println("GML RESULT!!!");
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
					for (String ns : WebService.nameSpaceCache.get(collectionid.toLowerCase()).keySet()) {
						writer.writeAttribute(
								"xmlns:" + WebService.nameSpaceCache.get(collectionid.toLowerCase()).get(ns), ns);
					}
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
					for (ResultFormatter formatter : ResultFormatter.resultMap.values()) {
						writer.writeStartElement("http://www.w3.org/2005/Atom", "link");
						if (formatter.exposedType.contains("json")) {
							writer.writeAttribute("rel", "self");
						} else {
							writer.writeAttribute("rel", "alternate");
						}
						writer.writeAttribute("title", workingobj.getString("name"));
						writer.writeAttribute("type", formatter.exposedType);
						writer.writeAttribute("href", wfsconf.getString("baseurl") + "/collections/"
								+ workingobj.getString("name") + "/items?f=" + formatter.exposedType);
						writer.writeEndElement();
					}
					strwriter.append(res);
					writer.writeEndElement();
					writer.writeEndDocument();
					writer.flush();
					System.out.println("GML RESULT!");
					return Response.ok(strwriter.toString()).type(ResultFormatter.getFormatter(format).mimeType)
							.build();
				} catch (XMLStreamException e) {
					e.printStackTrace();
					// TODO Auto-generated catch block
					return this.createExceptionResponse(e, "");
				}
			} else if (format == null || format.contains("html")) {
				StringBuilder builder = new StringBuilder();
				builder.append(htmlHead);
				builder.append("<body><header id=\"header\"><h1 align=\"center\">");
				builder.append(collectionid);
				builder.append("</h1></header>");
				builder.append("<div class=\"sticky row crumbs\"><div class=\"col-sm-12 col-md-10 col-md-offset-1\">");
				builder.append("<a href=\""+this.wfsconf.getString("baseurl")+"\">Landingpage</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/\">Collections</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"\">"+workingobj.getString("name")+"</a> / <a href=\""+this.wfsconf.getString("baseurl")+"/collections/"+collectionid+"/items?f=html\">Items</a>");
			    builder.append("</div></div>");
				builder.append("<div class=\"container-fluid\" role=\"main\"><div class=\"row\">");
				builder.append(res);
				// builder.append("<script>$( document ).ready(function()
				// {$('#queryres').DataTable({\"scrollX\":\"100%\",\"scrollCollapse\":
				// true});});</script>");
				if (Integer.valueOf(limit) == 1) {
					builder.append("<table width=100%><tr><td><a href=\"" + wfsconf.getString("baseurl")
							+ "/collections/" + workingobj.getString("name") + "/items?f=html&limit=1&offset="
							+ (Integer.valueOf(offset) != 0 ? (Integer.valueOf(offset) - 1) + "" : "0")
							+ "\">[Previous]</a></td><td align=right><a href=\"" + wfsconf.getString("baseurl")
							+ "/collections/" + workingobj.getString("name") + "/items?f=html&limit=1&offset="
							+ (Integer.valueOf(offset) + 1) + "\">[Next]</a></td></tr></table></div></div></div>");
				}
				builder.append("</div><footer id=\"footer\"><table width=100%><tbody><tr><td><a href=\"" + wfsconf.getString("baseurl") + "/collections/"
						+ collectionid + "?f=html\">Back to " + collectionid
						+ " Collection</a></td><td align=right>This page in <a href=\"" + wfsconf.getString("baseurl")
						+ "/collections/" + workingobj.getString("name") + "/items?f=gml&limit=" + limit + "&offset="
						+ offset + "\">[GML]</a> <a href=\"" + wfsconf.getString("baseurl") + "/collections/"
						+ workingobj.getString("name") + "/items?f=geojson&limit=" + limit + "&offset=" + offset
						+ "\">[JSON]</a></td></tr></tbody></table></footer>");
				builder.append("</body></html>");
				return Response.ok(builder.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
			} else {
				return Response.ok(res).type(ResultFormatter.getFormatter(format).mimeType).build();
			}
		} catch (JSONException | XMLStreamException e1) {
			e1.printStackTrace();
			return Response.ok("").type(MediaType.TEXT_PLAIN).build();
		}
	}

	
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/conformance")
	@Operation(
            summary = "Returns a conformance description",
            description = "Returns a conformance description in a given dataformat")
	public Response conformanceJSON(@Parameter(description="The format of the conformance page") @DefaultValue("json") @QueryParam("f") String format) {
		return conformance(format);
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Path("/conformance")
	@Operation(
            summary = "Returns a conformance description",
            description = "Returns a conformance description in a given dataformat")
	public Response conformanceXML(@Parameter(description="The format of the conformance page") @DefaultValue("xml") @QueryParam("f") String format) {
		return conformance(format);
	}
	
	@GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/conformance")
	@Operation(
            summary = "Returns a conformance description",
            description = "Returns a conformance description in a given dataformat")
	public Response conformanceHTML(@Parameter(description="The format of the conformance page") @DefaultValue("html") @QueryParam("f") String format) {
		return conformance(format);
	}
	
	/**
	 * Gets conformance information about the OGC API Features service.
	 * @param format The return format for the conformance declaration
	 * @return The conformance declaration as String 
	 */
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/conformance")
	@Operation(
            summary = "Returns a conformance description",
            description = "Returns a conformance description in a given dataformat")
	public Response conformance(@Parameter(description="The format of the conformance page") @DefaultValue("html") @QueryParam("f") String format) {
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
		} else if (format != null && format.contains("gml")) {
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
		} else if (format == null || format.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append("<html><head>" + htmlHead
					+ "</head><body><header id=\"header\"><h1 align=\"center\">Conformance</h1></header><div class=\"container-fluid\" role=\"main\"><div class=\"row\"><div class=\"col-sm-12\"><ul>");
			builder.append(
					"<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core\">Core</a></li>");
			builder.append(
					"<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30\">Oas30</a></li>");
			builder.append(
					"<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html\">HTML</a></li>");
			builder.append(
					"<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson\">GeoJSON</a></li>");
			builder.append(
					"<li><a target=\"_blank\" href=\"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0\">GMLSf0</a></li>");
			builder.append("</ul></div></div></div><footer id=\"footer\"><table width=\"100%\"><tbody><tr><td><a href=\"" + wfsconf.getString("baseurl")
					+ "/?f=html\">Back to LandingPage</a></td><td align=right>This page in <a href=\""
					+ wfsconf.getString("baseurl") + "/conformance?f=gml\">[GML]</a> <a href=\"" + wfsconf.getString("baseurl") 
					+ "/conformance?f=geojson\">[JSON]</a></td></tr></tbody></table></footer></body></html>");
			return Response.ok(builder.toString()).type(ResultFormatter.getFormatter(format).mimeType).build();
		} else {
			throw new NotFoundException();
		}

	}

	public String SERVICETYPEVERSION = "2.0.0";

	public String SERVERURL = "http://localhost:8080/WFSGeoSPARQL/rest/wfs?";

	/**
	 * Constructs a capabilities document for a WFS in version 1.0 .
	 * @param version The version of the WFS service
	 * @param versionnamespace The namespace to use
	 * @return The capabilities document as String
	 * @throws XMLStreamException on error
	 */
	public Response constructCapabilitiesWFS10(String version, String versionnamespace) throws XMLStreamException {
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
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("GetGmlObject");
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("GetPropertyValue");
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs");
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
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("GetFeature");
		writer.writeStartElement("ResultFormat");
		for (ResultFormatter format : ResultFormatter.resultMap.values()) {
			if (!format.exposedType.isEmpty()) {
				if (format.exposedType.contains("/")) {
					writer.writeStartElement(format.exposedType.substring(format.exposedType.lastIndexOf('/') + 1)
							.replace("+", "").toUpperCase());
					writer.writeEndElement();
				} else {
					writer.writeStartElement(format.exposedType.toUpperCase());
					writer.writeEndElement();
				}
			}
		}
		writer.writeEndElement();
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		writer.writeStartElement("Get");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement("Post");
		writer.writeAttribute("onlineResource", wfsconf.getString("baseurl") + "/wfs");
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
			describeFeatureTypeWFS10(writer, wfsconf.getJSONArray("datasets").getJSONObject(i), versionnamespace,
					version);
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ogc", "Filter_Capabilities");
		describeSpatialCapabilitiesWFS10(writer, versionnamespace, "http://www.opengis.net/ogc");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
	}

	
	/**
	 * Constructs a capabilities document for a CSW service .
	 * @param version The version of the WFS service
	 * @param versionnamespace The namespace to use
	 * @return The capabilities document as String
	 * @throws XMLStreamException on error
	 */
	public Response constructCapabilitiesCSW(String version, String versionnamespace) throws XMLStreamException {
		String serviceType = "CSW";
		String owsns = "http://www.opengis.net/ows/1.1";
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter = new StringWriter();
		XMLStreamWriter xmlwriter;
		xmlwriter = factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.setPrefix("csw", "http://www.opengis.net/cat/csw/" + versionnamespace);
		writer.writeStartElement("http://www.opengis.net/cat/csw/" + versionnamespace, "Capabilities");
		writer.writeDefaultNamespace("http://www.opengis.net/wfs" + versionnamespace);
		writer.writeAttribute("version", version);
		writer.writeNamespace("csw", "http://www.opengis.net/cat/csw/" + versionnamespace);
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs" + versionnamespace);
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes" + versionnamespace);
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
		writer.writeStartElement(owsns, "ServiceIdentification");
		writer.writeStartElement(owsns, "ServiceType");
		writer.writeAttribute("codeSpace", "OGC");
		writer.writeCharacters(serviceType);
		writer.writeEndElement();
		writer.writeStartElement(owsns, "ServiceTypeVersion");
		writer.writeCharacters(version);
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Title");
		writer.writeCharacters(
				wfsconf.has("servicetitle") ? wfsconf.getString("servicetitle").replace("WFS", "CSW") : "");
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
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Parameter");
		writer.writeAttribute("name", "sections");
		writer.writeStartElement(owsns, "Value");
		writer.writeCharacters("ServiceIdentification");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Value");
		writer.writeCharacters("ServiceProvider");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Value");
		writer.writeCharacters("OperationsMetadata");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Value");
		writer.writeCharacters("Filter_Capabilities");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "DescribeRecord");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "GetRecords");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "GetRecordById");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "Harvest");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/csw");
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
		writer.writeStartElement("http://www.opengis.net/fes" + versionnamespace, "Filter_Capabilities");
		describeConformance(writer, versionnamespace, "http://www.opengis.net/fes" + versionnamespace);
		describeSpatialCapabilities(writer, versionnamespace, "http://www.opengis.net/fes" + versionnamespace);
		describeScalarCapabilities(writer, versionnamespace, "http://www.opengis.net/fes" + versionnamespace);
		writer.writeEndElement();
		writer.writeEndDocument();
		return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
	}

	/**
	 * Constructs a capabilities document for a WFS in version 1.1 .
	 * @param version The version of the WFS service
	 * @param versionnamespace The namespace to use
	 * @return The capabilities document as String
	 * @throws XMLStreamException on error
	 */
	public Response constructCapabilities(String version, String versionnamespace) throws XMLStreamException {
		String serviceType = "WFS";
		String owsns = "http://www.opengis.net/ows/1.1";
		if ("1.0.0".equals(version))
			return constructCapabilitiesWFS10(version, versionnamespace);
		if ("1.0.0".equals(version) || "1.1.0".equals(version)) {
			versionnamespace = "";
		} else {
			versionnamespace = "/" + versionnamespace;
		}
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter strwriter = new StringWriter();
		XMLStreamWriter xmlwriter = factory.createXMLStreamWriter(strwriter);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlwriter);
		writer.writeStartDocument();
		writer.writeStartElement("WFS_Capabilities");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs" + versionnamespace);
		writer.writeDefaultNamespace("http://www.opengis.net/wfs" + versionnamespace);
		writer.writeAttribute("version", version);
		writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs" + versionnamespace);
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes" + versionnamespace);
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
		writer.writeAttribute("xsi:schemaLocation", "http://www.opengis.net/wfs" + versionnamespace+" http://schemas.opengis.net/wfs/"+versionnamespace+"/wfs.xsd http://inspire.ec.europa.eu/schemas/inspire_dls/1.0 http://inspire.ec.europa.eu/schemas/inspire_dls/1.0/inspire_dls.xsd");
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
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs");
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
		writer.writeAttribute("name", "GetPropertyValue");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "DescribeFeatureType");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Operation");
		writer.writeAttribute("name", "GetGmlObject");
		writer.writeStartElement(owsns, "DCP");
		writer.writeStartElement(owsns, "HTTP");
		writer.writeStartElement(owsns, "Get");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs");
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
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs?");
		writer.writeEndElement();
		writer.writeStartElement(owsns, "Post");
		writer.writeAttribute("xlink:type", "simple");
		writer.writeAttribute("xlink:href", wfsconf.getString("baseurl") + "/wfs");
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
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsBasicWFS");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsTransactionalWFS");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "KVPEncoding");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsAdHocQuery");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsResourceId");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsMinStandardFilter");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsMinTemporalFilter");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsMinimumXPath");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(owsns,"Constraint");
		writer.writeAttribute("name", "ImplementsResultPaging");
		writer.writeStartElement(owsns,"DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();		
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "FeatureTypeList");
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "Operations");
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "Operation");
		writer.writeCharacters("Query");
		writer.writeEndElement();
		writer.writeEndElement();
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			describeFeatureType(writer, wfsconf.getJSONArray("datasets").getJSONObject(i), versionnamespace, version);
		}
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/fes" + versionnamespace, "Filter_Capabilities");
		describeConformance(writer,versionnamespace,"http://www.opengis.net/fes"+versionnamespace);
		describeSpatialCapabilities(writer, versionnamespace, "http://www.opengis.net/fes" + versionnamespace);
		describeScalarCapabilities(writer, versionnamespace, "http://www.opengis.net/fes" + versionnamespace);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
	}

	/**
	 * Describes the conformance part of a WFS webservices.
	 * @param writer The XML writer
	 * @param versionnamespace The versionnamespace
	 * @param namespace The namespace to follow
	 * @throws XMLStreamException on error
	 */
	public void describeConformance(IndentingXMLStreamWriter writer,String versionnamespace, String namespace) throws XMLStreamException {
		System.out.println(namespace);
		writer.writeStartElement(namespace,"Conformance");
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsQuery");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsAdHocQuery");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsFunctions");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsMinStandardFilter");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsMinSpatialFilter");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsSpatialFilter");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsMinTemporalFilter");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsResourceId");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsMinimumXPath");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsTemporalFilter");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("FALSE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsVersionNav");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("TRUE");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsSorting");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","AllowedValues");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Value");
		writer.writeCharacters("ASC");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","Value");
		writer.writeCharacters("DESC");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("ASC");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeStartElement(namespace,"Constraint");
		writer.writeAttribute("name","ImplementsExtendedOperators");
		writer.writeStartElement("http://www.opengis.net/ows/1.1","DefaultValue");
		writer.writeCharacters("FALSE");
		writer.writeEndElement();
		writer.writeEndElement();	
		writer.writeEndElement();		
	}

	/**
	 * Returns the capabilities document for a WFS service.
	 * @param version The version of the WFS service to target
	 * @return The capabilities document as String
	 * @throws XMLStreamException on error
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs/getCapabilities")
	@Operation(
            summary = "Returns the capabilities document for a WFS service",
            description = "Returns the capabilities document for a WFS service")
	public Response getCapabilities(
			@Parameter(description="The version of the WFS service to target") @DefaultValue("2.0.0") @QueryParam("version") String version)
			throws XMLStreamException {
		if (!version.equals("2.0.0") && !version.equals("1.1.0"))
			version = "2.0.0";
		return constructCapabilities(version, version.substring(0, version.lastIndexOf('.')));
	}

	/**
	 * Describes a feature type according to the WFS1.0 specification.
	 * 
	 * @param writer XMLWriter to write the specification
	 * @param featuretype the feature type to consider
	 * @param versionnamespace the versionnamespace to write
	 * @param version the version to use
	 * @throws XMLStreamException on error
	 */
	public void describeFeatureTypeWFS10(XMLStreamWriter writer, JSONObject featuretype, String versionnamespace,
			String version) throws XMLStreamException {
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
		for (ResultFormatter format : ResultFormatter.resultMap.values()) {
			if (!format.exposedType.isEmpty()) {
				writer.writeStartElement("http://www.opengis.net/wfs", "Format");
				writer.writeCharacters(format.exposedType);
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
		if (!bboxCache.containsKey(featuretype.getString("name").toLowerCase())) {
			bboxCache.put(featuretype.getString("name").toLowerCase(),
					TripleStoreConnector.getBoundingBoxFromTripleStoreData(featuretype.getString("triplestore"),
							featuretype.getString("query")));
		}
		Double[] bbox = bboxCache.get(featuretype.getString("name").toLowerCase());
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "LatLongBoundingBox");
		writer.writeAttribute("minx", bbox[0] + "");
		writer.writeAttribute("miny", bbox[1] + "");
		writer.writeAttribute("maxx", bbox[2] + "");
		writer.writeAttribute("maxy", bbox[3] + "");
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Creates a feature type description.
	 * @param writer the XMLWriter for writing the feature type description
	 * @param featuretype the feature type to describe
	 * @param versionnamespace the versionnamespace to use
	 * @param version the version to write
	 * @throws XMLStreamException on error
	 */
	public void describeFeatureType(XMLStreamWriter writer, JSONObject featuretype, String versionnamespace,
			String version) throws XMLStreamException {
		if ("1.0.0".equals(version) || "1.1.0".equals(version)) {
			versionnamespace = "";
		} else {
			versionnamespace = "/" + versionnamespace;
			versionnamespace = versionnamespace.replace("//", "/");
		}
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "FeatureType");
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "Name");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "Title");
		writer.writeCharacters(featuretype.getString("name"));
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "DefaultCRS");
		writer.writeCharacters("urn:ogc:def:crs:EPSG::4326");
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "OutputFormats");
		for (ResultFormatter format : ResultFormatter.resultMap.values()) {
			if (!format.exposedType.isEmpty()) {
				writer.writeStartElement("http://www.opengis.net/wfs" + versionnamespace, "Format");
				writer.writeCharacters(format.exposedType);
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
		if (!bboxCache.containsKey(featuretype.getString("name").toLowerCase())) {
			bboxCache.put(featuretype.getString("name").toLowerCase(),
					TripleStoreConnector.getBoundingBoxFromTripleStoreData(featuretype.getString("triplestore"),
							featuretype.getString("query")));
		}
		Double[] bbox = bboxCache.get(featuretype.getString("name").toLowerCase());
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "WGS84BoundingBox");
		writer.writeAttribute("dimensions", "2");
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "lowerCorner");
		writer.writeCharacters(bbox[0] + " " + bbox[1]);
		writer.writeEndElement();
		writer.writeStartElement("http://www.opengis.net/ows/1.1", "upperCorner");
		writer.writeCharacters(bbox[2] + " " + bbox[3]);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Writes the spatial capabilities part of the WFS document.
	 * @param writer The XMLWriter to write the document part
	 * @param versionnamespace The versionnamespace to write
	 * @param namespace The namespace to use
	 * @throws XMLStreamException on error
	 */
	public void describeSpatialCapabilities(XMLStreamWriter writer, String versionnamespace, String namespace)
			throws XMLStreamException {
		writer.writeStartElement(namespace, "Spatial_Capabilities");
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
		writer.writeEndElement();
	}

	/**
	 * Creates the spatial capabilities document for a WFS in version 1.0.
	 * @param writer The XMLWriter to write the documentation with
	 * @param versionnamespace The versionnamespace to use for writing
	 * @param namespace The namespace to use
	 * @throws XMLStreamException on error
	 */
	public void describeSpatialCapabilitiesWFS10(XMLStreamWriter writer, String versionnamespace, String namespace)
			throws XMLStreamException {
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
		writer.writeStartElement(namespace, "Equals");
		writer.writeEndElement();
		writer.writeStartElement(namespace, "DWithin");
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Writes the scalar capabilities document for a WFS.
	 * @param writer The XMLStreamWriter to write the capabilities document with
	 * @param versionnamespace The versionnamespace to use
	 * @param namespace The namespace to use for writing
	 * @throws XMLStreamException on error
	 */
	public void describeScalarCapabilities(XMLStreamWriter writer, String versionnamespace, String namespace)
			throws XMLStreamException {
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

	/**
	 * Describes a feature type as an answer to a WFS request.
	 * @param typename The feature type to describe
	 * @param version The version of the WFS
	 * @return The feature type description document
	 * @throws XMLStreamException on error
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/wfs/describeFeatureType")
	@Operation(
            summary = "Describes a feature type as an answer to a WFS request",
            description = "Describes a feature type as an answer to a WFS request")
	public Response describeFeatureType(
			@Parameter(description="The feature type name to describe") @QueryParam("typename") String typename,
			@Parameter(description="The version of the WFS service") @DefaultValue("version") @QueryParam("version") String version) throws XMLStreamException {
		if (typename == null)
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
		String versionnamespace = version.substring(0, version.lastIndexOf('.'));
		if ("1.0.0".equals(version) || "1.1.0".equals(version)) {
			versionnamespace = "";
		} else {
			versionnamespace = "/" + versionnamespace;
			versionnamespace = versionnamespace.replace("//", "/");
		}
		writer.writeStartDocument();
		writer.writeStartElement("schema");
		// writer.writeAttribute("targetNamespace",(workingobj.has("namespace")?workingobj.getString("namespace"):wfsconf.getString("baseurl")));
		writer.writeDefaultNamespace("http://www.w3.org/2001/XMLSchema");
		writer.writeNamespace("app",
				(workingobj.has("namespace") ? workingobj.getString("namespace") : wfsconf.getString("baseurl")));
		writer.setPrefix("app",
				(workingobj.has("namespace") ? workingobj.getString("namespace") : wfsconf.getString("baseurl")));
		writer.writeNamespace("wfs", "http://www.opengis.net/wfs" + versionnamespace);
		writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
		writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
		writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
		writer.writeNamespace("fes", "http://www.opengis.net/fes" + versionnamespace);
		writer.writeNamespace("gml", "http://www.opengis.net/gml");
		writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
		writer.writeStartElement("element");
		writer.writeAttribute("name", typename);
		writer.writeAttribute("type", typename + "Type");
		writer.writeAttribute("substitutionGroup", "gml:_Feature");
		writer.writeEndElement();
		writer.writeStartElement("complexType");
		writer.writeAttribute("name", typename + "Type");
		writer.writeStartElement("complexContent");
		writer.writeStartElement("extension");
		writer.writeAttribute("base", "gml:AbstractFeatureType");
		writer.writeStartElement("all");
		if (!featureTypeCache.containsKey(typename.toLowerCase())) {
			featureTypeCache.put(typename.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
		}
		writer.writeStartElement("element");
		writer.writeAttribute("name", "the_geom");
		writer.writeAttribute("minOccurs", "0");
		writer.writeAttribute("type",
				"gml:" + (workingobj.has("geometrytype") ? workingobj.getString("geometrytype") : "Geometry")
						+ "PropertyType");
		writer.writeEndElement();
		Map<String, String> mapping = featureTypeCache.get(typename.toLowerCase());
		for (String elem : mapping.keySet()) {
			if (elem.equals("namespaces"))
				continue;
			writer.writeStartElement("element");
			if (elem.startsWith("http") && elem.contains("#")) {
				writer.writeAttribute("name", elem.substring(elem.lastIndexOf('#') + 1));
			} else if (elem.startsWith("http") && elem.contains("/")) {
				writer.writeAttribute("name", elem.substring(elem.lastIndexOf('/') + 1));
			} else {
				writer.writeAttribute("name", elem);
			}
			if (mapping.get(elem).contains("^^")) {
				writer.writeAttribute("type", mapping.get(elem).substring(mapping.get(elem).lastIndexOf("^^") + 2));
			} else if (mapping.get(elem).startsWith("http") || mapping.get(elem).startsWith("file:/")) {
				writer.writeAttribute("type", "string");
			} else {
				writer.writeAttribute("type", "string");
			}
			writer.writeAttribute("minOccurs", "0");
			writer.writeEndElement();
		}
		// writer.writeEndElement();
		// this.describeFeatureType(writer,
		// workingobj,version.substring(0,version.lastIndexOf('.')),version);
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
	}

	/**
	 * Describes a feature type in JSON.
	 * @param typename The type name to describe
	 * @param version The version of the WFS
	 * @return The JSON document to return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/wfs/describeFeatureTypeJSON")
	@Operation(
            summary = "Describes a feature type in JSON",
            description = "Describes a feature type in JSON")
	public Response describeFeatureTypeJSON(
			@Parameter(description="The feature type to describe") @QueryParam("typename") String typename,
			@Parameter(description="The version of the WFS service") @DefaultValue("version") @QueryParam("version") String version) {
		if (typename == null)
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
		JSONObject schema = new JSONObject();
		JSONArray required = new JSONArray();
		schema.put("definitions", new JSONObject());
		schema.put("$schema", "http://json-schema.org/draft-07/schema#");
		schema.put("$id", wfsconf.getString("baseurl") + "/collections/" + typename + "/schema?f=json");
		schema.put("type", "object");
		schema.put("title", typename);
		schema.put("description", (workingobj.has("description") ? workingobj.getString("description") : ""));
		schema.put("readOnly", true);
		schema.put("writeOnly", false);
		schema.put("required", required);
		JSONObject properties = new JSONObject();
		schema.put("properties", properties);
		if (!featureTypeCache.containsKey(typename.toLowerCase())) {
			featureTypeCache.put(typename.toLowerCase(),
					TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
							workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
		}
		Map<String, String> mapping = featureTypeCache.get(typename.toLowerCase());
		for (String elem : mapping.keySet()) {
			if (elem.equals("namespaces"))
				continue;
			if (elem.startsWith("http") && elem.contains("#")) {
				required.put(elem.substring(elem.lastIndexOf('#') + 1));
			} else if (elem.startsWith("http") && elem.contains("/")) {
				required.put(elem.substring(elem.lastIndexOf('/') + 1));
			} else {
				required.put(elem);
			}
		}
		for (String elem : mapping.keySet()) {
			if (elem.equals("namespaces"))
				continue;
			JSONObject prop = new JSONObject();
			String curprop = "";
			if (elem.startsWith("http") && elem.contains("#")) {
				curprop = elem.substring(elem.lastIndexOf('#') + 1);
				properties.put(curprop, prop);
			} else if (elem.startsWith("http") && elem.contains("/")) {
				curprop = elem.substring(elem.lastIndexOf('/') + 1);
				properties.put(elem.substring(elem.lastIndexOf('/') + 1), prop);
			} else {
				curprop = elem;
				properties.put(elem, prop);
			}
			prop.put("$id", wfsconf.getString("baseurl") + "/collections/" + typename + "/" + curprop);
			if (mapping.get(elem).contains("^^")) {
				prop.put("type", mapping.get(elem).substring(mapping.get(elem).lastIndexOf("^^") + 2));
			} else if (mapping.get(elem).startsWith("http") || mapping.get(elem).startsWith("file:/")) {
				prop.put("type", "string");
			} else {
				prop.put("type", "string");
			}
			prop.put("title", curprop);
			prop.put("description", curprop + " Property");
			JSONArray examples = new JSONArray();
			prop.put("examples", examples);
		}
		return Response.ok(schema.toString(2)).type(MediaType.APPLICATION_JSON).build();
	}

	
	/**
	 * Gets geospatial classes from a SPARQL endpoint.
	 * @param endpoint The SPARQL endpoint
	 * @return A JSON document containing class descriptions
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/service/getGeoClassesFromEndpoint")
	@Operation(
            summary = "Gets geospatial classes from a SPARQL endpoint",
            description = "Gets geospatial classes from a SPARQL endpoint")
	public Response getGeoClassesFromOntology(
			@Parameter(description="The SPARQL endpoint to load classes from") @QueryParam("endpoint") String endpoint) {
		if(triplestoreconf.getJSONObject("endpoints").has(endpoint)) {
			Map<String,String> classes=TripleStoreConnector.getClassesFromOntology(triplestoreconf.getJSONObject("endpoints").getJSONObject(endpoint));
			JSONObject result=new JSONObject();
			for(String cls:classes.keySet()) {
				result.put(cls, classes.get(cls));
			}
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();			
		}else {
			return Response.ok("{}").type(MediaType.APPLICATION_JSON).build();	
		}
	}
	
	/**
	 * Gets available SPARQL endpoitns from the triplestore configuration.
	 * @return A JSON document containing SPARQL endpoint descriptions
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/service/getEndpoints")
	@Operation(
            summary = "Gets available SPARQL endpoitns from the triplestore configuration",
            description = "Gets available SPARQL endpoitns from the triplestore configuration")
	public Response getEndpoints() {
		return Response.ok(triplestoreconf.toString(2)).type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Gets available properties associated with a certain class.
	 * @param endpoint The SPARQL endpoint to query
	 * @param classs The class to use in the query
	 * @return A JSON document containing the queried properties
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/service/getPropertiesByClass")
	@Operation(
            summary = "Gets available properties associated with a certain class",
            description = "Gets available properties associated with a certain class")
	public Response getPropertiesByClass(
			@Parameter(description="The SPARQL endpoint to load properties from") @QueryParam("endpoint") String endpoint,
			@Parameter(description="The class from which properties should be loaded") @QueryParam("class") String classs) {
		Map<String, String> classes=TripleStoreConnector.getPropertiesByClass(endpoint, classs);
		JSONObject result=new JSONObject();
		for(String cls:classes.keySet()) {
			result.put(cls,classes.get(cls).toString());
		}
		return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
	}
	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/getFeature")
	@Operation(
            summary = "Gets a feature with a given feature id",
            description = "Gets a feature with a given feature id")
	public Response getFeature(@QueryParam("typename") String typename,
			@Parameter(description="The output format of the WFS service request") @DefaultValue("json") @QueryParam("outputFormat") String output,
			@Parameter(description="The amount of features to be returned",example="10") @DefaultValue("10") @QueryParam("count") String count,
			@Parameter(description="The starting index of the WFS request") @DefaultValue("0") @QueryParam("startindex") String startindex,
			@Parameter(description="The name of the CRS to be used") @DefaultValue("") @QueryParam("srsName") String srsName,
			@Parameter(description="Indicates the sorting order") @DefaultValue("ASC") @QueryParam("sortBy") String sortBy,
			@Parameter(description="The style to apply to the returned collection if any") @DefaultValue("") @QueryParam("styles") String style,
			@Parameter(description="The version of the WFS",example="2.0.0") @DefaultValue("2.0.0") @QueryParam("version") String version,
			@Parameter(description="Indicates a specific resource id to be queried") @DefaultValue("") @QueryParam("resourceid") String resourceids,
			@Parameter(description="A WFS filter expression") @DefaultValue("") @QueryParam("filter") String filter,
			@Parameter(description="The filter language to be used in the filterExpression parameter") @DefaultValue("CQL") @QueryParam("filterLanguage") String filterLanguage,
			@Parameter(description="The result type to return") @DefaultValue("results") @QueryParam("resultType") String resultType)
			throws JSONException, XMLStreamException {
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
		if (workingobj == null)
			throw new NotFoundException();
		String res = "";
		System.out.println(hitCache);
		if (resultType.equalsIgnoreCase("hits") && hitCache.containsKey(typename.toLowerCase())
				&& (hitCache.get(typename.toLowerCase()).getOne().getTime() + milliesInDays) > System
						.currentTimeMillis()) {
			res = hitCache.get(typename.toLowerCase()).getTwo();
		} else {
			if (!workingobj.has("attcount") && !workingobj.getString("query").contains("?rel")
					&& !workingobj.getString("query").contains("?val")) {
				featureTypeCache.put(typename.toLowerCase(),
						TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
								workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
				workingobj.put("attcount", 1);
			} else if (!workingobj.has("attcount")) {
				featureTypeCache.put(typename.toLowerCase(),
						TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
								workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
			}
			if (workingobj.getInt("attcount") == 0) {
				workingobj.put("attcount", 1);
			}
			try {
				res = TripleStoreConnector.executeQuery(workingobj.getString("query"),
						workingobj.getString("triplestore"), output,
						"" + count,
						"" + startindex, "gml:featureMember",
						typename, resourceids, workingobj, filter, resultType, srsName, "", style,false,(workingobj.has("invertXY")?workingobj.getBoolean("invertXY"):false));
				System.out.println(res);
				if (res==null || res.isEmpty()) {
					throw new NotFoundException();
				}
				if (resultType.equalsIgnoreCase("hits")) {
					hitCache.put(typename.toLowerCase(),
							new Tuple<Date, String>(new Date(System.currentTimeMillis()), res));
				}
			} catch (JSONException | XMLStreamException e1) {
				e1.printStackTrace();
				return this.createExceptionResponse(e1, "");
			}
		}
		System.out.println(output);
		if (output.contains("gml")) {
			StringWriter strwriter = new StringWriter();
			XMLOutputFactory outputf = XMLOutputFactory.newInstance();
			XMLStreamWriter writer;
			try {
				writer = new IndentingXMLStreamWriter(outputf.createXMLStreamWriter(strwriter));
				writer.writeStartDocument();
				writer.writeStartElement("wfs:FeatureCollection");
				writer.writeDefaultNamespace((wfsconf.getString("baseurl") + "/").replace("//", "/"));
				writer.writeNamespace("ows", "http://www.opengis.net/ows/1.1");
				writer.writeNamespace("sf", "http://www.opengis.net/ogcapi-features-1/1.0/sf");
				writer.writeNamespace("ogc", "http://www.opengis.net/ogc");
				writer.writeNamespace("gml", "http://www.opengis.net/gml");
				writer.writeNamespace("wfs", "http://www.opengis.net/wfs");
				writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
				writer.setPrefix("gml", "http://www.opengis.net/gml");
				writer.setPrefix("wfs", "http://www.opengis.net/wfs");

				if (resultType.equalsIgnoreCase("hits")) {
					writer.writeAttribute("numberOfFeatures", res);
				} else {
					for (String ns : nameSpaceCache.get(typename.toLowerCase()).keySet()) {
						writer.setPrefix(nameSpaceCache.get(typename.toLowerCase()).get(ns), ns);
						writer.writeNamespace(nameSpaceCache.get(typename.toLowerCase()).get(ns), ns);
					}
					writer.writeCharacters("");
					writer.flush();
					strwriter.write(res);
				}
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.flush();
				String result=strwriter.toString();
				String firstline=result.substring(0,result.indexOf(System.lineSeparator())).trim();
				if(firstline.isEmpty()) {
					return Response.ok(result.substring(result.indexOf(System.lineSeparator())+1)).type(MediaType.APPLICATION_XML).build();
				}else {
					return Response.ok(result).type(MediaType.APPLICATION_XML).build();
				}			
			} catch (Exception e) {
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}
		} else if (output.contains("json")) {
			JSONObject result = new JSONObject();
			JSONArray links = new JSONArray();
			JSONObject jsonresult = new JSONObject(res);
			JSONArray features = jsonresult.getJSONArray("features");
			if (jsonresult.has("@context")) {
				result.put("@context", jsonresult.getJSONObject("@context"));
			}
			result.put("type", "FeatureCollection");
			result.put("links", links);
			result.put("timeStamp", System.currentTimeMillis());
			if (resultType.equalsIgnoreCase("hits")) {
				result.put("numberMatched", res);
			} else {
				result.put("numberMatched", features.length());
			}
			result.put("numberReturned", features.length());
			result.put("features", features);
			System.out.println("EXPORT JSON: " + result.toString());
			return Response.ok(result.toString(2)).type(MediaType.APPLICATION_JSON).build();
		} else if (output.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append(htmlHead);
			builder.append("<body><header><h1 align=\"center\">");
			builder.append(typename);
			builder.append("</h1></header><div class=\"container-fluid\" role=\"main\"><div class=\"row\">");
			builder.append(res);
			// builder.append("<script>$( document ).ready(function()
			// {$('#queryres').DataTable({\"scrollX\":\"100%\",\"scrollCollapse\":
			// true});});</script></div></div></div></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else if (output.contains("csv") || output.contains("geouri") || output.contains("geohash")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		} else if (output.contains("gpx")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/getGmlObject")
	public Response getGmlObject(@QueryParam("typename") String typename, @QueryParam("GmlObjectId") String gmlobjectid,
			@DefaultValue("4") @QueryParam("traverseXlinkDepth") String traverseXlinkDepth,
			@DefaultValue("gml") @QueryParam("outputFormat") String output) {
		try {
			return this.getFeature(typename, output, "1", "0", "", "ASC", "", "2.0.0", gmlobjectid, "", "CQL", "");
		} catch (JSONException | XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return this.createExceptionResponse(e, "");
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wfs/getPropertyValue")
	public Response getPropertyValue(
			@Parameter(description="Feature type to query") @QueryParam("typename") String typename,
			@QueryParam("valuereference") String propertyname,
			@DefaultValue("json") @QueryParam("outputFormat") String output,
			@DefaultValue("") @QueryParam("resourceids") String resourceids,
			@DefaultValue("") @QueryParam("filter") String filter, @DefaultValue("0") @QueryParam("count") String count,
			@DefaultValue("results") @QueryParam("resultType") String resultType) {
		System.out.println(typename);
		System.out.println(propertyname);
		if (typename == null || propertyname == null) {
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
		if (workingobj == null)
			throw new NotFoundException();
		if (!propertyname.startsWith("http")) {
			if (!featureTypeCache.containsKey(typename.toLowerCase())) {
				featureTypeCache.put(typename.toLowerCase(),
						TripleStoreConnector.getFeatureTypeInformation(workingobj.getString("query"),
								workingobj.getString("triplestore"), workingobj.getString("name"), workingobj));
			}
			for (String key : featureTypeCache.get(typename.toLowerCase()).keySet()) {
				if (key.contains(propertyname)) {
					propertyname = key;
					break;
				}
			}
		}
		String res = "";
		try {
			res = TripleStoreConnector.executePropertyValueQuery(workingobj.getString("triplestore"), output,
					propertyname, "gml:featureMember", typename, resourceids, workingobj, filter, count, resultType,
					"");
			System.out.println(res);
		} catch (JSONException | XMLStreamException e1) {
			e1.printStackTrace();
			return this.createExceptionResponse(e1, "");
		}
		if (resultType.equalsIgnoreCase("hits")) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}
		if (output != null && output.contains("json")) {
			return Response.ok(res).type(MediaType.APPLICATION_JSON).build();
		} else if (output != null && output.contains("gml")) {
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
			} catch (Exception e) {
				e.printStackTrace();
				return this.createExceptionResponse(e, "");
			}
			return Response.ok(strwriter.toString()).type(MediaType.APPLICATION_XML).build();
		} else if (output != null && output.contains("html")) {
			StringBuilder builder = new StringBuilder();
			builder.append("<html><head></head><body><header id=\"header\"><h1 align=center>PropertyRequest: " + typename + "["
					+ propertyname
					+ "]</h1></header><div class=\"container-fluid\" role=\"main\"><div class=\"row\"><div class=\"col-sm-12\">");
			builder.append(res);
			builder.append("</div></div></div><footer id=\"footer\"><a href=\"" + wfsconf.getString("baseurl")
					+ "/?f=html\">Back to LandingPage</a></footer></body></html>");
			return Response.ok(builder.toString()).type(MediaType.TEXT_HTML).build();
		} else if (output != null) {
			return Response.ok(res).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}

	
	/**
	 * Adds a feature type to the SemanticWFS.
	 * @param sparqlQuery The SPARQL query to be used by this feature type.
	 * @param name The feature type name
	 * @param indvar The individual variable
	 * @param bboxlimit 
	 * @param classs
	 * @param type
	 * @param description
	 * @param targetCRS
	 * @param namespace
	 * @param triplestore
	 * @param username
	 * @param password
	 * @param authtoken
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/addFeatureType")
	@Operation(
            summary = "Adds a feature type to the SemanticWFS",
            description = "Adds a feature type to the SemanticWFS")
	public Boolean addFeatureType(
			@Parameter(description="The SPARQL query used to retrive the feature type") @QueryParam("query") String sparqlQuery, 
			@Parameter(description="The featuretype name") @QueryParam("typename") String name, 
			@Parameter(description="The variable indicating the individual in the query") @DefaultValue("item") @QueryParam("indvar") String indvar,
			@Parameter(description="The limit of the initial sample to retrieve") @DefaultValue("500") @QueryParam("bboxlimit") String bboxlimit,
			@Parameter(description="The class to query") @QueryParam("class") String classs, 
			@Parameter(description="The service type which is queried") @DefaultValue("WFS") @QueryParam("type") String type,
			@Parameter(description="The feature type description") @DefaultValue("")  @QueryParam("description") String description, 
			@Parameter(description="The EPSG of the feature type to be added") @DefaultValue("EPSG:4326") @QueryParam("targetCRS") String targetCRS,
			@Parameter(description="The namespace used for the feature type") @QueryParam("namespace") String namespace, 
			@Parameter(description="The triple store to query when loading the feature type")  @QueryParam("triplestore") String triplestore,
			@Parameter(description="The username needed for authentication") @DefaultValue("") @QueryParam("username") String username, 
			@Parameter(description="The password needed for authentication") @DefaultValue("") @QueryParam("password") String password,
			@Parameter(description="The authtoken for authentication") @DefaultValue("") @QueryParam("authtoken") String authtoken) {
		User user=UserManagementConnection.getInstance().loginAuthToken(authtoken);
		System.out.println("Add Feature Type");
		if(name==null && classs!=null) {
			name=classs;
		}
		if(true || name!=null && !name.isEmpty() && user!=null && (user.getUserlevel()==UserType.Configurer || user.getUserlevel()==UserType.Administrator)) {
			JSONArray datasets = wfsconf.getJSONArray("datasets");
			System.out.println(wfsconf);
			System.out.println(datasets);
			System.out.println("To add: "+name+" "+namespace+" "+triplestore+" "+sparqlQuery);
			JSONObject toadd = new JSONObject();
			toadd.put("name", name);
			toadd.put("indvar", indvar);
			toadd.put("bboxlimit",bboxlimit);
			toadd.put("class", classs);
			toadd.put("type",type);
			toadd.put("description",description);
			toadd.put("targetCRS",targetCRS);
			toadd.put("namespace", namespace);
			toadd.put("triplestore", triplestore);
			toadd.put("query", sparqlQuery);
			System.out.println("Adding feature type: "+toadd);
			datasets.put(toadd);
			return true;
		}
		return false;
	}
	
	/**
	 * Adds a SPARQL endpoint to the triplestoreconf configuration.
	 * @param name The name of the SPARQL endpoint
	 * @param endpoint The SPARQL endpoint address
	 * @param typerel The type relation of the SPARQL endpoint
	 * @param georel The geometrical relation of the SPARQL endpoint
	 * @param username A username required to save the configuration
	 * @param password A password required to save the configuration
	 * @param authtoken An authtoken authorizing the configuration
	 * @return True if the endpoint has been successfully added, false otherwise
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/addEndpoint")
	@Operation(
            summary = "Adds a SPARQL endpoint to the triplestoreconf configuration",
            description = "Adds a SPARQL endpoint to the triplestoreconf configuration")
	public Boolean addEndpoint(@Parameter(description="The name of the SPARQL endpoint to add") @QueryParam("name") String name,
			@Parameter(description="The address of the SPARQL endpoint") @QueryParam("endpoint") String endpoint,
			@Parameter(description="The type property used in this SPARQL endpoint") @QueryParam("typerel") String typerel,
			@Parameter(description="The geometry property used by this SPARQL endpoint") @QueryParam("georel") String georel,
			@Parameter(description="Username for authorization") @QueryParam("username") String username, 
			@Parameter(description="Password for authorization") @QueryParam("password") String password,
			@Parameter(description="Authtoken if the user is already logged in") @DefaultValue("") @QueryParam("authtoken") String authtoken) {
		User user=UserManagementConnection.getInstance().loginAuthToken(authtoken);
		System.out.println("Add Feature Type");
		if(true || name!=null && !name.isEmpty() && user!=null && (user.getUserlevel()==UserType.Configurer || user.getUserlevel()==UserType.Administrator)) {
			JSONArray datasets = wfsconf.getJSONArray("datasets");
			System.out.println(wfsconf);
			System.out.println(datasets);
			System.out.println("To add: "+name+" "+endpoint+" "+typerel+" "+georel);
			JSONObject toadd = new JSONObject();
			toadd.put("name", name);
			toadd.put("endpoint", endpoint);
			toadd.put("type", typerel);
			toadd.put("geo", new JSONArray());
			toadd.getJSONArray("geo").put(georel);
			System.out.println("Adding feature type: "+toadd);
			triplestoreconf.getJSONObject("endpoints").put(endpoint,toadd);
			return true;
		}
		return false;
	}

	
	/**
	 * Saves feature types in the SemanticWFS Service.
	 * @param featureTypesJSON The featuretype JSON to save
	 * @param authtoken The authtoken authorizing the save operation
	 * @return True if successful, false otherwise
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/saveFeatureTypes")
	@Operation(
            summary = "Saves feature types in the SemanticWFS Service",
            description = "Saves feature types in the SemanticWFS Service")
	public Boolean saveFeatureTypes(
			@Parameter(description="JSON object containing feature types to be saved") @QueryParam("featjson") String featureTypesJSON,
			@Parameter(description="Authtoken for authorization") @DefaultValue("") @QueryParam("authtoken") String authtoken) {
		User user=UserManagementConnection.getInstance().loginAuthToken(authtoken);
		if(user!=null && (user.getUserlevel()==UserType.Configurer || user.getUserlevel()==UserType.Administrator)) {
			JSONArray datasets = wfsconf.getJSONArray("featjson");
			wfsconf.put("datasets", datasets);
			return true;
		}
		return false;
	}
	
	/**
	 * Performs a login returning an authtoken if successful.
	 * @param username The username
	 * @param password The password
	 * @return An authtoken if successful, an emptry String if not
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/login")
	@Operation(
            summary = "Performs a login returning an authtoken if successful",
            description = "Performs a login returning an authtoken if successful")
    public Response login(@Parameter(description="Username for authorization") @QueryParam("username") String username,
    		@Parameter(description="Password for authorization") @QueryParam("password") String password) { 
		final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir); 
        User user=UserManagementConnection.getInstance().login(username, password);
        if(user!=null) {
        	return Response.ok(user.authToken).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.ok("").type(MediaType.TEXT_PLAIN).build();
	}

	/**
	 * Returns featuretypes known by the SemanticWFS service.
	 * @return The feature type document as JSON.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/featureTypes")
	public String getFeatureTypes() {
		return wfsconf.toString(2);
	}

	/**
	 * Returns prefixes known by the SemanticWFS service.
	 * @return The prefix document as JSON.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/prefixes")
	@Operation(
            summary = "Returns prefixes known by the SemanticWFS service",
            description = "Returns prefixes known by the SemanticWFS service")
	public String prefixes() {
		return TripleStoreConnector.prefixCollection;
	}

	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/addPrefixes")
	public String addPrefixes(@QueryParam("query") String sparqlQuery, @QueryParam("typename") String name,
			@QueryParam("namespace") String namespace, @QueryParam("triplestore") String triplestore) {
		return wfsconf.toString(2);
	}

	/**
	 * Returns the query configuration document.
	 * @return The query configuration document as JSON.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/queryConfigs")
	@Operation(
            summary = "Returns the query configuration document",
            description = "Returns the query configuration document")
	public Response queryConfigs() {
		JSONObject res = new JSONObject();
		for (int i = 0; i < wfsconf.getJSONArray("datasets").length(); i++) {
			JSONObject curjson = wfsconf.getJSONArray("datasets").getJSONObject(i);
			JSONObject instance = new JSONObject();
			if (!res.has(curjson.getString("triplestore"))) {
				res.put(curjson.getString("triplestore"), new JSONArray());
			}
			res.getJSONArray(curjson.getString("triplestore")).put(instance);
			instance.put("triplestore", curjson.getString("triplestore"));
			instance.put("query", curjson.getString("query"));
			instance.put("name", curjson.getString("name"));
		}
		return Response.ok(res.toString(2)).type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Exposes a query service which returns a SPARQL query result as REST.
	 * @param query The query to send
	 * @param endpoint The endpoint to query
	 * @return The query result
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/queryservice")
	@Operation(
            summary = "Exposes a query service which returns a SPARQL query result as REST",
            description = "Exposes a query service which returns a SPARQL query result as REST")
	public String queryService(
			@Parameter(description="SPARQL query to be resolved") @QueryParam("query") String query,
			@Parameter(description="SPARQL endpoint to query") @QueryParam("endpoint") String endpoint) {
		final String dir = System.getProperty("user.dir");
		System.out.println("current dir = " + dir);
		return TripleStoreConnector.executeQuery(query, endpoint, false);
	}

	/**
	 * Exposes a query service which returns a SPARQL query result as REST.
	 * @param query The query to send
	 * @param endpoint The endpoint to query
	 * @param geojson indicates whether to return an additional representation as GeoJSON for a map view
	 * @return The query result
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/service/queryservicegeojson")
	@Operation(
            summary = "Exposes a query service which returns a SPARQL query result as REST",
            description = "Exposes a query service which returns a SPARQL query result as REST with the option of returning an additional GeoJSON")
	public String queryService(
			@Parameter(description="SPARQL query to be resolved") @QueryParam("query") String query,
			@Parameter(description="SPARQL endpoint to be queried") @QueryParam("endpoint") String endpoint,
			@Parameter(description="Indicates whether geojson should be returned to be shown in a map view") @QueryParam("geojson") String geojson) {
		final String dir = System.getProperty("user.dir");
		System.out.println("current dir = " + dir);
		return TripleStoreConnector.executeQuery(query, endpoint, true);
	}

	/**
	 * Transaction method of the WFS specification.
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/transaction")
	public String transaction() {
		return null;
	}

	/**
	 * Lock Feature method of the WFS specification.
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/lockFeature")
	public String lockFeature() {
		return null;
	}

}
