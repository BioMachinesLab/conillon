package code;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import result.ClassRequest;

import comm.CompressedObjectInputStream;
import comm.CompressedObjectOutputStream;

public class ClientInfo {

	private int id;
//	private int problemNumber; 
//	private int version;
	private long codeBase;
	private ObjectOutputStream out; 
	private ObjectInputStream in;
	private boolean deadClient = false;

	public ClientInfo(int id, ObjectOutputStream out,
			ObjectInputStream in, long codeBase) {
		super();
//		this.problemNumber = problemNumber;
//		this.version = version;
		this.out = out; 
		this.in = in;
		this.id = id;
		this.codeBase = codeBase;

	}
	
	public boolean checkId(int otherIdd){
		return this.id==otherIdd;
	}
	
	public long getCodeBase() {
		return codeBase;
	}

//	public void addNewClassProvider(ObjectOutputStream out, ObjectInputStream in) {
//		//TODO:
//
//	}

//	public byte[] getClass(ClassRequest request) {
//		byte[] neededClass = null; 
//		try {
//			synchronized (out) {
//				out.writeObject(request);
//				neededClass = (byte[]) in.readObject();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return neededClass;
//	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientInfo other = (ClientInfo) obj;
		if (codeBase != other.codeBase)
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	public void requestClass(ClassRequest request) {
		try {
			synchronized (out) {
//				System.out.println("__requesting "+request.getName()+" "+request.getId());
				out.writeObject(request);
//				System.out.println("__received "+request.getName()+" "+request.getId());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setDeadClient(boolean dead){
		this.deadClient = dead;
	}
	
	public boolean isDeadClient() {
		return deadClient;
	}
}
