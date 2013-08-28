package comm.translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;

public class Utils {

	private static String PATH_SEPARATOR = System.getProperty("file.separator");

	public static boolean nonSystemName(String name, String packageName) {
		// System.out.println(name);
		// if(name.startsWith("__")){
		// System.out.println(name + " ----- " + packageName);
		// }
		return !name.startsWith("sun.") && !name.startsWith("java.")
				&& !name.equals(tasks.Task.class.getName())
				&& !name.equals(result.Result.class.getName())
				&& !name.equals(comm.FileProvider.class.getName())
				&& !name.equals(comm.ClientPriority.class.getName())
				&& !name.startsWith(packageName) && nonPrimitiveName(name);
	}

	public static boolean nonSystemName(String name) {
		// System.out.println(name);
		return !name.startsWith("sun.") && !name.startsWith("java.")
				&& !name.equals(tasks.Task.class.getName())
				&& !name.equals(result.Result.class.getName())
				&& !name.equals(comm.FileProvider.class.getName())
				&& !name.equals(comm.ClientPriority.class.getName())
				&& nonPrimitiveName(name);
	}

	public static boolean nonPrimitiveName(String name) {
		// System.out.println(name);
		return !name.equals(Long.class.getName())
				&& !name.equals(Integer.class.getName())
				&& !name.equals(Character.class.getName())
				&& !name.equals(Double.class.getName())
				&& !name.equals(Float.class.getName())
				&& !name.equals(Short.class.getName())
				&& !name.equals(Boolean.class.getName())
				&& !name.equals(String.class.getName())
				&& !name.equals(void.class.getName()) && !name.equals("long")
				&& !name.equals("J") && !name.equals("int")
				&& !name.equals("I") && !name.equals("char")
				&& !name.equals("C") && !name.equals("double")
				&& !name.equals("D") && !name.equals("float")
				&& !name.equals("F") && !name.equals("short")
				&& !name.equals("S") && !name.equals("boolean")
				&& !name.equals("Z") && !name.equals("void")
				&& !name.equals("V") && !name.equals("byte")
				&& !name.equals("B") && !name.startsWith("java.lang");
	}

	public static ClientTaskClassInformation getClassIn(String name) {
		HashMap<String, Long> referencedFiles = new HashMap<String, Long>();

		try {
			referencedFiles.put(name.replaceAll("/", "."),
					getClassHashCode(name));
			getClassIn(name, referencedFiles);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long clientHash = 0;
		for (Long hash : referencedFiles.values()) {
			clientHash += hash;
		}
		return new ClientTaskClassInformation(clientHash, name, referencedFiles);
	}

	private static void getClassIn(String name,
			HashMap<String, Long> referencedFiles)
			throws ClassNotFoundException {

		Set<String> classNameList = getClassNamesIn(name);

		Iterator<String> iteratorNames = classNameList.iterator();
		while (iteratorNames.hasNext()) {
			String className = iteratorNames.next();
			if (!referencedFiles.containsKey(className)) {
				referencedFiles.put(className, getClassHashCode(className));
			} else {
				iteratorNames.remove();
			}
		}
		iteratorNames = classNameList.iterator();

		while (iteratorNames.hasNext()) {
			getClassIn(iteratorNames.next(), referencedFiles);

		}
	}

	private static Set<String> getClassNamesIn(String name)
			throws ClassNotFoundException {

		byte[] b = Utils.getBytesFromClass(name);

		ClassReader cr = new ClassReader(b);
		// ClassWriter cw = new ClassWriter(cr, 0);
		// CheckClassAdapter cca = new CheckClassAdapter(cw);

		ClassNameGetter ca = new ClassNameGetter();
		cr.accept(ca, 0);
		// cw.toByteArray();

		return ca.getClassNames();

		// Class workClass = Class.forName(name);
		//
		// // super class
		// String className = workClass.getSuperclass().getName();
		// if (nonSystemName(name)) {
		// classNames.add(name);
		// }
		//
		// //interfaces
		// for (Class<?> inter : workClass.getInterfaces()) {
		// className = inter.getName();
		// if (nonSystemName(name)) {
		// classNames.add(name);
		// }
		// }
		//
		//
		// // fields
		// for (Field field : workClass.getDeclaredFields()) {
		// className = field.getName();
		// if (nonSystemName(name)) {
		// classNames.add(name);
		// }
		// }
		//
		// //methods
		// for(Method method: workClass.getDeclaredMethods()){
		// //return type
		// className = method.getReturnType().getName();
		// if (nonSystemName(name)) {
		// classNames.add(name);
		// }
		//
		// //parameters
		// for(Class<?> parameter: method.getParameterTypes()){
		// className = parameter.getName();
		// if (nonSystemName(name)) {
		// classNames.add(name);
		// }
		// }
		//
		// //local variables
		// method.get
		//
		//
		// //execeptions
		// for(Class<?> exceptions: method.getExceptionTypes()){
		// className = exceptions.getName();
		// if (nonSystemName(name)) {
		// classNames.add(name);
		// }
		// }
		//
		//
		// }

	}

	public static byte[] getBytesFromClass(String className)
			throws ClassNotFoundException {

		System.out.println("get bytes for " + className);		
		String classAsPath = className.replace('.', '/') + ".class";
		InputStream stream = ClassLoader.getSystemClassLoader()
				.getResourceAsStream(classAsPath);

		// File file = new File(className);
		// if (!file.exists()) {
		// file = new File("bin" + PATH_SEPARATOR
		// + className.replace(".", PATH_SEPARATOR) + ".class");
		// }
		// byte[] classData = null;
		// if (file.exists()) {
		// try {
		// return getBytesFromFile(file);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		if (stream != null) {
			try {
				return IOUtils.toByteArray(stream);
				// getBytesFromFile(stream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new ClassNotFoundException(className);
		}
		return null;

	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	public static String convertName(String className, String newPackageName) {
		if (nonSystemName(className.replaceAll("/", "."), newPackageName)) {
			className = newPackageName + className;
		}
		return className;
	}

	public static long getClassHashCode(String className)
			throws ClassNotFoundException {
		byte[] m = getBytesFromClass(className);
		long hashCode = 0;
		for (byte b : m) {
			hashCode += b;
		}
		return hashCode;
	}
	public static long getFileHashCode(String fileName)
			throws ClassNotFoundException, IOException {
		File file = new File(fileName);
		byte[] m = getBytesFromFile(file);
		long hashCode = 0;
		for (byte b : m) {
			hashCode += b;
		}
		return hashCode;
	}

	// public static long getClientHashCode(){
	//
	// }

	// public static String getClassFullName(String className) {
	// List<String> names = searchFullNameInPath(className);
	// if (names.size() == 0) {
	// throw new RuntimeException("Class not found " + className);
	// } else if (names.size() > 1) {
	// throw new RuntimeException("Multiple implementations of class: "
	// + className + " - " + names);
	// }
	// return names.get(0);
	// }

	public static ClientTaskClassInformation getClassInClassPath() {
		HashMap<String, Long> referencedFiles = new HashMap<String, Long>();
		List<String> classes = null;
		try {
			classes= searchAllClassNamesInPath();
			for (String name : classes) {

				referencedFiles.put(name.replaceAll("/", "."),
						getFileHashCode(name));
				//getClassIn(name, referencedFiles);

			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long clientHash = 0;
		for (Long hash : referencedFiles.values()) {
			clientHash += hash;
		}
		return new ClientTaskClassInformation(clientHash, referencedFiles);
	}

	public static List<String> searchAllClassNamesInPath() throws IOException {

		ArrayList<String> classNames = new ArrayList<String>();
		String classpath = System.getProperty("java.class.path");

		StringTokenizer tokenizer = new StringTokenizer(classpath,
				File.pathSeparator);
		String token;
		File dir;
		String name;
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			dir = new File(token);
			if (dir.isDirectory()) {
				lookForNamesInDirectory(dir, classNames);
			} else if (dir.isFile()) {
				name = dir.getName().toLowerCase();
				if (name.endsWith(".jar")) {
					classNames.add(dir.getCanonicalPath());
				}
			}
		}
		return classNames;
	}

	/**
	 * @param name
	 *            Name of to parent directories in java class notation (dot
	 *            separator)
	 * @param dir
	 *            Directory to be searched for classes.
	 */
	private static void lookForNamesInDirectory(File dir,
			ArrayList<String> classNames) {
		File[] files = dir.listFiles();

		final int size = files.length;
		for (int i = 0; i < size; i++) {
			File file = files[i];

			if (file.isDirectory()) {
				lookForNamesInDirectory(file, classNames);
			} else {
				String fileName = file.getAbsolutePath();
				if (fileName.endsWith(".class")) {
					classNames.add(fileName);
				}

			}
		}
	}

	/**
	 * Search archive files for required resource.
	 * 
	 * @param archive
	 *            Jar or zip to be searched for classes or other resources.
	 */
	private static void lookForNamesInArchive(File archive,
			ArrayList<String> classNames) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(archive);
		} catch (IOException e) {
			return;
		}
		Enumeration entries = jarFile.entries();
		JarEntry entry;
		String entryName;
		while (entries.hasMoreElements()) {
			entry = (JarEntry) entries.nextElement();
			entryName = entry.getName();
			if (entryName.toLowerCase().endsWith(".class")) {
				try {
					entryName = entryName.replace('/', '.');
					classNames.add(entryName);
				} catch (Throwable e) {
				}
			}
		}
	}

}
