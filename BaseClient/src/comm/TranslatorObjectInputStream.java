package comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class TranslatorObjectInputStream extends ObjectInputStream {

	private ClassLoader classLoader;

	public TranslatorObjectInputStream(InputStream in) throws IOException,
			SecurityException {
		super(in);
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		if (classLoader != null) {
			return classLoader.loadClass(desc.getName());
		} else {
			return super.resolveClass(desc);
		}
	}

}
