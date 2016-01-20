package dataaccess.dataobjects;

import java.sql.Time;

public class Worker {
	

	private double average_speed;
	public double getAverageSpeed() {
		return average_speed;
	}
	public void setAverageSpeed(double average_speed) {
		this.average_speed = average_speed;
	}
	
	private long average_time; 
	public long getAverageTime() {
		return average_time;
	}
	public void setAverageTime(long average_time) {
		this.average_time = average_time;
	}

	private String host_name; 
	public String getHost_name() {
		return host_name;
	}	
	public void setHost_name(String host_name) {
		this.host_name = host_name;
	}
		
	private long id; 
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	private int id_room;
	public int getId_room() {
		return id_room;
	}
	public void setId_room(int id_room) {
		this.id_room = id_room;
	}
	
	private String ip;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip.startsWith("/") ? ip.substring(1) : ip;
	}
	
	private Boolean is_banned;
	public Boolean getIsBanned() {
		return is_banned;
	}
	public void setIsBanned(Boolean is_banned) {
		this.is_banned = is_banned;
	}
	
	private String mac_address;
	public String getMac_address() {
		return mac_address;
	}
	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}
	
	private int num_cores;
	public int getNum_cores() {
		return num_cores;
	}
	public void setNum_cores(int num_cores) {
		this.num_cores = num_cores;
	}
	
	private String operative_system; 
	public String getOperative_system() {
		return operative_system;
	}
	public void setOperative_system(String operative_system) {
		this.operative_system = operative_system;
	}
	
	private long tasks_processed;
	public long getNumberOfTasksProcessed() {
		return tasks_processed;
	}
	public void setNumberOfTasksProcessed(long tasks_processed) {
		this.tasks_processed = tasks_processed;
	}
	
	private long total_running_time; 
	public long getTotal_running_time() {
		return total_running_time;
	}
	public void setTotal_running_time(long total_running_time) {
		this.total_running_time = total_running_time;
	}
	
	
	
	
	
	
	
}
