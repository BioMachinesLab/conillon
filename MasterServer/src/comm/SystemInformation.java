package comm;

import java.io.Serializable;
import java.net.InetAddress;

public class SystemInformation implements Serializable {
	
	private int numberOfProcessors;
	private String operatingSystem;
	private InetAddress ip;
	
	public SystemInformation(int numberOfProcessors, String operatingSystem,  InetAddress ip) {
		super();
		this.numberOfProcessors = numberOfProcessors;
		this.operatingSystem = operatingSystem;
		this.ip = ip;
	}
	
	public int getNumberOfProcessors() {
		return numberOfProcessors;
	}
	
	public String getOperatingSystem(){
		return operatingSystem;
	}
	
	public InetAddress getIP(){
		return ip;
	}

	@Override
	public String toString() {
		return "SystemInformation [numberOfProcessors=" + numberOfProcessors
				+ ", operatingSystem=" + operatingSystem + ", ip=" + ip + "]";
	}

}