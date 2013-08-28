package main;

import java.awt.Color;

public class WorkerStatus {
	private Color color;
	private int time;
	public WorkerStatus(Color color, int time) {
		super();
		this.color = color;
		this.time = time;
	}
	public Color getColor() {
		return color;
	}
	public int getTime() {
		return time;
	}
	
	
}
