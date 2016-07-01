package masterserver;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.logging.Logger;

import client.ClientData;
import client.ClientDescription;
import comm.ClientPriority;
import comm.ConnectionType;
import comm.InfrastructureInformation;
import helpers.BlackList;
import helpers.RoomInformation;
import logging.Logger.Event;
import scheduler.IParallelizator;
import scheduler.YaTaskScheduler;
import tasks.CompletedTask;
import tasks.TaskDescription;
import tasks.TaskStatus;
import worker.ClassLoaderObjectInputStream;
import worker.WorkerData;

public class MasterServer {

	// private LinkedList<LinkedList<TaskDescription>> statefulFaultTolerance =
	// new LinkedList<LinkedList<TaskDescription>>();
	private Hashtable<Long, LinkedList<CompletedTask>> pendingResults = new Hashtable<Long, LinkedList<CompletedTask>>();
	// private Hashtable<Long, LinkedList<TaskDescription>> pendingTasks = new
	// Hashtable<Long, LinkedList<TaskDescription>>();

	// private LinkedList<LinkedList<TaskDescription>> pendingTasks = new
	// LinkedList<LinkedList<TaskDescription>>();

	private Hashtable<Long, WorkerData> workerDataVector = new Hashtable<Long, WorkerData>();
	private Hashtable<Long, ClientData> clientDataVector = new Hashtable<Long, ClientData>();

	private Hashtable<Long, WorkerThread> workerThread = new Hashtable<Long, WorkerThread>();
	private Hashtable<Long, ClientThread> clientThread = new Hashtable<Long, ClientThread>();

	private long workerID = 0;
	// private long clientID=0;

	private TalkToCodeServer talkToCodeServer;
	private final static String className = MasterServer.class.getName();
	private static Logger log = Logger.getLogger(className);

	private long startTime;
	private long totalCPUTime = 0;
	private long numTasksDone = 0;

	private static ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
	private InfrastructureInformation infrastructureInformation;

	private CanceledTaskObserver taskObserver = new CanceledTaskObserver();

	//TODO DEVsgf	conversion to interface usage
	//private TaskScheduler taskScheduler = new TaskScheduler();	//OLD
	private IParallelizator taskScheduler = new YaTaskScheduler();
	
	private BlackList blackList;
	private RoomInformation roomsInformation;
	
	private File workerJar = new File("worker.jar");

	public MasterServer(InfrastructureInformation infrastructureInformation) {
		super();
		this.infrastructureInformation = infrastructureInformation;
		startTime = System.currentTimeMillis();
		System.out.println(tmxb.getCurrentThreadCpuTime());
		System.out.println(tmxb.getCurrentThreadUserTime());
		System.out.println(tmxb.getDaemonThreadCount());
//		System.out.println("Version: Oct - 2014 - Evolve");
		
		blackList  = new BlackList();
		blackList.start();
		
		roomsInformation = new RoomInformation();
		roomsInformation.start();
	}

	public void execute() {
		connectCodeServer();

		new Thread(new Runnable() {
			//TODO DEVsimao check Master Server Thread behavior
			@Override
			public void run() {
				try {
					LinkedList<WorkerThread> toBeRemoved = new LinkedList<WorkerThread>();
					while (true) {
						synchronized (workerDataVector) {
							for (WorkerData workerData : workerDataVector
									.values()) {
								workerData.increaseTime();
								if (workerData.getNumberOfRequestedTasks() > 0
										&& workerData.getTimeSinceLastTask() > 3 * 60) {	//TODO DEVsimao If requested tasks > 0 AND time since last task > 180: BAN
									synchronized (workerThread) {
										if (workerThread.containsKey(workerData
												.getId())) {

											WorkerThread wt = workerThread
													.get(workerData.getId());
											toBeRemoved.add(wt);

										}
									}
								}

							}
						}
						for (WorkerThread wt : toBeRemoved) {
							wt.terminateWorker();
							System.out.println("KICKED " + wt.getId());
						}
						toBeRemoved.clear();
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}).start();

		new ReceiveConnectionsThread().start();
	}

	private void connectCodeServer() {

		talkToCodeServer = new TalkToCodeServer(
				infrastructureInformation.getCodeServerIPaddress(),
				infrastructureInformation.getCodeServerEncryptedPort());
		talkToCodeServer.connectCodeServer();

	}

	long addWorkerData(WorkerData data, WorkerThread wt) {
		
		logging.Logger l = logging.Logger.getInstance();
		//SF logging
		try {
			if (l.getDoLog()) {
				workerID = l.log(Event.START, data);
			}
			else {
				workerID = 0;
			}
			
		} catch (Exception e) {
			workerID = 0;	//SF default (before logging approach)
			logging.Logger.getInstance().setDoLog(false);
		}
		
		synchronized (workerDataVector) {
			workerDataVector.put(this.workerID, data);
		}
		synchronized (workerThread) {
			workerThread.put(this.workerID, wt);
		}
		synchronized (this) {
			//SF Assure that application keeps running
			//In case if logging didn t succeed
			return workerID == 0 ? workerID++ : workerID;
		}
	}

	void addClient(ClientData cd, ClientThread ct) {
		
		logging.Logger l = logging.Logger.getInstance();
		try {
			if(l.getDoLog()) { l.log(Event.START, cd); };
		} catch (Exception e) {
			//SF Assure that application keeps running 
			l.setDoLog(false);
		}
		
		synchronized (clientDataVector) {
			clientDataVector.put(cd.getId(), cd);
		}
		synchronized (clientThread) {
			clientThread.put(cd.getId(), ct);
		}

	}

	void removeClient(ClientData cd) {
		
		logging.Logger l = logging.Logger.getInstance();
		try {
			if(l.getDoLog()) { l.log(Event.STOP, cd); };
		} catch (Exception e) {
			//SF Assure that application keeps running 
			logging.Logger.getInstance().setDoLog(false);
		}
		
		
		synchronized (clientThread) {
			if (clientThread.containsKey(cd.getId()))
				clientThread.remove(cd.getId());
		}
		synchronized (clientDataVector) {
			if (clientDataVector.containsKey(cd.getId()))
				clientDataVector.remove(cd.getId());
		}
		deleteClientTasks(cd.getId());
	}
	
	
	void removeWorker(long id, WorkerThread wt) {
		
		logging.Logger l = logging.Logger.getInstance();
		try {
			if(l.getDoLog()) { l.log(Event.STOP, wt.getWorkerData()); }
		} catch (Exception e) {
			//SF Assure that application keeps running 
			logging.Logger.getInstance().setDoLog(false);
		}				
		
		//SF the id argument was replaced by the worker data property
		long workerId = wt.getWorkerData().getId();
		
		synchronized (workerDataVector) {
			if (workerDataVector.containsKey(workerId))
				workerDataVector.remove(workerId);
		}
		synchronized (workerThread) {
			if (workerThread.containsKey(workerId))
				workerThread.remove(workerId);
		}
		taskObserver.deleteObserver(wt);
		taskObserver.deleteObserver(wt);
		
		taskScheduler.removeWorker(id);
	}

	// void addTaskList(LinkedList<TaskDescription> taskList, long clientID) {
	// synchronized (pendingTasks) {
	// pendingTasks.put(clientID, taskList);
	// }
	// synchronized (this) {
	// notifyAll();
	// }
	// }

	public void resendTask(TaskDescription task) {
		taskScheduler.addToResend(task);
	}

	public void addTaskList(LinkedList<TaskDescription> taskList, int clientID,
			ClientPriority priority) {
		taskScheduler.addTaskList(taskList, clientID, priority);

	}

	void addSingleTask(TaskDescription task) {
		taskScheduler.addTask(task);
	}

	public TaskDescription getTask(WorkerData workerData)
			throws InterruptedException {
		return taskScheduler.getTask(workerData); 
	}

	void deleteClientTasks(long clientID) {

		taskScheduler.removeClientTasks(clientID);

		synchronized (pendingResults) { 
			pendingResults.remove(clientID);

		}
	}

	void broadcastWorkersClearClient(int clientId) {
		synchronized (workerThread) {
			for (Long workerId : workerThread.keySet()) {
				workerThread.get(workerId).clearClient(clientId);
			}
		}
	}

	public long getTotalCPUTime() {
		return totalCPUTime;
	}

	public synchronized void addCPUTime(long newCPUTime) {
		this.totalCPUTime += newCPUTime;
	}

	public long getNumTasksDone() {
		return numTasksDone;
	}

	public synchronized void increaseNumTasksDone() {
		this.numTasksDone++;
	}

	public long getStartTime() {
		return startTime;
	}

	class Checker extends Thread {

		private LinkedList<TaskDescription> taskDescriptions;
		private boolean endChecker;
		private double averageTime;
		private long fastestTask;
		private long slowestTask;
		private int numberOfCompletedTasks;

		public Checker(LinkedList<TaskDescription> taskDescriptions) {
			this.taskDescriptions = taskDescriptions;
			this.endChecker = true;
			this.numberOfCompletedTasks = 0;
			this.averageTime = 0;
			this.slowestTask = 0;
			this.fastestTask = Long.MAX_VALUE;

		}

		@Override
		public void run() {
			while (endChecker) {
				synchronized (taskDescriptions) {

					Iterator<TaskDescription> tl = taskDescriptions.iterator();
					while (tl.hasNext()) {
						TaskDescription td = tl.next();
						if (td.getTaskStatus() == TaskStatus.TERMINATED_OK) {
							numberOfCompletedTasks++;
							averageTime += td.getTimeToComplete();
							if (td.getTimeToComplete() < fastestTask)
								fastestTask = td.getTimeToComplete();
							if (td.getTimeToComplete() > slowestTask)
								slowestTask = td.getTimeToComplete();
							System.out.println("Average time to complete task:"
									+ (averageTime / numberOfCompletedTasks)
									/ 1000 + " s");
							td.changeTaskStatus(TaskStatus.COMPLETE);

						} else if (td.getTaskStatus() == TaskStatus.SCHEDULE) {
							long time = System.currentTimeMillis()
									- td.getStartTime();
							if (time != 0 && time > averageTime) {
								// System.out.println("Superior to Average!! ");
							}
						}
					}

				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void addToPendingResults(long id,
			LinkedList<CompletedTask> localTaskList) {
		synchronized (pendingResults) {
			pendingResults.put(id, localTaskList);
		}

	}

	void addSingleResult(CompletedTask task) {
		increaseNumTasksDone();
		LinkedList<Long> list = taskScheduler.taskDone(task);

		if (list != null) {
			if (task.getResult().getException() == null
					&& task.getTaskDescription().getLastWorkerId() != -1) {
				System.out.println("KILL BAD WORKER "
						+ task.getTaskDescription().getLastWorkerId() + list
						+ " taskID= " + task.getTid());
				kickWorker(task.getTaskDescription().getLastWorkerId());
			}
			taskObserver.canceledSingleTask(task);
			// addCPUTime(task.getTaskDescription().getTimeToComplete());
			synchronized (pendingResults) {
				if (pendingResults.containsKey(task.getTaskDescription()
						.getId())) {
					LinkedList<CompletedTask> localTaskList = pendingResults
							.get(task.getTaskDescription().getId());
					synchronized (localTaskList) {
						localTaskList.add(task);
						localTaskList.notify();
					}

					return;
				} else {
					LinkedList<CompletedTask> localTaskList = new LinkedList<CompletedTask>();
					// localTaskList.add(task);
					pendingResults.put(task.getTaskDescription().getId(),
							localTaskList);
					// new Deliver(localTaskList,
					// task.getTaskDescription().getId())
					// .start();

				}
			}
		} else {
			System.out.println("Task already sent" + task);
		}
	}

	class CanceledTaskObserver extends Observable {
		// private ClientDescription clientDescription;

		public void canceledTaskWarning(ClientDescription clientDescription) {
			// this.clientDescription = clientDescription;

			setChanged();
			notifyObservers(clientDescription);
		}

		public void canceledSingleTask(CompletedTask task) {
			// this.clientDescription = clientDescription;

			setChanged();
			notifyObservers(task);
		}
	}

	private String getIdAndTime(WorkerData workerData) {
		return "(" + workerData.getId() + ") ["
				+ (System.currentTimeMillis() - startTime) + "]";
	}

	class ReceiveConnectionsThread extends Thread {
		public ReceiveConnectionsThread() {

		}

		@Override
		public void run() {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(
						infrastructureInformation
								.getMasterServerEncryptedPort());
				System.out.println("Waiting for connections on socket: "
						+ serverSocket);
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						socket.setSoTimeout(0);
						socket.setKeepAlive(true);

						ObjectOutputStream out = new ObjectOutputStream(
								socket.getOutputStream());
						ClassLoaderObjectInputStream in = new ClassLoaderObjectInputStream(
								socket.getInputStream(), talkToCodeServer);
						ConnectionType type = (ConnectionType) in.readObject();
						if (!(type instanceof ConnectionType)) {
							System.out
									.println("Not what i was expecting... Ignoring...");
							in.close();
							out.close();
							socket.close();
						} else {
							switch (type) {
							case CLIENT:
								ClientThread ct = new ClientThread(socket, in,
										out, MasterServer.this, taskObserver);
								ct.start();
								break;
							case WORKER:
								int workerVersion = (Integer) in.readObject();
								out.writeObject(new Integer(
										WorkerData.CONILLON_VERSION));
								out.writeObject(new Long(workerJarLastModified()));
								if (workerVersion != WorkerData.CONILLON_VERSION) {
									System.out
											.println("Invalid Worker version "
													+ workerVersion
													+ " UPGRADE to version "
													+ WorkerData.CONILLON_VERSION);
								} else {

									WorkerThread wt = new WorkerThread(socket,
											in, out, MasterServer.this);
									taskObserver.addObserver(wt);
									wt.start();
								}
								break;
							case ADMIN:
								AdminThread at = new AdminThread(socket, out,
										in, MasterServer.this,
										workerDataVector, clientDataVector,
										workerThread, clientThread);
								at.start();
								break;
							default:
								System.out.println("ERROR!");
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
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

	public void killWorker(long idWorker) {
		synchronized (workerThread) {
			if (workerThread.containsKey(idWorker)) {
				WorkerThread wt = workerThread.get(idWorker);
				wt.terminateWorker(false);
				workerThread.remove(wt);
			}
		}
	}
	
	public void kickWorker(long idWorker) {
		synchronized (workerThread) {
			if (workerThread.containsKey(idWorker)) {
				WorkerThread wt = workerThread.get(idWorker);
				wt.terminateWorker();
				workerThread.remove(wt);
			}
		}
	}
	
	public void banWorker(long idWorker) {
		synchronized (workerDataVector) {
			if (workerDataVector.containsKey(idWorker)) {
				WorkerData wd = workerDataVector.get(idWorker);
				String workerAddress = wd.getWorkerAddress();
				
				if(blackList.containsAddress(workerAddress))
					blackList.removeFromBlackList(workerAddress);
				else
					blackList.addToBlackList(workerAddress);
				
			}
		}
	}
	
	public void saveNewBlackList(String newBlackList){
		blackList.replaceBlackListContent(newBlackList);
	}

	public void checkIfBanned(String ip){
		blackList.checkIfBanned(ip);
	}
	
	public BlackList getBlackList() {
		return blackList;
	}
	
	public RoomInformation getRoomInformation() {
		return roomsInformation;
	}
	
	public File getWorkerJar() {
		return workerJar;
	}
	
	public long workerJarLastModified(){
		return workerJar.lastModified();
	}
	
	
}
