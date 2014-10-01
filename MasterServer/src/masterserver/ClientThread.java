package masterserver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.ListIterator;

import masterserver.MasterServer.CanceledTaskObserver;
import tasks.CompletedTask;
import tasks.Task;
import tasks.TaskDescription;
import worker.ClassLoaderObjectInputStream;
import client.ClientData;
import client.ClientDescription;

import comm.ClientPriority;
import comm.ConnectionType;

class ClientThread extends Thread {
	private Socket socket;
	private ObjectOutputStream out;
	private ClassLoaderObjectInputStream in;
	private ClientPriority priority;
	private LinkedList<TaskDescription> localTaskList = new LinkedList<TaskDescription>();
	private int myID;

	private ClientDescription description;
	private long taskCounter = 0;
	ClientData cd;
	private MasterServer master;
	private CanceledTaskObserver taskObserver;
	private Deliver deliver;
	private int totalNumberOfTasks = 0;
	private String desc;

	public ClientThread(Socket socket, ClassLoaderObjectInputStream in,
			ObjectOutputStream out, MasterServer masterServer,
			CanceledTaskObserver taskObserver) {
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.master = masterServer;
		this.taskObserver = taskObserver;
		System.out.println("New Client " + socket);

	}

	@Override
	public void run() {
		try {
			this.cd = new ClientData(socket.getInetAddress().toString(),
					socket.getPort(), totalNumberOfTasks, desc);

			while (true) {
				ConnectionType type = (ConnectionType) in.readObject();

				switch (type) {
				case CLIENT_NEW_PROBLEM:
					this.myID = (Integer) in.readObject();
					cd.setId(myID);
					master.addClient(cd, this);
					this.totalNumberOfTasks = (Integer) in.readObject();
					cd.setTotalNumberOfTasks(totalNumberOfTasks);
					this.desc = (String) in.readObject();
					cd.setDesc(desc);
					in.setProblemAndVersion(myID);
					this.priority = (ClientPriority) in.readObject();
					description = new ClientDescription(myID, priority, out);
					cd.setClientPriority(priority);

					LinkedList<CompletedTask> taskList = new LinkedList<CompletedTask>();
					master.addToPendingResults(myID, taskList);
					deliver = new Deliver(taskList, cd, myID);
					deliver.start();

					break;
				case CLIENT_NEW_TASK:
					// Object o = in.readObject();
					Task task = (Task) in.readObject();
					cd.addTaskCounter();
					master.addSingleTask(new TaskDescription(task, description,
							cd.getTaskCounter()));
					break;
				case CLIENT_CANCEL_ALL_TASKS:
					// System.out.println(description.toString());
					taskObserver.canceledTaskWarning(description);
					break;
				case START_WORK:
					master.addTaskList(localTaskList, myID, priority);
					break;
				case CLIENT_NEW_TASK_WAIT:
					Task task2 = (Task) in.readObject();
					localTaskList.add(new TaskDescription(task2, description,
							taskCounter++));
					break;
				case CLIENT_DISCONNECT:
					master.deleteClientTasks(myID);
					disconnect();
					break;
				case UPDATE_CLIENT_INFORMATION:
					this.totalNumberOfTasks = (Integer) in.readObject();
					cd.setTotalNumberOfTasks(totalNumberOfTasks);
					this.desc = (String) in.readObject();
					cd.setDesc(desc);
					break;
				}
			}
		} catch (IOException e) {
			 e.printStackTrace();
			System.out.println("CLIENT: DISCONNECTED OR KILLED:" + this.myID);
			//e.printStackTrace();
			disconnect();
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("CLIENT: DISCONNECTED OR KILLED:" + this.myID);
			disconnect();
		}
	}

	public void disconnect() {
		try {
			master.removeClient(myID);
			master.deleteClientTasks(myID);
			taskObserver.canceledTaskWarning(description);
			in.close();
			out.close();
			socket.close();
			master.broadcastWorkersClearClient(myID);
			deliver.interrupt();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private class Deliver extends Thread {

		private LinkedList<CompletedTask> localTaskList;
		private long id;
		private ClientData cd;

		public Deliver(LinkedList<CompletedTask> localTaskList, ClientData cd,
				long id) {
			super();
			this.id = id;
			this.localTaskList = localTaskList;
			this.cd = cd;
			// synchronized (clientDataVector) {
			// if (clientDataVector.containsKey(id))
			// cd = clientDataVector.get(id);
			// }
		}

		@Override
		public void run() {

			while (socket.isConnected()) {
				synchronized (localTaskList) {
					while (localTaskList.size() == 0) {// &&
														// cd.getTotalNumberOfTasksDone()<cd.getTaskCounter()){
						try {
							localTaskList.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}

					ListIterator<CompletedTask> itr = localTaskList
							.listIterator();
					while (itr.hasNext()) {
						CompletedTask task = itr.next();

						try {

							task.deliverTaskToClient();
							itr.remove();
							cd.addTotalNumberOfTasksDone();

						} catch (IOException e) {
							e.printStackTrace();
							return;
						}

					}
					// if (cd.getTotalNumberOfTasksDone() >=
					// cd.getTaskCounter()) {
					// synchronized (pendingResults) {
					// pendingResults.remove(cd.getId());
					// }
					// return;
					// }
				}
			}
		}
	}

}
