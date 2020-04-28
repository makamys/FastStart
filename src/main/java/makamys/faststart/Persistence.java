package makamys.faststart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Persistence {
	
	private static final Logger logger = LogManager.getLogger("faststart");
	
	private static Properties props;
	
	public static String lastMods;
	
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
