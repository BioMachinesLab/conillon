package worker;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SimpleWorkerApplet extends JApplet{
	
	SimpleWorker simpleWorker;
	CapturePane capturePane;
	
	@Override
	public void init() {
		
		capturePane = new CapturePane();
		
		getContentPane().add(capturePane, BorderLayout.CENTER);
		PrintStream ps = System.out;
		System.setOut(new PrintStream(new StreamCapturer(capturePane, ps)));
		
		simpleWorker = new SimpleWorker();
		simpleWorker.start();
	}
	
	@Override
	public void stop() {
		simpleWorker.cleanUp();
	}
	
	public class CapturePane extends JPanel implements Consumer {

        private JTextArea output;

        public CapturePane() {
            setLayout(new BorderLayout());
            output = new JTextArea();
            output.setLineWrap(true);
            add(new JScrollPane(output));
        }

        @Override
        public void appendText(final String text) {
            if (EventQueue.isDispatchThread()) {
                output.append(text);
                output.setCaretPosition(output.getText().length());
            } else {

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        appendText(text);
                    }
                });

            }
        }        
    }

    public interface Consumer {        
        public void appendText(String text);        
    }
	
	 public class StreamCapturer extends OutputStream {

        private StringBuilder buffer;
        private Consumer consumer;
        private PrintStream old;

        public StreamCapturer(Consumer consumer, PrintStream old) {
            buffer = new StringBuilder(128);
            this.old = old;
            this.consumer = consumer;
        }

        @Override
        public void write(int b) throws IOException {
            char c = (char) b;
            String value = Character.toString(c);
            buffer.append(value);
            if (value.equals("\n")) {
                consumer.appendText(buffer.toString());
                buffer.delete(0, buffer.length());
            }
            old.print(c);
        }        
    }  
}
