package metadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import debug.Log;
import tool.Database;

public class HousePrice extends Database {
	public String name;
	public int floor;
	public int iRoomNum;
	public float area;
	public float area_actual;
	public float price_per_square_meter;
	public float price_total;
	
	public static String TableName;
	
	public static void setTableName(String name){
		TableName = "Price" + createTableNameStr(name);
	}
	
	public static String createTable(){
		String ret =  "CREATE TABLE IF NOT EXISTS " + TableName +
				" (id integer primary key AutoIncrement,name TEXT,floor INT, "
				+ "roomnumber int, area FLOAT, areaacutal FLOAT, priceper FLOAT, "
				+ "pricetotal FLOAT);";
		return ret;
	}
	
	public String insertItem(){
		String ret= "INSERT INTO " + TableName +
				" (name, floor, roomnumber, area, areaacutal, priceper, pricetotal)" + 
				" VALUES('" + name + "'," + floor +
				"," + iRoomNum + "," + area + "," + area_actual + ", " + 
				price_per_square_meter + "," + price_total +
				");";
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
	
	public ArrayList<HousePrice> decodeAllToArray(){
		ResultSet rs = null;
		ArrayList<HousePrice> array = new ArrayList<>();
		HousePrice item;
		String statement = "SELECT * FROM " + TableName+ ";";
		
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(statement);
			while(rs.next()){
				item = new HousePrice();
				item.name = rs.getString("name");
				item.floor = rs.getInt("floor");
				item.iRoomNum = rs.getInt("roomnumber");
				item.area = rs.getFloat("area");
				item.area_actual = rs.getFloat("areaacutal");
				item.price_per_square_meter = rs.getFloat("priceper");
				item.price_total = rs.getFloat("pricetotal");
				
				array.add(item);
			}
		    stmt.close();
		} catch (SQLException e) {
			Log.e("Execute statement: " + statement + " error!");
			e.printStackTrace();
			return null;
		}
		
		return array;
	}
	
	public String decodeAllToString(){
		ResultSet rs = null;
		StringBuffer result = new StringBuffer();
		HousePrice item;
		String statement = "SELECT * FROM " + TableName+ ";";
		
		result.append("name, ");
		result.append("floor, ");
		result.append("room, ");
		result.append("area, ");
		result.append("area actual, ");
		result.append("price per, ");
		result.append("price total\n");
		
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(statement);
			while(rs.next()){
				item = new HousePrice();
				item.name = rs.getString("name");
				item.floor = rs.getInt("floor");
				item.iRoomNum = rs.getInt("roomnumber");
				item.area = rs.getFloat("area");
				item.area_actual = rs.getFloat("areaacutal");
				item.price_per_square_meter = rs.getFloat("priceper");
				item.price_total = rs.getFloat("pricetotal");
				
				result.append(item.name + ",");
				result.append(item.floor + ",");
				result.append(item.iRoomNum + ",");
				result.append(item.area + ",");
				result.append(item.area_actual + ",");
				result.append(item.price_per_square_meter + ",");
				result.append(item.price_total + "\n");
			}
		    stmt.close();
		} catch (SQLException e) {
			Log.e("Execute statement: " + statement + " error!");
			e.printStackTrace();
			return null;
		}
		
		return result.toString();
	}
}
