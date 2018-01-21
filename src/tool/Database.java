package tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import app.GetHouseBuildingDetail;
import config.AppConfig;
import debug.Log;

public class Database {
	private static String databaseName = "hz_house";
	protected static Connection conn = null;

	private static final String strDBName = "hz_house.db";
	private static final String strDBXZName = strDBName + ".xz";

	static {
		/* Check for dir */
		File fDir = new File(AppConfig.WORKING_DIR);
		if (fDir.exists() == true) {
			if (fDir.isDirectory() == false) {
				fDir.delete();
			}
		} else {
			fDir.mkdirs();
		}
		
		databaseName = "jdbc:sqlite:" + AppConfig.WORKING_DIR + databaseName + ".db";
		try {
			Class.forName("org.sqlite.JDBC");
			conn = (Connection) DriverManager.getConnection(databaseName);
		} catch (SQLException e) {
			Log.e("Connection error!");
			e.printStackTrace();
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			Log.e("Class no found error!");
			e.printStackTrace();
			System.exit(-2);
		}
		Log.d("Database: " + databaseName + " load sucessfully!");
	}

	public static boolean execSqlTable(String statement) {
		boolean status = true;

		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(statement);
			stmt.close();
		} catch (SQLException e) {
			Log.e("Execute statement: " + statement + " error!");
			e.printStackTrace();
		}

		return status;
	}

	public static String queryTable(String statement, String item) {
		StringBuffer sb = new StringBuffer();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(statement);
			while (rs.next()) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(rs.getString(item));
			}
			stmt.close();
		} catch (SQLException e) {
			Log.e("Execute statement: " + statement + " error!");
			e.printStackTrace();
			return null;
		}

		return sb.toString();
	}

	public static void closeDatabase() {
		if (conn != null) {
			try {
				conn.close();
				conn = null;
				Log.d("Close database sucessfully!");
			} catch (SQLException e) {
				Log.e("Close database fail!");
				e.printStackTrace();
			}
		}
	}

	public static String createTableNameStr(String name) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			Byte b[] = new Byte[2];
			b[0] = (byte) ((name.charAt(i) & 0xf) + '0');
			b[1] = (byte) (((name.charAt(i) >> 4) & 0xf) + '0');
			sb.append(b[0] + b[1]);
		}

		return sb.toString();
	}

	public static String qureyForTable(String name) {
		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + name + "';";
		StringBuffer sb = new StringBuffer();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(rs.getString("name"));
			}
			stmt.close();
		} catch (SQLException e) {
			Log.e("Execute statement: " + sql + " error!");
			e.printStackTrace();
			return null;
		}

		return sb.toString();
	}

	public static void dropTable(String name) {
		String sql = "DROP TABLE IF EXISTS " + name + " ;";
		execSqlTable(sql);
	}

	public static String compressDB() {
		LZMA2Options options = new LZMA2Options();
		FileOutputStream outfile;
		Log.d("Encoder memory usage: " + options.getEncoderMemoryUsage() + " KiB");
		Log.d("Decoder memory usage: " + options.getDecoderMemoryUsage() + " KiB");
		try {
			File fDBOut = new File(AppConfig.WORKING_DIR + strDBXZName);
			File fDBIn = new File(AppConfig.WORKING_DIR + strDBName);
			if (fDBOut.exists() == true) {
				fDBOut.delete();
			}
			outfile = new FileOutputStream(AppConfig.WORKING_DIR + strDBXZName);
			XZOutputStream out = new XZOutputStream(outfile, options);
			InputStream in = new FileInputStream(fDBIn);

			byte[] buf = new byte[8192];
			int size;
			while ((size = in.read(buf, 0, buf.length)) != -1)
				out.write(buf, 0, size);
			out.finish();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			outfile = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			outfile = null;
		}

		if (outfile == null) {
			return AppConfig.WORKING_DIR + strDBName;
		}
		return AppConfig.WORKING_DIR + strDBXZName;
	}

	public static void main(String[] args) {
		compressDB();
	}
}
