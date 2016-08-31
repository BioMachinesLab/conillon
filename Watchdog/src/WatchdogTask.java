import result.Result;
import tasks.Task;


public class WatchdogTask extends Task{

	@Override
	public void run() {
		System.out.println("We're running fine!");
	}

	@Override
	public Result getResult() {
		return new WatchdogResult(this.getId());
	}

}
