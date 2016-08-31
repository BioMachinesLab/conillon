import client.Client;
import comm.ClientPriority;

/**
 * @author miguelduarte42
 * 
 * The purpose of this program is to check if Conillon is accepting tasks or not.
 * If there is some problem with Conillon, then the program will connect via SSH
 * and restarts it. It's not pretty, but it works! :)
 * 
 * The machine where this code runs should have passwordless access to the Conillon server.
 */
public class Watchdog {
	
	static long timeInMs = 5 * 60  * 1000; //5 minutes
	static String server = "evolve.dcti.iscte.pt";
	
	public static void main(String[] args) {
		
		while(true) {
			
			try {
				Client c = new Client("watchdog",ClientPriority.VERY_HIGH,server,0,server, 0, false); 
				c.commit(new WatchdogTask());
				c.getNextResult();
				c.disconnect();
				System.out.println("Everything is ok here!");
			} catch(Exception e) {
				System.out.println("Conillon is down!");
				restartConillon(); 
			}
			
			try {
				Thread.sleep(timeInMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void restartConillon() {
		try {
			ProcessBuilder pb = new ProcessBuilder("ssh", 
	                "evolve.dcti.iscte.pt", 
	                "screen -d -m ./start_conillon.sh;");
			Process process = pb.start();
			process.waitFor();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
