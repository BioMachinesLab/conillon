package screensaver;

import worker.SimpleWorker;

public class MainScreensaver {
	
	private SimpleWorker simpleWorker;
	
	public static void main(String[] args) {
		new MainScreensaver().init();
	}

	public void init() {
		
		ScreenSaverWindow sw = null;
		
		simpleWorker = new SimpleWorker();
		
		sw = new ScreenSaverWindow(simpleWorker);
		sw.start();
		
	}
}
