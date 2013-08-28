package main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import worker.WorkerData;

public class Gui extends JApplet {
	private JLabel idLabel = new JLabel("Id: ");
	private JLabel ipLabel = new JLabel("IP: ");
	private JLabel nrCoresLabel = new JLabel("Number of Cores: ");
	private JLabel osLabel = new JLabel("OS: ");
	private JLabel farmingTime = new JLabel("Farming Time: ");
	private JLabel nTasks = new JLabel("Number of Tasks: ");
	private JLabel averageTime = new JLabel("Average Time/Task: ");

	private JTextArea textArea;
//	private String serverAddress = "evolve.dcti.iscte.pt";
//	private String serverAddress = "10.40.50.96";
	private String serverAddress = "localhost";
	protected Main main;

	private boolean on = true;
	
	private PrintStream out = System.out;
	
	

	public void init() {
		// Set up the text input boxes
		Container container = getContentPane();

		serverAddress = getCodeBase().toString();
		serverAddress = serverAddress.substring(7, serverAddress.length() - 1);
		if (serverAddress.contains(":")) {
			serverAddress = serverAddress.substring(0,
					serverAddress.indexOf(':'));
		}

		JPanel inputPanel = new JPanel(new GridLayout(7, 1));
		inputPanel.add(idLabel);
		inputPanel.add(ipLabel);
		inputPanel.add(nrCoresLabel);
		inputPanel.add(osLabel);
		inputPanel.add(farmingTime);
		inputPanel.add(nTasks);
		inputPanel.add(averageTime);

		// inputPanel.add(new JLabel(" ", SwingConstants.RIGHT));
		// inputNumPlaces = new JTextField(3);
		// inputPanel.add(inputNumPlaces);
		// inputNumPlaces.addActionListener(this);
		container.add(inputPanel, BorderLayout.NORTH);

		// JPanel textPanel = new JPanel();
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		// textArea.setMargin(new Insets(0, 20, 20, 20));
		JScrollPane scrollPane = new JScrollPane(textArea);

		// textPanel.add(scrollPane);
		container.add(scrollPane, BorderLayout.CENTER);

		// JPanel buttonPanel = new JPanel();

		// connect = new JButton("connect");
		// connect.addActionListener(this);
		//
		// buttonPanel.add(connect);
		// container.add(buttonPanel, BorderLayout.SOUTH);

		// new Updater().start();
		out = new PrintStream(new JTextAreaOutputStream(
				textArea));

		System.out.println("starting");
			new Thread(new Runnable() {


			@Override
			public void run() {
				String[] args = new String[0];
				while (on) {
					try {
						GuiUpdater guiUpdater = new GuiUpdater();
						main = new Main(args, serverAddress, true, true,
								out, guiUpdater);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out
							.println("Connection closed! Trying to reconnect.");
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();

	}

	@Override
	public void destroy() {
		super.destroy();
		on = false;
		main.end();
	}

	private class GuiUpdater extends GuiClientInfoUpdater {
		@Override
		public void updateClientInfo(WorkerData workerData) {
			idLabel.setText("Id: " + workerData.getId());
			ipLabel.setText("IP: " + workerData.getWorkerAddress() + "/"
					+ workerData.getWorkerPort());
			nrCoresLabel.setText("Number of Cores: "
					+ workerData.getNumberOfProcessors());
			osLabel.setText("OS: " + workerData.getOperatingSystem());
			farmingTime.setText("Farming Time: "
					+ workerData.getTotalTimeSpentFarming());
			nTasks.setText("Number of Tasks: "
					+ workerData.getNumberOfTasksProcessed());
			averageTime.setText("Average Time/Task: "
					+ workerData.getAverageTimePerTask());

		}
	}

	// class Updater extends Thread {
	//
	// public void run() {
	// while (true) {
	// OperatingSystemMXBean OSMXBean = ManagementFactory
	// .getOperatingSystemMXBean();
	// String str = String.valueOf(Math.round(OSMXBean
	// .getSystemLoadAverage()));
	// System.out.println(str);
	// inputNumPlaces.setText(str);
	// try {
	// Thread.sleep(3000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// }

//	@Override
//	public void actionPerformed(ActionEvent arg0) {
//		// TODO Auto-generated method stub
//		Object source = arg0.getSource();
//
//		if (source == inputNumPlaces) {
//			try {
//				ndigits = Integer.parseInt(inputNumPlaces.getText());
//			}
//
//			catch (NumberFormatException e) {
//				JOptionPane.showMessageDialog(this, "teste", "teste!",
//						JOptionPane.ERROR_MESSAGE);
//			}
//
//			// } else if (source == connect) { // connect text area
//			// String[] args = new String[0];
//			// while (true) {
//			// try {
//			// new Main(args, true, new PrintStream(
//			// new JTextAreaOutputStream(textArea)));
//			// } catch (IOException e) {
//			// // TODO Auto-generated catch block
//			// e.printStackTrace();
//			// } catch (ClassNotFoundException e) {
//			// // TODO Auto-generated catch block
//			// e.printStackTrace();
//			// } catch (InterruptedException e) {
//			// // TODO Auto-generated catch block
//			// e.printStackTrace();
//			// }
//			// System.out.println("Connection closed! Trying to reconnect.");
//			// try {
//			// Thread.sleep(60000);
//			// } catch (InterruptedException e) {
//			// }
//			// }
//		}
//
//	}

	public class JTextAreaOutputStream extends OutputStream {

		private static final int NUM_CHARS = 10000;
		JTextArea textArea;
		private int size = 0;

		public JTextAreaOutputStream(JTextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void write(int arg) throws IOException {
			if (size++ > NUM_CHARS) {
				this.textArea.setText("");
				size = 0;
			}
			this.textArea.append((new Character((char) arg)).toString());
			// System.out.print((char) arg);
			// int len = this.textArea.getText().length();
			// this.textArea.setCaretPosition(len);
		}
	}
}
