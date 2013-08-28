package stats;

public class Stat {
	private String startTime = "N/A";
	private String endTime = "N/A";
	
	private double bytesSent = 0;
	private double bytesReceived = 0;
	
	
	private int inputedTasks = 0;
	private int tasksServed = 0;
	
	
	private int availableCores = 0;
	
	
	
	public Stat() {
		super();
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public int getTasksServed() {
		return tasksServed;
	}

	public int getAvailableCores() {
		return availableCores;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setTasksServed() {
		this.tasksServed++;
	}

	public int getInputedTasks() {
		return inputedTasks;
	}

	public void setInputedTasks() {
		this.inputedTasks++;
	}

	public void setAvailableCores(int availableCores) {
		this.availableCores += availableCores;
	}

	@Override
	public String toString() {
		return "Stat [startTime=" + startTime + ", endTime=" + endTime
				+ ", bytesSent=" + bytesSent + ", bytesReceived="
				+ bytesReceived + ", inputedTasks=" + inputedTasks
				+ ", tasksServed=" + tasksServed + ", availableCores="
				+ availableCores + "]";
	}

	
	
	

}
