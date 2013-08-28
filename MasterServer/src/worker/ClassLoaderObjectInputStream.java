package worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import masterserver.TalkToCodeServer;

public class ClassLoaderObjectInputStream extends ObjectInputStream {

	private RemoteClassLoader classLoader;
	private TalkToCodeServer talkToCodeServer;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	// public ClassLoaderObjectInputStream(ClassSolver classSolver, int
	// problenNum, int version) throws IOException {
	// ClientInfo clientInfo = classSolver.getClientInfo(problenNum, version);
	// classLoader = new RemoteClassLoader(clientInfo.getOut(),
	// clientInfo.getIn());
	// // TODO Auto-generated constructor stub
	// }
	private int id;

	
	
	public ClassLoaderObjectInputStream(InputStream stream, TalkToCodeServer talkToCodeServer) throws IOException {
		super(stream);
		this.id = -1;
		this.talkToCodeServer = talkToCodeServer;
		classLoader = new RemoteClassLoader(talkToCodeServer);

	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		try {
			String name = desc.getName();
		//	System.out.println("Need " + name);
			return Class.forName(name, false, classLoader);
		} catch (ClassNotFoundException e) {
			System.out.println("Ops"+desc.getSerialVersionUID()+e);
			return super.resolveClass(desc);
		}
	}

	public synchronized void setProblemAndVersion(int id) {
		if(this.id!=id){
			this.id = id;
			classLoader.setId(id);
			//classLoader = new RemoteClassLoader(id, talkToCodeServer);
		}

	}
}