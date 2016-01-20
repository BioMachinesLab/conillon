package dataaccess.dataobjects;

public class Client {

	private long id;
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip.startsWith("/") ? ip.substring(1) : ip;
	}
	/**
	 * @return the mac_address
	 */
	public String getMac_address() {
		return mac_address;
	}
	/**
	 * @param mac_address the mac_address to set
	 */
	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}
	/**
	 * @return the host_name
	 */
	public String getHost_name() {
		return host_name;
	}
	/**
	 * @param host_name the host_name to set
	 */
	public void setHost_name(String host_name) {
		this.host_name = host_name;
	}
	/**
	 * @return the average_time
	 */
	public long getAverage_time() {
		return average_time;
	}
	/**
	 * @param average_time the average_time to set
	 */
	public void setAverage_time(long average_time) {
		this.average_time = average_time;
	}
	/**
	 * @return the average_speed
	 */
	public double getAverage_speed() {
		return average_speed;
	}
	/**
	 * @param average_speed the average_speed to set
	 */
	public void setAverage_speed(double average_speed) {
		this.average_speed = average_speed;
	}
	/**
	 * @return the total_running_time
	 */
	public long getTotal_running_time() {
		return total_running_time;
	}
	/**
	 * @param total_running_time the total_running_time to set
	 */
	public void setTotal_running_time(long total_running_time) {
		this.total_running_time = total_running_time;
	}
	/**
	 * @return the total_tasks
	 */
	public long getTotal_tasks() {
		return total_tasks;
	}
	/**
	 * @param total_tasks the total_tasks to set
	 */
	public void setTotal_tasks(long total_tasks) {
		this.total_tasks = total_tasks;
	}
	/**
	 * @return the eta
	 */
	public String getEta() {
		return eta;
	}
	/**
	 * @param eta the eta to set
	 */
	public void setEta(String eta) {
		this.eta = eta;
	}
	
	private String ip;
	private String mac_address;
	private String host_name; 
	private long average_time; 
	private double average_speed;
	private long total_running_time;
	private long total_tasks;
	private String eta;
}
