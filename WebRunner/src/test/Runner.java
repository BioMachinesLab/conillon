package test;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import web.WebRunner;

public class Runner extends JFrame implements MouseInputListener, KeyListener, WebRunner{

	public static final int RUNNER_PORT = 10070;
	
	private JPanel screenSaver;
	private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
	private GraphicsDevice      gs = ge.getDefaultScreenDevice();
	private Process[] procs =  new Process[Runtime.getRuntime().availableProcessors()];
	private long startUpTime;
	private long delayTime;

	private ObjectOutputStream[] outs; 
	
	

	public void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		try {

			URL url = new URL("http://evolve.dcti.iscte.pt/screensaver/");
			URL[] urls = new URL[]{url};

			ClassLoader loader = new URLClassLoader(urls);

			Class cls = loader.loadClass("web.ScreenSaver");

			screenSaver = (JPanel)(cls.newInstance());
			screenSaver.addMouseListener(this);
			screenSaver.addMouseMotionListener(this);
			screenSaver.addKeyListener(this);	
			screenSaver.setBackground(Color.black);
			startUpTime = System.currentTimeMillis();

			getContentPane().add(screenSaver);

			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);

			setUndecorated(true);
			setVisible(true);
//			gs.setFullScreenWindow(this); 

//			for(int i = 0; i < procs.length; i++){
//				new Thread(new Controller(i)).start();
//				System.out.println(i+" - java web.AppletRunner running");
//			}
//			
//			ServerSocket socket = new ServerSocket(RUNNER_PORT);
//			outs = new ObjectOutputStream[procs.length];
//			System.out.println("wainting for connections!");
//			while(true){
//				Socket newSocket = socket.accept();
//				ObjectInputStream in = new ObjectInputStream(newSocket.getInputStream());
//				int id = (Integer)(in.readObject());
//				System.out.println("Got new connection id = " + id);
//				outs[id] = new ObjectOutputStream(newSocket.getOutputStream());
//				
//				System.out.println(id+" - new connection");
//			}
		
			

			//			while(true){
			//
			//
			//				//				String version = getURL("http://evolve.dcti.iscte.pt/lastversion.html");
			//				//				url = new URL("http://evolve.dcti.iscte.pt/v"+version+"/bin/");
			//				//				urls = new URL[]{url};
			//				//
			//				//				loader = new URLClassLoader(urls);
			//				//
			//				//				cls = loader.loadClass("evolutionaryrobotics.parallel.applet.RemoteSlaveApplet");
			//				//				applet = (JApplet) (cls.newInstance());
			//				//
			//				//				//getContentPane().add(applet);
			//				//
			//				//				//				getContentPane().addMouseListener(this);
			//				//				//				getContentPane().addKeyListener(this);	
			//				//
			//				//
			//				//				applet.init();
			//				//				applet.start();
			//			}
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


	//	@Override
	public void mouseClicked(MouseEvent arg0) {
		end();
	}
	//	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	//	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	//	@Override
	public void mousePressed(MouseEvent arg0) {
		end();
	}
	//	@Override
	public void mouseReleased(MouseEvent arg0) {
		end();
	}
	//	@Override
	public void mouseDragged(MouseEvent arg0) {
		end();
	}
	//	@Override
	public void mouseMoved(MouseEvent arg0) {
		end();
	}
	//	@Override
	public void keyPressed(KeyEvent e) {
		end();
	}
	//	@Override
	public void keyReleased(KeyEvent e) {
		end();
	}
	//	@Override
	public void keyTyped(KeyEvent e) {
		end();
	}	

	public void end() {
		if (System.currentTimeMillis() - startUpTime > delayTime) {	
			System.out.println("Trying to end slave");
			//		textArea.setText(textArea.getText()+ "\n" + "TRYING TO END SLAVE");

//			for(int i = 0; i < procs.length; i++){
//				try {
//					if(outs[i] != null)
//						outs[i].writeObject(new Integer(0));
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

//			for(int i = 0; i < procs.length; i++){
//				procs[i].destroy();
//			}

			//		if (applet != null) 
			//			applet.stop();
			System.exit(0);
		}
	}


	private class Controller implements Runnable{
		private int pos;

		public Controller(int pos) {
			super();
			this.pos = pos;
		}

		@Override
		public void run() {
			while (true){
				try {
					procs [pos] = Runtime.getRuntime().exec("java web/Launcher " + pos +" web.AppletRunner");
					procs [pos].waitFor();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void setAttribute(String time) {
		delayTime = new Integer(time);
	}
	
	
	public static void main(String[] args) {
		String delayTime="2000";
		try {
			if (args.length > 0) 
				delayTime= args[0];
		} catch (NumberFormatException e) {
			System.out.println("Wrong delay time, use: java web/Runner [time-ms]");
		}
		Runner runner = new Runner();
		runner.setAttribute(delayTime);
		runner.init();
	}




}
