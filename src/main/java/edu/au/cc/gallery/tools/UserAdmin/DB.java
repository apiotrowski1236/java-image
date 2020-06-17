package edu.au.cc.gallery.tools;
import java.util.ArrayList;
import org.json.simple.parser.*;
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;
public class DB {
    Secrets secretManager = new Secrets();
    private Connection connection;
    private static final String dbURL = "jdbc:postgresql://image-gallery.cgtykvv08hlh.us-east-2.rds.amazonaws.com:5432/";


    /*Connnect to the database. */ 
    public void connect() throws SQLException {
	try {
	    Class.forName("org.postgresql.Driver");
	    JSONObject secret = getSecret();
	    String userName = getUserName(secret);
	    String password = getPassword(secret);
	    connection = DriverManager.getConnection(dbURL, userName, password);
	}
	catch (Exception ex) {
	    connection.close();
	    ex.printStackTrace();
	    System.exit(1);
	  
	}
 }

    /*************************************Secret Retrieving and Parsing Functions*******************/
    /*Retrieve the secret */
    public JSONObject getSecret() {
	String jsonString = secretManager.getImageGallerySecret();
	JSONObject jo = null;
	try {
	    Object obj = new JSONParser().parse(jsonString);
	    jo = (JSONObject) obj;
	}
	catch (ParseException p) {
	    p.printStackTrace();
	}
	if (jo == null) {
	    System.out.println("Secret not retrieved.");
	}
	    return jo;
    }

    /*Retrieve the password from the secret.*/
	public  String getPassword(JSONObject secret) {
	    String password = (String) secret.get("password");
	    return password;
	}

    /*Retrieve the username from the secret.*/
    public String getUserName(JSONObject secret) {
	String userName = (String) secret.get("username");
	return userName;
    }



    /************************DB Search/Change Functions*************************************/

    /*Display all the users in the users table.*/
    public JSONArray  listUsers() throws SQLException {
	connect();

	PreparedStatement statement = connection.prepareStatement("select * from users");
	JSONArray ja = new JSONArray();
	ResultSet results = statement.executeQuery();

	while (results.next()) {
		//Make a user JSON object from a row.
	    String username  = results.getString(1);
	    String password = results.getString(2);
            String fullname = results.getString(3);
	    JSONObject jo = new JSONObject();
	    jo.put("username", username);
	    jo.put("password", password);
	    jo.put("fullname", fullname);
	    ja.add(jo);
	    }
        close();
        return ja;
    }

    /*Search for a user: helpful for edits and deletes. */
    public boolean searchForUser(String userIn) throws SQLException {
	connect();
	PreparedStatement statement = connection.prepareStatement("select * from users");
	ResultSet results = statement.executeQuery();
	while (results.next()) {
	    if (results.getString(1).equals(userIn)) {
		close();
		return true;
		}
	}
	return false;
    }

    /*Add a new user*/
    public void addUser(String userIn, String passwordIn, String nameIn) throws SQLException {
	connect();
	String sql = "INSERT INTO users(username, password, full_name)" + "VALUES(?,?,?)";
	PreparedStatement statement = connection.prepareStatement(sql);
	statement.setString(1, userIn);
	statement.setString(2, passwordIn);
	statement.setString(3, nameIn);
	statement.execute();
	close();
    }

    /*Delete a user*/
    public void deleteUser(String userIn) throws SQLException {
	if (searchForUser(userIn) == true) {
	    connect();
	    String sql = "DELETE FROM users WHERE username=?";
	    PreparedStatement statement = connection.prepareStatement(sql);
	    statement.setString(1, userIn);
	    statement.execute();
	}
	close();
    }

    
    /*Change an existing user's password.*/
    public void updateUserPassword(String userIn, String passwordIn) throws SQLException {
	if (searchForUser(userIn) == true) {
	    connect();
	String sql = "UPDATE users SET password=? WHERE username=?";
	PreparedStatement statement = connection.prepareStatement(sql);
	statement.setString(1, passwordIn);
	statement.setString(2,userIn);
	statement.execute();
    }
	close();
    }

    /*Change an existing user's full name.*/
    public void updateUserFullName(String userIn, String fullNameIn) throws SQLException {
	if (searchForUser(userIn) == true) {
	    connect();
	    String sql = "UPDATE users SET full_name=? WHERE username=?";
	    PreparedStatement statement = connection.prepareStatement(sql);
	    statement.setString(1, fullNameIn);
	    statement.setString(2,userIn);
	    statement.execute();
	}
	close();
    }

    /*Close the connection.*/
    public void close() throws SQLException {
	connection.close();
    }
}
