package result;

import comm.translator.ClientTaskClassInformation;

public class TaskClientNumberRequest implements Request {
	private ClientTaskClassInformation taskClasses;

	public TaskClientNumberRequest(ClientTaskClassInformation taskClasses) {
		super();
		this.taskClasses = taskClasses;
	}

	public ClientTaskClassInformation getTaskClasses() {
		return taskClasses;
	}

}
