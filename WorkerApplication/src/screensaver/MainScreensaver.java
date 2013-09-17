package screensaver;

import main.Main;
import web.WebRunner;
import worker.Worker;

public class MainScreensaver implements WebRunner {
	
	

	@Override
	public void init() {
		while (!Worker.shutdown()) {
			Main main = new Main(true);
			main.init();
			System.out.println("Dead");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public void setAttribute(String a) {
	}

}
