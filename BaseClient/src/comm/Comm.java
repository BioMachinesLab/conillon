package comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Comm {
	private int serverport;
	private String address;
	private final int MAX_SLEEP_TIME = 2000;
	
	public Comm(int serverport, String address) {
		super();
		this.serverport = serverport;
		this.address = address;
	}


	public Streams startConnectionToServer(){
		boolean errorSocket = false;
		while (errorSocket == false )
		try {
			
			System.out.println("Trying to connecto to server: "+ address+" : port"+serverport);
			Socket socket = new Socket(this.address, this.serverport);
			System.out.println("Connecto to scoket: "+socket);
			ObjectInputStream inputStream = new TranslatorObjectInputStream(
					socket.getInputStream());
			ObjectOutputStream outputStream = new ObjectOutputStream(
					socket.getOutputStream());

			socket.setKeepAlive(true);
			socket.setSoTimeout(0);

			errorSocket = true;
			return new Streams(inputStream, outputStream);
		} catch (IOException e) {
			errorSocket = false;
			
			try {
				Thread.sleep(MAX_SLEEP_TIME);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				System.err.println(e1.getMessage());
			}
			System.err.println(e.getMessage());
			
		}
		
		return null;
	}


	
	
}
