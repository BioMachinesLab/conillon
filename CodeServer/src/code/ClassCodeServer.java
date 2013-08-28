package code;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import result.ClassNameRequest;
import result.ClassRequest;
import result.Request;
import webserver.Webserver;

import comm.ConnectionType;

public class ClassCodeServer {

	public static int DEFAULTCLASSPORT = 10000;
	public static int DEFAULTWEBSEVERPORT = 8080;
	private int masterClassesPort = DEFAULTCLASSPORT;

	private ClassSolver classSolver = new ClassSolver();

	// private ClassProvider classProvider = new ClassProvider(classSolver);
	// private ClassManager classNameManager = new ClassManager();

	// public HashMap<ClassRequest, byte[]> classes = new HashMap<ClassRequest,
	// byte[]>();

	private ClassProvider classProvider = new ClassProvider(classSolver);

	// ,classNameManager);

	public ClassCodeServer() {

	}

	public ClassCodeServer(String webServerPort) {
		DEFAULTWEBSEVERPORT = Integer.parseInt(webServerPort);
	}

	public void execute() {
		ReceiveConnectionsForClassesThread receiveConnectionsForClassesThread = new ReceiveConnectionsForClassesThread();
		receiveConnectionsForClassesThread.start();

		System.out.println("Class Code Server started... ");
		System.out.println("Version: May 31 - 2011");
	}

	public byte[] getClassBy(ClassRequest request) throws InterruptedException {
		return classProvider.getClassBy(request);
	}

	public static void main(String[] args) {
		ClassCodeServer ccs;
		if (args.length > 0) {
			ccs = new ClassCodeServer(args[0]);
		} else {
			ccs = new ClassCodeServer();
		}
		ccs.execute();
		new Webserver(DEFAULTWEBSEVERPORT, ccs);
	}

	class ReceiveConnectionsForClassesThread extends Thread {

		public ReceiveConnectionsForClassesThread() {

		}

		public void run() {
			int id = 1000;
			id = new Random().nextInt(20000);
			ServerSocket serverSocket = null;
			ObjectOutputStream out = null;
			ObjectInputStream in = null;
			try {
				serverSocket = new ServerSocket(masterClassesPort);
				System.out.println("Server:" + serverSocket);
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						System.out.println("socket" + socket);
						ConnectionType type;
						out = new ObjectOutputStream((socket.getOutputStream()));

						in = new ObjectInputStream((socket.getInputStream()));

						Object input = in.readObject();
						if (!(input instanceof ConnectionType))
							System.out.println("Not what i was expecting...");
						else {
							type = (ConnectionType) input;

							switch (type) {
							case CLASS_SOLVER:
								id++;
								out.writeObject(id);

								System.out.println("Got new connection:" + type
										+ "(" + id + ")");
								long codeBase = (Long) in.readObject();
								classProvider.addProvider(new ClientInfo(id, out, in, codeBase));
								new ProviderThread(id, out, in, classProvider)
										.start();
								System.out
										.println("New class solver connection "
												+ socket);

								break;
							case CLASS_REQUESTER:
								new RequesterThread(socket, in, out).start();
								System.out
										.println("New class requester connection "
												+ socket);
								break;

							case APPLET_CLASS_REQUESTER: // TODO ... finish
								new AppletRequesterThread(socket, in, out)
										.start();
								System.out
										.println("New Japplet class requester connection "
												+ socket);
								break;

							default:
								System.out.println("ERROR!");
							}
						}
					} catch (IOException e) {

						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();

			} finally {
				System.out.println("something wrong!!!");
				try {
					classSolver.remove(id);
					if (serverSocket != null)
						serverSocket.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ProviderThread extends Thread {
		private int id;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		private ClassProvider classProvider;

		// private ClassManager classNameManager;

		public ProviderThread(int id, ObjectOutputStream out,
				ObjectInputStream in, ClassProvider classProvider) {
			// ClassManager classNameManager) {
			super();
			this.id = id;
			this.out = out;
			this.in = in;
			this.classProvider = classProvider;
			// this.classNameManager = classNameManager;
		}

		public void run() {
			try {
				while (true) {
					Request request = null;
					request = (Request) in.readObject();
					if (request instanceof ClassRequest) {
						byte[] neededClass = null;

						ClassRequest classRequest = (ClassRequest) request;
						neededClass = (byte[]) in.readObject();

						classProvider.addClass(classRequest, neededClass);
					} 
//					else if (request instanceof ClassNameRequest) {
//						ClassNameRequest classNameRequest = (ClassNameRequest) request;
//						int classProviderId = classProvider.getClassProvider(
//								classNameRequest, id);
//						classNameRequest.setClassProvider(classProviderId);
//						synchronized (out) {
//							out.writeObject(classNameRequest);
//						}
//					}
				}
			} catch (IOException e) {
				classProvider.clientDone(id);
				System.out.println("Client " + id + " disconected ");
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			
		}
	}

	class RequesterThread extends Thread {

		private ObjectOutputStream out;
		private ObjectInputStream in;
		private Socket socket;

		public RequesterThread(Socket socket, ObjectInputStream in,
				ObjectOutputStream out) {
			super();
			this.out = out;
			this.in = in;
			this.socket = socket;

			System.out.println("New Class Requester");
		}

		@Override
		public void run() {
			try {
				try {
					while (true) {

						ClassRequest neededClassRequest = (ClassRequest) in
								.readObject();
						byte[] neededClass = null;
						System.out.println("Worker asked for class: " + neededClassRequest.getName());
						neededClass = getClassBy(neededClassRequest);

						out.writeObject(neededClass);
						// out.writeObject(newClass);
						 System.out.println("Sent class to worker: " + neededClass);
					}
				} catch (EOFException e) {
					try {
						in.close();
						out.close();
						socket.close();
						return;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					try {
						in.close();
						out.close();
						socket.close();
						return;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (InterruptedException e) {
				// TODO: handle exception
			}
		}
	}

	class AppletRequesterThread extends Thread {

		private ObjectOutputStream out;
		private ObjectInputStream in;
		private Socket socket;

		public AppletRequesterThread(Socket socket, ObjectInputStream in,
				ObjectOutputStream out) {
			super();
			this.out = out;
			this.in = in;
			this.socket = socket;

			System.out.println("New Class Requester");
		}

		@Override
		public void run() {
		}
	}

}
