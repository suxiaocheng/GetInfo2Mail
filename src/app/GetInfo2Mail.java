package app;

import java.util.ArrayList;

import debug.Log;

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
				for(Thread t:alThread){
					try {
						t.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
			while (GetHouseNameUpdate.getNumberThread() > 3) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		System.out.println("Program quit");
	}
}
