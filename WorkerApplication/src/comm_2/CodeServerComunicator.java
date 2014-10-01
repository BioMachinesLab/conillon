package comm_2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import result.ClassRequest;

public class CodeServerComunicator {
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private int gettingCode = 0;
	private boolean killingTask = false;

	public Hashtable<ClassRequest, byte[]> classes = new Hashtable<ClassRequest, byte[]>();

	public CodeServerComunicator(ObjectInputStream in, ObjectOutputStream out) {
		super();
		this.in = in;
		this.out = out;
	}

//	public void clearClientData(int id) {
//
//		Iterator<ClassRequest> iterator = classes.keySet().iterator();
//		while (iterator.hasNext()) {
//			ClassRequest request = iterator.next();
//			if (request.getId() == id) {
//				iterator.remove();
//			}
//		}
//
//	}

	public byte[] requestClass(int id, String name) throws IOException,
			ClassNotFoundException {
		ClassRequest classRequest = new ClassRequest(id, name);
		byte[] classInfo = null;
		
		boolean releasedPermission = false;
		
		try {
			if (classes.containsKey(classRequest)) {
				classInfo = classes.get(classRequest);
			} else {
	
				synchronized (this) {
					permissionToGetCode();
					
					out.writeObject(classRequest);
					classInfo = (byte[]) in.readObject();
					
					gettingCodeDone();
					releasedPermission = true;
					
					if (classInfo == null) {
						System.out.println("Did not found file or class " + name);
						if(!releasedPermission) {gettingCodeDone(); releasedPermission = true;}
						throw new ClassNotFoundException(name);
					}
					classes.put(classRequest, classInfo);
				}
			}
		}catch(ClassCastException e) {
			e.printStackTrace();
			if(!releasedPermission) {gettingCodeDone(); releasedPermission = true;}
//			throw new AbortWorkerException();
		} catch(IOException e){
			if(!releasedPermission) {gettingCodeDone(); releasedPermission = true;}
			throw e;
		} catch(ClassNotFoundException e) {
			if(!releasedPermission) {gettingCodeDone(); releasedPermission = true;}
			throw e;
		}
		return classInfo;
	}

	public synchronized void sendObject(Object object) throws IOException {
		out.writeObject(object);
	}
	
	public synchronized void permissionToKill() {
		while(gettingCode > 0){
			try{
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		killingTask = true;
	}
	
	public synchronized void killingDone() {
		killingTask = false;
		notifyAll();
	}
	
	public synchronized void permissionToGetCode() {
		while(killingTask) {
			try{
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		gettingCode++;
	}
	
	public synchronized void gettingCodeDone() {
		gettingCode--;
		notifyAll();
	}

	public void disconect() {
		try {
			System.out.println("DISCONNECTING CODE SERVER COMM!");
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
