package benchmark;

import result.Result;

public class SimpleBenchmarkResult extends Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4409630848597315686L;

	public SimpleBenchmarkResult(int result) {
		super();
		this.result = result;
	}
	
	public double getResult() {
		return result;
	}
	
	public int result;
}
