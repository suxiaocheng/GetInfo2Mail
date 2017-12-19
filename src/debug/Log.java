package debug;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	public static Logger loger;
	private static Level logLevel = Level.ALL;

	static {
		loger = Logger.getLogger(Log.class.getPackage().getName());
		FileHandler fh;

		Date today = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(today);

		try {
			fh = new FileHandler("./test.log", true);
			loger.addHandler(fh);
			loger.setLevel(logLevel);
			SimpleFormatter sf = new SimpleFormatter();
			fh.setFormatter(sf);
			loger.log(Level.INFO, "<<<<Log start: " + strDate);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("loger create fail");
		}
	}
	
	public static void logi(String msg) {
		return;
	}

	public static void logd(String msg) {
		System.out.println(msg);
	}
	
	public static void logw(String msg) {
		log(Level.WARNING, msg);
	}
	
	public static void loge(String msg) {
		log(Level.SEVERE, msg);
	}

	public static void log(Level level, String msg) {
		if (loger != null) {
			loger.log(level, msg);
		} else {
			System.out.println(level + ": " + msg);
		}
	}
}
