package utils;

public class RoomHostInformation {

	private String room;
	private String hostname;
	private String address;
	private boolean connected;
	private boolean banned;
	
	public RoomHostInformation() {
		
	}
	
	public String getRoom() {
		return room;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public String getAddress() {
		return address;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isBanned() {
		return banned;
	}
	
	public void setRoom(String room) {
		this.room = room;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public void setBanned(boolean banned) {
		this.banned = banned;
	}
	
}
