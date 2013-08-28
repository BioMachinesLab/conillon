package comm.translator;


public class ASMClassLoader extends ClassLoader {

	// private String newPackageName;
	private ASMTranslator translator;

	public ASMClassLoader(ASMTranslator translator) {
		super();
		// this.newPackageName = newPackageName;
		this.translator = translator;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return Class.forName(name);

		} catch (ClassNotFoundException e) {
			if (name.startsWith(ASMTranslator.newPackageName)) {
				try {
					byte[] bytesForClass = translator.getByteCodeForClass(name);
					return defineClass(name, bytesForClass, 0,
							bytesForClass.length);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			return null;
		}
	}
}
