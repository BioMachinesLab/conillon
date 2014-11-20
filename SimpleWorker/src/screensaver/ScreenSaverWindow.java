package screensaver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputAdapter;

import worker.SimpleWorker;

public class ScreenSaverWindow extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	private Shape shape;
	private Graphics2D g2d;
	private Font fontTitle = new Font("Serif", Font.PLAIN, 40);	
	private Font fontStatus = new Font("Serif", Font.PLAIN, 15);
	private Thread proc;
	private SimpleWorker worker;
	private boolean triedToStop = false;
	private long nTasks = 0;
	
	public ScreenSaverWindow(SimpleWorker worker){
		this.worker = worker;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setUndecorated(true);
			setResizable(false);
			setTitle("Conillon S.S.");
			validate();
//			GraphicsEnvironment.getLocalGraphicsEnvironment()
//			.getDefaultScreenDevice()
//			.setFullScreenWindow(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		init();
	}

	public void init() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		setSize((int) screenSize.getWidth(),(int)screenSize.getHeight());
		setVisible(true);

		shape = new Rectangle2D.Double( -1.0, -1.0, 1.0, 1.0 );
		addMouseMotionListener(new MyListener());
		addKeyListener(new MyKeyListener());
	}  

	public void paint( Graphics g ){
		g2d = ( Graphics2D )g;
		AffineTransform identify = new AffineTransform();
		Random rand = new Random();
		int width = getSize().width;
		int height = getSize().height;
		g2d.setColor( Color.BLACK );

		g2d.fillRect( 0, 0, width, height );
		g2d.setColor( Color.WHITE );
		g2d.setFont(fontTitle);
//		g2d.drawString("Conillon ScreenSaver", width/2-200,40);		
		
		if(worker!=null){
//			WorkerData wd = worker.getWorkerData();
//			if(wd!=null){
			g2d.setFont(fontStatus);
			g2d.drawString("ISCTE-IUL Conillon Screensaver",(int)(width*rand.nextDouble()),(int)(height*rand.nextDouble()));
//			g2d.drawString("Number of Tasks Processed: "+(nTasks+wd.getNumberOfTasksProcessed()), (int)(width*rand.nextDouble()),(int)(height*rand.nextDouble()));
//			}
		}
			
/*
		g2d.setColor( Color.BLACK );
		for( int x = 0;x< 800; x++ ){
			g2d.setTransform( identify );
			g2d.translate( rand.nextInt() % width, rand.nextInt() % height );
			g2d.rotate( Math.toRadians( 360 * rand.nextDouble() ) );
			g2d.scale( 60 * rand.nextDouble(), 60 * rand.nextDouble() );
			g2d.setColor( new Color( rand.nextInt() ) );
			g2d.fill( shape );
		}*/
	}

	public void update( Graphics g ){
		paint( g2d );
	}

	public void start(){
		proc = new Thread( this );

		proc.start();
	}

	public void run(){
		worker.start();
		Thread t = Thread.currentThread();

		while( t == proc ){
			try{
				if(!worker.running) {
					worker = new SimpleWorker(new String[]{SimpleWorker.EVOLVE_ADDRESS});
					worker.start();
				}
				Thread.sleep( 2000 );
			}catch( InterruptedException iex ){
				iex.printStackTrace();
			}
			repaint();
		}
		
		worker.cleanUp();
		
		System.exit(0);
	}
	
	public void stop(){
		if(triedToStop)
			proc = null;
		else
			triedToStop = true;
	}
	
	private class MyKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
		}
		@Override
		public void keyReleased(KeyEvent arg0) {
			stop();
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
		}
	}
	
	private class MyListener extends MouseInputAdapter {

	    @Override
	    public void mouseMoved(MouseEvent arg0) {
	    	stop();
	    }
	}
	
}