package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import debug.Log;
import tool.Database;

public class GetHouseNameUpdate implements Runnable {
	private static final String TAG = "GetHouseNameUpdate";
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final boolean DEBUG = false;
	public static boolean bNeedQuit = false;
	private String strInternetAddr = "http://data.fz0752.com/jygs/yshouselist.shtml?code=&num=&name=&comp=&address=&pageNO=";
	int count = 0;
	int updateCount = 0;
	Database db = new Database();
	String strDBTableName = "AllHouse";
	String strDBCreateTable = "CREATE TABLE " + strDBTableName + "(" + "ID INT PRIMARY KEY NOT NULL AUTOINCREMENT, "
			+ "NAME TEXT NOT NULL," + "DEVELOP TEXT NOT NULL," + "IDCARD TEXT NOT NULL," + "DATE TEXT," +"SELL INT,"
			+ "NOTSELL INT);";

	GetHouseNameUpdate() {
		super();
	}

	private boolean checkForUpdateDBTable(int page) {
		boolean result = true;
		StringBuffer sb = new StringBuffer();
		URL url;
		try {
			url = new URL(strInternetAddr + page);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;			
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.loge("MalformedURL: " + strInternetAddr + page);
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.loge("IOException: " + strInternetAddr + page);
			return false;
		}

		return result;
	}

	public void run() {
		List<String> listTmp;
		String sql;		

		db.createTable(strDBCreateTable);
		
		while (true) {
			if((count % 100) == 0){
				System.out.println("Thread " + strInternetAddr + " " + count);
			}
			count++;
			try {
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch(IndexOutOfBoundsException e){
				e.printStackTrace();
				System.out.println("Out of bound");
				break;
			}
			if(bNeedQuit == true){
				System.out.println("User quit");
				break;
			}
		}
		System.out.println("Execute " + strInternetAddr + " count: " + count + 
				", Times: " + updateCount);
	}
}
