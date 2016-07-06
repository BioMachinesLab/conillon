package logging;

import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import client.ClientData;
import helpers.PropertiesHandler;
import worker.WorkerData;

/**
 * @author simao
 *
 */
public class Logger {

	/**
	 * @param event
	 *            Context event
	 * @param cd
	 *            ClientData instance
	 */
	public void log(Event event, ClientData cd) {

		switch (event) {
		case START: {
			clientDo = new dataaccess.dataobjects.Client();
			clientDo.setId(cd.getId());
			clientDo.setIp(cd.getIpAdress());
			clientDo.setMac_address(cd.getMacAddress());
			clientDo.setHost_name(cd.getHostName());
			dataaccess.DbHandler.InsertNewClient(clientDo);

			// log on file
			//setFile(Entity.CLIENT, event);
			log(Entity.CLIENT, event,
				String.format("%d,%s,%s,%s,%s,%s", 
							  clientDo.getId(), 
							  clientDo.getIp(), 
							  clientDo.getMac_address(),
							  clientDo.getHost_name(),							 
							  cd.getDesc(),
							  getTimestamp()));

			break;
		}
		case STOP: {
			long currentServerTime = new Date().getTime();

			clientDo = new dataaccess.dataobjects.Client();
			clientDo.setId(cd.getId());
			clientDo.setAverage_speed(this.getAverageSpeed(cd.getTotalNumberOfTasksDone(), cd.getStartTime()));
			clientDo.setTotal_running_time(this.getTotalRunningTime(cd.getStartTime()));
			clientDo.setTotal_tasks(cd.getTotalNumberOfTasksDone());
			dataaccess.DbHandler.UpdateClient(clientDo);

			// log on file
			//setFile(Entity.CLIENT, event);
			log(Entity.CLIENT, event,
				String.format("%d,%d,%s,%d,%s", //%1$,.2f
							  clientDo.getId(), 
							  clientDo.getTotal_tasks(),
							  clientDo.getFormattedAverageSpeed(),							  
							  clientDo.getTotal_running_time(), 
							  getTimestamp()
							  ));
			break;
		}
		default:
			throw new UnsupportedOperationException("log Client event");
		}
		// this.log = new File(filePathClient);

	}

	/**
	 * @param event
	 *            Context event
	 * @param wd
	 *            WorkerData instance
	 */
	public long log(Event event, WorkerData wd) {

		long insertedOidWorker = 0;

		switch (event) {
		case START: {
			// write on DB
			workerDo = new dataaccess.dataobjects.Worker();
			// workerDo.setId(wd.getId());
			workerDo.setIp(wd.getWorkerAddress());
			workerDo.setMac_address(wd.getMacAddress());
			workerDo.setHost_name(wd.getHostName());
			workerDo.setNum_cores(wd.getNumberOfProcessors());
			workerDo.setOperative_system(wd.getOperatingSystem());
			workerDo.setPerformance(wd.getPerformance());
			workerDo.setIsBanned(false);
			insertedOidWorker = dataaccess.DbHandler.InsertNewWorker(workerDo);
			wd.setId(insertedOidWorker);

			// write on file
			//setFile(Entity.WORKER, event);
			log(Entity.WORKER, event,
				String.format("%d,%s,%s,%s,%d,%s,%d,%s", 
							  workerDo.getId(),
							  workerDo.getIp(), 							  							  
							  //wd.getWorkerPort(), 
							  workerDo.getMac_address(),
							  workerDo.getHost_name(), 
							  workerDo.getNum_cores(), 
							  workerDo.getOperative_system(),
							  workerDo.getPerformance(),
							  getTimestamp()
							  ));
			return insertedOidWorker;
		}
		case STOP: {

			double avgTime = this.getAverageTimePerTask(wd);
			long totalRunningtime = this.getTotalRunningTime(wd.getStartTime());

			double avgSpeed = this.getAverageSpeed(wd.getNumberOfTasksProcessed(), wd.getStartTime());
			double speed = this.getSpeed(wd.getNumberOfTasksProcessed(), wd.getStartTime());
			double speedBycore = this.getSpeedByCore(wd.getNumberOfTasksProcessed(), wd.getStartTime(),
					wd.getNumberOfProcessors());

			// write on DB
			workerDo = new dataaccess.dataobjects.Worker();
			workerDo.setId(wd.getId());
			workerDo.setMac_address(wd.getMacAddress());
			workerDo.setAverageTime((long)avgTime);
			workerDo.setAverageSpeed(avgSpeed);
			workerDo.setNumberOfTasksProcessed(wd.getNumberOfTasksProcessed());
			workerDo.setTotal_running_time(totalRunningtime);
			dataaccess.DbHandler.UpdateWorkerSession(workerDo);

			// write on file
			//setFile(Entity.WORKER, event);
			log(Entity.WORKER, event,
				String.format("%d,%d,%d,%d,%s,%s", 
							  workerDo.getId(),
							  workerDo.getNumberOfTasksProcessed(),
							  workerDo.getAverageTime(),
							  workerDo.getTotal_running_time(),
							  workerDo.getAverageSpeed(),
							  getTimestamp()
			// speedBycore //speed by core %f
							  
			));

			return 0;
		}
		default:
			throw new UnsupportedOperationException("log Worker event");
		}
	}

	private double getSpeed(long totalProcessedTasks, long startTime) {

		// code @admin
		// Long newServerTime = (Long) in.readObject();
		// Long elapsedTime = (currentServerTime == null) ? 1 : newServerTime -
		// currentServerTime;
		// currentServerTime = newServerTime;

		Long elapsedTime = System.currentTimeMillis() - startTime;
		return ((int) ((totalProcessedTasks) * 1000000.0 / elapsedTime)) / 1000.0;
	}

	private double getAverageSpeed(long numberOfTasksProcessed, long startTime) {
		return (int) (numberOfTasksProcessed / ((new Date().getTime() - startTime) / 1000.0) * 1000) / 1000.0;
	}

	private double getSpeedByCore(long numberTasksProcessed, long startTime, int numberOfProcessors) {
		return (numberTasksProcessed / ((new Date().getTime() - startTime) / 1000.0) * 1000) / 1000.0
				/ numberOfProcessors;
	}

	private long getTotalRunningTime(long startTime) {
		return new Date().getTime() - startTime;
	}

	private double getAverageTimePerTask(WorkerData wd) {
		// return Math.round(wd.getAverageTimePerTask());
		return wd.getAverageTimePerTask();
	}

	private int getNumIdle(int numberOfProcessors, int numberOfRequestedTasks) {
		return numberOfProcessors - numberOfRequestedTasks;
	}


	/**
	 * @param message
	 *            Message to log
	 */
	private void log(Entity entity, Event event, String message) {
		
		PropertiesHandler prop = PropertiesHandler.getInstance();
		String header,
			   loggingPath = prop.getPropValue("logging_path");
		
				
		switch (entity) {
		case WORKER:
			switch (event) {
			case START:
				this.log = new File(String.format("%s/%s", loggingPath, prop.getPropValue("file_path_worker_start")));				
				header = "id,ip,mac_address,host_name,num_cores,operative_system,time_stamp\n";
				break;
			case STOP:
				this.log = new File(String.format("%s/%s", loggingPath, prop.getPropValue("file_path_worker_stop")));
				header = "id,tasks_processed,average_time,total_running_time,average_speed,time_stamp\n";
				break;
			default:
				throw new UnsupportedOperationException();
			}
			break;
		case CLIENT:
			switch (event) {
			case START:
				this.log = new File(String.format("%s/%s", loggingPath, prop.getPropValue("file_path_client_start")));
				header = "id,ip,mac_address,host_name,description,time_stamp\n";
				break;
			case STOP:
				this.log = new File(String.format("%s/%s", loggingPath, prop.getPropValue("file_path_client_stop")));				
				header = "id,taks_submited,average_speed,total_running_time,time_stamp\n";
				
				break;
			default:
				throw new UnsupportedOperationException();
			}
			break;
		default:
			throw new UnsupportedOperationException();
		}
		
		Boolean writeHeader = false;
		
		try {
			
			if (!this.log.exists()) {
				this.log.createNewFile();
				writeHeader = true;
			}
			
			this.fileWritter = new FileWriter(this.log.getAbsoluteFile(), true);
			this.bufferWritter = new BufferedWriter(this.fileWritter);

			if (writeHeader) {
				this.bufferWritter.write(header);
			}
			this.bufferWritter.write(String.format("%s\n", message));

			

		} catch (IOException ioEx) {
			this.handleException(ioEx);
			return;
		} finally {
			try {
				this.bufferWritter.flush();
				this.bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
	}

	/**
	 * @return Formated current timestamp
	 */
	private String getTimestamp() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmmm").format(new Date());
	}

	/**
	 * @param ex
	 *            Exception to be handled
	 */
	private void handleException(Exception ex) {
		System.out.println("Excepcao: " + ex.getMessage());
	}

	/**
	 * @author simao Handled entities
	 */
	public enum Entity {
		WORKER, CLIENT, TASK
	};

	/**
	 * @author simao Handled events
	 */
	public enum Event {
		START, STOP
	};

	protected Logger() {
		this.setDoLog(true);
	}

	public static Logger getInstance() {

		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}

	
	private Boolean doLog;
	public Boolean getDoLog() {
		return doLog;
	}
	public void setDoLog(Boolean doLog) {
		this.doLog = doLog;
	}


	private static Logger instance = null;

	/*private static final String filePathWorkerStart = "/home/simao/dev/conillon.logs/log.worker.start.CSV",
			filePathWorkerStop = "log.worker.stop.CSV", filePathClientStart = "log.client.start.CSV",
			filePathClientStop = "log.client.stop.CSV";
	 */
	File log = null;
	FileWriter fileWritter = null;
	BufferedWriter bufferWritter = null;
	dataaccess.dataobjects.Worker workerDo = null;
	dataaccess.dataobjects.Client clientDo = null;
}
