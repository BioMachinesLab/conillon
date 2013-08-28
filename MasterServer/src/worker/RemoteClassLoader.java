package worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import masterserver.TalkToCodeServer;

import result.ClassRequest;

public class RemoteClassLoader extends ClassLoader {

	private int id;
	private TalkToCodeServer talkToCodeServer;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;

	public RemoteClassLoader(TalkToCodeServer talkToCodeServer) {
		this.talkToCodeServer = talkToCodeServer;
	}

	public RemoteClassLoader(int id, TalkToCodeServer talkToCodeServer) {
		super();
		this.id = id;
		this.talkToCodeServer = talkToCodeServer;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] classInfo = null;

		if (outStream == null && inStream == null) {
			synchronized (talkToCodeServer) {
				talkToCodeServer.sendObject((new ClassRequest(id, name)));
				classInfo = (byte[]) talkToCodeServer.receiveObject();
			}
			System.out.println("Class request: "+name);
			return defineClass(name, classInfo, 0, classInfo.length);
		} else
			try {

				outStream.writeObject(new ClassRequest(id, name));

				classInfo = (byte[]) inStream.readObject();

				return defineClass(name, classInfo, 0, classInfo.length);
			} catch (IOException e) {
				e.printStackTrace();

				throw new ClassNotFoundException(name + " - " + e.toString());
			}

	}

}
