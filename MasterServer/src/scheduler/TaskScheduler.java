package scheduler;

import java.util.Iterator;
import java.util.LinkedList;

import tasks.CompletedTask;
import tasks.TaskDescription;
import worker.WorkerData;

import comm.ClientPriority;

public class TaskScheduler {
	private int numberOfSendTasks = 0;
	// private LinkedList<TaskDescription> lastUsedLinkedList = null;
	private int position = 0;
	private LinkedList<ClientTasks> pendingTasks = new LinkedList<TaskScheduler.ClientTasks>();
	private LinkedList<TaskDescription> resendTasks = new LinkedList<TaskDescription>();
	private LinkedList<WorkerTasks> workingTasks = new LinkedList<WorkerTasks>();

	public LinkedList<Long> taskDone(CompletedTask task) {
		synchronized (workingTasks) {
			Iterator<WorkerTasks> iterator = workingTasks.iterator();
			while (iterator.hasNext()) {
				WorkerTasks wt = iterator.next();
				if (wt.task.getId() == task.getTaskDescription().getId()
						&& wt.task.getTaskId() == task.getTaskDescription()
								.getTaskId()) {
					iterator.remove();
					return wt.getWorkers();
				}
			}
		}
		return null;
	}

	public TaskDescription getTask(WorkerData workerData)
			throws InterruptedException {
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
			Iterator<TaskDescription> iterator = resendTasks.iterator();
			while (iterator.hasNext()){
				TaskDescription task =iterator.next();
				if (task.getLastWorkerId() != workerData.getId()) {
					iterator.remove();
					return task;
				}
			}
		}
		synchronized (pendingTasks) {
			if (pendingTasks.size() > 0) {
				ClientTasks clientTasks = pendingTasks.get(position);
				TaskDescription task = clientTasks.getNextTask();
				if (clientTasks.isEmpty()) {
					pendingTasks.remove(position);
					resetPosition();
				} else {
					numberOfSendTasks++;
					if (numberOfSendTasks > clientTasks.getClientPriority())
						position = (position + 1) % pendingTasks.size();

				}
				return task;
			}
		}
		synchronized (workingTasks) {
			for (WorkerTasks wt : workingTasks) {
				if (wt.getNumberWorkers() == 1
						&& wt.taskNotOnWorker(workerData.getId())) {
					return wt.task;
				}
			}
		}
		return null;
	}

	public int getNumberOfSendedTasks() {
		return numberOfSendTasks;
	}

	public void addToResend(TaskDescription task) {
		synchronized (resendTasks) {
			resendTasks.add(task);
		}
	}

	public void addTaskList(LinkedList<TaskDescription> taskList, int clientID,
			ClientPriority priority) {
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
		ClientTasks newClient = new ClientTasks(clientID, priority.ordinal(),
				taskList);

		synchronized (pendingTasks) {
			pendingTasks.add(newClient);
		}
		synchronized (this) {
			notifyAll();
		}

	}

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
		ClientTasks newClient = new ClientTasks(task.getId(), task
				.getPriority().ordinal());
		newClient.add(task);

		synchronized (pendingTasks) {
			pendingTasks.add(newClient);
		}
		synchronized (this) {
			notifyAll();
		}
	}

	public void removeClientTasks(long clientID) {
		synchronized (pendingTasks) {
			Iterator<ClientTasks> iterator = pendingTasks.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getClientID() == clientID) {
					iterator.remove();
					resetPosition();
					return;
				}
			}
			pendingTasks.remove(clientID);
		}

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

		public ClientTasks(int clientID2, int ordinal,
				LinkedList<TaskDescription> taskList) {
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

	// public TaskScheduler(LinkedList<TaskDescription> lastUsedLinkedList, int
	// pos) {
	// super();
	// this.position = pos;
	// this.lastUsedLinkedList = lastUsedLinkedList;
	// if (lastUsedLinkedList.size() > 0) {
	// synchronized (lastUsedLinkedList) {
	// TaskDescription td = lastUsedLinkedList.getFirst();
	// this.priority = td.getPriority().ordinal();
	// this.clientID = td.getId();
	// }
	// }
	// }
	//

	// public boolean dispatchSame() {
	// if (numberOfSendTasks <= priority && lastUsedLinkedList.size() > 0)
	// return true;
	// else
	// return false;
	//
	// }

	// public int getListHash() {
	// return lastUsedLinkedList.hashCode();
	// }
	//
	// public void addNumberOfSendTasks() {
	// numberOfSendTasks++;
	// }

	// public TaskDescription getTask(WorkerData workerData) {
	// numberOfSendTasks++;
	// TaskDescription td = null;
	// synchronized (lastUsedLinkedList) {
	// Iterator<TaskDescription> it = lastUsedLinkedList.iterator();
	// while(it.hasNext()){
	// td = it.next();
	// if (td.getTrials() >0 && td.getLastWorkerId() != workerData){
	// it.remove();
	// break;
	// }
	// }
	// // td = lastUsedLinkedList.pollFirst();
	// }
	// return td;
	//
	// }

	// public LinkedList<TaskDescription> getLastUsedLinkedList() {
	// return lastUsedLinkedList;
	// }
	//
	// public void setLastUsedLinkedList(
	// LinkedList<TaskDescription> lastUsedLinkedList) {
	// this.lastUsedLinkedList = lastUsedLinkedList;
	// }
	//
	// // public int getClientPriority() {
	// return priority;
	// }
	//
	// public long getClientID() {
	// return clientID;
	// }
	//
	// public int getPosition() {
	// return this.position;
	// }

}
