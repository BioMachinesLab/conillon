package result;

public class ClassNameRequest implements Request {
	private String name;
	private long hashcode;
	private int classProvider;

	public ClassNameRequest(String name, long hashcode) {
		super();
		this.name = name;
		this.hashcode = hashcode;
	}

	public int getClassProvider() {
		return classProvider;
	}

	public void setClassProvider(int classProvider) {
		this.classProvider = classProvider;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (hashcode ^ (hashcode >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ClassNameRequest other = (ClassNameRequest) obj;
		if (hashcode != other.hashcode)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public long getHashcode() {
		return hashcode;
	}
	
	
}
