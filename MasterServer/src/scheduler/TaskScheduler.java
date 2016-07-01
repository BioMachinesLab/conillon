package scheduler;

import java.util.Iterator;
import java.util.LinkedList;

import tasks.CompletedTask;
import tasks.TaskDescription;
import worker.WorkerData;

import comm.ClientPriority;

public class TaskScheduler implements IParallelizator {
	private int numberOfSendTasks = 0;
	// private LinkedList<TaskDescription> lastUsedLinkedList = null;
	private int position = 0;
	private LinkedList<ClientTasks> pendingTasks = new LinkedList<TaskScheduler.ClientTasks>();
	private LinkedList<TaskDescription> resendTasks = new LinkedList<TaskDescription>();
	private LinkedList<WorkerTasks> workingTasks = new LinkedList<WorkerTasks>();

	@Override
	public LinkedList<Long> taskDone(CompletedTask task) {
		synchronized (workingTasks) {
			Iterator<WorkerTasks> iterator = workingTasks.iterator();
			while (iterator.hasNext()) {
				WorkerTasks wt = iterator.next();
				if (wt.task.getId() == task.getTaskDescription().getId()
						&& wt.task.getTaskId() == task.getTaskDescription().getTaskId()) {
					iterator.remove();
					return wt.getWorkers();
				}
			}
		}
		return null;
	}

	/**
	 * DEVsgf
	 * Old method to get task to worker
	 */
	@Override
	public TaskDescription getTask(WorkerData workerData) throws InterruptedException {
		// choose best
		TaskDescription task = getNextTask(workerData);
		while (task == null) {
			synchronized (this) {
				wait();
			}
			task = getNextTask(workerData);
		}
		addToWorkingTasks(task, workerData.getId());
		return task;
	}	

	private void addToWorkingTasks(TaskDescription task, long id) {
		synchronized (workingTasks) {
			for (WorkerTasks wt : workingTasks) {
				if (wt.task == task) {
					wt.add(id);
					return;
				}
			}
			workingTasks.addFirst(new WorkerTasks(task, id));
		}
	}

	private TaskDescription getNextTask(WorkerData workerData) {
		synchronized (resendTasks) {
			Iterator<TaskDescription> iterator = resendTasks.iterator();		//for the resent task vector
			while (iterator.hasNext()) {										//for the current task
				TaskDescription task = iterator.next();							//	if the last worker was not the
				if (task.getLastWorkerId() != workerData.getId()) {				//	current worker
					iterator.remove();											//		remove and return the task
					return task;
				}
			}
		}
		synchronized (pendingTasks) {
			if (pendingTasks.size() > 0) {										//for the pending tasks (Client Tasks list)
				ClientTasks clientTasks = pendingTasks.get(position);			//get the ClientTasks instance at position 'position'
				TaskDescription task = clientTasks.getNextTask();				//gets the nest task from ClientTasks instance
				if (clientTasks.isEmpty()) {
					pendingTasks.remove(position);								//handle empty client tasks
					resetPosition();											
				} else {
					numberOfSendTasks++;
					if (numberOfSendTasks > clientTasks.getClientPriority())	//if nb of sent task if bigger than client priority
						position = (position + 1) % pendingTasks.size();		//redefine client position

				}
				return task;													//return the task
			}
		}
		synchronized (workingTasks) {
			for (WorkerTasks wt : workingTasks) {

				if ((wt.getNumberWorkers() == 0 || wt.getNumberWorkers() == 1)	//if nb workers is 0 or 1 for the current working task
						&& wt.taskNotOnWorker(workerData.getId())) {			//and if task not on current worker
					return wt.task;												//return the task
				}
			}
		}
		return null;
	}

	@Override
	public int getNumberOfSendedTasks() {
		return numberOfSendTasks;
	}
	@Override
	public void addToResend(TaskDescription task) {
		synchronized (resendTasks) {
			resendTasks.add(task);
		}
	}
	@Override
	public void addTaskList(LinkedList<TaskDescription> taskList, int clientID, ClientPriority priority) {
		synchronized (pendingTasks) {
			for (ClientTasks tasks : pendingTasks) {
				if (tasks.getClientID() == clientID) {
					tasks.addAll(taskList);
					synchronized (this) {
						notifyAll();
					}
					return;
				}
			}
		}
		ClientTasks newClient = new ClientTasks(clientID, priority.ordinal(), taskList);

		synchronized (pendingTasks) {
			pendingTasks.add(newClient);
		}
		synchronized (this) {
			notifyAll();
		}

	}
	@Override
	public void addTask(TaskDescription task) {
		long clientID = task.getId();
		synchronized (pendingTasks) {

			for (ClientTasks tasks : pendingTasks) {
				if (tasks.getClientID() == clientID) {
					tasks.add(task);
					synchronized (this) {
						notifyAll();
					}
					return;
				}
			}
		}
		ClientTasks newClient = new ClientTasks(task.getId(), task.getPriority().ordinal());
		newClient.add(task);

		synchronized (pendingTasks) {
			pendingTasks.add(newClient);
		}
		synchronized (this) {
			notifyAll();
		}
	}
	@Override
	public void removeClientTasks(long clientID) {
		System.out.println(
				"before client exited: " + pendingTasks.size() + " " + resendTasks.size() + " " + workingTasks.size());
		synchronized (pendingTasks) {
			Iterator<ClientTasks> iterator = pendingTasks.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getClientID() == clientID) {
					iterator.remove();
					resetPosition();
					// return;
				}
			}
			pendingTasks.remove(clientID);
		}
		synchronized (workingTasks) {
			Iterator<WorkerTasks> iterator = workingTasks.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getTask().getClient().getID() == clientID) {
					iterator.remove();
					// return;
				}
			}
		}
		System.out.println(
				"after client exited: " + pendingTasks.size() + " " + resendTasks.size() + " " + workingTasks.size());
	}
	
	private void resetPosition() {
		numberOfSendTasks = 0;
		if (position >= pendingTasks.size()) {
			position = 0;
		}
	}

	private class WorkerTasks {
		private TaskDescription task;
		private LinkedList<Long> workers = new LinkedList<Long>();

		public WorkerTasks(TaskDescription task, long id) {
			super();
			this.task = task;
			workers.add(id);
		}

		public int getNumberWorkers() {
			return workers.size();
		}

		public boolean taskNotOnWorker(long workerId) {
			for (Long id : workers) {
				if (id == workerId)
					return false;
			}
			return true;
		}

		public void add(Long client) {
			workers.add(client);
		}

		public void remove(Long client) {
			workers.remove(client);
		}

		public TaskDescription getTask() {
			return task;
		}

		public LinkedList<Long> getWorkers() {
			return workers;
		}

	}

	private class ClientTasks {
		private long clientID;
		private int priority;
		private LinkedList<TaskDescription> tasks = new LinkedList<TaskDescription>();

		public ClientTasks(long clientID, int priority) {
			super();
			this.clientID = clientID;
			this.priority = priority;
		}

		public boolean isEmpty() {
			return tasks.isEmpty();
		}

		public ClientTasks(int clientID2, int ordinal, LinkedList<TaskDescription> taskList) {
			// TODO Auto-generated constructor stub
		}

		public void addAll(LinkedList<TaskDescription> taskList) {
			// TODO Auto-generated method stub

		}

		public long getClientID() {
			return clientID;
		}

		public int getClientPriority() {
			return priority;
		}

		private void add(TaskDescription task) {
			tasks.add(task);
		}

		public TaskDescription getNextTask() {
			return tasks.pollFirst();
		}

	}
	@Override
	public void removeWorker(long id) {
		synchronized (workingTasks) {
			Iterator<WorkerTasks> iterator = workingTasks.iterator();
			while (iterator.hasNext()) {
				WorkerTasks task = iterator.next();
				if (!task.taskNotOnWorker(id)) {
					task.remove(new Long(id));
				}
			}
		}
		synchronized (this) {
			notifyAll();
		}
	}

}
