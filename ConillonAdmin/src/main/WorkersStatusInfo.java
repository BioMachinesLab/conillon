package main;

public class WorkersStatusInfo {
	private long numTasks;
	private long lastUpdade;
	private int numCiclesWithNoUpdate = 0;

	public WorkersStatusInfo(long numTasks, long lastUpdade) {
		super();
		this.numTasks = numTasks;
		this.lastUpdade = lastUpdade;
	}

	public long getNumTasks() {
		return numTasks;
	}

	public long getLastUpdade() {
		return lastUpdade;
	}

	public int getNumCiclesWithNoUpdate() {
		return numCiclesWithNoUpdate;
	}

	public void update(long task, long time) {
		if (task > numTasks) {
			numTasks = task;
			lastUpdade = time;
			numCiclesWithNoUpdate = 0;
		} else {
			numCiclesWithNoUpdate++;
		}
	}
	
	@Override
	public String toString() {
		return numCiclesWithNoUpdate + "";
	}
}
