package app;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import config.AppConfig;
import debug.Log;
import metadata.HouseBuilding;
import metadata.HouseProject;
import tool.Database;
import tool.SendEmail;

public class GetHouseBuildingUpdate {
	private static final String TAG = "GetHouseBuildingUpdate";
	private int iBuildingLevel;
	private int iBuildingHeaderMatch;
	private final static String strBuildingHead[] = { "楼栋名称", "栋号", "预售证号",
			"发证日期", "楼盘表", "备案" };
	private int iNewItemCount;
	private int iValidItem;
	private String strBuildingName;
	private HouseBuilding hb = new HouseBuilding();
	public static boolean bFirstBuild = false;

	public void getAllHouseBuilding() {
		HouseProject prj = new HouseProject();
		ArrayList<HouseProject> list = prj.decodeAllToArray();
		boolean bFail;
		boolean bRet = true;
		int iRetryCount = 0;
		StringBuffer sb = new StringBuffer();

		for (HouseProject item : list) {
			Log.logd("name: " + item.strProjectName + ", addr: "
					+ item.strHtmlAddr);
			iRetryCount = 0;
			bFail = true;
			bRet = true;

			HouseBuilding.setTableName(item.strProjectName);
			Database.execSqlTable(HouseBuilding.createTable());

			while (bFail) {
				Document doc = null;
				strBuildingName = item.strProjectName;
				try {
					doc = Jsoup.connect(item.strHtmlAddr).get();
					Elements table = doc.select("table");
					iBuildingLevel = 0;
					iValidItem = 0;
					bRet = getElementBuilding(table);
					bFail = false;
				} catch (IOException e) {
					// Remove current table
					Database.dropTable(HouseBuilding.TableName);
					Database.execSqlTable(HouseBuilding.createTable());

					// TODO Auto-generated catch block
					iRetryCount++;
					if (iRetryCount > AppConfig.RETRY_TIMES) {
						e.printStackTrace();
						bFail = false;
						sb.append(item.strHtmlAddr + "\n");
					}
				}
			}
			if (bRet == false) {
				Database.dropTable(HouseBuilding.TableName);
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
		int iItemCount = 0;
		boolean bRet;

		if (parent == null) {
			return false;
		}
		for (Element child : parent) {
			if (iBuildingLevel == 0) {
				iBuildingHeaderMatch = 0;
			}
			if (iBuildingHeaderMatch < strBuildingHead.length) {
				if (child.tag().toString().compareTo("th") == 0) {
					if (child.text().compareTo(
							strBuildingHead[iBuildingHeaderMatch]) == 0) {
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
						hb.strDate = child.text();
						break;
					case 4:
						break;
					case 5:
						hb.strHtmlAddr = getElementRef(child.children());
						break;
					default:
						Log.logd("iItemCount exceed max value: " + iItemCount);
						break;
					}

					if (iItemCount >= 5) {
						/* Check if item is exist */
						String strQuery = Database.queryTable(
								HouseBuilding.queryAddrItem(hb.strHtmlAddr),
								"addr");
						if ((strQuery != null) && (strQuery.length() > 0)) {
							if (strQuery.compareTo(hb.strHtmlAddr) == 0) {
								Log.logi("Match item: " + hb.strBuildingName);
							} else {
								Log.loge("Never hit here");
							}
						} else {
							/* Create building database here */
							GetHouseBuildingDetail detail = new GetHouseBuildingDetail();
							bRet = detail.getOnePageFromInternet(
									hb.strHtmlAddr, hb.strBuildingName);

							if (bRet == true) {
								Database.execSqlTable(HouseBuilding.insertItem(
										hb.strBuildingName, hb.strHtmlAddr,
										hb.strDate));
								/* SendEmail to me */
								if (bFirstBuild == false) {
									SendEmail sendEmail = new SendEmail(
											hb.strBuildingName,
											detail.hp.decodeAllToString(), null);
									Thread t1 = new Thread(sendEmail);
									t1.start();
									try {
										t1.join();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								iNewItemCount++;
							}
						}
						iValidItem++;
						Log.logd("name: " + hb.strBuildingName + ", addr: "
								+ hb.strHtmlAddr + ", count: " + iNewItemCount);

						break;
					}
					iItemCount++;
				}
			}
			iBuildingLevel++;
			getElementBuilding(child.children());
			iBuildingLevel--;
		}
		if (iNewItemCount > 0) {
			bRet = true;
		} else {
			bRet = false;
		}
		return bRet;
	}
}
