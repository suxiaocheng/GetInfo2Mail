package tool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import debug.Log;

public class Database {
	private String databaseName = "hz_house";
	private Connection conn = null;	

	public Database() {		
		databaseName = "jdbc:sqlite:" + databaseName + ".db";
		try {
			Class.forName("org.sqlite.JDBC");
			conn = (Connection) DriverManager.getConnection(databaseName);
		} catch (SQLException e) {
			Log.loge("Connection error!");
			e.printStackTrace();
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			Log.loge("Class no found error!");
			e.printStackTrace();
			System.exit(-2);
		}
		Log.logd("Database: " + databaseName + " load sucessfully!");
	}

	public void createTable(String sql) {
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		    stmt.close();
		    Log.logd("Table created successfully");
		} catch (SQLException e) {
			Log.loge("createTable statement[ " + sql + "] operation error!");
			e.printStackTrace();			
		}		
	}
	
	public boolean insertTable(String statement){
		boolean status = true;
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(statement);
		    stmt.close();
		} catch (SQLException e) {
			Log.loge("Execute statement: " + statement + " error!");
			e.printStackTrace();			
		}
		
		return status;
	}
	
	public String queryTable(String statement, String item){
		boolean status = true;
		StringBuffer sb = new StringBuffer();
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(statement);
			while(rs.next()){
				if(sb.length() > 0){
					sb.append(' ');
				}
				sb.append(rs.getString(item));
			}
		    stmt.close();
		} catch (SQLException e) {
			Log.loge("Execute statement: " + statement + " error!");
			e.printStackTrace();
			return null;
		}
		
		return sb.toString();
	}

	public void closeDatabase() {
		if (conn != null) {
			try {
				conn.close();
				conn = null;
				Log.logd("Close database sucessfully!");
			} catch (SQLException e) {
				Log.loge("Close database fail!");
				e.printStackTrace();
			}			
		}
	}
}
