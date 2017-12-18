package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import debug.Log;
import metadata.HouseProject;
import tool.Database;

public class GetHouseNameUpdate implements Runnable {
	private static final String TAG = "GetHouseNameUpdate";
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final boolean DEBUG = false;
	public static boolean bNeedQuit = false;
	private String strInternetAddr = "http://data.fz0752.com/jygs/yshouselist.shtml?code=&num=&name=&comp=&address=&pageNO=";
	Database db = new Database();

	private final static String strProjectHead[] = { "项目名称", "开发商", "预售证", "已售套数", "未售套数" };
	private int iProjectLevel;
	private int iProjectHeaderMatch;
	private int iValidItem;
	private int iNewItemCount = 0;

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

	public String getElementRef(Elements parent) {
		for (Element child : parent) {
			Attributes atrChild = child.attributes();
			if (atrChild != null) {
				String strAttrClass = atrChild.get("href");
				if ((strAttrClass != null) && (strAttrClass.length() > 0)) {
					return strAttrClass;
				}
			}
		}
		return null;
	}

	public boolean getElementProject(Elements parent) {
		StringBuffer sb = new StringBuffer();
		int iItemCount = 0;

		if (parent == null) {
			return false;
		}
		for (Element child : parent) {
			if (iProjectLevel == 0) {
				iProjectHeaderMatch = 0;
			}
			if (iProjectHeaderMatch < strProjectHead.length) {
				if (child.tag().toString().compareTo("th") == 0) {
					if (child.text().compareTo(strProjectHead[iProjectHeaderMatch]) == 0) {
						iProjectHeaderMatch++;
					} else {
						break;
					}
				}
			} else {
				if (child.tag().toString().compareTo("td") == 0) {
					if (iItemCount == 0) {
						iItemCount++;
						continue;
					}
					String strRefAddr = getElementRef(child.children());
					/* Check if item is exist */
					String strQuery = db.queryTable(HouseProject.queryNameItem(child.text()), "NAME");
					if ((strQuery != null) && (strQuery.length() > 0)) {
						if (strQuery.compareTo(child.text()) == 0) {
							System.out.println("Match item: " + child.text());
						}
					} else {
						db.insertTable(HouseProject.insertItem(child.text(), strRefAddr));
						iNewItemCount++;
					}
					iValidItem++;
					iItemCount++;
					break;
				}
			}
			iProjectLevel++;
			getElementProject(child.children());
			iProjectLevel--;
		}
		if (sb.length() != 0) {
			System.out.println(sb.toString());
		}
		return true;
	}

	public void getAllHouseName() {
		String addr_area_base = "http://data.fz0752.com/jygs/yshouselist.shtml?code=0&num=&name=&comp=&address=&pageNO=";
		StringBuffer sb = new StringBuffer();
		int iRetryCount = 0;
		boolean bFail;

		db.createTable(HouseProject.createTable());
		iNewItemCount = 0;
		for (int i = 0;; i++) {
			String addr_area = addr_area_base + (i + 1);
			System.out.println("Get house name from->" + addr_area);
			iRetryCount = 0;
			bFail = true;
			while (bFail) {
				Document doc = null;
				try {
					doc = Jsoup.connect(addr_area).get();
					Elements table = doc.select("table");
					iProjectLevel = 0;
					iValidItem = 0;
					getElementProject(table);
					bFail = false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					iRetryCount++;
					if (iRetryCount > 5) {
						e.printStackTrace();
						bFail = false;
						sb.append(addr_area + "\n");
					}
				}
			}
			if (iValidItem == 0) {
				break;
			}
		}
		System.out.println("Adding " + iNewItemCount + " item to database\n");
		if (sb.length() > 0) {
			System.out.println("Error assess addr list: " + sb.toString());
		}
	}

	public void run() {
		List<String> listTmp;
		String sql;

		getAllHouseName();

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (bNeedQuit == true) {
				System.out.println("User quit");
				break;
			}
		}
	}
}
