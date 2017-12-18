package metadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import debug.Log;
import tool.Database;

public class HouseProject extends Database {
	public String strProjectName;
	public String strHtmlAddr;
	public static final String TableName = "HouseProject";
	
	public static String createTable(){
		String ret =  "CREATE TABLE IF NOT EXISTS " + TableName +
				" (id integer primary key AutoIncrement,name TEXT,addr TEXT);";
		return ret;
	}
	
	public static String insertItem(String name, String addr){
		String ret= "INSERT INTO " + TableName +
				" (name, addr)" + " VALUES('" + name + "','" + addr +"');";
		return ret;
	}
	
	public static String queryNameItem(String name){
		String ret = null;
		ret = "SELECT name FROM " + TableName + " WHERE NAME LIKE '" + name + "';";
		return ret;
	}
	
	public static String queryAll(){
		String ret = null;
		ret = "SELECT * FROM " + TableName+ ";";
		return ret;
	}
	
	public ArrayList<HouseProject> decodeAllToArray(){
		ResultSet rs = null;
		ArrayList<HouseProject> array = new ArrayList<>();
		HouseProject item;
		String statement = "SELECT * FROM " + TableName+ ";";
		
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(statement);
			while(rs.next()){
				item = new HouseProject();
				item.strProjectName = rs.getString("name");
				item.strHtmlAddr = rs.getString("addr");
				
				array.add(item);
			}
		    stmt.close();
		} catch (SQLException e) {
			Log.loge("Execute statement: " + statement + " error!");
			e.printStackTrace();
			return null;
		}
		
		return array;
	}
}
