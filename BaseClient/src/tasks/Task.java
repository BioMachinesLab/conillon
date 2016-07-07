package tasks;

import java.io.Serializable;

import result.Result;

import comm.FileProvider;

public abstract class Task implements Runnable, Serializable, Comparable<Task> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7236548233517593971L;
	private static int TASK_ID = 0;
	protected int id;
	public boolean mineFlag = false;
	private FileProvider fileProvider = FileProvider.getDefaultFileProvider();

	public Task(){
		super();
		this.id = TASK_ID++;
	}
	
	public Task(int id) {
		super();
		this.id = id;
	}

	@Override
	public int compareTo(Task other) {
		return id-other.id;
	}

	public abstract Result getResult();

	public void setFileProvider(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	public FileProvider getFileProvider() {
		return fileProvider;
	}
	
	public int getId() {
		return id;
	}

}
