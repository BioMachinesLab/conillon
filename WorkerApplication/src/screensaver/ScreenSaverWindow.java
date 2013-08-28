package screensaver;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import worker.Worker;
import worker.WorkerData;

import java.awt.geom.*;
import java.util.*;

/**
 *
 * @version 0.0.1
 *
 */
public class ScreenSaverWindow extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Shape shape;

	private Graphics2D g2d;
	private Font fontTitle = new Font("Serif", Font.PLAIN, 40);	
	private Font fontStatus = new Font("Serif", Font.PLAIN, 15);
	private Thread proc;
	private Worker worker;
	public ScreenSaverWindow(Worker worker){
		this.worker = worker;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setUndecorated(true);
			setResizable(false);
			setTitle("Conillon S.S.");
			validate();
			GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice()
			.setFullScreenWindow(this);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		init();
	}

	public void init() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		setSize((int) screenSize.getWidth(),(int)screenSize.getHeight());
		setVisible(true);


		shape = new Rectangle2D.Double( -1.0, -1.0, 1.0, 1.0 );



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
		g2d.drawString("Conillon ScreenSaver", width/2-200,40);		
		
		if(worker!=null){
			WorkerData wd = worker.getWorkerData();
			if(wd!=null){
			g2d.setFont(fontStatus);
			g2d.drawString("Number of Tasks Processed: "+wd.getNumberOfTasksProcessed(), width/2-200,80);}
			}
			

		g2d.setColor( Color.BLACK );
		for( int x = 0;x< 800; x++ ){
			g2d.setTransform( identify );
			g2d.translate( rand.nextInt() % width, rand.nextInt() % height );
			g2d.rotate( Math.toRadians( 360 * rand.nextDouble() ) );
			g2d.scale( 60 * rand.nextDouble(), 60 * rand.nextDouble() );
			g2d.setColor( new Color( rand.nextInt() ) );
			g2d.fill( shape );
		}
	}

	public void update( Graphics g ){
		paint( g2d );
	}

	public void start(){
		proc = new Thread( this );

		proc.start();
	}

	public void run(){
		Thread t = Thread.currentThread();

		while( t == proc ){
			try{
				Thread.sleep( 2000 );

			}catch( InterruptedException iex ){
				iex.printStackTrace();
			}
			repaint();
		}
	}

	public void stop(){
		proc = null;
	}
}