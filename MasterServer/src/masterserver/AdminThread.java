package masterserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.JSONArray;

import worker.WorkerData;
import client.ClientData;
import comm.ConnectionType;

public class AdminThread extends Thread {
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Hashtable<Long, ClientData> clientDataVector;
	private Hashtable<Long, WorkerData> workerDataVector;
	private Hashtable<Long, WorkerThread> workerThread;
	private Hashtable<Long, ClientThread> clientThread;
	private MasterServer master;

	public AdminThread(Socket socket, ObjectOutputStream out,
			ObjectInputStream in, MasterServer master,
			Hashtable<Long, WorkerData> workerDataVector,
			Hashtable<Long, ClientData> clientDataVector,
			Hashtable<Long, WorkerThread> workerThread,
			Hashtable<Long, ClientThread> clientThread) {
		super();
		this.socket = socket;
		this.out = out;
		this.in = in;
		this.master = master;
		this.workerDataVector = workerDataVector;
		this.clientDataVector = clientDataVector;
		this.workerThread = workerThread;
		this.clientThread = clientThread;
	}

	@Override
	public void run() {
		while (true) {
			ConnectionType type;
			try {
				type = (ConnectionType) in.readObject();
				long idWorker;
				
				switch (type) {
				case FULL_UPDATE:
					fullUpdate();

					break;
				case KILL_WORKER:
					idWorker = (Long) in.readObject();
					master.killWorker(idWorker);
					break;
				case KICK_WORKER:
					idWorker = (Long) in.readObject();
					master.kickWorker(idWorker);
					break;
				case KILL_CLIENT:
					long idClient = (Long) in.readObject();
					synchronized (clientThread) {
						if (clientThread.containsKey(idClient)) {
							ClientThread ct = clientThread.get(idClient);
							ct.disconnect();
						}
					}
					break;
				case BAN_WORKER:
					idWorker = (Long) in.readObject();
					master.banWorker(idWorker);
					break;
				case REFRESH_BANNED_LIST:			
					String newBlacklist = (String) in.readObject();
					master.saveNewBlackList(newBlacklist);
					break;
				default:
					break;
				}

			} catch (IOException e) {
				try {
					in.close();
					out.close();
					socket.close();
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}
			} catch (ClassNotFoundException e) {
				try {
					in.close();
					out.close();
					socket.close();
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return;
			}
		}
	}

	private void fullUpdate() throws IOException {
		JSONArray jsonArray;
		out.writeObject(ConnectionType.FULL_UPDATE);
		out.writeObject(new Long(System.currentTimeMillis()));
		
		try {
			File worker = new File("worker.jar");
			SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
			Date adminDate = sdf.parse(sdf.format(worker.lastModified()));
			out.writeObject(adminDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		out.writeObject(master.getBlackList().getList());
		
		out.writeObject(master.getRoomInformation().getRooms());
		
		synchronized (workerDataVector) {
			out.writeObject(workerDataVector);
			out.reset();
		}
		synchronized (clientDataVector) {
			out.writeObject(clientDataVector);
			out.reset();
		}
 
	}
}