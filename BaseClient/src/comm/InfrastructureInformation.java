package comm;

import java.io.Serializable;

public class InfrastructureInformation implements Serializable {
	private String codeServerIPaddress = "";
	private int codeServerEncryptedPort = 11080;
	private int codeServerEncryptedPort_TaskDeliver;
	

	private String MasterServerIPaddress = "";
	private int MasterServerEncryptedPort = 10080;
	
	private RunMode runMode = RunMode.mediumWorkerLoad;
	
	public InfrastructureInformation(String codeServerIPaddress,
			int codeServerEncryptedPort,int codeServerEncryptedPort_TaskDeliver, String taskDispatcherServerIPaddress,
			int taskDispatcherServerEncryptedPort, RunMode runMode) {
		super();
		this.codeServerIPaddress = codeServerIPaddress;
		this.codeServerEncryptedPort_TaskDeliver = codeServerEncryptedPort_TaskDeliver;
		this.codeServerEncryptedPort = codeServerEncryptedPort;
		this.MasterServerIPaddress = taskDispatcherServerIPaddress;
		this.MasterServerEncryptedPort = taskDispatcherServerEncryptedPort;
		this.runMode = runMode;
	}
	public int getCodeServerEncryptedPort_TaskDeliver() {
		return codeServerEncryptedPort_TaskDeliver;
	}

	public void setCodeServerEncryptedPort_TaskDeliver(
			int codeServerEncryptedPort_TaskDeliver) {
		this.codeServerEncryptedPort_TaskDeliver = codeServerEncryptedPort_TaskDeliver;
	}
	
	public String getCodeServerIPaddress() {
		return codeServerIPaddress;
	}
	public int getCodeServerEncryptedPort() {
		return codeServerEncryptedPort;
	}
	public String getMasterServerIPaddress() {
		return MasterServerIPaddress;
	}
	public int getMasterServerEncryptedPort() {
		return MasterServerEncryptedPort;
	}

	@Override
	public String toString() {
		return "infrastructureInformation [codeServerIPaddress="
				+ codeServerIPaddress + ", codeServerEncryptedPort="
				+ codeServerEncryptedPort + ", taskDispatcherServerIPaddress="
				+ MasterServerIPaddress
				+ ", taskDispatcherServerEncryptedPort="
				+ MasterServerEncryptedPort + "]";
	}
	public void setRunMode(RunMode runMode) {
		this.runMode = runMode;
	}
	public RunMode getRunMode() {
		return runMode;
	}

	
	

}
