package app;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import metadata.HouseProject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import config.AppConfig;
import tool.Database;
import debug.Log;

public class GetHouseNameUpdate implements Runnable {
	private static final String TAG = "GetHouseNameUpdate";
	public static boolean bNeedQuit = false;
	public static boolean bComplete = false;
	private final static String strProjectHead[] = { "��Ŀ����", "������", "Ԥ��֤",
			"��������", "δ������" };

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

	public static ArrayList<String> alStringNewHouse;
	public static String strFirstItem;
	public static boolean bCheckForUpdate;
	public static boolean bNeedUpdate;
	
	static {
		lock = new Object();
		alStringNewHouse = new ArrayList<>();
		Database.execSqlTable(HouseProject.createTable());
		strFirstItem = null;
		bCheckForUpdate = false;
		bNeedUpdate = false;
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
					String strQuery = Database.queryTable(
							HouseProject.queryNameItem(child.text()), "NAME");
					if ((strQuery != null) && (strQuery.length() > 0)) {
						if (strQuery.compareTo(child.text()) == 0) {
							Log.i("Match item: " + child.text());
						} else {
							Log.e("Never hit here");
						}
					} else {
						Database.execSqlTable(HouseProject.insertItem(
								child.text(), strRefAddr));
						alStringNewHouse.add(child.text());
						iNewItemCount++;

					}

					if (iValidItem == 0) {
						if (strFirstItem == null) {
							strFirstItem = child.text();
						} else {
							if (bCheckForUpdate == true) {
								if (strFirstItem.compareTo(child.text()) != 0) {
									strFirstItem = child.text();
									bNeedUpdate = true;
								}
							}
						}
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

	public static void initUpdateParam() {
		bCheckForUpdate = true;
		bNeedUpdate = false;
	}

	public void getAllHouseName() {
		int iRetryCount = 0;
		boolean bFail;
		int iTimeout = 30000;

		iNewItemCount = 0;

		String addr_area = strAddr;
		Log.d("Get house name from->" + addr_area);
		iRetryCount = 0;
		bFail = true;
		while (bFail) {
			Document doc = null;
			try {
				doc = Jsoup.connect(addr_area).timeout(iTimeout).get();
				Elements table = doc.select("table");
				iProjectLevel = 0;
				iValidItem = 0;
				getElementProject(table);
				bFail = false;
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				Log.d("addr: " + addr_area + " timeout " + iRetryCount
						+ ", func: " + TAG);
				iRetryCount++;
				iTimeout = 120000;
				if (iRetryCount > AppConfig.RETRY_TIMES) {
					e.printStackTrace();
					bFail = false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("addr: " + addr_area + " IOException " + iRetryCount
						+ ", func: " + TAG);
				iRetryCount++;
				if (iRetryCount > AppConfig.RETRY_TIMES) {
					e.printStackTrace();
					bFail = false;
				}
			}
		}
		if (iValidItem == 0) {
			synchronized (lock) {
				bComplete = true;
			}
		}

		Log.d("Adding " + addr_area + "->" + iNewItemCount
				+ " item to database");
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
		getAllHouseName();

		synchronized (lock) {
			iNumberThread--;
		}
	}
}
