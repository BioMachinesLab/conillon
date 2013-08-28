package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JApplet;

import screensaver.ScreenSaverWindow;
import worker.Worker;
import worker.WorkerData;
import worker.WorkerInformation;

import comm.ConnectionType;
import comm.InfrastructureInformation;
import comm_2.Comm;
import comm_2.Streams;

public class Main {

	// ********** VERSION ******************
	private final static String VERSION = "Version "
			+ WorkerData.CONILLON_VERSION + " - Evolve";
	// *************************************
	private static int MAINSERVERPORT = 9999;
	//private static String MAINSERVERADDRESS = "localhost";
	 private static String MAINSERVERADDRESS = "evolve.dcti.iscte.pt";
	// private static String MAINSERVERADDRESS = "10.40.50.96";
	// private static String MAINSERVERADDRESS = "10.10.34.85";
	// private static String MAINSERVERADDRESS = "192.168.1.4";

	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private WorkerInformation localhostinfo;
	private InfrastructureInformation infrastructureInformation;
	private int numberOfProcessors = 0; // at least one
	private int myID;
	private final static int retryConnect = 5000;
	private final static boolean screenSaverMode = false;
	private ScreenSaverWindow sw;
	private Worker worker;

	private boolean isRestricted;

	private PrintStream out;

	public void init() {
		try {
			execute();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println("???");
	}

	public Main() {
		this.out = System.out;
		this.isRestricted = false;
		out.println(MAINSERVERADDRESS);
		worker = new Worker(isRestricted, new GuiClientInfoUpdater());

		out.println(VERSION);

	}

	public Main(String[] args, String server, boolean screesaver,
			boolean isRestricted, PrintStream out,
			GuiClientInfoUpdater guiUpdater) throws IOException,
			ClassNotFoundException, InterruptedException {

		super();

		if (args.length > 0) {
			MAINSERVERADDRESS = args[0];
		}

		if (server != null) {
			MAINSERVERADDRESS = server;
		}

		this.isRestricted = isRestricted;
		this.out = out;
		out.println(MAINSERVERADDRESS);
		worker = new Worker(isRestricted, guiUpdater);

		if (args.length > 1)
			numberOfProcessors = new Integer(args[1]);
		if (screenSaverMode) {
			sw = new ScreenSaverWindow(worker);
			sw.start();
		}
		out.println(VERSION);
		execute();

	}

	public static void main(String[] args) {
		while (!Worker.shutdown()) {
			try {
				new Main(args, null, true, false, System.out,
						new GuiClientInfoUpdater());
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("Can't start worker!");
				System.exit(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("ERROR: Lost conection to server.");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Connection closed! Trying to reconnect...");
			try {
				Thread.sleep(retryConnect);
			} catch (InterruptedException e) {

			}
		}

		System.out.println("Problems detected. Shutting down.");
	}

	private void getLocalHostInfo() throws UnknownHostException {
		Runtime runtime = Runtime.getRuntime();
		// int numberOfProcessors;
		String operatingSystem;
		operatingSystem = System.getProperty("os.name");

		if (numberOfProcessors == 0)
			this.numberOfProcessors = runtime.availableProcessors();
		// if (numberOfProcessors>0)
		// this.numberOfProcessors = numberOfProcessors;

		localhostinfo = new WorkerInformation(numberOfProcessors,
				operatingSystem, InetAddress.getLocalHost());
		out.println(localhostinfo.toString());

	}

	private void execute() throws IOException, ClassNotFoundException,
			InterruptedException {
		getLocalHostInfo();

		doConnections();
		startNegotiations();
	}

	private void startNegotiations() throws IOException,
			ClassNotFoundException, InterruptedException {

		outputStream.writeObject(ConnectionType.JA_WORKER_HELLO);

		outputStream.writeObject(localhostinfo);
//		Object o = inputStream.readObject();
		this.myID = (Integer) inputStream.readObject();
		out.println("MY ID IS:" + this.myID);
		// RECEIVE INFRASTRUTURE INFORMATIONS
		infrastructureInformation = (InfrastructureInformation) inputStream
				.readObject();
		out.println("Received Infra struture information:"
				+ infrastructureInformation.toString());

		worker.startWorker(infrastructureInformation, out, this.myID,
				this.numberOfProcessors);

	}

	private void doConnections() throws IOException {
		Streams InOut = new Comm(MAINSERVERPORT, MAINSERVERADDRESS)
				.startConnectionToServer(out);

		if (InOut != null) {
			this.inputStream = InOut.returnObjectInputStream();
			this.outputStream = InOut.returnObjectOutputStream();
			out.println("Done Connections");
		} else {
			out.println("Comm has problems!!");
		}
	}

	public void end() {
		try {
			inputStream.close();
			outputStream.close();
			worker.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
