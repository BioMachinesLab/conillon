package client;

import java.io.ObjectOutputStream;
import java.io.Serializable;

import comm.ClientPriority;

public class ClientDescription implements Comparable<ClientDescription>,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -672335622269551091L;	
	
	private int totalNumberOfTasksDone = 0;
	private ClientPriority priority;
	private int lastStamp = 0;
	private long id = 0;
	private ObjectOutputStream out;
	

	public ClientDescription(long id,ClientPriority priority, ObjectOutputStream out) {
		this.priority = priority;
		this.out = out;
		this.id = id;
		
	}

	public int getTotalNumberOfTasksDone() {
		return totalNumberOfTasksDone;
	}

	
	
	public ObjectOutputStream getOut() {
		return out;
	}
	
	public long getID(){
		return id;
	}
	
	
	public ClientPriority getPriority(){
		return this.priority;
	}
	
	
	@Override
	public int compareTo(ClientDescription other) {
		if (other.priority == priority)
			return other.lastStamp - lastStamp;
		else
			return other.priority.ordinal() - priority.ordinal();
	}

}
