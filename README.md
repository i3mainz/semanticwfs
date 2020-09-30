# semanticwfs
Semantic WFS with a Linked Data Backend

This project is a Java wrapper connecting to a triple store and exposing results of SPARQL queries including a geometry literal as a WFS geospatial webservice.

The project maps FeatureTypes to SPARQL queries and allows the configuration of said SPARQL queries in a webinterface.

This webservice supports the following WFS types:

* WFS 1.0, 1.1.0 and 2.0
* OGC API Features Core, OGC API Features CRS and OGC API Features CQL partly

![SemanticWFS Infrastructure](infrastructure.png)

## Adding Triple Stores

Triple stores can be added to the SemanticWFS using the triplestoreconf.json file. The file needs a SPARQL endpoint address, and certain configurations

## Adding Collections

Once a triple store has been configured in the SemanticWFS implementation, a OGC API Feature Collection may be defined from a SPARQL query and a CRS definition.
These collections are stored in the wfsconf.json configuration file.

## Export formats

The SemanticWFS currently supports the following formats:

  * RDF Serizalizations:  [HexTuples](https://github.com/ontola/hextuples), [JSON-LD](https://json-ld.org/spec/latest/json-ld/), [N-Triples](https://www.w3.org/TR/n-triples/), [Notation3](https://www.w3.org/TeamSubmission/n3/), [NQuads](https://www.w3.org/TR/n-quads/), [RDF/JSON](https://www.w3.org/TR/rdf-json/), [RDF/XML](https://www.w3.org/TR/rdf-syntax-grammar/), [TriG](https://www.w3.org/TR/trig/), [TriX](https://www.hpl.hp.com/techreports/2004/HPL-2004-56.html), [TTL](https://www.w3.org/TR/turtle/)
  * Vector geometry formats: [GeoJSON](https://geojson.org/), [GeoJSON-LD](https://geojson.org/geojson-ld/), [GeoURI](https://geouri.org), [KML](https://www.ogc.org/standards/kml/), [GML](https://www.ogc.org/standards/gml), 
  [GPX](https://www.topografix.com/gpx.asp), LatLonText, [MapML](https://maps4html.org/MapML/spec/), [OSM/XML](https://wiki.openstreetmap.org/wiki/OSM_XML), [SVG](https://www.w3.org/Graphics/SVG/), [WKT](https://www.ogc.org/standards/sfa), [HexWKB](https://www.iso.org/standard/40114.html)
  * Coverage-centric formats:
     * [CoverageJSON](https://covjson.org) to display Points, LineStrings, Polygons and GridCoverages
     * [XYZASCII](https://gdal.org/drivers/raster/xyz.html) to display coverage data or simply a list of points
  * Multiple purpose formats: [CSV](https://tools.ietf.org/html/rfc4180), [JSON](https://www.json.org/json-en.html), [JSONP](http://jsonp.eu), [ODS](http://www.openoffice.org/sc/excelfileformat.pdf), [XLS](http://www.openoffice.org/sc/excelfileformat.pdf), [XLSX](http://www.openoffice.org/sc/excelfileformat.pdf), [YAML](https://yaml.org)
  * Binary formats: [BSON](http://bsonspec.org/), [RDF/EXI](https://www.w3.org/TR/exi/), [RDF/Thrift](https://afs.github.io/rdf-thrift/)
  * Streaming formats: [JSON Sequential](https://tools.ietf.org/html/rfc7464), [GeoJSON Sequential](https://github.com/geojson/geojson-text-sequences)



