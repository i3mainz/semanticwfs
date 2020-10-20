package de.hsmainz.cs.semgis.wfs.readers;

import de.hsmainz.cs.semgis.wfs.util.CovJSONCoverage;

public abstract class CoverageReader {

	public abstract CovJSONCoverage readCoverageLiteral(String literal);
	
}
