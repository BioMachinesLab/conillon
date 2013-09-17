package web;

import java.net.URL;
import java.net.URLClassLoader;

public class Launcher {

	private void init() {

		try {
//			URL url = new URL("http://localhost:8080/WebRunner.jar");
//			URL url = new URL("http://10.40.50.96:8080/WebRunner.jar");
			URL url = new URL("http://evolve.dcti.iscte.pt:8080/worker.jar");
			URL[] urls = new URL[]{url};
			System.out.println("waiting for classes...");
			ClassLoader loader = new URLClassLoader(urls);
			Class<?> cls = loader.loadClass("screensaver.MainScreensaver");
			System.out.println("got classes... ");
			WebRunner runner = (WebRunner)(cls.newInstance());
			runner.init();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		new Launcher().init();
	}



}
