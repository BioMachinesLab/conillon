package worker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import main.GuiClientInfoUpdater;
import result.Result;
import tasks.CachedTask;
import tasks.Task;
import tasks.TaskId;
import worker.Worker.FeedWorker;
import worker.Worker.GetTasks;
import worker.Worker.WorkerThread.TaskThread;

import comm.ConnectionType;
import comm.FileProvider;
import comm.InfrastructureInformation;
import comm.RunMode;
import comm_2.CodeServerComunicator;
import comm_2.Comm;
import comm_2.Streams;
import comm_2.SystemInformation;

public class Worker {

	String masterAddress;
	private PrintStream out;
	boolean keepRunning = true;
	boolean screenSaverMode = false;
	private float workerEvaluation = 0;;
	private int mainWorkerID;
	private InfrastructureInformation infrastructureInformation;

	private Semaphore numberOfAllowedThreads;
	private Semaphore numberOfAllowedCache;
	private Semaphore oneTaskAtTime;

	private static Logger logToMasterServer = Logger.getLogger(Worker.class
			.getName());
	private static Logger logToClient = Logger
			.getLogger(Worker.class.getName());

	private ClassLoaderObjectInputStream socketIn;
	private ObjectOutputStream socketOut;

	// private ObjectInputStream getCodeInputStream;
	// private ObjectOutputStream getCodeOutputStream;
	private SystemInformation localhostinfo;
	private WorkerData workerData;
	private int numberofCores;
	private int cacheSize;
	private int numberOfWorkerThreads;
	private HashMap<TaskId, TaskThread> currentTask = new HashMap<TaskId, TaskThread>();
	private LinkedList<CachedTask> cachedTaskList = new LinkedList<CachedTask>();

	private static ThreadMXBean threadBean = ManagementFactory
			.getThreadMXBean();
	private final static int retryConnect = 5000;
	protected static final long RESET_PERIOD = 7600000;

	private RunMode runMode = RunMode.minimumWorkerLoad;
	private ExecutorService execSvc;
	private boolean isRestricted;
	private CodeServerComunicator codeServerComunicator;

	private GuiClientInfoUpdater guiUpdater;
	private boolean on = true;
	public static boolean shutdown = false;

	private Restarter restarter;
	private GetTasks taskGetter;
	private FeedWorker taskFeeder;

	public Worker(boolean isRestricted, GuiClientInfoUpdater guiUpdater) {
		this.isRestricted = isRestricted;
		this.guiUpdater = guiUpdater;
	}

	public static boolean shutdown() {
		return shutdown;
	}

	public void startWorker(
			InfrastructureInformation infrastructureInformation,
			PrintStream out, int mainWorkerID, int numberOfCores)
			throws InterruptedException, IOException {

		this.out = out;
		this.mainWorkerID = mainWorkerID;
		this.numberofCores = numberOfCores;
		this.infrastructureInformation = infrastructureInformation;

		this.workerData = new WorkerData();
		out.println(threadBean.getCurrentThreadCpuTime());
		out.println(threadBean.getCurrentThreadUserTime());
		out.println(threadBean.getDaemonThreadCount());

		this.runMode = infrastructureInformation.getRunMode();
		this.cacheSize = (numberOfCores / 2);// this.numberofCores +
												// (numberOfCores / 2);
		out.println("CACHE" + cacheSize + " " + numberOfCores);
		this.numberOfAllowedCache = new Semaphore(cacheSize); // cachesize
		this.numberOfAllowedThreads = new Semaphore(numberOfCores);
		this.oneTaskAtTime = new Semaphore(1);
		this.execSvc = Executors.newFixedThreadPool(numberofCores);

		getLocalHostInfo();
		connectCodeServer();
		connectMasterServer();

		restarter = new Restarter();
		restarter.start();

		run();
		// this.start();
		// this.join();
	}

	public void run() {
		out.println("Starting to serv...");
		try {
			serve();
		} catch (IOException e) {
			out.println("IO Exception - Connection was closed");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			out.println("Class Not Found....");
			 e.printStackTrace(out);
		} catch (Exception e) {
			e.printStackTrace();
			out.println(e);
		}
		// benchmark();

		// logToMasterServer.log(Level.SEVERE, e.toString());
		disconnect();
	}

	public WorkerData getWorkerData() {
		return this.workerData;
	}

	private void connectCodeServer() throws InterruptedException, IOException {
		Streams inOut = null;
		out.println("Trying to connect to CodeServer: "
				+ infrastructureInformation.getCodeServerIPaddress()
				+ " on port "
				+ infrastructureInformation.getCodeServerEncryptedPort());
		while (inOut == null) {

			inOut = new Comm(
					infrastructureInformation.getCodeServerEncryptedPort(),
					infrastructureInformation.getCodeServerIPaddress())
					.startConnectionToServer(out);

			if (inOut != null) {
				// this.getCodeInputStream = InOut.returnObjectInputStream();
				// this.getCodeOutputStream = InOut.returnObjectOutputStream();

				codeServerComunicator = new CodeServerComunicator(
						inOut.returnObjectInputStream(),
						inOut.returnObjectOutputStream());
				codeServerComunicator
						.sendObject(ConnectionType.CLASS_REQUESTER);

				return;
			} else {
				out.println("Comm has problems!!");
			}
			Thread.sleep(retryConnect);
		}

	}

	private void connectMasterServer() throws InterruptedException, IOException {
		Streams inOut = null;
		while (inOut == null) {

			inOut = new Comm(
					infrastructureInformation.getMasterServerEncryptedPort(),
					infrastructureInformation.getMasterServerIPaddress())
					.startConnectionToServerObject(codeServerComunicator, out,
							isRestricted);

			if (inOut != null) {
				this.socketIn = inOut.returnClassLoaderObjectInputStream();
				this.socketOut = inOut.returnObjectOutputStream();

				this.socketOut.writeObject(ConnectionType.WORKER);
				this.socketOut.writeObject(WorkerData.CONILLON_VERSION);

				try {
					int serverVersion = (Integer) this.socketIn.readObject();
					if (serverVersion != WorkerData.CONILLON_VERSION) {
						out.println("Invalid Worker version "
								+ WorkerData.CONILLON_VERSION
								+ " UPGRADE to version " + serverVersion);
						startShutdown();
					}
					System.out.println("connected " + serverVersion);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					System.out.println("killing because of ClassNotFoundException on connectMasterServer");
					startShutdown();
				}

				return;
			} else {
				out.println("Comm has problems!!");
			}

			Thread.sleep(retryConnect);
		}

	}

	private void benchmark() {

		int numberOfProcessors = this.workerData.getNumberOfProcessors();
		calculatorFIB[] c = new calculatorFIB[16];

		for (int i = 0; i < numberOfProcessors; i++) {
			c[i] = new calculatorFIB(160000);
			c[i].start();

		}

	}

	private class calculatorFIB extends Thread {

		int nr;

		public calculatorFIB(int nr) {
			this.nr = nr;

		}

		public void run() {
			BigInteger sub2 = new BigInteger("1");
			BigInteger sub1 = new BigInteger("0");

			BigInteger total = new BigInteger("0");

			int p = 0;
			long start = System.currentTimeMillis();
			while (p < nr) {

				total = sub1;
				sub1 = sub1.add(sub2);
				sub2 = total;
				p++;
			}
			long end = System.currentTimeMillis();
			out.println("Terminou:" + this.getId() + " Tempo: " + (end - start)
					/ 1000 + " - " + sub2);
			setCpuEvaluation((end - start) / 1000);
		}

	}

	private void setCpuEvaluation(float eval) {

		this.workerEvaluation = eval / workerData.getNumberOfProcessors();
		out.println("evaluation" + workerEvaluation);

	}

	private void getLocalHostInfo() {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors;
		String operatingSystem;
		operatingSystem = System.getProperty("os.name");

		numberOfProcessors = numberofCores;// = runtime.availableProcessors();
		try {
			this.workerData.setMainWorkerID(mainWorkerID);
			localhostinfo = new SystemInformation(numberOfProcessors,
					operatingSystem, InetAddress.getLocalHost());
			out.println(localhostinfo.toString());
			this.workerData.setOperatingSystem(operatingSystem);
			this.workerData.setWorkerAddress(InetAddress.getLocalHost()
					.toString());
			this.workerData.setNumberOfProcessors(numberOfProcessors);
			// DateFormat dateFormat = new
			// SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			// Date date = new Date();
			// this.workerData.setStartTime(dateFormat.format(date));

			out.println(localhostinfo.toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(out);
		}

	}

	private synchronized void addWork() {
		// out.println("HERE");

		while (numberOfWorkerThreads >= numberofCores) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(out);
			}

			numberOfWorkerThreads++;
		}
		out.println("Numero de workers:" + numberOfWorkerThreads);
	}

	private synchronized void freeTask() {
		numberOfAllowedThreads.release();

	}

	private void addTaskcacheList(TaskId taskid, Task task) {
		// out.println("add");
		synchronized (this) {
			cachedTaskList.add(new CachedTask(taskid, task));
			// out.println(taskid.getTaskId());
			notify();
		}

	}

	private boolean removeTaskcacheList(long clientID, long taskID) {
		synchronized (this) {
			Iterator<CachedTask> iterator = cachedTaskList.iterator();
			while (iterator.hasNext()) {
				CachedTask cashedTask = iterator.next();
				if (cashedTask.getTaskid().getClientID() == clientID
						&& cashedTask.getTaskid().getTaskId() == taskID) {
					iterator.remove();
//					System.out.println("Removed from cache :" + taskID + ", " + clientID);
					return true;
				}
			}
		}
		return false;

	}
	
	private boolean removeTaskcacheList(long clientID) {
		synchronized (this) {
			Iterator<CachedTask> iterator = cachedTaskList.iterator();
			while (iterator.hasNext()) {
				CachedTask cashedTask = iterator.next();
				if (cashedTask.getTaskid().getClientID() == clientID) {
					long taskID = cashedTask.getTaskid().getTaskId();
					iterator.remove();
//					System.out.println("Removed from cache :" + taskID + ", " + clientID);
					return true;
				}
			}
		}
		return false;

	}

	private CachedTask getTask() throws InterruptedException {
		CachedTask cachedTask = null;

		synchronized (this) {
			while (cachedTaskList.size() == 0)

				wait();

			cachedTask = cachedTaskList.poll();
		}
		numberOfAllowedCache.release();
		return cachedTask;
	}

	private void serve() throws Exception {
		keepRunning = true;
		socketOut.reset();
//		out.println("Sending worker data");
		socketOut.writeObject(workerData);
//		out.println("Waiting for worker data from server");
		workerData = (WorkerData) socketIn.readObject();
//		out.println("Got worker data from server");
		taskGetter = new GetTasks();
		taskGetter.start();
		taskFeeder = new FeedWorker();
		taskFeeder.start();

		ConnectionType type;
		while (on) {
			type = (ConnectionType) socketIn.readObject();
			switch (type) {
			case FEED_WORKER:
				TaskId tid = (TaskId) socketIn.readObject();
				socketIn.setProblemAndVersion((int) tid.getClientID());
				try {
					Task task = (Task) socketIn.readObject();
					addTaskcacheList(tid, task);
				} catch (Exception e) {
					e.printStackTrace(out);
					System.out.println("Had a problem with a class, let's start from scratch");
					disconnect();
				}
				oneTaskAtTime.release();
//				out.println("released");
				break;
			case CANCEL_TASK:
				long clientID = (Long) socketIn.readObject();
				long taskID = (Long) socketIn.readObject();
//				out.println("Tenta matar:" + clientID +" "+ taskID);
				if (!removeTaskcacheList(clientID, taskID)) {
					synchronized (currentTask) {

						Set<Entry<TaskId, TaskThread>> set = currentTask
								.entrySet();
//						System.out.println("I have "+set.size()+" tasks in my queue");
						Iterator<Entry<TaskId, TaskThread>> i = set.iterator();
						while (i.hasNext()) {
							Map.Entry me = i.next();
							TaskId tidlocal = (TaskId) me.getKey();
//							out.println(tidlocal.getTaskId() +" == "+taskID+" && "+tidlocal.getClientID() +" == "+ clientID);
							if (tidlocal.getTaskId() == taskID
									&& tidlocal.getClientID() == clientID) {
								Thread th = (Thread) me.getValue();
								// out.println(th.getState());

								th.stop();
								
								i.remove();
								// try {
								// th.join();
								// } catch (InterruptedException e) {
								// e.printStackTrace();
								// }
								// //i.remove();
								// out.println(th.getState());
								//
								// // freeTask();
								// out.println("Matou Thread" + th.getId());

								
//								synchronized (socketOut) {
//									socketOut
//											.writeObject(ConnectionType.WORKER_CANCELED_TASK_OK);
//									socketOut.writeObject(clientID);
//									socketOut.writeObject(taskID);
//									out.println("KILLED:" + clientID + ","
//											+ taskID);
//								}
								break;

							}
						}
//						System.out.println("Now, I have "+set.size()+" tasks in my queue");

					}
				} else {
					numberOfAllowedCache.release();
				}
//				out.println("matou: " + clientID +" "+ taskID );
				break;
			case WORKER_CANCEL_CLIENT_TASK:
				break;
			case PING:
				synchronized (socketOut) {
					// out.println("CachedTasklistsize: " +
					// cachedTaskList.size());
					// out.println("CurrentTasksize: " + currentTask.size());
					socketOut.writeObject(ConnectionType.PONG);

				}
				break;
			case CLEAR_CLIENT:

				int clientId = (Integer) socketIn.readObject();
				
//				out.println("Tenta matar tudo do client:" + clientId);
				if (!removeTaskcacheList(clientId)) {
					synchronized (currentTask) {

						Set<Entry<TaskId, TaskThread>> set = currentTask
								.entrySet();
//						System.out.println("I have "+set.size()+" tasks in my queue");
						Iterator<Entry<TaskId, TaskThread>> i = set.iterator();
						while (i.hasNext()) {
							Map.Entry me = i.next();
							TaskId tidlocal = (TaskId) me.getKey();
//							out.println(tidlocal.getClientID() +" == "+ clientId);
							if (tidlocal.getClientID() == clientId) {
								Thread th = (Thread) me.getValue();
								// out.println(th.getState());

								th.stop();
								
								i.remove();
								// try {
								// th.join();
								// } catch (InterruptedException e) {
								// e.printStackTrace();
								// }
								// //i.remove();
								// out.println(th.getState());
								//
								// // freeTask();
								// out.println("Matou Thread" + th.getId());

								
//								synchronized (socketOut) {
//									socketOut
//											.writeObject(ConnectionType.WORKER_CANCELED_TASK_OK);
//									socketOut.writeObject(clientID);
//									socketOut.writeObject(taskID);
//									out.println("KILLED:" + clientID + ","
//											+ taskID);
//								}

							}
						}
//						System.out.println("Now, I have "+set.size()+" tasks in my queue");

					}
				} else {
					numberOfAllowedCache.release();
				}
				
				
//				codeServerComunicator.clearClientData(clientId);

				break;
			case KILL_WORKER:
//				out.println("I'm being killed!");
				System.out.println("type: "+type);
				startShutdown();
				break;
			case KICK_WORKER:
//				out.println("I'm being kicked!");
				disconnect();
				break;
			}

		}
		out.println("Done serving!");
	}

	class GetTasks extends Thread {

		public void run() {

			try {
				while (on) {
					numberOfAllowedCache.acquire();
					oneTaskAtTime.acquire();
					// out.println("ASk for task"+cacheCounter++);
					synchronized (socketOut) {
						socketOut.writeObject(ConnectionType.WORKER_FEED);
					}
				}
			} catch (Exception e) {
				e.printStackTrace(out);
			}
		}
	}

	class FeedWorker extends Thread {

		public void run() {
			try {
				while (on) {
					numberOfAllowedThreads.acquire();
					CachedTask cachedTask = getTask();
					// execSvc.execute( new
					// WorkerThread(cachedTask.getTask(),cachedTask.getTaskid()));
					
//					System.out.println("starting new task: " + cachedTask.getTaskid());

					
					execSvc.submit(new WorkerThread(cachedTask.getTask(),
							cachedTask.getTaskid()));
					// wt.start();
				}
			} catch (InterruptedException e) {
				e.printStackTrace(out);
			}
		}

	}

	class IdleController extends Thread {
		private Task task;
		private TaskId tid;
		int nr = 0;

		public IdleController() {

		}

		public void run() {
			while (on) {
				// detect iddle
				// idle out -> terminate tasks
				//
				// out.println("I:" + nr++);
				try {

					// out.println("aki");
					synchronized (socketOut) {

						socketOut.writeObject(ConnectionType.WORKER_FEED);

					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(out);
				}

			}
		}
	}

	class WorkerThread implements Runnable {

		private Task task;
		private TaskId tid;
		private MyUncaughtExceptionHandler handler = new MyUncaughtExceptionHandler();

		public WorkerThread(Task task, TaskId tid) {
			this.task = task;
			this.tid = tid;
		}

		public void run() {
//			System.out.println("Running Thread!");
			boolean flag = false;
			long elapsedTime = 0L;
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			task.setFileProvider(new RemoteFileProvider((int) tid.getClientID()));
			TaskThread thread = new TaskThread(task, tid);
//			System.out.println("Let's go");
			synchronized (currentTask) {
				currentTask.put(tid, thread);
			}
			synchronized (workerData) {
				workerData.setRunning(true);
				workerData.setWorkerStatus("Starting...");

			}
			try {

				while (!Thread.currentThread().isInterrupted() && !flag) {

					long id = Thread.currentThread().getId();
					long startTime = System.currentTimeMillis();
					Result result;
					thread.start();
					thread.setUncaughtExceptionHandler(handler);

					try {
//						System.out.println("Waiting for task to finish...");
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
						// TODO Auto-generated catch block

						// e.printStackTrace(out);
					}
					// if (thread.getException() != null) {
					// result = new Result();
					// result.setException(thread.getException());
					// } else {
					// if(!thread.isDone()){
					// System.exit(1);
					// }
					// result = task.getResult();
					// }

					// if(result == null){
					// result = new Result() {};
					// }

					if (thread.isDone() || thread.getException() != null) {

						result = thread.getResult();

						// result.setException(thread.getException());

						flag = true;
						elapsedTime = System.currentTimeMillis() - startTime;
						// out.println("Elapsed time - task related:"+elapsedTime);

						Date date = new Date();
						String endTime = dateFormat.format(date);
//						System.out.println("Finalizing....");
						synchronized (workerData) {
							workerData.setNumberOfTasksProcessed();
							workerData.setTotalTimeSpentFarming(elapsedTime);
							workerData.setRunning(false);
							workerData.setLastTaskTime(elapsedTime);
							workerData.setEndTime(endTime);
							workerData.setWorkerStatus("Stopped...");
							if (result != null) {
								synchronized (socketOut) {
									socketOut
											.writeObject(ConnectionType.WORKER_RESULTS);
									socketOut.writeObject(tid);
									socketOut.writeObject(result);
									socketOut.writeObject(workerData);
									// out.println("\n - enviar" +
									// task.toString()
									// + " C" + tid.getClientID());
									socketOut.reset();
									guiUpdater.updateClientInfo(workerData);
								}
							}
						}
//						System.out.println("Everything OK!");
					}else {
//						System.out.println("KILLED not ended...");
					}
					synchronized (currentTask) {
//						System.out.println("Removing task");
						currentTask.remove(tid);
						freeTask();
//						System.out.println("Task removed");
					}

				}
			} catch (IOException e) {
				e.printStackTrace(out);
			}
			long end = System.currentTimeMillis();
			// out.println("___________DONE__________ " + tid);
		}

		class TaskThread extends Thread {
			private Task task;
			private TaskId tid;
			private Result result;
			private RuntimeException exception;
			private boolean done = false;

			public TaskThread(Task task, TaskId tid) {
				this.task = task;
				this.tid = tid;
			}

			public Result getResult() {
				return result;
			}

			public boolean isDone() {
				return done;
			}

			public RuntimeException getException() {
				return exception;
			}

			public void run() {
				try {
					try {
						long startCpuTime = threadBean.getCurrentThreadCpuTime();
						task.run();
						long cpuTime = (threadBean.getCurrentThreadCpuTime() - startCpuTime) / 1000000L;
						out.println("Done task number (" + tid.getClientID() + ", "
								+ tid.getTaskId() + ") CPU Time:" + cpuTime
								+ "(ms)");
						// throw new IllegalArgumentException(
						// "ERRO- em modo debug - tirar esta mensagem");
	
					} catch (RuntimeException e) {
						e.printStackTrace(out);
						exception = e;
						// this.interrupt();
					}
//					catch(Throwable e){
//						e.printStackTrace();
//					}
					if (getException() != null) {
						result = new Result();
						result.setException(getException());
					} else {
						result = task.getResult();
					}
					done = true;
				} catch(Exception e) {
					e.printStackTrace();
				}
//				catch(Throwable e) {
//					e.printStackTrace();
//				}
				
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		}
	}

	class RemoteFileProvider extends FileProvider {
		private int id;

		public RemoteFileProvider(int id) {
			super();
			this.id = id;
		}

		@Override
		public ByteArrayInputStream getFile(String name) throws IOException {
			byte[] file = null;
			try {
				file = codeServerComunicator.requestClass(id, name);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
//			} catch (AbortWorkerException e ) {
//				e.printStackTrace();
//				System.out.println("Aborting worker! Kicking myself off");
//				disconnect();
//			}
			}
			if (file == null)
				return null;
			else
				return new ByteArrayInputStream(file);
		}

		@Override
		public Class<?> getClassByName(String className)
				throws ClassNotFoundException {
			className = "__" + id + "." + className;
			return Class.forName(className,true,socketIn.getClassLoader());
		}

	}

	private void startShutdown() {
		shutdown = true;
//		System.out.println("I'm REALLY being killed!");
		disconnect();
	}

	public void disconnect() {
		System.out.println("DISCONNECTING");
		on = false;

		try {
			socketIn.close();
			socketOut.close();
			socketIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		codeServerComunicator.disconect();
		restarter.interrupt();
		taskGetter.interrupt();
		taskFeeder.interrupt();

		execSvc.shutdownNow();

		synchronized (currentTask) {
			for (Thread thread : currentTask.values()) {
				thread.stop();
			}
		}

		System.out.println("ENDED...");
		System.gc();
	}

	private class Restarter extends Thread {

		@Override
		public void run() {
			if (!isRestricted) {
				try {
					Thread.sleep(RESET_PERIOD);
					System.out
							.println("\n\n\n\n ____________________ \n SHUTTING DOWN \n__________________\n\n\n\n");
					Worker.this.disconnect();
				} catch (InterruptedException e) {
				}

			}
		}
	}
	
}
class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
//		System.out.println("###########################This was uncaught");
//		if(e != null)
//			e.printStackTrace();
	}
	
}
