package masterserver;

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

import scheduler.TaskScheduler;
import tasks.CompletedTask;
import tasks.Task;
import tasks.TaskDescription;
import tasks.TaskStatus;
import worker.ClassLoaderObjectInputStream;
import worker.WorkerData;
import client.ClientData;
import client.ClientDescription;

import comm.ClientPriority;
import comm.ConnectionType;
import comm.InfrastructureInformation;

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

	private TaskScheduler taskScheduler = new TaskScheduler();

	public MasterServer(InfrastructureInformation infrastructureInformation) {
		super();
		this.infrastructureInformation = infrastructureInformation;
		startTime = System.currentTimeMillis();
		System.out.println(tmxb.getCurrentThreadCpuTime());
		System.out.println(tmxb.getCurrentThreadUserTime());
		System.out.println(tmxb.getDaemonThreadCount());
		System.out.println("Version: Aug 30 - 2013 - Evolve");

	}

	public void execute() {
		connectCodeServer();

		new Thread(new Runnable() {

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
										&& workerData.getTimeSinceLastTask() > 3 * 60) {
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
							System.out.println("TERMINATED " + wt.getId());
						}
						toBeRemoved.clear();
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {

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
		synchronized (workerDataVector) {
			workerDataVector.put(this.workerID, data);
		}
		synchronized (workerThread) {
			workerThread.put(this.workerID, wt);
		}
		synchronized (this) {
			return workerID++;
		}
	}

	void addClient(ClientData cd, ClientThread ct) {

		synchronized (clientDataVector) {
			clientDataVector.put(cd.getId(), cd);
		}
		synchronized (clientThread) {
			clientThread.put(cd.getId(), ct);
		}

	}

	void removeClient(long id) {
		synchronized (clientThread) {
			if (clientThread.containsKey(id))
				clientThread.remove(id);
		}
		synchronized (clientDataVector) {
			if (clientDataVector.containsKey(id))
				clientDataVector.remove(id);
		}
		deleteClientTasks(id);
	}

	void removeWorker(long id, WorkerThread wt) {

		synchronized (workerDataVector) {
			if (workerDataVector.containsKey(id))
				workerDataVector.remove(id);
		}
		synchronized (workerThread) {
			if (workerThread.containsKey(id))
				workerThread.remove(id);
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
		// boolean flag = false;
		// // addSendedTask_FaultTolerance(task);
		// synchronized (pendingTasks) {
		// long clientID = task.getId();
		// if (pendingTasks.containsKey(clientID)) {
		// LinkedList<TaskDescription> list = pendingTasks.get(clientID);
		// synchronized (list) {
		// list.add(task);
		// }
		// flag = true;
		// return;
		// }
		// }
		// if (!flag) {
		// LinkedList<TaskDescription> localTaskList = new
		// LinkedList<TaskDescription>();
		// localTaskList.add(task);
		// addTaskList(localTaskList, task.getId());
		// // notifyAll();
		// }
	}

	// synchronized TaskDescription getTask(WorkerData workerData) {
	// TaskDescription taskToReturn = null;
	// // long lastClientID = -1;
	// int position = -1;
	//
	// while (taskToReturn == null) {
	//
	// try {
	// while (pendingTasks.size() == 0) {
	// wait();
	// }
	// } catch (InterruptedException e) {
	// // e.printStackTrace();
	// System.out.println(getIdAndTime(workerData)
	// + " ->Was interrupted while waiting!");
	// return null;
	// }
	//
	// if (taskScheduler != null) {
	// if (taskScheduler.dispatchSame()) {
	// taskToReturn = taskScheduler.getTask(workerData.getId());
	// // lastClientID = taskScheduler.getClientID();
	// // position = taskScheduler.getPosition();
	// return taskToReturn;
	// }
	// position = taskScheduler.getPosition();
	//
	// }
	// synchronized (pendingTasks) {
	// Long vector[] = new Long[pendingTasks.size()];
	// int i = 0;
	// Enumeration<Long> keys = pendingTasks.keys();
	// while (keys.hasMoreElements()) {
	// long key = (Long) keys.nextElement();
	// LinkedList<TaskDescription> list = pendingTasks.get(key);
	// synchronized (list) {
	// if (list.size() == 0) {
	// pendingTasks.remove(key);
	// } else {
	// vector[i++] = key;
	// }
	// }
	// }
	// if (vector.length > 0) {
	// if (position == -1) {
	// taskScheduler = new TaskScheduler(
	// pendingTasks.get(vector[0]), 0);
	//
	// } else {
	// int new_position = position;
	// new_position++;
	//
	// if (new_position < vector.length) // Verificar esta
	// // altera������o
	// {
	// if (vector[new_position] != null)
	// if (pendingTasks
	// .containsKey(vector[new_position])) {
	// taskScheduler = new TaskScheduler(
	// pendingTasks
	// .get(vector[new_position]),
	// new_position);
	// return taskScheduler.getTask();
	// }
	// } else {
	// if (new_position >= vector.length
	// && pendingTasks.size() > 0
	// && vector.length > 0) // verificar
	// // altera������o
	// {
	// taskScheduler = new TaskScheduler(
	// pendingTasks.get(vector[0]), 0);
	// return taskScheduler.getTask();
	// }
	// }
	//
	// }
	// }
	// }
	// }
	//
	// return null;
	// }

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

	/*
	 * private void addSendedTask_FaultTolerance(TaskDescription task) { boolean
	 * flag = false;
	 * 
	 * 
	 * synchronized (statefulFaultTolerance) {
	 * Iterator<LinkedList<TaskDescription>> taskIterator =
	 * statefulFaultTolerance.iterator(); while(taskIterator.hasNext()){
	 * LinkedList<TaskDescription> list =
	 * (LinkedList<TaskDescription>)taskIterator.next();
	 * Iterator<TaskDescription> tl = list.iterator(); while(tl.hasNext()){
	 * TaskDescription td =(TaskDescription) tl.next();
	 * if(td.getClientID()==task.getClientID()) { synchronized (list) {
	 * list.add(task); } flag = true; break; } } } if(!flag){
	 * LinkedList<TaskDescription> localTaskList = new
	 * LinkedList<TaskDescription>(); localTaskList.add(task);
	 * statefulFaultTolerance.add(localTaskList); //Checker ch = new
	 * Checker(localTaskList); //ch.start(); } }
	 * //System.out.println("FAULT TOLERANCE: Number of tasks:"
	 * +pendingTasks.size()); }
	 */

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
				killWorker(task.getTaskDescription().getLastWorkerId());
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
				wt.terminateWorker();
				workerThread.remove(wt);
			}
		}
	}
	public void kickWorker(long idWorker) {
		synchronized (workerThread) {
			if (workerThread.containsKey(idWorker)) {
				WorkerThread wt = workerThread.get(idWorker);
				wt.terminateWorker(true);
				workerThread.remove(wt);
			}
		}
	}
}
