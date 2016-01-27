package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import result.ClassNameRequest;
import result.ClassRequest;
import result.Request;
import result.Result;
import tasks.Task;
import comm.ClassNameManager;
import comm.ClientPriority;
import comm.Comm;
import comm.ConnectionType;
import comm.Streams;
import comm.TranslatorObjectInputStream;
import comm.translator.ASMTranslator;
import comm.translator.Translator;

public class Client {

	private long startTime;
	private int masterPort;
	private String masterAddress;
	private Socket socket;
	private ObjectOutputStream out;
	private TranslatorObjectInputStream in;
	private ClientPriority priority;
	private int codeServerPort;
	private String codeServerAddress;
	private LinkedList<Result> results = new LinkedList<Result>();
	private int numberOfUnrespondedTasks = 0;
	private int myID;
	// private int problemNumber;
	// private int version;
	private Translator translator;
	private String packageName;
	private int totalNumberOfTasks;
	private ClassNameManager classNameManager;
	private ObjectOutputStream outputStream;
	private String desc;
	private ObjectInputStream inputStream;
	
	private String macAddress;
	private String hostName;
	
	public Client(String desc, ClientPriority priority, String masterAddress,
			int masterPort, String codeServerAddress, int codeServerPort) {
		this(desc, priority, masterAddress, masterPort, codeServerAddress,
				codeServerPort, 0);
	}

	public Client(String desc, ClientPriority priority, String masterAddress,
			int masterPort, String codeServerAddress, int codeServerPort,
			int totalNumberOfTasks) {
		this.desc = desc;
		this.priority = priority;
		// this.problemNumber = problemNumber;
		// this.version = version;
		this.masterAddress = masterAddress;
		this.codeServerAddress = codeServerAddress;
		this.totalNumberOfTasks = totalNumberOfTasks;

		startTime = System.currentTimeMillis();

		if (masterPort == 0) {
			this.masterPort = 10001;
		} else {
			this.masterPort = masterPort;
		}

		if (codeServerPort == 0) {
			this.codeServerPort = 10000;
		} else {
			this.codeServerPort = codeServerPort;
		}

		connectCodeServer();
		connectMaster();
		// execute();
	}

	public int getMyID() {
		return myID;
	}

	private void connectCodeServer() {
		System.out.println("Trying to connect to CodeServer: "
				+ codeServerAddress + " on port " + this.codeServerPort);
		try {

			Streams InOut = new Comm(codeServerPort, codeServerAddress)
					.startConnectionToServer();

			if (InOut != null) {
				inputStream = InOut.returnObjectInputStream();
				outputStream = InOut.returnObjectOutputStream();
				outputStream.writeObject(ConnectionType.CLASS_SOLVER);

				try {
					this.myID = (Integer) inputStream.readObject();
					packageName = "C" + myID + "/";
					classNameManager = new ClassNameManager(this);
					translator = new ASMTranslator(packageName,
							classNameManager);// .setPackageName(packageName);
					
					outputStream.writeObject(classNameManager.getClientHash());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Client ID:" + myID);

				// outputStream.writeObject(new Integer(problemNumber));
				// outputStream.writeObject(new Integer(version));
				new returnClassesThread(outputStream, inputStream).start();

				System.out.println("Connected to CodeServer");
			} else {
				System.out.println("Com has problems!!");
			}

		} catch (IOException e) {
			e.printStackTrace();
			try {
				inputStream.close();
				outputStream.close();
				socket.close();
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	public void sendMessageToCodeServer(Object message) {
		synchronized (outputStream) {
			try {
				outputStream.writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void connectMaster() {

		System.out.println("Trying to connect to Master Server: "
				+ masterAddress + " on port " + this.masterPort);
		try {
			Streams InOut = new Comm(masterPort, masterAddress)
					.startConnectionToServer();

			if (InOut != null) {

				in = (TranslatorObjectInputStream) InOut
						.returnObjectInputStream();
				in.setClassLoader(translator.getClassLoader());
				out = InOut.returnObjectOutputStream();
				out.writeObject(ConnectionType.CLIENT);

				out.writeObject(ConnectionType.CLIENT_NEW_PROBLEM);
				out.writeObject(new Integer(myID));
				out.writeObject(new Integer(totalNumberOfTasks));
				out.writeObject(desc);
				// out.writeObject(new Integer(version));
				out.writeObject(priority);

				this.macAddress = getMacAddress();
				this.hostName = getHostName(); 
						   
				out.writeObject(String.format("<info><mac_address>%s</mac_address><host_name>%s</host_name></info>", this.macAddress, this.hostName));
				
				new resultFetcher().start();
				System.out.println("Connected to MasterServer!");
			} else {
				System.out.println("Comm has problems!!");
			}

		} catch (IOException e) {
			e.printStackTrace();
			try {
				in.close();
				out.close();
				socket.close();
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void setTotalNumberOfTasks(int totalNumberOfTasks) {
		this.totalNumberOfTasks = totalNumberOfTasks;
		updateClientInfo();
	}

	public void setDesc(String desc) {
		this.desc = desc;
		updateClientInfo();
	}

	private void updateClientInfo() {
		try {
			synchronized (out) {
				out.writeObject(ConnectionType.UPDATE_CLIENT_INFORMATION);
				out.writeObject(new Integer(totalNumberOfTasks));
				out.writeObject(desc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPackageName(String className) {
		return classNameManager.getPackegeName(className).replace("/", ".");
	}

	public void commit(Task task) {
		try {
			synchronized (out) {
				// System.out.println("Sending new Task...");
				out.writeObject(ConnectionType.CLIENT_NEW_TASK);
				out.reset();
				// System.out.println("nam2:"+task.getClass().getName());
				try {
					Object tt = translator.duplicate(task);
					out.writeObject(tt);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// return getResult();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return null;
	}

	public Result commitAndWait(Task task) { // adds task to master server but
												// they are not added to
												// pendinglist; need
												// startWork();
		try {
			out.writeObject(ConnectionType.CLIENT_NEW_TASK_WAIT);
			out.writeObject(translator.duplicate(task));
			out.reset();
			return getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void cancelAllTasks() {
		try {
			System.out.println("Canceling all Tasks...");
			out.writeObject(ConnectionType.CLIENT_CANCEL_ALL_TASKS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startWork() {
		try {
			out.writeObject(ConnectionType.START_WORK);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			//master
			out.writeObject(ConnectionType.CLIENT_DISCONNECT);
			out.close();
			in.close();
			
			//code
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Result getNextResult() {
		synchronized (results) {
			// while(numberOfUnrespondedTasks > 0 && results.size() == 0){
			while (results.size() == 0) {
				try {
					results.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// System.out.println("Tenho nova task"+results.toString());
			Result result = results.pollFirst();
			RuntimeException exception = result.getException();
			if (exception != null) {
				throw exception;
			}
			return result;
		}
	}

	private Result getResult() throws Exception {
		Result result = null;
		Object aux = in.readObject();
		result = (Result) translator.revert(aux);
		return result;
	}

	private class resultFetcher extends Thread {
		@Override
		public void run() {
			try {
				while (true) {
					Result newResult = getResult();
					synchronized (results) {
						results.addLast(newResult);
						results.notify();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class returnClassesThread extends Thread {

		private String PATH_SEPARATOR = System.getProperty("file.separator");
		private ObjectOutputStream out;
		private ObjectInputStream in;

		public returnClassesThread(ObjectOutputStream out, ObjectInputStream in) {
			super();
			this.out = out;
			this.in = in;
		}

		@Override
		public void run() {
			try {
				while (true) {

					Request newRequest = (Request) in.readObject();

					if (newRequest instanceof ClassRequest) {
						ClassRequest request = (ClassRequest) newRequest;
						new SendClass(request).start();
						// System.out.println("sent class " + className);
					} else if (newRequest instanceof ClassNameRequest) {
						ClassNameRequest newClassName = (ClassNameRequest) newRequest;
						classNameManager.registerPackageName(newClassName);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private class SendClass extends Thread {
			private ClassRequest request;

			public SendClass(ClassRequest request) {
				super();
				this.request = request;
			}

			public void run() {
				String className = request.getName();

//				 System.out.println("Got request for: " + className);
				// Class<?> neededClass = Class.forName(className); //
				// getClass().getClassLoader().loadClass(className);

				File file = new File(className);
				if (!file.exists()) {
					file = new File("bin" + PATH_SEPARATOR
							+ className.replace(".", PATH_SEPARATOR) + ".class");
				}
				try {
					byte[] classData = null;
					if (file.exists()) {
						// System.out.println(file.getAbsolutePath());
						classData = getBytesFromFile(file);
						// System.out.println(classData.length);
					} else {
						classData = translator.getByteCodeForClass(className);
					}
					synchronized (out) {
						out.writeObject(request);
						out.writeObject(classData);
					}
				} catch(IllegalArgumentException e) {
					System.err.println("This might be a problem because one sub-project has a different java version");
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Returns the contents of the file in a byte array.
	// from: http://www.exampledepot.com/egs/java.io/File2ByteArray.html
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
	/*
	 * Se se conseguir determinar o IP usar a funcao getByIp
	 */
	private String getMacAddress() {
		Enumeration<NetworkInterface> networkInterfaces = null;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		byte[] mac = null;
		while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
			
			try {
				mac = networkInterface.getHardwareAddress();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			if (mac != null) { break; }
		}					
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) { sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "")); }
		return sb.toString();
	}
	
	/**
	 * Gets the host name
	 * @return hte host name
	 */
	private String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return "FAIL";
		}
	}
}