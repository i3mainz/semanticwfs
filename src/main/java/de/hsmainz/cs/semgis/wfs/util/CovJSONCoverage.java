package de.hsmainz.cs.semgis.wfs.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;


public class CovJSONCoverage {
	
	public List<Coordinate> coords=new LinkedList<Coordinate>();
	
	public Map<String,List<Object>> ranges=new TreeMap<>();
	
	public String epsg;
	
	public Double minX, minY,maxX,maxY;
	
	
	
}
