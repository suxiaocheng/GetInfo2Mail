package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import debug.Log;
import metadata.HousePrice;

public class DecodePrice {
	private static final String TAG = "TestHtmlParser";
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final boolean DEBUG = false;
	private static String strTestAddr = "http://data.fz0752.com/jygs/yszbuilding.shtml?code=0&bnum=201600009001&pnum=2016000090&view=2&type=&id=";

	private static String strOutputDir = "Output";

	private static String strOutputFile = "price";
	private static File fOutputFile;
	private static BufferedWriter bwOutputFile;

	private static String strTableHeader[] = { "楼层", "房号", "建筑面积(㎡)", "备案均价(元/㎡)", "备案价(元/套)", "状态", "详情" };
	private static int iTableHeaderMatch;
	private static int iGetElementLevel;
	private static String strCurrentFloor;
	private static int iItemValidCount;
	private static int iCurrentRoomNumber;
	private static int iMaxFloor = 0;
	private static int iMaxRoom = 0;
	private static ArrayList<HousePrice> arrayHousePrice = new ArrayList<>();

	public static boolean getElement(Elements parent) {
		boolean leaf = false;
		StringBuffer sb = new StringBuffer();
		HousePrice item = null;

		if (parent == null) {
			return false;
		}
		for (Element child : parent) {
			if (iGetElementLevel == 0) {
				iTableHeaderMatch = 0;
				if(arrayHousePrice.isEmpty() == false){
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
							item.price_per_square_meter = (int)Float.parseFloat(child.text());
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
							for(int i=0; i<strCurrentFloor.length(); i++){								
								if(Character.isDigit(strCurrentFloor.charAt(i)) == true){
									item.floor *= 10;
									item.floor += strCurrentFloor.charAt(i) - '0';
								} else {
									break;
								}
							}
							if(iMaxFloor < item.floor){
								iMaxFloor = item.floor;
							}
							item.iRoomNum = iCurrentRoomNumber++;
							if(iMaxRoom < item.iRoomNum){
								iMaxRoom = item.iRoomNum;
							}
							arrayHousePrice.add(item);
							
							sb.append(item.floor + "," + item.iRoomNum + "," + item.area + "," + item.price_per_square_meter + "," + item.price_total);
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
		StringBuffer sb;
		Log.logd("Start to get data form->" + addr);

		Document doc = null;
		try {
			doc = Jsoup.connect(addr).get();
			if (doc != null) {
				Elements table = doc.select("table");
				iGetElementLevel = 0;
				arrayHousePrice.clear();
				iMaxFloor = 0;
				iMaxRoom = 0;
				getElement(table);
				if(arrayHousePrice.isEmpty() == false){
					int iCurrentFloor = -1;
					int iCurrentRoom = 0;
					/* printf header */
					sb = new StringBuffer();
					sb.append("\n\nfloor");
					for(int i=0; i<iMaxRoom; i++){
						sb.append("," + i);
					}
					bwOutputFile.write(sb.toString()+"\n");
					sb = null;
					
					for(HousePrice item:arrayHousePrice){
						if(iCurrentFloor != item.floor){
							if(sb != null){
								bwOutputFile.write(sb.toString()+"\n");
							}
							sb = new StringBuffer();
							iCurrentFloor = item.floor;
							iCurrentRoom = 1;
							sb.append(iCurrentFloor);
						}
						if(iCurrentRoom == item.iRoomNum){
							sb.append("," + item.price_per_square_meter);
							iCurrentRoom++;
						} else {
							System.out.println("room number not match, current:" + iCurrentRoom + ", actual: " + item.iRoomNum);
						}
					}
					if(sb.length() != 0){
						bwOutputFile.write(sb.toString()+"\n");
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		Log.logd("Finish->" + addr);
		return true;
	}

	public static void test() {
		File fOutputDir = new File(strOutputDir);
		if (fOutputDir.isDirectory() == false) {
			fOutputDir.mkdir();
		}
		/* Init output file */
		fOutputFile = new File(strOutputDir + File.separator + strOutputFile + ".csv");
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

		if (getOnePageFromInternet(strTestAddr) == false) {

		}

		try {
			bwOutputFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
