package app;

import debug.Log;
import test.DecodePrice;

public class GetInfo2Mail {
	public static void main(String[] args) {
		Log loger = new Log();
		
		GetHouseBuildingUpdate getHouseBuildingUpdate = new GetHouseBuildingUpdate();
		getHouseBuildingUpdate.getAllHouseBuilding();
		System.out.println("Program quit");
	}
}
