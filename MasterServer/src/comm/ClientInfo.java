package comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import result.ClassRequest;

public class ClientInfo {

	private int problemNumber; 
	private int version;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public ClientInfo(int problemNumber, int version, ObjectOutputStream out,
			ObjectInputStream in) {
		super();
		this.problemNumber = problemNumber;
		this.version = version;
		this.out = out;
		this.in = in;
	}

	public boolean checkVersion(int problemNumber, int version) {
		return this.problemNumber == problemNumber && this.version == version;
	}

	public void addNewClassProvider(ObjectOutputStream out, ObjectInputStream in) {
		//TODO:

	}

	public byte[] getClass(ClassRequest request) {
		byte[] neededClass = null; 
		try {
			out.writeObject(request);
			neededClass = (byte[]) in.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return neededClass;
	}

}
