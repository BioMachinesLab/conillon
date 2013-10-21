package comm_2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import result.ClassRequest;

public class CodeServerComunicator {
	private ObjectInputStream in;
	private ObjectOutputStream out;

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
		
		try {
			if (classes.containsKey(classRequest)) {
				classInfo = classes.get(classRequest);
			} else {
	
				synchronized (this) {
					System.out.println("Need " + name);
					out.writeObject(classRequest);
					classInfo = (byte[]) in.readObject();
					if (classInfo == null) {
						System.out.println("Did not found file or class " + name);
						throw new ClassNotFoundException(name);
					}
					System.out.println("GOT CLASS" + classInfo.toString());
	
					classes.put(classRequest, classInfo);
				}
			}
		}catch(ClassCastException e) {
			e.printStackTrace();
//			throw new AbortWorkerException();
		}
		return classInfo;
	}

	public synchronized void sendObject(Object object) throws IOException {
		out.writeObject(object);
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
