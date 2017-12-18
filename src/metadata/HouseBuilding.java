package metadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import debug.Log;
import tool.Database;

public class HouseBuilding extends Database  {
	public String strBuildingName;
	public String strHtmlAddr;
	public static String TableName;
	
	public static void setTableName(String name){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<name.length(); i++){
			Byte b[] = new Byte[2];
			b[0] = (byte) ((name.charAt(i) & 0xf) + '0');
			b[1] = (byte) (((name.charAt(i)>>4) & 0xf) + '0');
			sb.append(b[0] + b[1]);
		}
		TableName = "House" + sb.toString();
	}
	
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
	
	public static String queryAddrItem(String addr){
		String ret = null;
		ret = "SELECT addr FROM " + TableName + " WHERE addr LIKE '" + addr + "';";
		return ret;
	}
	
	public static String queryAll(){
		String ret = null;
		ret = "SELECT * FROM " + TableName+ ";";
		return ret;
	}
	
	public ArrayList<HouseBuilding> decodeAllToArray(){
		ResultSet rs = null;
		ArrayList<HouseBuilding> array = new ArrayList<>();
		HouseBuilding item;
		String statement = "SELECT * FROM " + TableName+ ";";
		
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(statement);
			while(rs.next()){
				item = new HouseBuilding();
				item.strBuildingName = rs.getString("name");
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
