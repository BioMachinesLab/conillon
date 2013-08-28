package tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import client.ClientDescription;

import comm.ClientPriority;

public class TaskDescription implements Comparable<TaskDescription>,
		Serializable {

	private Task task;
	private ClientDescription client;
	private long taskid;
	private int numberOfTasks;
	private long startTime;
	private long endTime;
	private long timeToComplete;

	private TaskStatus status;

	private int trials = 0;
	private long lastWorkerId = -1;

	public TaskDescription(Task task, ClientDescription client, long taskid) {
		super();
		this.task = task;
		this.client = client;
		this.taskid = taskid;
		this.status = TaskStatus.SCHEDULE;
		this.timeToComplete = 0;
		this.startTime = System.currentTimeMillis();

	}

	public int getTrials() {
		return trials;
	}

	public void incTrials() {
		trials++;
	}

	public long getLastWorkerId() {
		return lastWorkerId;
	}

	public void setLastWorkerId(long lastWorkerId) {
		this.lastWorkerId = lastWorkerId;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public TaskStatus getTaskStatus() {
		return this.status;
	}

	public void changeTaskStatus(TaskStatus newStatus) {
		if (newStatus == TaskStatus.TERMINATED_OK)
			this.endTime = System.currentTimeMillis() - this.startTime;
		this.status = newStatus;
	}

	public void changeTimeToComplete(long newTime) {
		this.timeToComplete = newTime;
	}

	public long getTimeToComplete() {
		return this.timeToComplete;
	}

	public Task getTask() {
		return task;
	}

	public ClientDescription getClient() {
		return client;
	}

	@Override
	public int compareTo(TaskDescription other) {
		int clientComp = client.compareTo(other.getClient());
		if (clientComp == 0) {
			return task.compareTo(other.task);
		}
		return clientComp;
	}

	public void sendObjectToClient(Object toSend) throws IOException {
		ObjectOutputStream out = client.getOut();
		synchronized (out) {
			out.writeObject(toSend);
			out.reset();
		}
	}

	public long getId() {
		return client.getID();
	}

	public long getTaskId() {
		return taskid;
	}

	public void setNumberOfTasks() {
		this.numberOfTasks++;
	}

	public int getNumberOfTasks() {
		return numberOfTasks;
	}

	public ClientPriority getPriority() {
		return client.getPriority();
	}

	public boolean isEqual(TaskDescription otherTask) {
		if (this.taskid == otherTask.getTaskId()
				&& this.getId() == otherTask.getId())
			return true;
		else
			return false;

	}

	@Override
	public String toString() {
		return "TaskDescription [task=" + task + ", client=" + client
				+ ", taskid=" + taskid + ", numberOfTasks=" + numberOfTasks
				+ ", startTime=" + startTime + ", endTime=" + endTime
				+ ", timeToComplete=" + timeToComplete + ", status=" + status
				+ "]";
	}

}
