package main;

import javax.swing.JFrame;

public class StandAloneAdm extends JFrame {

	private Gui gui = new Gui();
	public StandAloneAdm(String serverName, boolean export) {
		add(gui);
		gui.setServer(serverName);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		if(export)
			new DataExport(gui).start();
	}
	
	public static void main(String[] args) {
		
		boolean export = args.length > 1 && args[1].equals("1"); 			
		
		new StandAloneAdm(args[0],export).init();

	}
	private void init() {
		gui.init();
		gui.start();
		setVisible(true);
	}
}