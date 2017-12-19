package app;

import java.util.ArrayList;

import metadata.HouseProject;
import tool.Database;
import tool.SendEmail;
import debug.Log;

public class GetInfo2Mail {
	public static void main(String[] args) {
		Log loger = new Log();

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
				SendEmail sendEmail = new SendEmail("Project new found",
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
