package de.hsmainz.cs.semgis.wfs.util.user;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class UserManagementConnection {
	
	private Map<String,User> userNameToPasswordHash;
	
	private Map<String,User> uuidToUser;
	
	private static UserManagementConnection instance;
	
	private static String USERS="users.xml";
	
	private UserManagementConnection(){
		this.userNameToPasswordHash=new TreeMap<String,User>();
		this.uuidToUser=new TreeMap<String,User>();
		try {
			this.readUsers();
		} catch (ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String getHashedPassword(String password){
		try {
	        MessageDigest md = MessageDigest.getInstance("SHA-512");
	        byte[] messageDigest = md.digest(password.getBytes());
	        BigInteger number = new BigInteger(1, messageDigest);
	        String hashtext = number.toString(16);
	        // Now we need to zero pad it if you actually want the full 32 chars.
	        while (hashtext.length() < 32) {
	            hashtext = "0" + hashtext;
	        }
	        return hashtext;
	    }catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	public static void main(String[] args){
		System.out.println(UserManagementConnection.getInstance().getHashedPassword("123"));
	}
	
	public String addUser(String userToAdd,String userToAddPasswordHash,String userlevel) throws XMLStreamException, IOException{
		if(this.userNameToPasswordHash.containsKey(userToAdd)){
			return "User already exists";
		}
		User user=new User();
		user.setName(userToAdd);
		user.setPasswordHash(this.getHashedPassword(userToAddPasswordHash));
		user.setUserlevel(UserType.valueOf(userlevel));
		this.userNameToPasswordHash.put(userToAdd, user);
		this.toXML();
		return "User successfully added";
	}
	
	public String updateUser(String userToAdd,String userToAddPasswordHash,String userlevel) throws XMLStreamException, IOException{
		if(!this.userNameToPasswordHash.containsKey(userToAdd)){
			return "User does not exist";
		}
		User user=new User();
		user.setName(userToAdd);
		user.setPasswordHash(this.getHashedPassword(userToAddPasswordHash));
		user.setUserlevel(UserType.valueOf(userlevel));
		this.userNameToPasswordHash.put(userToAdd, user);
		this.toXML();
		return "User successfully edited";
	}
	
	public String deleteUser(String userToAdd,String userToAddPasswordHash,Integer userlevel) throws XMLStreamException, IOException{
		this.userNameToPasswordHash.remove(userToAdd);
		this.toXML();
		return "User successfully deleted";
	}
	
	public User login(String username,String password){
		System.out.println("UserNameToPasswordHash: "+userNameToPasswordHash.toString());
		if(!this.userNameToPasswordHash.containsKey(username)){
			System.out.println("Not logged in: Username "+username+" does not exist! ");
			return null;//"usernamenotex_false";
		}
		if(this.getHashedPassword(password).equals(this.userNameToPasswordHash.get(username).getPasswordHash())){
			System.out.println("Logged in: "+this.userNameToPasswordHash.get(username).toString());
			User user=this.userNameToPasswordHash.get(username);
			user.setUuid(UUID.randomUUID().toString());
			return this.userNameToPasswordHash.get(username);//"loginsuccessful_true";
		}
		System.out.println("Not logged in: Login failed! ");
		return null;//"loginfailed_false";
	}
	
	public User loginAuthToken(String authToken){
		System.out.println("UserNameToPasswordHash: "+userNameToPasswordHash.toString());
		if(!this.uuidToUser.containsKey(authToken)){
			System.out.println("Not logged in: Auth Token is invalid! ");
			return this.uuidToUser.get(authToken);//"usernamenotex_false";
		}
		return null;
	}
		
	public static UserManagementConnection getInstance(){
		if(instance==null){
			instance=new UserManagementConnection();
		}
		return instance;
	}
	
	private void readUsers() throws ParserConfigurationException, SAXException{
		SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
		try {
			parser.parse(USERS, new UserHandler(this.userNameToPasswordHash,this.uuidToUser));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String usersToHTML(){
		StringBuilder result=new StringBuilder();
		if(userNameToPasswordHash.isEmpty()){
			return "<tr><td>No user available!</td></tr>";
		}
		for(String user:this.userNameToPasswordHash.keySet()){
			User usr=this.userNameToPasswordHash.get(user);
			result.append(usr.getName());
			result.append(",");
			result.append(usr.getUserlevel()+";");
		}
		return result.substring(0,result.length()-1).toString();
	}
	
	public void toXML() throws XMLStreamException, IOException{
		StringWriter sw = new StringWriter();
	    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	    XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(sw);
	    xmlStreamWriter.writeStartDocument();
	    xmlStreamWriter.writeStartElement("data");
	    for(String name:userNameToPasswordHash.keySet()){
	    	xmlStreamWriter.writeStartElement("user");
	    	xmlStreamWriter.writeAttribute("name", name);
	    	xmlStreamWriter.writeAttribute("password", userNameToPasswordHash.get(name).getPasswordHash());
	    	xmlStreamWriter.writeAttribute("level", userNameToPasswordHash.get(name).getUserlevel().toString());
	    	xmlStreamWriter.writeEndElement();
	    }
	    xmlStreamWriter.writeEndElement();
	    xmlStreamWriter.writeEndDocument();
	    xmlStreamWriter.flush();
	    xmlStreamWriter.close();
	    FileWriter writer=new FileWriter(new File(USERS));
	    writer.write(sw.toString());
	    writer.close();
	}
	
	
	public class UserHandler extends DefaultHandler2{
		
		private Map<String,User> usermap;	
		
		private Map<String,User> uuidToUser;
		
		public UserHandler(Map<String,User> usermap,Map<String,User> uuidToUser){
			this.usermap=usermap;
			this.uuidToUser=uuidToUser;
		}
		
		@Override
		public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
			super.startElement(arg0, arg1, arg2, arg3);
			if(arg2.equals("user")){
				User user=new User();
				user.setName(arg3.getValue("name"));
				user.setPasswordHash(arg3.getValue("password"));
				user.setUserlevel(UserType.valueOf(arg3.getValue("level")));
				user.setUuid(arg3.getValue("uuid"));
				this.usermap.put(user.getName(),user);
				this.uuidToUser.put(arg3.getValue("uuid"),user);
			}
		}
	}
}
