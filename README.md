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



