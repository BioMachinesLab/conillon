package worker;

import java.io.Serializable;
import java.net.InetAddress;

public class WorkerInformation implements Serializable {
	private String operativeSystem;
	private int numberOfProcessors = 1;
	private InetAddress ip;
	
	
	
	public WorkerInformation(int numberOfProcessors, String operatingSystem,  InetAddress ip) {
		super();
		this.numberOfProcessors = numberOfProcessors;
		this.operativeSystem = operatingSystem;
		this.ip = ip;
	}
	
	public int getNumberOfProcessors() {
		return numberOfProcessors;
	}
	
	public String getOperatingSystem(){
		return operativeSystem;
	}
	
	public InetAddress getIP(){
		return ip;
	}

	@Override
	public String toString() {
		return "SystemInformation [numberOfProcessors=" + numberOfProcessors
				+ ", operatingSystem=" + operativeSystem + ", ip=" + ip + "]";
	}
}

