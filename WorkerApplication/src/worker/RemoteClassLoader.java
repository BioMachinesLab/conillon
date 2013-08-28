package worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import result.ClassRequest;

import comm_2.CodeServerComunicator;

public class RemoteClassLoader extends ClassLoader {

	private int id;
	
//	private ObjectOutputStream outStream;
//	private ObjectInputStream inStream;
	private CodeServerComunicator codeServerComunicator;

	public RemoteClassLoader(int id,
			CodeServerComunicator codeServerComunicator) {
		super();
		this.id = id;
		this.codeServerComunicator = codeServerComunicator;
//		this.outStream = outStream;
//		this.inStream = inStream;
	}

	public RemoteClassLoader(CodeServerComunicator codeServerComunicator) {
		this.codeServerComunicator = codeServerComunicator;
//		this.outStream = outStream;
//		this.inStream = inStream;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			int id = Integer.parseInt(name.substring(2, name.indexOf(".")));
			byte[] classInfo = codeServerComunicator.requestClass(id, name);

//			synchronized (outStream) {
//
//				outStream.writeObject(new ClassRequest(problemNumber, version,
//						name));
//				classInfo = (byte[]) inStream.readObject();
//				//			System.out.println("FIND CLASS" + classInfo.toString());
//			}

			return defineClass(name, classInfo, 0, classInfo.length);
		}catch (java.lang.NumberFormatException e){
			System.out.println("ERRO");
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassNotFoundException(name + " - " + e.toString());
		}
	}

}