package dataaccess.dataobjects;

import java.math.BigDecimal;
import java.util.Map;

public class Worker {
	

	public Worker() {
		
	}
	public Worker(Map<String, Object> row) {
		if (row.containsKey("average_speed")) {
			setAverageSpeed(row.get("average_speed"));
		}
		if (row.containsKey("average_time")) {
			setAverageTime(row.get("average_time"));
		}
		if (row.containsKey("host_name")) {
			setHost_name(row.get("host_name"));
		}
		if (row.containsKey("id")) {
			setId(row.get("id"));
		}
		if (row.containsKey("id_room")) {
			setId_room(row.get("id_room"));
		}
		if (row.containsKey("ip")) {
			setIp(row.get("ip"));
		}
		if (row.containsKey("is_banned")) {
			setIsBanned(row.get("is_banned"));
		}
		if (row.containsKey("mac_address")) {
			setMac_address(row.get("mac_address"));		
		}
		if (row.containsKey("num_cores")) {
			setNum_cores(row.get("num_cores"));
		}
		if (row.containsKey("operative_system")) {
			setOperative_system(row.get("operative_system"));
		}
		if (row.containsKey("total_running_time")) {
			setTotal_running_time(row.get("total_running_time"));		
		}
		if (row.containsKey("performance")) {
			setPerformance(row.get("performance"));
		}
	}
	
	private double average_speed;
	public double getAverageSpeed() {
		return average_speed;
	}
	public String getFormattedAverageSpeed() {
		return Double.toString(average_speed).replace(',', '.');
	}
	public void setAverageSpeed(double average_speed) {
		this.average_speed = average_speed;
	}
	public void setAverageSpeed(Object average_speed) {
		this.average_speed = dataaccess.DbHandler.convertBigDecimalToDouble((BigDecimal)average_speed);		
	}
	
	
	private long average_time; 
	public long getAverageTime() {
		return average_time;
	}
	public void setAverageTime(long average_time) {
		this.average_time = average_time;
	}
	public void setAverageTime(Object average_time) {
		this.average_time = (Long)average_time;
	}

	private String host_name; 
	public String getHost_name() {
		return host_name;
	}	
	public void setHost_name(String host_name) {
		this.host_name = host_name;
	}
	public void setHost_name(Object host_name) {
		this.host_name = host_name.toString();
	}
	
	
	private long id; 
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setId(Object id) {
		Integer i = (Integer)id;
		this.id = new Long(i);
	}
	
	private int id_room;
	public int getId_room() {
		return id_room;
	}
	public void setId_room(int id_room) {
		this.id_room = id_room;
	}
	public void setId_room(Object id_room) {
		this.id_room = dataaccess.DbHandler.convertBigDecimalToInt((BigDecimal)id_room);
	}
	
	
	private String ip;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip.startsWith("/") ? ip.substring(1) : ip;
	}
	public void setIp(Object ip) {
		setIp(ip.toString());
	}
	
	private Boolean is_banned;
	public Boolean getIsBanned() {
		return is_banned;
	}
	public void setIsBanned(Boolean is_banned) {
		this.is_banned = is_banned;
	}
	public void setIsBanned(Object is_banned) {
		this.is_banned = (Boolean)is_banned;
	}
	
	
	private String mac_address;
	public String getMac_address() {
		return mac_address;
	}
	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}
	public void setMac_address(Object mac_address) {
		this.mac_address = mac_address == null ? null : mac_address.toString();
	}
	
	private int num_cores;
	public int getNum_cores() {
		return num_cores;
	}
	public void setNum_cores(int num_cores) {
		this.num_cores = num_cores;
	}
	public void setNum_cores(Object num_cores) {
		this.num_cores = (Integer)num_cores;
	}
	
	private String operative_system; 
	public String getOperative_system() {
		return operative_system;
	}
	public void setOperative_system(String operative_system) {
		this.operative_system = operative_system;
	}
	public void setOperative_system(Object operative_system) {
		this.operative_system = operative_system != null ? operative_system.toString() : null;
	}
	
	private long performance;
	public long getPerformance() {
		return performance;
	}
	public void setPerformance(long performance) {
		this.performance = performance;
	}
	public void setPerformance(Object performance) {
		this.performance = performance != null ? (long)performance : 0;
	}
	
	
	//TODO DEVsimao DEPRECATED
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
	public void setTotal_running_time(Object total_running_time) {
		this.total_running_time = (Long)total_running_time;
	}	
}
