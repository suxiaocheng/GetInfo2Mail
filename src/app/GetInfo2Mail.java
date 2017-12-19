package app;

import java.util.ArrayList;

import debug.Log;
import tool.SendEmail;

public class GetInfo2Mail {
	public static void main(String[] args) {
		Log loger = new Log();

		ArrayList<Thread> alThread = new ArrayList<>();

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
		Log.logd("Program quit");
	}
}
