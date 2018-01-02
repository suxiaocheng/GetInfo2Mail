package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import debug.Log;

public class TestParser {
	private static final String TAG = "TestHtmlParser";
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final boolean DEBUG = false;
	private static String strTestAddr = "D:\\work\\java\\GetInfo2Mail\\test.html";
	private static String[] strHouseDealAddr = { "http://data.fz0752.com/chart/housedeal.shtml?code=",
			"&date1=2017-12-31&date2=2017-01-01&pageNO=" };
	private static String[] strHouseArea = { "0", "1", "2"};
	private static String[] strHouseAreaName = { "HuiCheng", "HuiDong", "BoLuo", "LongMen" };

	private static int iValidItem;

	private static String strOutputFile = "data";
	private static File fOutputFile;
	private static BufferedWriter bwOutputFile;

	public static boolean getElement(Elements parent) {
		boolean leaf = false;
		StringBuffer sb = new StringBuffer();

		if (parent == null) {
			return false;
		}
		for (Element child : parent) {
			if (child.tag().toString().compareTo("tr") == 0) {
				Attributes atrChild = child.attributes();
				if (atrChild != null) {
					String strAttrClass = atrChild.get("class");
					if ((strAttrClass != null) && (strAttrClass.compareTo("txt-title") == 0)) {
						continue;
					}
				}
			}
			if (child.tag().toString().compareTo("td") == 0) {
				/* Verify if the string is valid */
				String tmp = child.text();
				for (int i = 0; i < tmp.length(); i++) {
					if ((Character.isDigit(tmp.charAt(i)) == false) && (tmp.charAt(i) != '-')) {
						return false;
					}
				}
				iValidItem++;
				sb.append(child.text() + ",");
				// System.out.println("Get td->" + child.text() + "");
				continue;
			}
			leaf = getElement(child.children());
			if (leaf == false) {
				break;
			}
		}
		if (sb.length() != 0) {
			System.out.println(sb.toString());
			try {
				bwOutputFile.write(sb.toString() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	public static boolean getOnePageFromInternet(String addr) {
		iValidItem = 0;

		Log.d("Start to get data form->" + addr);

		Document doc = null;
		try {
			doc = Jsoup.connect(addr).get();
			if (doc != null) {
				Elements table = doc.select("table");
				getElement(table);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		if (iValidItem == 0) {
			Log.d("Invalid->" + addr);
			return false;
		}
		Log.d("Finish->" + addr);
		return true;
	}

	public static void test() {
		for (int area = 0; area < strHouseArea.length; area++) {
			/* Init output file */
			fOutputFile = new File(strOutputFile+strHouseAreaName[area]+".csv");
			if (fOutputFile.exists()) {
				fOutputFile.delete();
			}
			FileWriter fw = null;
			try {
				fw = new FileWriter(fOutputFile.getAbsoluteFile());
				bwOutputFile = new BufferedWriter(fw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			for (int i = 0;; i++) {
				if (getOnePageFromInternet(
						strHouseDealAddr[0] + strHouseArea[area] + strHouseDealAddr[1] + (i + 1)) == false) {
					break;
				}
			}

			try {
				bwOutputFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
