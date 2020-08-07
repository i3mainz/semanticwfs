package de.hsmainz.cs.semgis.wfs.util.user;

import java.util.UUID;

/**
 * Represents a user who may log in to the system.
 *
 */
public class User {

	/** The name of the user.*/
	private String name;
	
	/**The password hash used for authentification.*/
	private String passwordHash;
	
	/**The authorization level given to this user.*/
	private UserType userlevel;
	
	/**The current UUID assigned to this user.*/
	private String uuid;
	
	/**An auth token which has been given to this user to authenticate for web services.*/
	public String authToken;
	
	/**
	 * Empty constructor to gradually fill the user object.
	 */
	public User(){
		
	}
	
	/**
	 * Constructor for this class.
	 * @param name The user name
	 * @param password The password assigned to the user
	 * @param level the user level
	 */
	public User(String name,String password,UserType level){
		this.name=name;
		this.passwordHash=password;
		this.userlevel=level;
		this.uuid=UUID.randomUUID().toString();
		this.authToken=UUID.randomUUID().toString();
	}

	/**
	 * Gets the name of the user.
	 * @return The name as String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the uuid of the user.
	 * @return The uuid as String
	 */
	public String getUuid() {
		return uuid;
	}

	
	/**
	 * Sets the UUID of the user.
	 * @param uuid The uuid as String
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Sets the name of the user.
	 * @param name The name as String
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the password hash of the user.
	 * @return The password hash as String
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * Sets the password hash of the user.
	 * @param passwordHash The passwordHash as String
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/**
	 * Gets the user level of the user.
	 * @return The userlevel as a Usertype
	 */
	public UserType getUserlevel() {
		return userlevel;
	}

	/**
	 * Sets the user level of the user.
	 * @param userlevel The userlevel as UserType
	 */
	public void setUserlevel(UserType userlevel) {
		this.userlevel = userlevel;
	}
	
	@Override
	public String toString() {
		return "Name: "+this.name+" - Level: "+userlevel;
	}
	
}
