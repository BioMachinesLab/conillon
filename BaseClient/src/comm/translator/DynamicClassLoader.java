package comm.translator;


public class DynamicClassLoader extends ClassLoader {
	private DynamicTranslator translator;

	public DynamicClassLoader(DynamicTranslator translator) {
		super();
		this.translator = translator;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] classBytes = translator.getbyteCodeForClass(name);
		return defineClass(name, classBytes, 0, classBytes.length);
	}

}
