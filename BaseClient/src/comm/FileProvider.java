package comm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import client.Client;

public class FileProvider implements Serializable{
	private static FileProvider defaultFileProvider = new FileProvider();
	
	public ByteArrayInputStream getFile(String name) throws IOException{
		return new ByteArrayInputStream(Client.getBytesFromFile(new File(name)));
	}

	public static FileProvider getDefaultFileProvider() {
		return defaultFileProvider;
	}
	
	public Class<?> getClassByName(String className) throws ClassNotFoundException{
		return Class.forName(className);
	}
}