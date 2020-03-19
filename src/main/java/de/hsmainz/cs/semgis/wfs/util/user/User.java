package de.hsmainz.cs.semgis.wfs.util.user;

import java.util.UUID;

public class User {

	private String name;
	
	private String passwordHash;
	
	private UserType userlevel;
	
	private String uuid;
	
	public String authToken;
	
	public User(){
		
	}
	
	public User(String name,String password,UserType level){
		this.name=name;
		this.passwordHash=password;
		this.userlevel=level;
		this.uuid=UUID.randomUUID().toString();
		this.authToken=UUID.randomUUID().toString();
	}

	public String getName() {
		return name;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public UserType getUserlevel() {
		return userlevel;
	}

	public void setUserlevel(UserType userlevel) {
		this.userlevel = userlevel;
	}
	
	@Override
	public String toString() {
		return "Name: "+this.name+" - Level: "+userlevel;
	}
	
}
