package app;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import metadata.HouseProject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import debug.Log;

public class GetHouseNameUpdate implements Runnable {
	private static final String TAG = "GetHouseNameUpdate";
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final boolean DEBUG = false;
	public static boolean bNeedQuit = false;
	public static boolean bComplete = false;	
	private final static String strProjectHead[] = { "项目名称", "开发商", "预售证",
			"已售套数", "未售套数" };

	private int count = 0;
	private int updateCount = 0;
	private int iProjectLevel;
	private int iProjectHeaderMatch;
	private int iValidItem;
	private int iNewItemCount = 0;
	private static final String strAddrBase = "http://data.fz0752.com/jygs/yshouselist.shtml?code=0&num=&name=&comp=&address=&pageNO=";
	private String strAddr;
	private static int iNumberThread = 0;

	private static Object lock = null;
	
	private static HouseProject hp;
	
	public static ArrayList<String> alStringNewHouse;

	static {
		lock = new Object();
		alStringNewHouse = new ArrayList<>();
		hp = new HouseProject();
		hp.execSqlTable(HouseProject.createTable());
	}

	GetHouseNameUpdate(int page) {
		super();
		strAddr = strAddrBase + page;
		synchronized (lock) {
			iNumberThread++;
		}
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
					if (child.text().compareTo(
							strProjectHead[iProjectHeaderMatch]) == 0) {
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
					String strQuery = hp.queryTable(
							HouseProject.queryNameItem(child.text()), "NAME");
					if ((strQuery != null) && (strQuery.length() > 0)) {
						if (strQuery.compareTo(child.text()) == 0) {
							Log.logi("Match item: " + child.text());
						}else {
							Log.loge("Never hit here");
						}
					} else {
						hp.execSqlTable(HouseProject.insertItem(child.text(),
								strRefAddr));
						alStringNewHouse.add(child.text());
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
		return true;
	}

	public void getAllHouseName() {
		StringBuffer sb = new StringBuffer();
		int iRetryCount = 0;
		boolean bFail;

		iNewItemCount = 0;

		String addr_area = strAddr;
		Log.logd("Get house name from->" + addr_area);
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
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				iRetryCount++;
				if (iRetryCount > 5) {
					e.printStackTrace();
					bFail = false;
					sb.append(addr_area + "\n");
				}
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
			synchronized (lock) {
				bComplete = true;
			}
		}

		Log.logd("Adding " + addr_area + "->" + iNewItemCount
				+ " item to database\n");
		if (sb.length() > 0) {
			Log.logd("Error assess addr list: " + sb.toString());
		}
	}

	public static boolean getCompleteFlag() {
		boolean ret;
		synchronized (lock) {
			ret = bComplete;
		}
		return ret;
	}

	public static int getNumberThread() {
		int ret;
		synchronized (lock) {
			ret = iNumberThread;
		}
		return ret;
	}

	public void run() {
		List<String> listTmp;
		String sql;

		getAllHouseName();

		synchronized (lock) {
			iNumberThread--;
		}
	}
}
