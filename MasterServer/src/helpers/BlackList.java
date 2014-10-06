package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class BlackList extends Thread {

	private ArrayList<String> list;
	private File file;
	
	public BlackList() {
		list = new ArrayList<String>();
		file = new File("blacklist.txt");
		
		readFromFile(file);
	}
	
	@Override
	public void run() {
		try {
			readFromFile(file);
			sleep(10*60*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void readFromFile(File file){
		try {
			Scanner s = new Scanner(file);
			list.clear();
			
			while(s.hasNextLine()) {
				list.add(s.nextLine().trim());
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			synchronized (this) {
				notifyAll();
			}
		}
	}
	
	public void addToBlackList(String workerAddress){
		PrintWriter printWriter = null;
		list.add(workerAddress);
		
		try {
			printWriter = new PrintWriter(file);
			for (String bl : list) {
				printWriter.append(bl);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			printWriter.close ();
		}
		
	}
	
	public void replaceBlackListContent(String newList){
		PrintWriter printWriter = null;
		
		try {
			printWriter = new PrintWriter(file);
			printWriter.println(newList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			printWriter.close ();
			readFromFile(file);
		}
		
	}
	
	public synchronized void checkIfBanned(String ip){
		while(isBanned(ip)){
			try {
				System.out.println(ip + " BANNED!");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean isBanned(String ip){
		return list.contains(ip);
	}
	
	public ArrayList<String> getList() {
		return list;
	}
	
}
