package screensaver;

import main.Main;
import web.WebRunner;
import worker.Worker;

public class MainScreensaver implements WebRunner {
	
	public static void main(String[] args) {
		new MainScreensaver().init();
	}

	@Override
	public void init() {
		
		ScreenSaverWindow sw = null;
		
		while(true) {
			Worker.shutdown = false;
			while (!Worker.shutdown()) {
				try{
					Main main = new Main();
					
					if(sw == null){
						sw = new ScreenSaverWindow(main.getWorker());
						sw.start();
					} else
						sw.setWorker(main.getWorker());
					
					main.init();
					System.out.println("Dead");
				}catch(Exception e){
					e.printStackTrace();
				}
				try {
					Thread.sleep(5*1000);
				} catch (InterruptedException e) {}
			}
			
			try {
				Thread.sleep(1000*60);
			} catch (InterruptedException e) {}
			
		}
	}

	@Override
	public void setAttribute(String a) {
	}

}
