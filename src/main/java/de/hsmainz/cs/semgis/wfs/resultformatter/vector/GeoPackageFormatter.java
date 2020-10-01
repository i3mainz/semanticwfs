package de.hsmainz.cs.semgis.wfs.resultformatter.vector;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.jena.query.ResultSet;

import de.hsmainz.cs.semgis.wfs.resultformatter.ResultFormatter;
import de.hsmainz.cs.semgis.wfs.resultstyleformatter.StyleObject;

public class GeoPackageFormatter extends ResultFormatter {

	@Override
	public String formatter(ResultSet results, String startingElement, String featuretype, String propertytype,
			String typeColumn, Boolean onlyproperty, Boolean onlyhits, String srsName, String indvar, String epsg,
			List<String> eligiblenamespaces, List<String> noteligiblenamespaces, StyleObject mapstyle,
			Boolean alternativeFormat, Boolean invertXY,Boolean coverage,Writer out) throws XMLStreamException {
		/*
		File createdGeoPackage = GeoPackageManager.create(newGeoPackage);

		// Open a GeoPackage
		GeoPackage geoPackage = GeoPackageManager.open(existingGeoPackage);

		// GeoPackage Table DAOs
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		ContentsDao contentsDao = geoPackage.getContentsDao();
		GeometryColumnsDao geomColumnsDao = geoPackage.getGeometryColumnsDao();
		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
		SchemaExtension schemaExtension = new SchemaExtension(geoPackage);
		DataColumnsDao dao = schemaExtension.getDataColumnsDao();
		DataColumnConstraintsDao dataColumnConstraintsDao = schemaExtension
				.getDataColumnConstraintsDao();
		MetadataExtension metadataExtension = new MetadataExtension(geoPackage);
		MetadataDao metadataDao = metadataExtension.getMetadataDao();
		MetadataReferenceDao metadataReferenceDao = metadataExtension
				.getMetadataReferenceDao();
		ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();

		// Feature and tile tables
		List<String> features = geoPackage.getFeatureTables();
		List<String> tiles = geoPackage.getTileTables();

		// Query Features
		FeatureDao featureDao = geoPackage.getFeatureDao(features.get(0));
		FeatureResultSet featureResultSet = featureDao.queryForAll();
		try {
			while (featureResultSet.moveToNext()) {
				FeatureRow featureRow = featureResultSet.getRow();
				GeoPackageGeometryData geometryData = featureRow.getGeometry();
				if (geometryData != null && !geometryData.isEmpty()) {
					Geometry geometry = geometryData.getGeometry();
					// ...
				}
			}
		} finally {
			featureResultSet.close();
		}*/
		return null;
	}

}
