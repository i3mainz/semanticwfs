package de.hsmainz.cs.semgis.wfs.util.user;

/**
 * Enumeration type including user levels which may be applied in the SemanticWFS.
 */
public enum UserType {
	
	/** Administrator role */
	Administrator("administrator"),
	/** Configurer role */
	Configurer("configurer"),
	/** User role */
	User("user");
	
	private final String userTypeString;
	
	/**
	 * Constructor for this class.
	 * @param userTypeString The String indicating the usertype to be assigned
	 */
	private UserType(String userTypeString){
		this.userTypeString=userTypeString;
	}

	/**
	 * Gets the String representation of the user type.
	 * @return The String representation
	 */
	public String getUserTypeString() {
		return userTypeString;
	}
	
	@Override
	public String toString() {
		return userTypeString;
	}

}
