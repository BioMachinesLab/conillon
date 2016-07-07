package worker;

import java.io.Serializable;
import java.util.Date;

import comm.RunMode;

public class WorkerData implements Serializable {

	//TODO: Find a way to remove this
	public static final int CONILLON_VERSION = 12;

	private long startTime;
	private String endTime = "N/A";
	private String workerStatus = "Starting...";
	private long numberOfTasksProcessed = 0;
	private double averageTimePerTask = 0.0;
	private long lastTaskTime = 0;
	private double totalTimeSpentFarming = 0.0;
	private String workerAddress = "N/A";
	private int workerPort = 0;
	private boolean running = false;
	private int connectionCount = 0;
	private long id;
	private int mainWorkerID; // if multiple cores, the all hava a main and
								// unique ID
	private int numberOfProcessors = 1;
	private String operatingSystem = "N/A";
	private String hostName = "N/A";
	private RunMode runMode = RunMode.minimumWorkerLoad;

	private int timeSinceLastTask = 0;
	private int numberOfRequestedTasks = 0;
	
	private Long jarDate;

	public WorkerData() {
		super();
	}

	public int getMainWorkerID() {
		return mainWorkerID;
	}

	public synchronized void update(WorkerData newWorkerData) {
		this.startTime = newWorkerData.getStartTime();
		this.endTime = newWorkerData.getEndTime();
		this.workerStatus = newWorkerData.getWorkerStatus();
		this.numberOfTasksProcessed = newWorkerData.getNumberOfTasksProcessed();
		this.averageTimePerTask = newWorkerData.getAverageTimePerTask();
		this.lastTaskTime = newWorkerData.getLastTaskTime();
		this.totalTimeSpentFarming = newWorkerData.getTotalTimeSpentFarming();
		this.running = newWorkerData.running;
		this.timeSinceLastTask = 0;
		this.jarDate = newWorkerData.getJarDate();
		
	}

	public int getNumberOfRequestedTasks() {
		return numberOfRequestedTasks;
	}

	public synchronized void increaseNumberOfRequestedTasks() {
		timeSinceLastTask = 0;
		numberOfRequestedTasks++;
	}

	public synchronized void decreaseNumberOfRequestedTasks() {
		numberOfRequestedTasks--;
	}

	public synchronized void increaseTime() {
		timeSinceLastTask++;
	}

	public int getTimeSinceLastTask() {
		return timeSinceLastTask;
	}

	public boolean getRunning() {
		return this.running;
	}

	public String getWorkerStatus() {
		return this.workerStatus;
	}

	public synchronized void setMainWorkerID(int mainWorkerID) {
		this.mainWorkerID = mainWorkerID;
	}

	public long getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getSlaveStatus() {
		return workerStatus;
	}

	public long getNumberOfTasksProcessed() {
		return numberOfTasksProcessed;
	}

	public synchronized double getAverageTimePerTask() {
		this.averageTimePerTask = this.totalTimeSpentFarming
				/ this.numberOfTasksProcessed;
		return averageTimePerTask;
	}

	public long getLastTaskTime() {
		return lastTaskTime;
	}

	public double getTotalTimeSpentFarming() {
		return totalTimeSpentFarming;
	}

	public String getWorkerAddress() {
		return workerAddress;
	}

	public int getWorkerPort() {
		return workerPort;
	}

	public boolean isRunning() {
		return running;
	}

	public int getConnectionCount() {
		return connectionCount;
	}

	public long getId() {
		return id;
	}

	public int getNumberOfProcessors() {
		return numberOfProcessors;
	}

	public String getHostName() {
		return hostName;
	}
	
	public String getOperatingSystem() {
		return operatingSystem;
	}

	public synchronized void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public synchronized void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public  synchronized void setWorkerStatus(String workerStatus) {
		this.workerStatus = workerStatus;
	}

	public synchronized void setNumberOfTasksProcessed() {
		this.numberOfTasksProcessed++;
	}

	public synchronized void setLastTaskTime(long lastTaskTime) {
		this.lastTaskTime = lastTaskTime;
	}

	public  synchronized void setTotalTimeSpentFarming(double totalTimeSpentFarming) {
		getAverageTimePerTask(); // update
		this.totalTimeSpentFarming += totalTimeSpentFarming;
	}

	public synchronized void setWorkerAddress(String slaveAddress) {
		this.workerAddress = slaveAddress;
	}

	public  synchronized void setWorkerPort(int slavePort) {
		this.workerPort = slavePort;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	public synchronized void setConnectionCount(int connectionCount) {
		this.connectionCount = connectionCount;
	}

	public synchronized void setId(long id) {
		this.id = id;
	}

	public synchronized void setNumberOfProcessors(int numberOfProcessors) {
		this.numberOfProcessors = numberOfProcessors;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public synchronized void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
	
	public synchronized Long getJarDate() {
		return jarDate;
	}

	public synchronized void setJarDate(Long jarDate) {
		this.jarDate = jarDate;
	}
	
	@Override
	public String toString() {
		return "SlaveData [startTime=" + startTime + ", endTime=" + endTime
				+ ", slaveStatus=" + workerStatus + ", numberOfTasksProcessed="
				+ numberOfTasksProcessed + ", averageTimePerTask="
				+ averageTimePerTask + ", lastTaskTime=" + lastTaskTime
				+ ", totalTimeSpentFarming=" + totalTimeSpentFarming
				+ ", slaveAddress=" + workerAddress + ", slavePort="
				+ workerPort + ", running=" + running + ", connectionCount="
				+ connectionCount + ", id=" + id + ", mainWorkerID="
				+ mainWorkerID + ", numberOfProcessors=" + numberOfProcessors
				+ ", operatingSystem=" + operatingSystem + "]";
	}

	public synchronized void setRunMode(RunMode runMode) {
		this.runMode = runMode;
	}

	public RunMode getRunMode() {
		return runMode;
	}


}