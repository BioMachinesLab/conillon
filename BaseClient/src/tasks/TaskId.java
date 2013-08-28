package tasks;

import java.io.Serializable;

public class TaskId implements Serializable {
	private long clientID;
	private long taskid;

	public TaskId(long clientID, long taskid) {
		super();
		this.clientID = clientID;
		this.taskid = taskid;

	}

	public long getClientID() {
		return clientID;
	}

	public long getTaskId() {
		return taskid;
	}

	@Override
	public String toString() {
		return "TaskId [clientID=" + clientID + ", taskid=" + taskid + "]";
	}

}
