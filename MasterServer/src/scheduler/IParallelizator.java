/**
 * 
 */
package scheduler;

import java.util.LinkedList;

import comm.ClientPriority;
import tasks.CompletedTask;
import tasks.TaskDescription;
import worker.WorkerData;

/**
 * @author Simão Fernandes
 *
 */
public interface IParallelizator {
	/**
	 * 
	 * @param task
	 * @return
	 */
	LinkedList<Long> taskDone(CompletedTask task);
	/**
	 * 
	 * @param workerData
	 * @return
	 * @throws InterruptedException
	 */
	TaskDescription getTask(WorkerData workerData) throws InterruptedException;
	/**
	 * Never used
	 * @return
	 */
	int getNumberOfSendedTasks();
	/**
	 * 
	 * @param task
	 */
	void addToResend(TaskDescription task);
	/**
	 * 
	 * @param taskList
	 * @param clientID
	 * @param priority
	 */
	void addTaskList(LinkedList<TaskDescription> taskList, int clientID, ClientPriority priority);
	/**
	 * 
	 * @param task
	 */
	void addTask(TaskDescription task);
	/**
	 * 
	 * @param clientID
	 */
	void removeClientTasks(long clientID);	
	/**
	 * 
	 * @param id
	 */
	void removeWorker(long id);
}
