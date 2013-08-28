package masterserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import worker.WorkerInformation;

import comm.ConnectionType;
import comm.InfrastructureInformation;
import comm.RunMode;

public class CommServer {
	// NETWORK PORTS AND IP ADDRESS
	public static int DEFAULTSERVERPORT = 9999;
	public static int DEFAULTENCRYPTEDSERVERPORT = 9999;

	public int serverport;
	// MASTERSERVER
	// private String masterServerIPaddress = "localhost";
	// private String masterServerIPaddress = "evolve.dcti.iscte.pt";
	private static String masterServerIPaddress; // = "10.40.50.96";
	// private String MasterServerIPaddress = "10.10.34.85";
	// private String MasterServerIPaddress = "192.168.1.4";
	// private String MasterServerIPaddress = "193.136.188.196";
	// private String MasterServerIPaddress = "192.168.0.31";

	private int MasterServerEncryptedPort = 10001;
	private int codeServerEncryptedPort_TaskDeliver = 10001;

	// CODE SERVER
	private String codeServerIPaddress; // = masterServerIPaddress;
	// private String codeServerIPaddress = "192.168.1.4";

	// private String codeServerIPaddress = "193.136.188.196";
	// private String codeServerIPaddress = "192.168.0.31";
	private int codeServerEncryptedPort = 10000;
	private boolean RUNINGLOCALHOST = true;

	private Hashtable<Integer, WorkerInformation> WorkerInformationVector = new Hashtable<Integer, WorkerInformation>();
	private int workerID = 0;

	private RunMode runMode;

	private InfrastructureInformation infrastructureInformation;

	public CommServer(boolean runinglocalhost) {
		super();
		this.RUNINGLOCALHOST = runinglocalhost;
		if (RUNINGLOCALHOST)
			this.serverport = DEFAULTSERVERPORT;
		else if (!RUNINGLOCALHOST)
			this.serverport = DEFAULTENCRYPTEDSERVERPORT;

		runMode = RunMode.fullWorkerLoad;
	}

	public CommServer(String server, boolean b) {
		this(b);
		masterServerIPaddress = server;
		codeServerIPaddress = server;
	}

	private synchronized int addWorkerData(WorkerInformation data) {

		WorkerInformationVector.put(this.workerID, data);
		notify();
		return workerID++;
	}

	// not encrypted
	class ReceiveConnectionsThread extends Thread {

		public ReceiveConnectionsThread() {

		}

		@Override
		public void run() {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(serverport);
				System.out.println("Waiting for connections on socket: "
						+ serverSocket);
				while (true) {
					Socket socket = serverSocket.accept();
					socket.setKeepAlive(true);
					socket.setSoTimeout(0);

					System.out.println("new connection:" + socket);
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(
							socket.getInputStream());
					new HandleConnectionsThread(socket, in, out).start();

				}
			} catch (IOException e) {
				e.printStackTrace();

			}

		}
	}

	// encrypt connection

	class ReceiveEncryptedConnectionsThread extends Thread {

		public ReceiveEncryptedConnectionsThread() {

		}

		@Override
		public void run() {
			ServerSocket serverSocket = null;
			try {
				SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
						.getDefault();
				SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory
						.createServerSocket(serverport);

				System.out.println("Waiting for connections on socket: "
						+ sslserversocketfactory);
				while (true) {
					try {
						SSLSocket sslsocket = (SSLSocket) sslserversocket
								.accept();
						System.out.println("new connection");
						ObjectOutputStream out = new ObjectOutputStream(
								sslsocket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(
								sslsocket.getInputStream());
						new HandleConnectionsThread(sslsocket, in, out).start();

					} catch (IOException e) {

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (serverSocket != null)
						serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class DoEncryptedConnectionsThread extends Thread {

		public DoEncryptedConnectionsThread() {

		}

		@Override
		public void run() {
			System.out.println("Trying to connect to Server: ");
			try {
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory
						.getDefault();
				SSLSocket sslsocket = (SSLSocket) sslsocketfactory
						.createSocket("localhost", serverport);
				ObjectInputStream inputStream = new ObjectInputStream(
						sslsocket.getInputStream());
				ObjectOutputStream outputStream = new ObjectOutputStream(
						sslsocket.getOutputStream());
				outputStream.writeObject(ConnectionType.CLASS_SOLVER);
				sslsocket.setKeepAlive(true);
				sslsocket.setSoTimeout(0);

				System.out.println("Connected to CodeServer");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class HandleConnectionsThread extends Thread {
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private Socket socket;

		public HandleConnectionsThread(Socket socket, ObjectInputStream in,
				ObjectOutputStream out) {
			this.in = in;
			this.out = out;
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				System.out.println("COM:waiting type:");

				while (true) {
					ConnectionType type = (ConnectionType) in.readObject();
					System.out.println("type:" + type);
					switch (type) {
					case JA_WORKER_HELLO:
						handleSlaveInformation(in, out);
						out.writeObject(ConnectionType.PING);
						break;
					case PONG:
						System.out.println("Pong");
						break;
					default:
						System.out.println("ERROR!");
					}
				}

			} catch (IOException e) {

				// e.printStackTrace();
			} catch (ClassNotFoundException e) {

				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e1) {

					// e1.printStackTrace();
				}
				return;

			}
		}

	}

	private void handleSlaveInformation(ObjectInputStream in,
			ObjectOutputStream out) {
		try {
			WorkerInformation si = (WorkerInformation) in.readObject();
			if (si != null) {	
				int id = addWorkerData(si);
				out.writeObject(id);
				System.out.println("Master server new slave informations: "
						+ si.toString());
				out.writeObject(infrastructureInformation);
				System.out
						.println("Master server sent infrastructureInformation: "
								+ infrastructureInformation.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void execute() {

		if (RUNINGLOCALHOST) {
			this.infrastructureInformation = new InfrastructureInformation(
					this.codeServerIPaddress, this.codeServerEncryptedPort,
					this.codeServerEncryptedPort_TaskDeliver,
					this.masterServerIPaddress, this.MasterServerEncryptedPort,
					this.runMode);
			new ReceiveConnectionsThread().start();
			new MasterServer(this.infrastructureInformation).execute();

			// DoConnectionsThread doConnectionsThread = new
			// DoConnectionsThread();
			// doConnectionsThread.start();
		} else if (!RUNINGLOCALHOST) {
			ReceiveEncryptedConnectionsThread receiveEncryptedConnectionsThread = new ReceiveEncryptedConnectionsThread();
			receiveEncryptedConnectionsThread.start();
			// DoEncryptedConnectionsThread doConnectionsThread = new
			// DoEncryptedConnectionsThread();
			// doConnectionsThread.start();
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[1];
			try {
				InetAddress addr = InetAddress.getLocalHost();
				// Get hostname
				args[0] = addr.getHostAddress();
			} catch (UnknownHostException e) {
			}

		}
		CommServer comServer = new CommServer(args[0], true);
		comServer.execute();
	}
}
