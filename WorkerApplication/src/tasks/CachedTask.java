package tasks;

public class CachedTask {
	private TaskId taskid;
	private Task task;
	public CachedTask(TaskId taskid, Task task) {
		super();
		this.taskid = taskid;
		this.task = task;
	}
	public TaskId getTaskid() {
		return taskid;
	}
	public void setTaskid(TaskId taskid) {
		this.taskid = taskid;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	

}
