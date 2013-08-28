package comm;

import result.ClassNameRequest;
import result.Result;

import comm.translator.ASMTranslator;

public class ClassId extends Result{
	private String name;
	private long hashCode;
	private int classProvider;
	
	public ClassId(String name, long hashCode) {
		super();
		this.name = name;
		this.hashCode = hashCode;
	}
	
	public ClassId(ClassNameRequest request) {
		this.name = request.getName();
		this.classProvider = request.getClassProvider();
		this.hashCode = request.getHashcode();
	}

	public void setClassProvider(int classProvider) {
		this.classProvider = classProvider;
	}

	public String getName() {
		return name;
	}

	public long getHashCode() {
		return hashCode;
	}

	public int getClassProvider() {
		return classProvider;
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (hashCode ^ (hashCode >>> 32));
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
		ClassId other = (ClassId) obj;
		if (hashCode != other.hashCode)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getPackageName() {
		return ASMTranslator.newPackageName+hashCode+"/";
	}
	
	
}
