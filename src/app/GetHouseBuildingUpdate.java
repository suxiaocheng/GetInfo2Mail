package app;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import metadata.HouseBuilding;
import metadata.HouseProject;
import tool.Database;

public class GetHouseBuildingUpdate {
	private static final String TAG = "GetHouseBuildingUpdate";
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final boolean DEBUG = false;
	//private Database db = new Database();
	private int iBuildingLevel;
	private int iBuildingHeaderMatch;
	private final static String strBuildingHead[] = { "¥������", "����", "Ԥ��֤��", "��֤����", "¥�̱�", "����" };
	private int iNewItemCount;
	private int iValidItem;
	private String strBuildingName;
	private HouseBuilding hb = new HouseBuilding();

	public void getAllHouseBuilding() {
		HouseProject prj = new HouseProject();
		ArrayList<HouseProject> list = prj.decodeAllToArray();
		boolean bFail;
		int iRetryCount = 0;
		StringBuffer sb = new StringBuffer();

		for (HouseProject item : list) {
			System.out.println("name: " + item.strProjectName + ", addr: " + item.strHtmlAddr);
			iRetryCount = 0;
			bFail = true;

			HouseBuilding.setTableName(item.strProjectName);
			hb.createTable(HouseBuilding.createTable());
			
			ArrayList<HouseBuilding> build = hb.decodeAllToArray();
			for(HouseBuilding b:build){
				System.out.println("->name: " + b.strBuildingName + "->" + b.strHtmlAddr);
			}

			while (bFail) {
				Document doc = null;
				strBuildingName = item.strProjectName;
				try {
					doc = Jsoup.connect(item.strHtmlAddr).get();
					Elements table = doc.select("table");
					iBuildingLevel = 0;
					iValidItem = 0;
					getElementBuilding(table);
					bFail = false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					iRetryCount++;
					if (iRetryCount > 5) {
						e.printStackTrace();
						bFail = false;
						sb.append(item.strHtmlAddr + "\n");
					}
				}
			}
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

	public boolean getElementBuilding(Elements parent) {
		StringBuffer sb = new StringBuffer();
		int iItemCount = 0;

		if (parent == null) {
			return false;
		}
		for (Element child : parent) {
			if (iBuildingLevel == 0) {
				iBuildingHeaderMatch = 0;
			}
			if (iBuildingHeaderMatch < strBuildingHead.length) {
				if (child.tag().toString().compareTo("th") == 0) {
					if (child.text().compareTo(strBuildingHead[iBuildingHeaderMatch]) == 0) {
						iBuildingHeaderMatch++;
					} else {
						break;
					}
				}
			} else {
				if (child.tag().toString().compareTo("td") == 0) {
					switch (iItemCount) {
					case 0:
						hb.strBuildingName = child.text();
						break;
					case 1:
						break;
					case 2:
						break;
					case 3:
						break;
					case 4:
						break;
					case 5:
						hb.strHtmlAddr = getElementRef(child.children());
						break;
					default:
						System.out.println("iItemCount exceed max value: " + iItemCount);
						break;
					}

					if (iItemCount >= 5) {
						/* Check if item is exist */
						String strQuery = hb.queryTable(HouseBuilding.queryAddrItem(hb.strHtmlAddr), "addr");
						if ((strQuery != null) && (strQuery.length() > 0)) {
							if (strQuery.compareTo(hb.strHtmlAddr) == 0) {
								System.out.println("Match item: " + hb.strBuildingName);
							}
						} else {
							hb.insertTable(HouseBuilding.insertItem(hb.strBuildingName, hb.strHtmlAddr));
							iNewItemCount++;
						}
						iValidItem++;
						System.out.println("name: " + hb.strBuildingName + ", addr: " + hb.strHtmlAddr);
						
						/* Create building database here */
						GetHouseBuildingDetail detail = new GetHouseBuildingDetail();
						detail.getOnePageFromInternet(hb.strHtmlAddr, hb.strBuildingName);
						
						break;
					}
					iItemCount++;
				}
			}
			iBuildingLevel++;
			getElementBuilding(child.children());
			iBuildingLevel--;
		}
		if (sb.length() != 0) {
			System.out.println(sb.toString());
		}
		return true;
	}
}