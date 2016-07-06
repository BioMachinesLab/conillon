package benchmark;

public class BenchmarkOutput {
		
	private long sumElapsedTime = 0;
	private int nbInputs = 0;

	public synchronized void addResult(long result) {
		nbInputs++;
		sumElapsedTime += result;
	}
	
	public long getAverageElapsedTime() {
		return this.nbInputs != 0 ?
				sumElapsedTime / nbInputs :
				0;
	}
	
	public int getNbInputs() {
		return nbInputs;
	}
}
