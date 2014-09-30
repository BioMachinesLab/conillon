package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Time;
import java.util.Hashtable;

import worker.WorkerData;
import client.ClientData;

public class DataExport extends Thread {
	
	private Gui gui;
	
	public DataExport(Gui gui) {
		this.gui = gui;
	}
	
	@Override
	public void run() {

		try {
			//wait until we have the first data points
			Thread.sleep(10*1000);
		} catch(Exception e) {}
		
		int times = 0;
		
		while(true) {
			try {
				String url = "http://miguelduarte.pt/conilon/put_data.php";
				String charset = "UTF-8";
			
				
				String query = String.format("stats=%s&idle=%s&tasks=%s&cores=%s&speed=%s",
						URLEncoder.encode("1", charset),
						URLEncoder.encode(""+gui.getIdle(), charset),
						URLEncoder.encode(""+gui.getTasks(), charset),
						URLEncoder.encode(""+gui.getCores(), charset),
						URLEncoder.encode(""+gui.getSpeed(), charset));
				
				URLConnection connection = new URL(url).openConnection();
				connection.setDoOutput(true); // Triggers POST.
				connection.setRequestProperty("Accept-Charset", charset);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
				OutputStream output = null;

				Hashtable<Long, ClientData> clientDataVector = gui.getClientDataVector();
				Hashtable<Long, WorkerData> workerDataVector = gui.getWorkerDataVector();
				
				if(times++ % 6 != 0)
					query = "";
				
				query+= "&clients={\"clients\": [";
				//id`,`ip`,`desc`,`time`,`speed`,`task_counter`,`tasks_done`,`eta`
				int i = 0;
				for(ClientData d : clientDataVector.values()) {
					if(i > 0)
						query+=",";
					int eta = (int) ((d.getTotalNumberOfTasks() - d
							.getTotalNumberOfTasksDone())
							/ (d.getTotalNumberOfTasksDone()
									/ ((System.currentTimeMillis() - d.getStartTime()) *1.0)));
					
					query+= String.format("{\"id\": \"%s\",\"ip\": \"%s\",\"desc\": \"%s\",\"time\": \"%s\",\"speed\": \"%s\",\"task_counter\": \"%s\",\"tasks_done\": \"%s\",\"eta\": \"%s\"}",
							URLEncoder.encode(""+d.getId(), charset),
							URLEncoder.encode(d.getIpAdress(), charset),
							URLEncoder.encode(d.getDesc(), charset),
							URLEncoder.encode(""+new Time(System.currentTimeMillis() - d.getStartTime() - 3600000), charset),
							URLEncoder.encode(""+(d.getTotalNumberOfTasksDone() / ((System.currentTimeMillis() - d.getStartTime()) / 1000.0) * 1000) / 1000.0, charset),
							URLEncoder.encode(""+d.getTaskCounter(), charset),
							URLEncoder.encode(""+d.getTotalNumberOfTasksDone(), charset),
							URLEncoder.encode(""+new Time(eta- 3600000), charset));
					i++;
				}
				
				query+="]}";
				
				query+= "&workers={\"workers\": [";
				//workers={"workers":[{"id":1,"ip":"1","cores":"1","os":"1","time_task":"1","running_time":"1","tasks_done":"1","todo":"1","status":"1"}]}
				i = 0;
				for(WorkerData d : workerDataVector.values()) {
					if(i > 0)
						query+=",";
					
					String timeTask = ""+(d.getAverageTimePerTask() / 1000.0);
					
					if(timeTask.equals("NaN"))
						timeTask = "0";
					
					query+= String.format("{\"id\": \"%s\",\"ip\": \"%s\",\"cores\": \"%s\",\"os\": \"%s\",\"time_task\": \"%s\",\"running_time\": \"%s\",\"tasks_done\": \"%s\",\"todo\": \"%s\",\"status\": \"%s\"}",
							URLEncoder.encode(""+d.getId(), charset),
							URLEncoder.encode(d.getWorkerAddress(), charset),
							URLEncoder.encode(""+d.getNumberOfProcessors(), charset),
							URLEncoder.encode(d.getOperatingSystem(), charset),
							URLEncoder.encode(timeTask,charset),
							URLEncoder.encode(""+new Time((long) d.getStartTime() - 3600000), charset),
							URLEncoder.encode(""+d.getNumberOfTasksProcessed(), charset),
							URLEncoder.encode(""+d.getNumberOfRequestedTasks(), charset),
							URLEncoder.encode(""+d.getTimeSinceLastTask(), charset));
					i++;
				}
				
				query+="]}";
				
//				query = URLEncoder.encode(query,charset);
//				System.out.println(query);
				try {
				     output = connection.getOutputStream();
				     output.write(query.getBytes(charset));
				     InputStream input = connection.getInputStream();
				     
				     BufferedReader br = new BufferedReader(new InputStreamReader(input));
				     String s = "";
				     do{
				    	 s = br.readLine();
				    	 System.out.println(s);
				     }
				     while(s != null && !s.isEmpty());
				} finally {
				     if (output != null) try { output.close(); } catch (IOException e) {e.printStackTrace();}
				}				
				sleep(10*1000);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
