package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RoomInformation extends Thread {

	private File file;
	private HashMap<String,ArrayList<String>> rooms;
	private long lastFileModification = 0;
	
	public RoomInformation() {
		file = new File("hostnames.txt");
		rooms = new HashMap<String, ArrayList<String>>();
		
		readHostFromFile();
	}
	
	@Override
	public void run() {
		while(true){
			try {
				readHostFromFile();
				Thread.sleep(10*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void readHostFromFile(){
		
		if(lastFileModification == file.lastModified()){
			return;
		}
		lastFileModification = file.lastModified();
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			rooms.clear();
			String room ="";
			ArrayList<String> hosts = new ArrayList<String>();
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.startsWith("#")){
					room = line.replace("#", "");
				}else if(line.isEmpty()){
					rooms.put(room, hosts);
					room = "";
					hosts = new ArrayList<String>();
				}else{
					hosts.add(line.trim());
				}
			}
			
			if(!hosts.isEmpty())
				rooms.put(room, hosts);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			System.out.println("CLOSING!!!");
			if(scanner != null)
				scanner.close();
		}
	}
	
	public HashMap<String, ArrayList<String>> getRooms() {
		return rooms;
	}
}