package comm.translator;


public interface Translator {

	public Object duplicate(Object instance) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InterruptedException;

	public Object revert(Object instance) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, NegativeArraySizeException, InterruptedException;

	public byte[] getByteCodeForClass(String name) throws InterruptedException, ClassNotFoundException;

	public ClassLoader getClassLoader();
}
