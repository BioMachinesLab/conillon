package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.Socket;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import worker.WorkerData;
import client.ClientData;

import comm.ConnectionType;

public class Gui extends JApplet implements ActionListener {

	private static final long serialVersionUID = 1L;
	// North
	private JTextField inputWorker;
	private JTextField inputClient;
	private JButton kickWorker;
	private JButton killWorker;
	private JButton kickClient;
	private JLabel serverTime;

	// South
	private JLabel numberOfWorkers;
	private JLabel numberOfClients;
	private long processedTasks;
	private JLabel numberOfProcessedTasks;
	private JLabel averageSystemSpeed;
	private String serverAddress = "localhost";
	private int serverPort = 10001;
	private JButton connect;

	private Hashtable<Long, WorkersStatusInfo> status = new Hashtable<Long, WorkersStatusInfo>();
	private Hashtable<Long, WorkerData> workerDataVector = new Hashtable<Long, WorkerData>();
	private Hashtable<Long, ClientData> clientDataVector = new Hashtable<Long, ClientData>();

	WorkerTableModel workerTableModel;
	ClientTableModel clientTableModel;

	private Object[] workerKeys;
	private Object[] clientKeys;

	private ObjectInputStream in;
	private ObjectOutputStream out;

	private long currentTime;
	private Long currentServerTime;
	private JTable jTableWorker;
	private JTable jTableClient;

	JTabbedPane tabbedPane = new JTabbedPane();

	private GraphingData speedGraph = new GraphingData();
	private GraphingData coresGraph = new GraphingData();
	private GraphingData idleGraph = new GraphingData();
	private GraphingData pendingGraph = new GraphingData();
	private boolean connected = false;
	
	private double speed;
	private int idle;
	private int cores;
	private int tasks;

	public void init() {

		gui();
		boolean firstTime = true;
		while (!connected) {
			connect();
			
			if(!firstTime) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			firstTime = false;
		}
	}

	private void connect() {
		connected = false;
		try {
			Socket socket = new Socket(this.serverAddress, this.serverPort);
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(ConnectionType.ADMIN);
			connected = true;
			Listener l = new Listener(socket);
			l.start();
			Command c = new Command();
			c.start();
			tabbedPane.setBackground(Color.LIGHT_GRAY);
		} catch (Exception e) {
			System.out.println("Can't connect to "+serverAddress);
			tabbedPane.setBackground(Color.RED);
		}
	}

	class Command extends Thread {

		public void run() {
			while (true) {
				if(!connected)
					break;

				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				try {
				String cmd = br.readLine();

				synchronized (out) {
					out.writeObject(ConnectionType.KILL_CLIENT);
					out.writeObject(Long.parseLong(cmd));
				}

				} catch (IOException e) {
					connected = false;
					e.printStackTrace();
				}
			}
		}
	}

	private class Listener extends Thread {

		private Socket socket;
		private int time = 3000;
		private int lastProcessed = 0;
		private double alpha = 2.0 / (20 + 1);
		private double speedEMA20 = 0;
		private HashMap<Long,WorkerData> workerData = new HashMap<Long,WorkerData>();

		public Listener(Socket socket) {
			super();
			this.socket = socket;
		}

		@SuppressWarnings("unchecked")
		public void run() {
			new TimeToUpdate().start();
			while (true) {
				if(!connected) {
					break;
				} else {
					try {
						ConnectionType ct = (ConnectionType) in.readObject();
						switch (ct) {
						case FULL_UPDATE:
	
							int clientSelecteRow = jTableClient.getSelectedRow();
							int workerSelecteRow = jTableWorker.getSelectedRow();
	
							Long newServerTime = (Long) in.readObject();
							Long elapsedTime = (currentServerTime == null) ? 1
									: newServerTime - currentServerTime;
							currentServerTime = newServerTime;
							serverTime.setText("Server Time: "
									+ new Time(currentServerTime - 3600000)
											.toString());
							workerDataVector = (Hashtable<Long, WorkerData>) in
									.readObject();
							clientDataVector = (Hashtable<Long, ClientData>) in
									.readObject();
	
							if (workerDataVector.keySet() != null
									&& !workerDataVector.keySet().isEmpty()) {
								workerKeys = workerDataVector.keySet().toArray();
								numberOfWorkers.setText(String
										.valueOf(workerDataVector.size()));
	
								int totalProcessed = 0;
								int numCores = 0;
								int numIdle = 0;
								
								for (WorkerData wd : workerDataVector.values()) {
									
									workerData.put(wd.getId(), wd);
									
									numCores += wd.getNumberOfProcessors();
									if (wd.getNumberOfRequestedTasks() < wd.getNumberOfProcessors()) {
										numIdle += wd.getNumberOfProcessors()
												- wd.getNumberOfRequestedTasks();
									}
								}
								
								for(WorkerData wd : workerData.values())
									totalProcessed+= wd.getNumberOfTasksProcessed();
								
								if (lastProcessed == 0) {
									lastProcessed = totalProcessed;
								}
								
								processedTasks += totalProcessed - lastProcessed;
	
								numberOfProcessedTasks.setText(String.valueOf(processedTasks));
								
								speed = ((int) ((totalProcessed - lastProcessed) * 1000000.0 / elapsedTime)) / 1000.0;
								
								cores = numCores;
								idle = numIdle;
								
								speedGraph.addData(speed);
								coresGraph.addData((double)numCores);
								idleGraph.addData((double)numIdle);
	
								speedEMA20 = speedEMA20 + alpha
										* (speed - speedEMA20);
								averageSystemSpeed
										.setText(String
												.valueOf(((int) (speedEMA20 * 1000)) / 1000.0)
												+ " ("
												+ String.valueOf(speed)
												+ ")");
	
								lastProcessed = totalProcessed;
								workerTableModel.fireTableDataChanged();
								if (workerSelecteRow >= 0
										&& workerSelecteRow < workerTableModel
												.getRowCount()) {
									jTableWorker.setRowSelectionInterval(
											workerSelecteRow, workerSelecteRow);
								}
	
							} else {
								workerKeys = null;
							}
							int numPending = 0;
	
							if (clientDataVector.keySet() != null
									&& !clientDataVector.keySet().isEmpty()) {
								clientKeys = clientDataVector.keySet().toArray();
								numberOfClients.setText(String
										.valueOf(clientDataVector.size()));
								for (ClientData cd : clientDataVector.values()) {
	
									numPending += cd.getTaskCounter()
											- cd.getTotalNumberOfTasksDone();
								}
	
								clientTableModel.fireTableDataChanged();
	
								if (clientSelecteRow >= 0
										&& clientSelecteRow < clientTableModel
												.getRowCount()) {
									jTableClient.setRowSelectionInterval(
											clientSelecteRow, clientSelecteRow);
								}
							} else {
								clientKeys = null;
							}
							
							tasks = numPending;
	
							pendingGraph.addData((double)numPending);
	
							break;
	
						default:
							break;
						}
	
					} catch (Exception e) {
						connected = false;
						e.printStackTrace();
					}
				}
			}
			while(!connected) {
				connect();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private class TimeToUpdate extends Thread {

			public TimeToUpdate() {
				super();
			}

			public void run() {
				while (true) {
					if(!connected)
						break;
					try {
						synchronized (out) {
							out.writeObject(ConnectionType.FULL_UPDATE);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						connected = false;
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void gui() {
		this.setSize(1000, 400);
		Container container = getContentPane();
		container.add(tabbedPane);

		JPanel inputPanel = new JPanel();
		inputPanel.add(new JLabel(" ", SwingConstants.RIGHT));

		inputWorker = new JTextField(5);
		inputClient = new JTextField(5);
		inputWorker.setEditable(false);
		inputClient.setEditable(false);

		serverTime = new JLabel("                 ");

		kickWorker = new JButton("Kick Worker");
		kickWorker.addActionListener(this);
		
		killWorker = new JButton("Kill Worker");
		killWorker.addActionListener(this);

		kickClient = new JButton("Kick Client");
		kickClient.addActionListener(this);

		JPanel buttonPanel1 = new JPanel();
		JPanel buttonPanel2 = new JPanel();

		buttonPanel1.add(inputWorker);
		buttonPanel1.add(kickWorker);
		buttonPanel1.add(killWorker);

		buttonPanel2.add(inputClient);
		buttonPanel2.add(kickClient);

		workerTableModel = new WorkerTableModel();
		clientTableModel = new ClientTableModel();
		jTableWorker = new JTable(workerTableModel);
		jTableWorker.setAutoCreateRowSorter(true);
		jTableClient = new JTable(clientTableModel);
		jTableClient.setAutoCreateRowSorter(true);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				jTableWorker.getModel());
		jTableWorker.setRowSorter(sorter);

		jTableWorker.setShowGrid(true);

		JPanel buttonPanelNorth = new JPanel();

		connect = new JButton("connect");
		connect.addActionListener(this);

		numberOfWorkers = new JLabel("   ");
		numberOfClients = new JLabel("   ");

		numberOfProcessedTasks = new JLabel("   ");
		averageSystemSpeed = new JLabel("   ");

		buttonPanelNorth.add(serverTime);
		buttonPanelNorth.add(new JLabel(" N Workers:"));
		buttonPanelNorth.add(numberOfWorkers);
		buttonPanelNorth.add(new JLabel(" N Clients:"));
		buttonPanelNorth.add(numberOfClients);
		buttonPanelNorth.add(new JLabel(" Processed Tasks:"));
		buttonPanelNorth.add(numberOfProcessedTasks);
		buttonPanelNorth.add(new JLabel(" System Speed:"));
		buttonPanelNorth.add(averageSystemSpeed);

		JPanel status = new JPanel(new BorderLayout());
		container.add(buttonPanelNorth, BorderLayout.NORTH);
		JPanel graphs = new JPanel(new GridLayout(2, 2));
		status.add(graphs);

		JPanel speed = new JPanel(new BorderLayout());
		speed.add(speedGraph);
		speed.add(makeTimeRangeButtons(speedGraph, "System Speed  -  "),
				BorderLayout.NORTH);
		graphs.add(speed);
		speed.setBorder(BorderFactory.createLoweredBevelBorder());

		JPanel cores = new JPanel(new BorderLayout());
		cores.add(
				makeTimeRangeButtons(coresGraph,
						"Number of available cores   -   "), BorderLayout.NORTH);
		cores.add(coresGraph);
		graphs.add(cores);
		cores.setBorder(BorderFactory.createLoweredBevelBorder());

		JPanel idle = new JPanel(new BorderLayout());
		idle.add(
				makeTimeRangeButtons(idleGraph, "Number of idle cores   -   "),
				BorderLayout.NORTH);
		idle.add(idleGraph);
		graphs.add(idle);
		idle.setBorder(BorderFactory.createLoweredBevelBorder());

		JPanel pending = new JPanel(new BorderLayout());

		pending.add(
				makeTimeRangeButtons(pendingGraph,
						"Number of tasks ready to be done   -   "),
				BorderLayout.NORTH);
		pending.add(pendingGraph);
		graphs.add(pending);
		
		pending.setBorder(BorderFactory.createLoweredBevelBorder());

		JPanel workers = new JPanel(new BorderLayout());
		JPanel clients = new JPanel(new BorderLayout());

		workers.add(buttonPanel1, BorderLayout.SOUTH);
		workers.add(new JScrollPane(jTableWorker));

		clients.add(buttonPanel2, BorderLayout.SOUTH);
		clients.add(new JScrollPane(jTableClient));

		tabbedPane.addTab("Status", status);
		tabbedPane.add("Workers", workers);
		tabbedPane.add("Clients", clients);

		jTableWorker.setDefaultRenderer(WorkerStatus.class, new ColorRenderer(
				true));
		jTableWorker.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					Point p = e.getPoint();

					int row = jTableWorker.rowAtPoint(p);
					inputWorker.setText(jTableWorker.getValueAt(row, 0)
							.toString());
				}
			}
		});

		jTableClient.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					Point p = e.getPoint();

					int row = jTableClient.rowAtPoint(p);
					inputClient.setText(jTableClient.getValueAt(row, 0)
							.toString());

				}
			}
		});

	}

	private JPanel makeTimeRangeButtons(GraphingData graph, String title) {
		JPanel panel = new JPanel();
		JRadioButton minute1 = new JRadioButton("1 min");
		minute1.setActionCommand("20");
		
		JRadioButton minute5 = new JRadioButton("10 min");
		minute5.setActionCommand("200");

		JRadioButton minute10 = new JRadioButton("100 min");
		minute10.setActionCommand("2000");
		minute10.setSelected(true);

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(minute1);
		group.add(minute5);
		group.add(minute10);

		JTextField scale = new JTextField("     ");

		class ActionL implements ActionListener {
			private GraphingData graph;
			private JTextField scale;

			public ActionL(GraphingData graph, JTextField scale) {
				super();
				this.graph = graph;
				this.scale = scale;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					graph.setShowLast(Integer.valueOf(scale.getText().trim()));
				} catch (NumberFormatException e1) {
					graph.setShowLast(Integer.valueOf(e.getActionCommand()));
				}
			}
		}
		
		ActionL actionL = new ActionL(graph, scale);

		// Register a listener for the radio buttons.
		minute1.addActionListener(actionL);
		minute5.addActionListener(actionL);
		minute10.addActionListener(actionL);
		scale.addActionListener(actionL);

		panel.add(new JLabel(title));
		panel.add(minute1);
		panel.add(minute5);
		panel.add(minute10);
		panel.add(scale);

		return panel;
	}

	class updater extends Thread {

		public void run() {
			while (true) {
				OperatingSystemMXBean OSMXBean = ManagementFactory
						.getOperatingSystemMXBean();
				String str = String.valueOf(Math.round(OSMXBean
						.getSystemLoadAverage()));
				System.out.println(str);

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();

		if (source.equals(kickWorker)) {
			String workerToKill = inputWorker.getText();
			if (!workerToKill.isEmpty())
				kickWorker(Long.parseLong(workerToKill));

		} else if (source.equals(killWorker)) {
			String workerToKill = inputWorker.getText();
			if (!workerToKill.isEmpty())
				killWorker(Long.parseLong(workerToKill));

		}else if (source.equals(kickClient)) {
			String clientToKill = inputClient.getText();
			if (!clientToKill.isEmpty())
				killClient(Long.parseLong(clientToKill));

		}
	}
	
	private void kickWorker(long id) {
		synchronized (out) {
			try {
				out.writeObject(ConnectionType.KICK_WORKER);
				out.writeObject(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void killWorker(long id) {
		synchronized (out) {
			try {
				out.writeObject(ConnectionType.KILL_WORKER);
				out.writeObject(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void killClient(long id) {
		synchronized (out) {
			try {
				out.writeObject(ConnectionType.KILL_CLIENT);
				out.writeObject(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class WorkerTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private static final int GREE_VALUE = 5;
		private static final int YELLOW_VALUE = 10;
		private static final int ORANGE_VALUE = 15;
		private static final int RED_VALUE = 20;

		@Override
		public int getColumnCount() {
			return 13;
		}

		@Override
		public int getRowCount() {
			return workerDataVector.size();
		}

		public String getColumnName(int x) {
			switch (x) {
			case 0:
				return "ID";
			case 1:
				return "IP";
			case 2:
				return "Port";
			case 3:
				return "Nr Cores";
			case 4:
				return "O.S.";
			case 5:
				return "Farming Time";
			case 6:
				return "Tasks Processed";
			case 7:
				return "Average time/Task";
			case 8:
				return "Running Time";
			case 9:
				return "# TODO";
			case 10:
				return "Speed";
			case 11:
				return "Speed/Core";
			case 12:
				return "STATUS";
			}
			return "Unknown";
		}

		@Override
		public Object getValueAt(int y, int x) {
			WorkerData workerData = workerDataVector
					.get(((Long) workerKeys[workerDataVector.size() - y - 1]));

			switch (x) {
			case 0:
				return workerData.getId();
			case 1:
				return workerData.getWorkerAddress();
			case 2:
				return workerData.getWorkerPort();
			case 3:
				return workerData.getNumberOfProcessors();
			case 4:
				return workerData.getOperatingSystem();
			case 5:
				Time time = new Time(
						(long) workerData.getTotalTimeSpentFarming() - 3600000);
				return time.toString();
			case 6:
				return workerData.getNumberOfTasksProcessed();
			case 7:
				return ((long) workerData.getAverageTimePerTask()) / 1000.0
						+ " s";
			case 8:
				time = new Time(currentServerTime
						- (long) workerData.getStartTime() - 3600000);
				return time.toString();
			case 9:
				return workerData.getNumberOfRequestedTasks();
			case 10:
				return (int) (workerData.getNumberOfTasksProcessed()
						/ ((currentServerTime - workerData.getStartTime()) / 1000.0) * 1000) / 1000.0;
			case 11:
				return ((workerData.getNumberOfTasksProcessed()
						/ ((currentServerTime - workerData.getStartTime()) / 1000.0) * 1000) / 1000.0 / workerData
							.getNumberOfProcessors());
			case 12:
				int lastRefresh = workerData.getTimeSinceLastTask();
				Color color;
				if (lastRefresh <= 25) {
					color = new Color(Math.min(255, 10 * lastRefresh), 255, 0);
				} else if (lastRefresh <= 51) {
					color = new Color(255, Math.min(255,
							255 - (10 * lastRefresh - 255)), 0);
				} else {
					color = new Color(Math.max(0, 255 - 10 * lastRefresh + 2
							* 255), 0, 0);
				}
				return new WorkerStatus(color, lastRefresh);
			}
			return "Unknown";
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	}

	public class ColorRenderer extends JLabel implements TableCellRenderer {
		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table, Object ws,
				boolean isSelected, boolean hasFocus, int row, int column) {
			WorkerStatus workerStatus = (WorkerStatus) ws;
			Color newColor = workerStatus.getColor();
			setHorizontalAlignment(CENTER);
			setText(workerStatus.getTime() + "");
			setBackground(newColor);
			setForeground(new Color(255 - newColor.getRed(),
					255 - newColor.getGreen(), 255 - newColor.getBlue()));
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2, 5,
								2, 5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,
								5, 2, 5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}
			return this;
		}
	}

	class ClientTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getColumnCount() {
			return 9;
		}

		@Override
		public int getRowCount() {
			return clientDataVector.size();
		}

		public String getColumnName(int x) {
			switch (x) {
			case 0:
				return "ID";
			case 1:
				return "IP";
			case 2:
				return "Desc";
			case 3:
				return "Priority";
			case 4:
				return "Running Time";
			case 5:
				return "Average Speed";
			case 6:
				return "Task Counter";
			case 7:
				return "Tasks Done";
			case 8:
				return "ETA";
			}
			return "Unknown";
		}

		@Override
		public Object getValueAt(int y, int x) {
			ClientData clientData = clientDataVector
					.get((Long) clientKeys[clientDataVector.size() - y - 1]);
			switch (x) {
			case 0:
				return clientData.getId();
			case 1:
				return clientData.getIpAdress();
			case 2:
				return clientData.getDesc();
			case 3:
				return clientData.getClientPriority();
			case 4:
				return new Time(currentServerTime - clientData.getStartTime()
						- 3600000);
			case 5:
				return (int) (clientData.getTotalNumberOfTasksDone()
						/ ((currentServerTime - clientData.getStartTime()) / 1000.0) * 1000) / 1000.0;
			case 6:
				return clientData.getTaskCounter();
			case 7:
				return clientData.getTotalNumberOfTasksDone();
			case 8:
				int eta = (int) ((clientData.getTotalNumberOfTasks() - clientData
						.getTotalNumberOfTasksDone())
						/ (clientData.getTotalNumberOfTasksDone()
								/ ((currentServerTime - clientData
										.getStartTime()) *1.0)));
				return  new Time(eta- 3600000);
			}
			return "Unknown";
		}
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public int getCores() {
		return cores;
	}
	
	public int getTasks() {
		return tasks;
	}
	
	public int getIdle() {
		return idle;
	}
	
	public Hashtable<Long, WorkerData> getWorkerDataVector() {
		return workerDataVector;
	}
	
	public Hashtable<Long, ClientData> getClientDataVector() {
		return clientDataVector;
	}

	public void setServer(String serverName) {
		if (serverName != null) {
			serverAddress = serverName;
		}
	}
}