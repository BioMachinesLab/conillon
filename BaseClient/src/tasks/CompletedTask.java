package tasks;

import java.io.IOException;

import result.Result;

public class CompletedTask {
	private Result result;
	private TaskDescription taskDescription;
	private TaskId tid;
	
	
	public CompletedTask(Result result, TaskDescription taskDescription, TaskId tid) {
		super();
		this.result = result;
		this.taskDescription = taskDescription;
		this.tid = tid;
	}
	
	public Result getResult() {
		return result;
	}
	public TaskDescription getTaskDescription() {
		return taskDescription;
	}
	public TaskId getTid() {
		return tid;
	}
	public void deliverTaskToClient() throws IOException{
		taskDescription.sendObjectToClient(result);
	}

}
