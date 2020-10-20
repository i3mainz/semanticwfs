package de.hsmainz.cs.semgis.wfs.readers;

import org.json.JSONObject;

import de.hsmainz.cs.semgis.wfs.util.CovJSONCoverage;

public class ASCIIGridReader extends CoverageReader {

	@Override
	public CovJSONCoverage readCoverageLiteral(String literalValue) {
		CovJSONCoverage result=new CovJSONCoverage();
		Double cellsize = 1., xllcorner = 1., yllcorner = 1.;
		Integer nxcols = null, nycols = null;
		for (String line : literalValue.split(System.lineSeparator())) {
			/*if (line.startsWith("ncols")) {
				result.getJSONObject("domain").getJSONObject("axes").getJSONObject("x").put("num",
						Integer.valueOf(line.replace("ncols", "").trim()));
				result.getJSONObject("domain").getJSONObject("axes").getJSONObject("y").put("num",
						Integer.valueOf(line.replace("ncols", "").trim()));
				nxcols = Integer.valueOf(line.replace("ncols", "").trim());
			} else if (line.startsWith("xllcorner")) {
				result.getJSONObject("domain").getJSONObject("axes").getJSONObject("x").put("start",
						Double.valueOf(line.replace("xllcorner", "").trim()));
				xllcorner = Double.valueOf(line.replace("xllcorner", "").trim());
			} else if (line.startsWith("yllcorner")) {
				result.getJSONObject("domain").getJSONObject("axes").getJSONObject("y").put("start",
						Double.valueOf(line.replace("yllcorner", "").trim()));
				yllcorner = Double.valueOf(line.replace("yllcorner", "").trim());
			} else if (line.startsWith("cellsize")) {
				cellsize = Double.valueOf(line.replace("cellsize", "").trim());
			} else if (line.startsWith("nrows")) {
				nycols = Integer.valueOf(line.replace("nrows", "").trim());
				continue;
			} else if (line.startsWith("NODATA_value")) {
				nodata = line.replace("NODATA_value", "").trim();
			} else {
				if (nxcols != null
						&& result.getJSONObject("domain").getJSONObject("axes").getJSONObject("x").has("start"))
					result.getJSONObject("domain").getJSONObject("axes").getJSONObject("x").put("stop",
							xllcorner + (cellsize * nxcols));
				if (nycols != null
						&& result.getJSONObject("domain").getJSONObject("axes").getJSONObject("y").has("start"))
					result.getJSONObject("domain").getJSONObject("axes").getJSONObject("y").put("stop",
							yllcorner + (cellsize * nycols));
				for (String val : line.split(" ")) {
					if (val.equals(nodata)) {
						zarray.put(JSONObject.NULL);
					} else if(!val.trim().isEmpty()) {
						zarray.put(Double.valueOf(val));
					}
				}
			}*/
		}
		return null;
	}

}
