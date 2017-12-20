package app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tool.SendEmail;
import debug.Log;

public class GetInfo2Mail {
	public static void main(String[] args) {
		ArrayList<Thread> alThread = new ArrayList<>();

		//if (Database.qureyForTable(HouseProject.TableName).compareTo(
		//		HouseProject.TableName) == 0) {
		{
			for (int i = 1;; i++) {
				if (GetHouseNameUpdate.getCompleteFlag() == false) {
					GetHouseNameUpdate getHouseNameUpdate = new GetHouseNameUpdate(
							i);
					Thread t1 = new Thread(getHouseNameUpdate);
					t1.start();
					alThread.add(t1);
				} else {
					for (Thread t : alThread) {
						try {
							t.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					alThread.clear();
					break;
				}
				while (GetHouseNameUpdate.getNumberThread() > 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (GetHouseNameUpdate.alStringNewHouse.isEmpty() == false) {
				StringBuffer sb = new StringBuffer();
				for (String item : GetHouseNameUpdate.alStringNewHouse) {
					sb.append(item + "\n");
				}
				Date date = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String dateString = formatter.format(date);
				SendEmail sendEmail = new SendEmail("New found-" + dateString,
						sb.toString(), null);
				Thread t1 = new Thread(sendEmail);
				t1.start();
				alThread.add(t1);
			}

			/* Continue to get all the building */
			GetHouseBuildingUpdate.bFirstBuild = true;

			GetHouseBuildingUpdate buildUpdate = new GetHouseBuildingUpdate();
			buildUpdate.getAllHouseBuilding();

			GetHouseBuildingUpdate.bFirstBuild = false;

			for (Thread t : alThread) {
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			alThread.clear();
		}

		while (true) {
			GetHouseNameUpdate.alStringNewHouse.clear();
			GetHouseNameUpdate getHouseNameUpdate = new GetHouseNameUpdate(1);
			Thread t1 = new Thread(getHouseNameUpdate);
			t1.start();
			try {
				t1.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				break;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}

		Log.logd("Program quit");
	}
}
