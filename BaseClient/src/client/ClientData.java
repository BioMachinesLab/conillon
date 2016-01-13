package client;

import java.io.Serializable;

import comm.ClientPriority;

public class ClientData implements Serializable {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	private long taskCounter = 0;
	private int port = 0;
	private String ip = "";
	private String macAddress;
	private String hostName;
	private ClientPriority clientPriority;
	private long totalNumberOfTasksDone = 0;
	private long totalNumberOfTasks = 0;
	private long id;
	private long startTime = System.currentTimeMillis();
	private String desc;

	public ClientData(String ip, int port) {
		this(ip, port, 0,"");
	}

	public ClientData(String ip, int port, long totalNumbetOfTasks, String desc) {
		super();
		this.ip = ip;
		this.port = port;
		this.totalNumberOfTasks = totalNumbetOfTasks;
		this.desc = desc;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * @param macAddress the macAddress to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	//;
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param macAddress the macAddress to set
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	//hostName;
	
	public long getTotalNumberOfTasksDone() {
		return totalNumberOfTasksDone;
	}

	public void addTotalNumberOfTasksDone() {
		this.totalNumberOfTasksDone++;
	}

	public String getIpAdress() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public void setClientPriority(ClientPriority clientPriority) {
		this.clientPriority = clientPriority;
	}

	public ClientPriority getClientPriority() {
		return clientPriority;
	}

	public void setTotalNumberOfTasksDone(int totalNumberOfTasksDone) {
		this.totalNumberOfTasksDone = totalNumberOfTasksDone;
	}

	public void addTaskCounter() {
		this.taskCounter++;

	}

	public long getTaskCounter() {
		return this.taskCounter;

	}

	public long getTotalNumberOfTasks() {
		return totalNumberOfTasks;
	}

	public void setTotalNumberOfTasks(long totalNumberOfTasks) {
		this.totalNumberOfTasks = totalNumberOfTasks;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}
