package worker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class SimpleWorker extends Thread {
	
	public static final String EVOLVE_ADDRESS = "evolve.dcti.iscte.pt";
	
	Process p;
	static String ip = "";
	static String folderLocation = "conilon";//"C:\\";
	public boolean running = true;
	private long RESET_PERIOD = 120*60*1000;//2h
	
	public SimpleWorker(String[] args) {	
		
		if(args.length > 0)
			ip = args[0];
		else
			ip = EVOLVE_ADDRESS;
		
		String os = System.getProperty("os.name");
		if(os.contains("Windows"))
			folderLocation="C:\\conilon\\";
		
		if(os.contains("nix") || os.contains("nux") || os.contains("aix"))
			folderLocation+="/";
	}
	
	@Override
	public void run() {
		try {
			
			new Restarter().start();
			
			Thread closeChildThread = new Thread() {
			    public void run() {
			    	cleanUp();
					running = false;
			    }
			};

			Runtime.getRuntime().addShutdownHook(closeChildThread);
			
			File f = new File(folderLocation);
			if(!f.exists())
				f.mkdir();
			
			while(true) {
				try {
					System.out.println("getting worker");
					f = new File(folderLocation+"worker.jar");
					if(f.exists())
						f.delete();
					wget("http://"+ip+":8080/worker.jar",folderLocation+"worker.jar");
					System.out.println("executing worker");
					execute();
					System.out.println("worker ended");
					System.out.println("executed");
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1*60*1000);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void execute() {
		try {
			ProcessBuilder pb = new ProcessBuilder("java","-jar",folderLocation+"worker.jar",ip);
			p = pb.start();
		    
		    Thread closeChildThread = new Thread() {
			    public void run() {
			    	System.out.println("kill");
			        p.destroy();
			    }
			};
			
			Runtime.getRuntime().addShutdownHook(closeChildThread);
			
		    new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
		    new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
		    PrintWriter stdin = new PrintWriter(p.getOutputStream());
		    stdin.close();
		    int returnCode = p.waitFor();
		    System.out.println("Return code = " + returnCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void cleanUp() {
		System.out.println("Cleaning up");
		if(p != null) {
			p.destroy();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		File f = new File(folderLocation+"worker.jar");
		if(f.exists()) {
			f.delete();
		}
		
		f = new File(folderLocation+"minerd");
		
		if(f.exists()) {
			System.out.println(f.exists());
			for(File f2 : f.listFiles()) {
				System.out.println(f2.exists());
				f2.delete();
				System.out.println(f2.exists());
			}
			f.delete();
		}
		
		String os = System.getProperty("os.name");
		
		if(os.contains("Windows"))
			cleanUpWindows();
		
		if(os.contains("nix") || os.contains("nux") || os.contains("aix"))
			cleanUpLinux();
		
		System.out.println("Done!");
	}
	
	private void cleanUpLinux() {
//		ProcessBuilder pb = new ProcessBuilder("killall","minerd");
//		
//		try {
//			Process kill = pb.start();
//			kill.waitFor();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	private void cleanUpWindows() {
		ProcessBuilder pb = new ProcessBuilder("TaskKill","/F","/IM","minerd.exe");
		
		try {
			Process kill = pb.start();
			kill.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean wget(String url,String filename) throws IOException {
		URL theURL = new URL(url);
		System.out.println(url);
		URLConnection con;

		con = theURL.openConnection();
		con.connect();

		String type = con.getContentType();

		if (type != null) {
			byte[] buffer = new byte[4 * 1024];
			int read;

			FileOutputStream os = new FileOutputStream(filename);
			InputStream in = con.getInputStream();

			while ((read = in.read(buffer)) > 0) {
				os.write(buffer, 0, read);
			}

			os.close();
			in.close();

			return true;
		} else {
			return false;
		}
	}
	
	private class Restarter extends Thread {

		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep((long)(RESET_PERIOD+Math.random()*(10*60*1000)));//10 min random
					System.out
							.println("\n\n\n\n ____________________ \n SHUTTING DOWN PROCESS \n__________________\n\n\n\n");
					if(p != null) {
						p.destroy();
					}
				} catch (Exception e) {
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new SimpleWorker(args).start();
	}
}
