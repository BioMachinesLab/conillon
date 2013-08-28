package masterserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import comm.Comm;
import comm.ConnectionType;
import comm.Streams;



public class TalkToCodeServer {
	private ObjectInputStream getCodeInputStream;
	private ObjectOutputStream getCodeOutputStream;
	private String codeServerAddress;
	private int codeServerPort;
	private boolean lock = false;

	public TalkToCodeServer(String codeServerAddress, int codeServerPort) {
		super();
		this.codeServerAddress = codeServerAddress;
		this.codeServerPort = codeServerPort;
		this.lock = false;
	}

	public void connectCodeServer() {
		System.out.println("Trying to connect to CodeServer: "
				+ codeServerAddress + " on port " + codeServerPort);
		try {
			
			Streams InOut = new Comm(codeServerPort, codeServerAddress).startConnectionToServer();

			if(InOut!=null){
				this.getCodeInputStream = InOut.returnObjectInputStream();
				this.getCodeOutputStream = InOut.returnObjectOutputStream();
				
				getCodeOutputStream.writeObject(ConnectionType.CLASS_REQUESTER);
				
			}
			else{
				System.out.println("Comm has problems!!");
			}
			
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*
 * 
 * 
 * 
 * public synchronized void lock(){
 
		while(this.lock){
			try {
 
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
		}
		this.lock = true;
 
	}
	public synchronized void unLock(){
 
		this.lock = false;
		notify();
 
	}
 
 */


	public synchronized void sendObject(Object data){
	//	System.out.println("fala"+this.lock);
		try {
			
				getCodeOutputStream.writeObject(data);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized Object receiveObject(){
		try {
				Object data = getCodeInputStream.readObject();
				return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
