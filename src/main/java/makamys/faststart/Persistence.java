package makamys.faststart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Persistence {
	
	static class Log {
		
		private File file;
		private OutputStream out;
		
		boolean failed = false;
		
		public Log(String path) {
			file = FastStart.getDataFile(path, false);
		}
		
		public void write(String msg) {
			if(failed) return;
			
			if(out == null) {
				try {
					file.createNewFile();
					out = new BufferedOutputStream(new FileOutputStream(file));
				} catch (IOException e) {
					logger.warn("Failed to open log file: " + file);
					e.getStackTrace();
					failed = true;
				}
			}
			if(out != null) {
				try {
					out.write((msg + "\n").getBytes(Charset.forName("UTF-8")));
				} catch (IOException e) {
					logger.warn("Failed to write to log file " + file);
					failed = true;
				}
			}
		}
		
		public void clear() {
			file.delete();
		}
	}
	
	private static final Logger logger = LogManager.getLogger("faststart");
	
	private static Properties props;
	
	public static String lastMods;
	
	public static Log erroredClassesLog = new Log("errored-classes.txt");
	
	public static void loadIfNotLoadedAlready() {
		if(props != null) return;
		
		props = new Properties();
		try {
			props.load(new BufferedInputStream(new FileInputStream(FastStart.getDataFile("persistence.txt"))));
		} catch (IOException e) {
			logger.warn("Failed to load persistence file");
			e.printStackTrace();
		}
		lastMods = props.getProperty("lastMods", "");
	}
	
	public static void save() {
		try {
			props.setProperty("lastMods", lastMods);
			
			props.store(new BufferedOutputStream(new FileOutputStream(FastStart.getDataFile("persistence.txt"))),
					"This file is used by FastStart to store data. You probably shouldn't edit it.");
		} catch (IOException e) {
			logger.warn("Failed to save persistence file");
			e.printStackTrace();
		}
	}
	
}
