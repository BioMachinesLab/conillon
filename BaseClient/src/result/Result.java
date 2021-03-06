package result;

import java.io.Serializable;

import worker.WorkerData;

public class Result implements Serializable{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RuntimeException exception;
	private WorkerData workerData;
	private int taskId;
	
	
	public Result(int taskId) {
		this.taskId = taskId;
	}

	
	public RuntimeException getException() {
		return exception;
	}

	public void setException(RuntimeException exception) {
		this.exception = exception;
	}

	public WorkerData getWorkerData() {
		return workerData;
	}

	public void setWorkerData(WorkerData workerData) {
		this.workerData = workerData;
	}
	
	public int getTaskId() {
		return taskId;
	}
	
}
