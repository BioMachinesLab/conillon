package main;

import javax.swing.JFrame;

public class StandAloneAdm extends JFrame {

	private Gui gui = new Gui();
	public StandAloneAdm(String serverName) {
		add(gui);
		gui.setServer(serverName);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		new StandAloneAdm(args[0]).init();

	}
	private void init() {
		gui.init();
		gui.start();
		setVisible(true);
	}
}