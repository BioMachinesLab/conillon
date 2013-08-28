package worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.PrintStream;

import comm_2.CodeServerComunicator;

public class ClassLoaderObjectInputStream extends ObjectInputStream {

	private ClassLoader classLoader = this.getClass().getClassLoader();
	// private ObjectInputStream in;
	// private ObjectOutputStream out;
	int id;
	private boolean isRestricted;
	private PrintStream sysout;

	private CodeServerComunicator codeServerComunicator;

	// public ClassLoaderObjectInputStream(ClassSolver classSolver, int
	// problenNum, int version) throws IOException {
	// ClientInfo clientInfo = classSolver.getClientInfo(problenNum, version);
	// classLoader = new RemoteClassLoader(clientInfo.getOut(),
	// clientInfo.getIn());
	// // TODO Auto-generated constructor stub
	// }

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public ClassLoaderObjectInputStream(InputStream stream, int id,
			CodeServerComunicator codeServerComunicator, boolean isRestricted)
			throws IOException {
		super(stream);
		this.isRestricted = isRestricted;
		this.id = id;
		this.codeServerComunicator = codeServerComunicator;
		// this.out = out;
		// this.in = in;
		if (!isRestricted) {
			classLoader = new RemoteClassLoader(id, codeServerComunicator);
		}
	}

	public ClassLoaderObjectInputStream(InputStream stream,
			CodeServerComunicator codeServerComunicator, PrintStream sysout,
			boolean isRestricted) throws IOException {
		super(stream);
		this.isRestricted = isRestricted;
		this.id = -1;
		this.codeServerComunicator = codeServerComunicator;
		// this.out = out;
		// this.in = in;
		this.sysout = sysout;
		if (!isRestricted) {
			sysout.println("Will try to do the ClassLoader ");
			classLoader = new RemoteClassLoader(codeServerComunicator);
			sysout.println("ClassLoader done");
		}

	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		try {
			return super.resolveClass(desc);
		} catch (ClassNotFoundException e) {
			String name = desc.getName();

			// if (isRestricted) {
			// name = problemNumber + "." + version + "." + name;
			// }
//			sysout.println("Waiting for class ..." + name);
			Class<?> askedClass = Class.forName(name, false, classLoader);
//			sysout.println(askedClass.getName());
			return askedClass;
		}
	}

	public void setProblemAndVersion(int id) {
		if (this.id != id) {
			this.id = id;
			// if (!isRestricted) {
			// classLoader = new RemoteClassLoader(id, codeServerComunicator);
			// }
		}
	}
}
