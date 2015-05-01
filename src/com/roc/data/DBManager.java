package com.roc.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import com.roc.config.Configuration;

public class DBManager 
{
  private Connection _connection = null;
  PreparedStatement _statement = null;

  public Connection getConnection(Configuration conf)
  {
	  closeStatement();
	  
	  if (_connection == null)
	  {
		    try 
		    {
		    	  Properties props = new Properties();
		    	  props.setProperty("user", conf.get_mysql_user());
		    	  props.setProperty("password", conf.get_mysql_password());
		    	  props.setProperty("autoReconnect", "true");
		    	  String url="jdbc:mysql://"+conf.get_mysql_host()+":"+conf.get_mysql_port()+"/"+conf.get_mysql_database();
		          // This will load the MySQL driver, each DB has its own driver
		          Class.forName("com.mysql.jdbc.Driver");
		          // Setup the connection with the DB
		          _connection = DriverManager.getConnection(url, props);
		    }
		    catch (Exception e)
		    {
		    	e.printStackTrace();
		    }
		        
	  }
	  return _connection;
  }

  public ResultSet executeQuery(String query, String[] args) throws Exception
  {
	  Connection connection = getConnection(null);
	  
	  if (connection != null)
	  {
		  _statement = connection.prepareStatement(query);
	      // Result set get the result of the SQL query
		  if (args != null && args.length > 0 && query.indexOf("=?")>0)
		  {
			  for (int i=0; i<args.length; i++)
				  _statement.setString(i+1, args[i]);
		  }
		  ResultSet resultSet = _statement.executeQuery();
		  
		  return resultSet;
	  }
	  return  null;
  }
  
  

  // You need to close the resultSet
  public void close() {
    try {
      if (_connection != null) {
    	  _connection.close();
      }
    } catch (Exception e) {

    }
  }
  // You need to close the resultSet
  public void closeStatement() {
    try {
      if (_statement != null) {
    	  _statement.close();
      }
    } catch (Exception e) {

    }
  }
  
}
