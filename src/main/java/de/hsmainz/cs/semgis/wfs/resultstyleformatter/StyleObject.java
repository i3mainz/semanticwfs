package de.hsmainz.cs.semgis.wfs.resultstyleformatter;

public class StyleObject {

	public String pointStyle;
	
	public String pointImage;
	
	public String lineStringStyle;

	public String lineStringImage;
	
	public String polygonStyle;

	public String polygonImage;

	public String hatch;

	public String lineStringImageStyle;

	@Override
	public String toString() {
		return "StyleObject [pointStyle=" + pointStyle + ", pointImage=" + pointImage + ", lineStringStyle="
				+ lineStringStyle + ", lineStringImage=" + lineStringImage + ", polygonStyle=" + polygonStyle
				+ ", polygonImage=" + polygonImage + ", hatch=" + hatch + "]";
	}
	
	
	
	
}
