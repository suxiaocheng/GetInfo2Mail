package app;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import debug.Log;
import metadata.HouseBuilding;
import metadata.HousePrice;

public class GetHouseBuildingDetail {
	private static final String TAG = "GetHouseBuildingUpdate";
	private static final boolean DEBUG = false;

	private final static String strTableHeader[] = { "楼层", "房号", "建筑面积(㎡)", "备案均价(元/㎡)", "备案价(元/套)", "状态", "详情" };
	private int iTableHeaderMatch;
	private int iGetElementLevel;
	private String strCurrentFloor;
	private int iItemValidCount;
	private int iCurrentRoomNumber;
	private int iMaxFloor = 0;
	private int iMaxRoom = 0;
	private ArrayList<HousePrice> arrayHousePrice = new ArrayList<>();
	public HousePrice hp = new HousePrice();

	public boolean getElement(Elements parent) {
		boolean leaf = false;
		StringBuffer sb = new StringBuffer();
		HousePrice item = null;

		if (parent == null) {
			return false;
		}
		for (Element child : parent) {
			if (iGetElementLevel == 0) {
				iTableHeaderMatch = 0;
				if (arrayHousePrice.isEmpty() == false) {
					break;
				}
			} else {
				if (iTableHeaderMatch < strTableHeader.length) {
					if (child.tag().toString().compareTo("th") == 0) {
						if (child.text().compareTo(strTableHeader[iTableHeaderMatch]) == 0) {
							iTableHeaderMatch++;
							if (iTableHeaderMatch >= strTableHeader.length) {
								System.out.println("Match");
								break;
							}
						}
					}
				} else {
					if (child.tag().toString().compareTo("tr") == 0) {
						iItemValidCount = 0;
					} else if (child.tag().toString().compareTo("td") == 0) {
						Attributes atrChild = child.attributes();
						if (atrChild != null) {
							String strAttrClass = atrChild.get("rowspan");
							if ((strAttrClass != null) && (strAttrClass.length() > 0)) {
								try {
									strCurrentFloor = child.text();
									iCurrentRoomNumber = 1;
									continue;
								} catch (NumberFormatException e) {
									System.out.println(child.text() + " is not number");
									break;
								}
							}
						}

						switch (iItemValidCount) {
						case 0:
							item = new HousePrice();
							item.name = child.text();
							break;
						case 1:
							item.area = Float.parseFloat(child.text());
							break;
						case 2:
							item.area_actual = Float.parseFloat(child.text());
							break;
						case 3:
						case 4:
							break;
						case 5:
							item.price_per_square_meter = (int) Float.parseFloat(child.text());
							break;
						case 6:
							item.price_total = item.area * item.price_per_square_meter;
							break;
						default:
							break;
						}
						iItemValidCount++;

						if (iItemValidCount >= 7) {
							/* End of parser */
							item.floor = 0;
							for (int i = 0; i < strCurrentFloor.length(); i++) {
								if (Character.isDigit(strCurrentFloor.charAt(i)) == true) {
									item.floor *= 10;
									item.floor += strCurrentFloor.charAt(i) - '0';
								} else {
									break;
								}
							}
							if (iMaxFloor < item.floor) {
								iMaxFloor = item.floor;
							}
							item.iRoomNum = iCurrentRoomNumber++;
							if (iMaxRoom < item.iRoomNum) {
								iMaxRoom = item.iRoomNum;
							}
							arrayHousePrice.add(item);

							sb.append(item.floor + "," + item.iRoomNum + "," + item.area + ","
									+ item.price_per_square_meter + "," + item.price_total);
							break;
						}
						continue;
					}
				}
			}
			iGetElementLevel++;
			leaf = getElement(child.children());
			iGetElementLevel--;
			if (leaf == false) {
				break;
			}
		}
		if (sb.length() != 0) {
			System.out.println(sb.toString());
		}
		return true;
	}

	public boolean getOnePageFromInternet(String addr, String sql_table) {
		StringBuffer sb;
		Log.logd("Start to get data form->" + addr);
		int iRetryCount = 0;
		boolean bFail = false;

		HousePrice.setTableName(sql_table);
		hp.createTable(HousePrice.createTable());

		while (bFail == false) {
			Document doc = null;
			try {
				doc = Jsoup.connect(addr).get();
				if (doc != null) {
					Elements table = doc.select("table");
					iGetElementLevel = 0;
					arrayHousePrice.clear();
					iMaxFloor = 0;
					iMaxRoom = 0;
					iItemValidCount = 0;

					getElement(table);

					for (HousePrice item : arrayHousePrice) {
						/* query for exist */
						String strQuery = hp.queryTable(HousePrice.queryNameItem(item.name), "name");
						if ((strQuery != null) && (strQuery.length() > 0)) {
							if (strQuery.compareTo(item.name) == 0) {
								System.out.println("Match item: " + item.name);
							}
						} else {
							hp.insertTable(item.insertItem());
							iItemValidCount++;
						}
					}
				}
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (NumberFormatException e) {
				System.out.println("Get Number format error");
				if (iRetryCount++ > 5) {
					bFail = true;
				}
			}
		}

		Log.logd("Finish->" + addr);
		return true;
	}
}
