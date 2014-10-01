package masterserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import result.Result;
import tasks.CompletedTask;
import tasks.TaskDescription;
import tasks.TaskId;
import worker.ClassLoaderObjectInputStream;
import worker.WorkerData;
import client.ClientDescription;

import comm.ConnectionType;
import comm.SystemInformation;

public class WorkerThread extends Thread implements Observer {
	private Socket socket;
	private ObjectOutputStream out;
	private ClassLoaderObjectInputStream in;
	private WorkerData workerData;
	private SystemInformation si;
	private long workerID;
	private LinkedList<TaskDescription> taskList = new LinkedList<TaskDescription>();

	private PingPong pingpong = new PingPong();
	private boolean socketInFlag = false;
	public boolean sendingData = false;

	private FeedWorker feedWorker = new FeedWorker();

	private MasterServer master;

	public WorkerThread(Socket socket, ClassLoaderObjectInputStream in,
			ObjectOutputStream out, MasterServer master) {
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.master = master;
		workerData = new WorkerData();
	}

	public void clearClient(int clientId) {
		synchronized (out) {
			try {
				out.writeObject(ConnectionType.CLEAR_CLIENT);
				out.writeObject(new Integer(clientId));
				out.reset();
			} catch (IOException e) {
				e.printStackTrace();
				terminateWorker();
			}
		}
	}

	@Override
	public void run() {

		try {
			workerData = (WorkerData) in.readObject();
			workerData.setWorkerAddress(socket.getInetAddress().toString());
			
			while(master.getBlackList().contains(workerData.getWorkerAddress())) {
				Thread.sleep(10*60*1000);
			}
			
			workerData.setStartTime(System.currentTimeMillis());
			// System.out.println("SERVER ADDRESS: " +
			// socket.getInetAddress().getHostAddress());

			workerData.setWorkerPort(socket.getPort());

			this.workerID = master.addWorkerData(this.workerData, this);
			workerData.setId(workerID);
			System.out.println("Worker id :" + workerID);
			out.writeObject(workerData);
			// System.out.println("Sent worker data back");
			out.reset();

			pingpong.start();
			feedWorker.start();
			ConnectionType type;
			while (socket.isConnected()) {
				type = (ConnectionType) in.readObject();
				
				switch (type) {
				case WORKER_FEED:
					feedWorker.feedAnotherTask();
					break;
				case WORKER_RESULTS:
					socketInFlag = true;
					TaskId tid = (TaskId) in.readObject();
					in.setProblemAndVersion((int) tid.getClientID());
					Result result = (Result) in.readObject();
					WorkerData tempWorkerData = (WorkerData) in.readObject();

					// System.out.println("got: " + result);

					workerData.update(tempWorkerData);
					
					workerData.decreaseNumberOfRequestedTasks();
					
					synchronized (out) {
						out.reset();
					}
					TaskDescription task = null;
					
					if(result.getException() == null) {
						synchronized (taskList) {
							Iterator<TaskDescription> taskIterator = taskList
									.iterator();
							while (taskIterator.hasNext()) {
								task = taskIterator.next();
								if (task.getId() == tid.getClientID()
										&& task.getTaskId() == tid.getTaskId()) {
									taskIterator.remove();
									break;
								}
								task = null;
							}
						}
					} else {
						System.out.println("An exception was returned by Worker "+workerID + " " + result.getException().getMessage());
					}
					
					if (task != null) {
						
						if (result.getException() != null
								&& task.getTrials() == 3) {
							task.incTrials();
							task.setLastWorkerId(workerID);
							master.resendTask(task);
						} else {
							master.addSingleResult(new CompletedTask(result,
									task, tid));
						}
						result.setWorkerData(workerData);
						// TODO update this line
						// changeStatusToTask_FaultTolerance(task,TaskStatus.TERMINATED_OK,workerData.getLastTaskTime());
						// tid = null;
						// result = null;

					}
					socketInFlag = false;
					break;

				case WORKER_CANCELED_TASK_OK:
					long clientID = (Long) in.readObject();
					long taskID = (Long) in.readObject();
					
					workerData.decreaseNumberOfRequestedTasks();
					
					synchronized (taskList) {
						Iterator<TaskDescription> taskIteratorDel = taskList
								.iterator();
						task = null;
						while (taskIteratorDel.hasNext()) {
							task = taskIteratorDel.next();
							if (task.getId() == clientID
									&& task.getTaskId() == taskID) {
								taskIteratorDel.remove();
								break;
							}
							// changeStatusToTask_FaultTolerance(task,TaskStatus.FAILED,0);

						}
//						if (task != null) {
						System.out.println("Worker "+workerID+" canceled a task");
						
//						}

					}
					break;

				case PONG:
					pingpong.acknolegde();
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			terminateWorker();
		}

		System.out.println("Worker Done!!" + workerID);
	}

	private class PingPong extends Thread {
		private int flag = 0;

		private final static int TIME_OUT = 5000;
		private static final int TIME_TO_SEND_NEXT_PING = 7000;

		public synchronized void waiter() throws InterruptedException {
			while (flag == 0 && !socketInFlag) {
				wait(TIME_OUT);
				if (flag == 0 && !socketInFlag) {
					flag = 2;
					return;
				} else if (socketInFlag || sendingData) {
					wait();
					flag = 1;
					return;
				}
			}
			flag = 1;
		}

		public synchronized void acknolegde() {
			flag = 1;
			notify();
		}

		@Override
		public void run() {
			try {

				while (socket.isConnected()) {
					
					try{
					
						Thread.sleep(TIME_TO_SEND_NEXT_PING);
					
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
					
					synchronized (out) {
						out.writeObject(ConnectionType.PING);
					}
					waiter();
					if (flag == 1 || socketInFlag) {
						flag = 0;
					} else {
						if (flag == 2) {
							System.out.println("Timeout");
							terminateWorker();
						}
					}
				}
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//				System.out.println("PingPong Over!");
			} catch (Exception e) {//IO
				System.out.println("PingPong Over!");
				e.printStackTrace();
				terminateWorker();
				return;
			}

		}
	}
	
	public void terminateWorker() {
		terminateWorker(true);
	}

	public void terminateWorker(boolean kick) {
		try {
			synchronized (out) {
				out.writeObject(kick ? ConnectionType.KICK_WORKER : ConnectionType.KILL_WORKER);
			}
//			synchronized (taskList) {
//				Iterator<TaskDescription> taskIterator = taskList.iterator();
//				while (taskIterator.hasNext()) {
//					TaskDescription task = taskIterator.next();
//					master.addSingleTask(task);
//					// changeStatusToTask_FaultTolerance(task,TaskStatus.RESCHEDULED,0);
//				}
//				master.removeWorker(this.workerID, this);
//			}
			
			taskList.clear();
			in.close();
			out.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
			try {
//watch out				master.removeWorker(this.workerID, this);
				in.close();
				out.close();
				socket.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		pingpong.interrupt();
		feedWorker.interrupt();
		master.removeWorker(this.workerID, this);//not here before
	}

	private class FeedWorker extends Thread {

		private int numberOfTasksToFeed = 0;

		public synchronized void feedAnotherTask() {
			numberOfTasksToFeed++;
			notifyAll();
		}

		@Override
		public void run() {
			try {
				while (true) {
					TaskDescription taskDescription;
					
					synchronized (this) {
						while (numberOfTasksToFeed <= 0) {
							wait();
						}
						numberOfTasksToFeed--;
					}
		
					taskDescription = master.getTask(workerData);
					if (taskDescription == null) {
						terminateWorker();
						return;
					}
					
					
					synchronized (taskList) {
						taskList.add(taskDescription);
					}
					workerData.increaseNumberOfRequestedTasks();
				
					synchronized (out) {
						sendingData = true;
						out.writeObject(ConnectionType.FEED_WORKER);
						out.writeObject(new TaskId(taskDescription.getId(),
								taskDescription.getTaskId()));
						out.writeObject(taskDescription.getTask());
						out.reset();
						sendingData = false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				terminateWorker();
				System.out.println("Feeder done!");
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {

		if (arg instanceof ClientDescription) {
			ClientDescription clientDescription = (ClientDescription) arg;
			synchronized (taskList) {
				Iterator<TaskDescription> taskIterator = taskList.iterator();

				while (taskIterator.hasNext()) {
					TaskDescription task = taskIterator.next();
					if (task.getId() == clientDescription.getID()) {
						//TODO
						/*
						synchronized (out) {
							try {
								out.writeObject(ConnectionType.CANCEL_TASK);
								out.writeObject(task.getId());
								out.writeObject(task.getTaskId());
							} catch (IOException e) {
								e.printStackTrace();
								terminateWorker();
							}
						}
						*/
						taskIterator.remove();
//						workerData.decreaseNumberOfRequestedTasks();
					}

				}
			}
		} else if (arg instanceof CompletedTask) {
			CompletedTask completedTask = (CompletedTask) arg;
			synchronized (taskList) {
				Iterator<TaskDescription> taskIterator = taskList.iterator();

				while (taskIterator.hasNext()) {
					TaskDescription task = taskIterator.next();
					if (task.getId() == completedTask.getTaskDescription()
							.getId()
							&& task.getTaskId() == completedTask
									.getTaskDescription().getTaskId()) {
						//TODO
						/*
						synchronized (out) {
							try {
								out.writeObject(ConnectionType.CANCEL_TASK);
								out.writeObject(task.getId());
								out.writeObject(task.getTaskId());
							} catch (IOException e) {
								e.printStackTrace();
								terminateWorker();
							}
						}
						*/
						taskIterator.remove();
//						workerData.decreaseNumberOfRequestedTasks();
					}

				}
			}

		}
	}
}
