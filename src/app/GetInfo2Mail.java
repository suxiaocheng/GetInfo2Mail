package app;

import debug.Log;
import test.DecodePrice;

public class GetInfo2Mail {
	public static void main(String[] args) {
		Log loger = new Log();
		
		GetHouseNameUpdate getHouseNameUpdate = new GetHouseNameUpdate();
		getHouseNameUpdate.getAllHouseName();
		System.out.println("Program quit");
	}
}
