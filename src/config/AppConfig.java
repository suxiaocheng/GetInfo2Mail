package config;

import java.io.File;

public class AppConfig {
	public static final int RETRY_TIMES = 100;
	public static final int CONNECTION_TIMEOUT = 30000;
	public static final boolean DEBUG = false;
	public static final String WORKING_DIR = File.separator + "home" + File.separator + "pi" + File.separator
			+ "hz_house" + File.separator;
}
