package debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private static final String strLogDir = "." + File.separator + "log";
	private static final String strLogName = "log";
	private static File fLogFile;
	private static File fLogDir;
	private static BufferedWriter bwLogFile;
	private static FileWriter fwLogFile = null;

	static {
		Date today = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm");
		String strDate = dateFormat.format(today);

		fLogDir = new File(strLogDir);
		if (fLogDir.exists() == true) {
			if (fLogDir.isDirectory() == false) {
				fLogDir.delete();
			}
		} else {
			fLogDir.mkdirs();
		}

		fLogFile = new File(strLogDir + File.separator + strLogName + strDate + ".log");
		try {
			fwLogFile = new FileWriter(fLogFile);
			bwLogFile = new BufferedWriter(fwLogFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bwLogFile = null;
		}
	}

	protected void finalize() {
		try {
			bwLogFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void i(String msg) {
		log(3, msg);
	}

	public static void d(String msg) {
		log(2, msg);
	}

	public static void w(String msg) {
		log(1, msg);
	}

	public static void e(String msg) {
		log(0, msg);
	}

	public static void log(int level, String msg) {
		String strOut;
		Date today = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd_HH:mm:ss]");
		strOut = dateFormat.format(today);

		switch (level) {
		case 0:
			strOut += "[Error]:";
			break;
		case 1:
			strOut += "[Warn]:";
			break;
		case 2:
			strOut += "[Debug]:";
			break;
		case 3:
			strOut += "[Info]:";
			break;
		}
		if (bwLogFile != null) {
			try {
				bwLogFile.write(strOut + msg + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (level < 3) {
				System.out.println(strOut + msg);
				try {
					bwLogFile.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.out.println(strOut + msg);
		}
	}
}
