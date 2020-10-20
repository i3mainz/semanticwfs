package de.hsmainz.cs.semgis.wfs.readers;

import java.util.LinkedList;

import org.locationtech.jts.geom.Coordinate;

import de.hsmainz.cs.semgis.wfs.util.CovJSONCoverage;

public class XYZReader extends CoverageReader {

	@Override
	public CovJSONCoverage readCoverageLiteral(String literalValue) {
		CovJSONCoverage result=new CovJSONCoverage();
		result.ranges.put("http://www.opengis.net/ont/geosparql#altitude", new LinkedList<>());
		for (String line : literalValue.split(System.lineSeparator())) {
			String[] coord = line.split(" ");
			result.coords.add(new Coordinate(Double.valueOf(coord[0]),Double.valueOf(coord[1])));
			result.ranges.get("http://www.opengis.net/ont/geosparql#altitude").add(Double.valueOf(coord[2]));
		}
		return result;
	}

}
