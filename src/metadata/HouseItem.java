package metadata;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HouseItem {
	public String strDate;
	public String strName;
	public String strDevelop;
	public String strIDCard;
	public int iSellNum;
	public int iUnSellNum;
	
	public HouseItem(){
		Date today = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		strDate = dateFormat.format(today);
	}
}
