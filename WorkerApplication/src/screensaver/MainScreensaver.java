package screensaver;

import main.Main;
import web.WebRunner;

public class MainScreensaver implements WebRunner {
	
	private Main main;
	
	public MainScreensaver() {
		main = new Main(true);
	}

	@Override
	public void init() {
		main.init();
	}

	@Override
	public void setAttribute(String a) {
	}

}
