package worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
		
		boolean relinquished = false;
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
			System.out.println("I got this class, but couldn't separate the package! "+name);
			
			throw e;
		}  catch(ThreadDeath e) {
			e.printStackTrace();
			
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new ClassNotFoundException(name + " - " + e.toString());
		} 
	}
	
}