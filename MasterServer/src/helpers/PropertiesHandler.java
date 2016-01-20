package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import logging.Logger;

public class PropertiesHandler {
	
 
	public String getPropValue(String key) {
 
		try {

			switch (key) {
			case "logging_path":
				return instance.properties.getProperty("logging_path");
			case "file_path_worker_start":
				return instance.properties.getProperty("file_path_worker_start");
			case "file_path_worker_stop":
				return instance.properties.getProperty("file_path_worker_stop");
			case "file_path_client_start":
				return instance.properties.getProperty("file_path_client_start");
			case "file_path_client_stop":
				return instance.properties.getProperty("file_path_client_stop");
			default:
				throw new UnsupportedOperationException("key:" + key);				
			}
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} 
		return "";
	}
	
	protected PropertiesHandler() {
				
	}
	
	private void checkModifiedTimestamp() {
		
		String propFileName = "config.properties";
		//f = new File(String.format(".//resources//%d", propFileName));
		
		//long lastModifiedAux = 0;
		
		//if(!f.exists()) { /*TODO handle exception*/ }
		
		//lastModifiedAux = f.lastModified();
		//if (instance.lastModified == lastModifiedAux) { return; }
		//instance.lastModified = lastModifiedAux;
		
		InputStream inputStream = null;
		try {
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream == null) {
				//TODO handle exception
				//throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				return;
			}		
			instance.properties.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {				
				if (inputStream != null) { inputStream.close(); }				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
	}
	
	
	public static PropertiesHandler getInstance() {

		if (instance == null) {
			instance = new PropertiesHandler();
		}
		instance.properties = new Properties();
		instance.checkModifiedTimestamp();
		return instance;
	}
	
	private static PropertiesHandler instance = null;	
	private Properties properties;	
	private File f;
	private long lastModified;
}
