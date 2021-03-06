package app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import debug.Log;
import metadata.HouseProject;
import tool.Database;
import tool.SendEmail;

public class GetInfo2Mail {
	public static void main(String[] args) {
		ArrayList<Thread> alThread = new ArrayList<>();
		String strQurey = Database.qureyForTable(HouseProject.TableName);
		GetHouseBuildingUpdate buildUpdate = new GetHouseBuildingUpdate();
		boolean bNeedFirstUpdateAll = true;

		if ((strQurey == null)
				|| (strQurey.contains(HouseProject.TableName) == false)) {
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

			/* Continue to get all the building */
			GetHouseBuildingUpdate.bFirstBuild = true;

			buildUpdate.getAllHouseBuilding();

			GetHouseBuildingUpdate.bFirstBuild = false;

			if (GetHouseNameUpdate.alStringNewHouse.isEmpty() == false) {
				int count = 1;
				StringBuffer sb = new StringBuffer();
				Date date = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm");
				String dateString = formatter.format(date);

				sb.append(dateString + "\n");
				for (String item : GetHouseNameUpdate.alStringNewHouse) {
					sb.append(count++ + ": " + item + "\n");
				}
				SendEmail sendEmail = new SendEmail("New found-" + dateString,
						sb.toString(), Database.compressDB());
				Thread t1 = new Thread(sendEmail);
				t1.start();
				alThread.add(t1);
			}

			for (Thread t : alThread) {
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			alThread.clear();
			bNeedFirstUpdateAll = false;
		}

		while (true) {
			GetHouseNameUpdate.initUpdateParam();
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

			if ((GetHouseNameUpdate.bNeedUpdate) || (bNeedFirstUpdateAll)) {
				bNeedFirstUpdateAll = false;
				buildUpdate.getAllHouseBuilding();
				
				/* If database has been update, send it to backup */
				if (GetHouseNameUpdate.alStringNewHouse.isEmpty() == false) {
					int count = 1;
					StringBuffer sb = new StringBuffer();
					Date date = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					String dateString = formatter.format(date);

					sb.append(dateString + "\n");
					for (String item : GetHouseNameUpdate.alStringNewHouse) {
						sb.append(count++ + ": " + item + "\n");
					}
					SendEmail sendEmail = new SendEmail("New found-" + dateString,
							sb.toString(), Database.compressDB());
					Thread t2 = new Thread(sendEmail);
					t2.start();
					alThread.add(t2);
				}

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

			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Log.d("Program quit");
	}
}
