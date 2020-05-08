package com.ken.kenutils.dbUtils;

import com.ken.kenutils.securityUtils.PasswordManager;
import com.ken.kenutils.constants.Constants;
import com.ken.kenutils.connection.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

	private Connection con = ConnectionManager.getConnection(Constants.DB_KENRIG);
	
	public DatabaseManager() {
		createTables();
	}
	
	public boolean authenticateLogin(String uname, String passwordToHash)
	{
		boolean authenticationSuccess = false;
		try 
		{
			PreparedStatement ps = con.prepareStatement("select * from users where username =?");
	    	ps.setString(1, uname);
	    	ResultSet rs = ps.executeQuery();
	    	
	    	if(rs.next())
	    	{
	    		String dbpwd = rs.getString("password");
	    		byte[] dbsalt =rs.getBytes("salt");
	    		
	    		String inputpwd = PasswordManager.get_SHA_1_SecurePassword(passwordToHash, dbsalt);
	    		
	    		if(inputpwd.equals(dbpwd))
	    		{
	    			authenticationSuccess = true;
	    		}
	    	}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			try 
			{
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return authenticationSuccess;
	}
	
	public boolean insertUser(String username, String passwordToHash)
	{
		try 
		{
			byte[] salt = PasswordManager.getSalt();
	        String secPwd = PasswordManager.get_SHA_1_SecurePassword(passwordToHash, salt);
	        
	        PreparedStatement ps = con.prepareStatement("insert into users(username,password,salt) values(?,?,?)");
	        ps.setString(1, username);
	        ps.setString(2, secPwd);
	        ps.setBytes(3, salt);
	        ps.executeUpdate();
	        
	        return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createTables()
	{
		try 
		{
			Statement stmt = con.createStatement();
			stmt.executeUpdate(" CREATE TABLE if not exists users (id INTEGER PRIMARY KEY"
					+ " AUTOINCREMENT, username VARCHAR(25) unique, password varchar,salt blob)");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
