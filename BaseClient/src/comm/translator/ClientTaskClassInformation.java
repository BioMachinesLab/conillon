package comm.translator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

public class ClientTaskClassInformation implements Serializable {

	private long clientHash;
	private String topClassName;
	private String packageName;
	private HashMap<String, Long> classHashs;
	private int randomNumber = new Random().nextInt(10000);

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
		return clientHash+randomNumber;
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
