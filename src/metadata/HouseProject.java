package metadata;

public class HouseProject {
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
}
