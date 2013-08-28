package web;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Launcher {


	private String attribute;
	private String className;

	public Launcher(String attribute, String className) {
		this.attribute = attribute;
		this.className = className;
	}


	private void init() {

		try {
//			URL url = new URL("http://localhost:8080/WebRunner.jar");
//			URL url = new URL("http://10.40.50.96:8080/WebRunner.jar");
			URL url = new URL("http://evolve.dcti.iscte.pt/WebRunner.jar");
			URL[] urls = new URL[]{url};
			System.out.println("waiting for classes...");
			ClassLoader loader = new URLClassLoader(urls);

			Class<?> cls = loader.loadClass(className);
			System.out.println("got classes... ");
			WebRunner runner = (WebRunner)(cls.newInstance());
			runner.setAttribute(attribute);
			runner.init();


		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public static void main(String[] args) {
		if (args.length >= 2) {
			String attribute= args[0];
			String className = args[1];
			for(String s :args){
				System.out.println(s);
			}
			new Launcher(attribute, className).init();
		} else {
			System.out.println("Error: wrong number of arguments... use java web.Launcher attributes classname");
		}
	}



}
