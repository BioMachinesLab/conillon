package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;

import javax.swing.JApplet;

public class AppletRunner implements WebRunner{

	private ObjectInputStream in;
	private JApplet applet;
	private Integer id;

//	public AppletRunner(Integer id) {
//		super();
//		this.id = id;
//	}

	public void init(){

		try{
			String version = getURL("http://evolve.dcti.iscte.pt/lastversion.html");

			URL url = new URL("http://evolve.dcti.iscte.pt/v"+version+"/bin/");
			URL[] urls = new URL[]{url};

			URLClassLoader loader = new URLClassLoader(urls);

			Class<?> cls = loader.loadClass("evolutionaryrobotics.parallel.applet.RemoteSlaveAppletForLauncher");
			applet = (JApplet) (cls.newInstance());

			//getContentPane().add(applet);

			//				getContentPane().addMouseListener(this);
			//				getContentPane().addKeyListener(this);	


			Socket socket = new Socket(InetAddress.getByName("localhost"),Runner.RUNNER_PORT);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(id);
			in = new ObjectInputStream(socket.getInputStream());
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Integer i = (Integer)in.readObject();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					applet.stop();
					System.out.println("I will exit! " + id);
					System.exit(0);
				}
			}).start();
			
			applet.init();
			applet.start();
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
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
//		} catch (Error e){
//			e.printStackTrace();
		}
	}

	private String getURL(String u) {
		URL url;
		InputStream is;
		InputStreamReader isr;
		BufferedReader r;
		String str="";

		try {
			System.out.println("Reading URL: " + u);
			url = new URL(u);
			is = url.openStream();
			isr = new InputStreamReader(is);
			r = new BufferedReader(isr);
			str = r.readLine();

		} catch (MalformedURLException e) {
			System.out.println("Must enter a valid URL");
		} catch (IOException e) {
			System.out.println("Can not connect");
		}
		return str;
	}

	@Override
	public void setAttribute(String attr) {
		id = new Integer(attr);
	}

	public static void main(String[] args) {
		System.out.println("STARTED!");
		String id;
		if(args.length > 0){
			id = args[0];
		} else {
			id = "0";
		}
		AppletRunner appletRunner = new AppletRunner();
		appletRunner.setAttribute(id);
		appletRunner.init();
	}

}
