package comm_2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import worker.ClassLoaderObjectInputStream;



public class Comm {
	private int serverport;
	private String address;
	private final int MAX_SLEEP_TIME = 7000;
	
	public Comm(int serverport, String address) {
		super();
		this.serverport = serverport;
		this.address = address;
	}


	public Streams startConnectionToServer(PrintStream out) throws IOException{
		boolean errorSocket = false;
	
		
			
			//InetAddress address = InetAddress.getByName("127.0.0.1");
			out.println("Trying to connecto to server: "+ address+" : port: "+serverport);
			Socket socket = new Socket(this.address, this.serverport);
			out.println("Connected to server: "+ address+" : port: "+serverport);
			ObjectInputStream inputStream = new ObjectInputStream(
					socket.getInputStream());
			ObjectOutputStream outputStream = new ObjectOutputStream(
					socket.getOutputStream());


			socket.setKeepAlive(true);
			socket.setSoTimeout(0);
			errorSocket = true;
			return new Streams(inputStream, outputStream);
	}

	public Streams startConnectionToServerObject(CodeServerComunicator codeServerComunicator, PrintStream sysout, boolean isRestricted){
		boolean errorSocket = false; 
		while (errorSocket == false )
		try {
			
			sysout.println("Trying to connect streams to server"+this.address + " on port "+ this.serverport);
			Socket socket = new Socket(this.address, this.serverport);
			sysout.println("Socket");
			ClassLoaderObjectInputStream inputStream = new ClassLoaderObjectInputStream(
					socket.getInputStream(),codeServerComunicator,sysout, isRestricted);
			sysout.println("inputStream");
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
			
	
			sysout.println("Connected to server"+this.address + " on port "+ this.serverport);

			socket.setKeepAlive(true);
			socket.setSoTimeout(0);
			errorSocket = true;
			return new Streams(inputStream, outputStream);
		} catch (IOException e) {
			errorSocket = false;
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			
		}
		
		return null;
	}
	
	
}
