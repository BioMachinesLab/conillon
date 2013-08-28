package comm.translator;

import java.io.Serializable;
import java.util.HashMap;

public class ClientTaskClassInformation implements Serializable {

	private long clientHash;
	private String topClassName;
	private String packageName;
	private HashMap<String, Long> classHashs;

	public ClientTaskClassInformation(long clientHash,
			HashMap<String, Long> classHashs) {
		super();
		this.clientHash = clientHash;
		this.packageName = packageName;
		this.classHashs = classHashs;
	}

	public ClientTaskClassInformation(long clientHash, String topClassName,
			HashMap<String, Long> classHashs) {
		super();
		this.clientHash = clientHash;
		this.topClassName = topClassName;
		this.classHashs = classHashs;
	}

	public long getClientHash() {
		return clientHash;
	}

	public String getTopClassName() {
		return topClassName;
	}

	public HashMap<String, Long> getClassHashs() {
		return classHashs;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
