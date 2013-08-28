package result;

public class FileRequest {
	private int problemNumber;
	private int version;
	private String name;

	public FileRequest(int problemNumber, int version, String name) {
		super();
		this.problemNumber = problemNumber;
		this.version = version;
		this.name = name;
	}

	public int getProblemNumber() {
		return problemNumber;
	}

	public int getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + problemNumber;
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileRequest other = (FileRequest) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (problemNumber != other.problemNumber)
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassRequest [problemNumber=" + problemNumber + ", version="
				+ version + ", name=" + name + "]";
	}

}
