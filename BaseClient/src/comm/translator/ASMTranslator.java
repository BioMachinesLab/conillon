package comm.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import comm.ClassId;
import comm.ClassNameManager;

public class ASMTranslator implements Translator {

	public static String newPackageName = "__";
	private ASMClassLoader classLoader;

	private ArrayList<Object> originals = new ArrayList<Object>();
	private ArrayList<Object> duplicates = new ArrayList<Object>();

	private HashMap<String, byte[]> translatedClasses = new HashMap<String, byte[]>();
	private HashMap<String, ClassId> classIds = new HashMap<String, ClassId>();

	// private HashMap<String, ClientTaskClassInformation> tasksClasses = new
	// HashMap<String, ClientTaskClassInformation>();
	private HashMap<String, String> classPackages = new HashMap<String, String>();

	private ClassNameManager classNameManager;

	private static PrintWriter file;

	public ASMTranslator(String newPackageName,
			ClassNameManager classNameManager) {
		super();
		if (file == null) {
			try {

				file = new PrintWriter(new File("output.log"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// this.newPackageName = newPackageName;
		this.classNameManager = classNameManager;
		classLoader = new ASMClassLoader(this);
	}

	@Override
	public synchronized Object duplicate(Object instance)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, InterruptedException {
		duplicates.clear();
		originals.clear();
		// ClientTaskClassInformation taskClasses = tasksClasses.get(name);
		// if (taskClasses == null){
		// taskClasses = Utils.getClassIn(name);
		// classNameManager.getPackegeName(taskClasses);
		// tasksClasses.put(name, taskClasses);
		// }
		// String packageName = taskClasses.getPackageName();

		return internalDuplicate(instance);
	}

	@Override
	public synchronized Object revert(Object instance)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NegativeArraySizeException,
			InterruptedException {
		duplicates.clear();
		originals.clear();
		return internalRevert(instance);
	}

	private boolean toBeTranslated(String className) {
		return !classIds.containsKey(className)
				&& Utils.nonSystemName(className);
	}

	// private String getPackegeName(String className) {
	// ClassId classId = classIds.get(className);
	// if (classId == null) {
	//
	// }
	// return classId.getPackageName();
	// }

	private Object internalDuplicate(Object instance)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, InterruptedException {

		String className = instance.getClass().getName();
		Object copyObject = null;
		// if (!Utils.nonSystemName(className, packageNameConverted)) {
		if (!Utils.nonPrimitiveName(className)
				|| className.startsWith(newPackageName)) {
			return instance;
		} else if (instance.getClass().isArray()) {
			Class<?> arrayType = getTypeWithPackageName(instance.getClass());
			Object o = Array.newInstance(arrayType, Array.getLength(instance));
			fillArrayWithPackageName(o, instance);
			return o;
		} else {
			int index = originals.indexOf(instance);
			if (index != -1) {
				return duplicates.get(index);
			} else {
				Class<?> copyClass = null;

				if (toBeTranslated(className)) {
					// System.out.println("Need to duplicate " + instance);

					// String packageName = classNameManager
					// .getPackegeName(className);
					String packageName = classPackages.get(className);
					if (packageName == null) {
						packageName = classNameManager
								.getPackegeName(className);
						classPackages.put(className, packageName);
					}

					String packageNameConverted = packageName.replaceAll("/",
							".");

					copyClass = classLoader.loadClass(packageNameConverted
							+ className);

					// System.out.println("Class: " + copyClass + ", Const: "
					// + copyClass.getConstructors());

					try {
						copyObject = copyClass.newInstance();
					} catch (InstantiationException e) {
						try {
							copyObject = instanciateByConstructor(copyClass);
						} catch (InvocationTargetException e1) {
							copyObject = instance;
						}
					}
				} else {
					copyClass = instance.getClass();
					// instance.getClass().
					try {
						Class<?> superClass = instance.getClass();
						Method m = null;
						try {
							m = superClass.getDeclaredMethod("clone");
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
						while (superClass.getSuperclass() != null && m == null) {
							superClass = superClass.getSuperclass();
							try {
								m = superClass.getDeclaredMethod("clone");
							} catch (NoSuchMethodException e) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}

						m.setAccessible(true);
						copyObject = m.invoke(instance, null);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						try {
							copyObject = instanciateByConstructor(copyClass);
						} catch (InvocationTargetException e1) {
							copyObject = instance;
						} catch (SecurityException e1) {
							copyObject = instance;
						} catch (java.lang.IllegalArgumentException e1) {
							copyObject = instance;
						}
					}
				}
				originals.add(instance);
				duplicates.add(copyObject);

				copyFieldsFromToWithPackage(instance, copyObject);
				// System.out.println(instance + " --> " + copyObject);
				return copyObject;

			}
		}

	}

	// private Object internalDuplicate(Object instance)
	// throws ClassNotFoundException, InstantiationException,
	// IllegalAccessException {
	//
	// String className = instance.getClass().getName();
	// Object copyObject = null;
	// if (!Utils.nonPrimitiveName(className)) {
	// return instance;
	// } else if (instance.getClass().isArray()) {
	// Class<?> arrayType = getTypeWithPackageName(instance.getClass());
	// Object o = Array.newInstance(arrayType, Array.getLength(instance));
	// fillArrayWithPackageName(o, instance);
	// return o;
	// } else {
	// int index = originals.indexOf(instance);
	// if (index != -1) {
	// return duplicates.get(index);
	// } else {
	// Class<?> copyClass = null;
	//
	// if (Utils.nonSystemName(className, newPackageName)) {
	// // System.out.println("Need to duplicate " + instance);
	// copyClass = classLoader.loadClass(newPackageName
	// .replaceAll("/", ".") + className);
	//
	// // System.out.println("Class: " + copyClass + ", Const: "
	// // + copyClass.getConstructors());
	//
	// try {
	// copyObject = copyClass.newInstance();
	// } catch (InstantiationException e) {
	// try {
	// copyObject = instanciateByConstructor(copyClass);
	// } catch (InvocationTargetException e1) {
	// copyObject = instance;
	// }
	// }
	// } else {
	// copyClass = instance.getClass();
	// // instance.getClass().
	// try {
	// Class<?> superClass = instance.getClass();
	// Method m = null;
	// try {
	// m = superClass.getDeclaredMethod("clone");
	// } catch (NoSuchMethodException e) {
	// // TODO Auto-generated catch block
	// // e.printStackTrace();
	// }
	// while (superClass.getSuperclass() != null && m == null) {
	// superClass = superClass.getSuperclass();
	// try {
	// m = superClass.getDeclaredMethod("clone");
	// } catch (NoSuchMethodException e) {
	// // TODO Auto-generated catch block
	// // e.printStackTrace();
	// }
	// }
	//
	// m.setAccessible(true);
	// copyObject = m.invoke(instance, null);
	// } catch (SecurityException e) {
	// e.printStackTrace();
	// } catch (IllegalArgumentException e) {
	// e.printStackTrace();
	// } catch (InvocationTargetException e) {
	// try {
	// copyObject = instanciateByConstructor(copyClass);
	// } catch (InvocationTargetException e1) {
	// copyObject = instance;
	// }
	// }
	// }
	// originals.add(instance);
	// duplicates.add(copyObject);
	//
	// copyFieldsFromToWithPackage(instance, copyObject);
	// return copyObject;
	//
	// }
	// }
	//
	// }

	private Object internalRevert(Object instance)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NegativeArraySizeException,
			InterruptedException {
		// System.out.println("reverting " + instance);

		String className = instance.getClass().getName();
		if (!Utils.nonPrimitiveName(className)) {
			return instance;
		} else if (instance.getClass().isArray()) {
			Class<?> array = instance.getClass();
			Class<?> arrayType = getTypeWithoutPackageName(array);
			Object o = Array.newInstance(arrayType, Array.getLength(instance));
			// fillArrayWithoutPackageName(o, instance);
			fillArrayWithoutPackageName(o, instance);
			return o;
		} else {
			int index = originals.indexOf(instance);
			if (index != -1) {
				return duplicates.get(index);
			} else {
				Class<?> copyClass = null;
				Object copyObject = instance;

				if (className.startsWith(newPackageName)) {
					copyClass = classLoader.loadClass(className
							.substring(className.indexOf(".") + 1));

					try {
						copyObject = copyClass.newInstance();

					} catch (InstantiationException e) {
						try {
							copyObject = instanciateByConstructor(copyClass);
						} catch (InvocationTargetException e1) {
							copyObject = instance;
						}
					}
				}

				originals.add(instance);
				duplicates.add(copyObject);

				// copyFieldsFromToWithoutPackage(instance, copyObject);
				copyFieldsFromToWithoutPackage(instance, copyObject);
				return copyObject;
			}
		}
	}

	@Override
	public byte[] getByteCodeForClass(String name) throws InterruptedException,
			ClassNotFoundException {
		synchronized (this) {
			if (translatedClasses.containsKey(name)) {
				return translatedClasses.get(name);
			}
		}
		if (name.startsWith(newPackageName)) {
			byte[] b = Utils
					.getBytesFromClass(name.substring(name.indexOf(".") + 1));

			// ClassReader cr1 = new ClassReader(b);
			// ClassWriter cw1 = new ClassWriter(cr1, 0);
			// CheckClassAdapter cca1 = new CheckClassAdapter(cw1);
			// TraceClassVisitor tcv1 = new TraceClassVisitor(cca1, new
			// PrintWriter(System.out));
			// cr1.accept(tcv1, 0);

			ClassReader cr = new ClassReader(b);

			ClassWriter cw = new ClassWriter(cr, 0);
			// CheckClassAdapter cca = new CheckClassAdapter(cw);
			// ClassVisitor cv = new ClassVisitor(ASM4, cca) { };
			TraceClassVisitor tcv = new TraceClassVisitor(cw, file);

			String packageName = name.substring(0, name.indexOf(".")) + "/";
			ClassPackageAdder ca = new ClassPackageAdder(tcv, packageName,
					classNameManager);
			cr.accept(ca, 0);
			byte[] bytesForClass = cw.toByteArray();
			synchronized (this) {
				translatedClasses.put(name, bytesForClass);
			}
			return bytesForClass;
		}
		return null;
	}

	private Class<?> getTypeWithPackageName(Class<?> arrayType)
			throws ClassNotFoundException, InterruptedException {
		Class<?> componentType = arrayType.getComponentType();
		if (componentType.isArray()) {
			return Array.newInstance(getTypeWithPackageName(componentType), 0)
					.getClass();
		} else {
			if (Utils.nonSystemName(componentType.getName(), newPackageName)) {
				String className = componentType.getName();
				String packageName = classNameManager.getPackegeName(className);
				componentType = classLoader.loadClass(packageName.replaceAll(
						"/", ".") + className);
			}
			return componentType;
		}
	}

	private Class<?> getTypeWithoutPackageName(Class<?> arrayType)
			throws ClassNotFoundException, NegativeArraySizeException,
			InterruptedException {
		Class<?> componentType = arrayType.getComponentType();
		if (componentType.isArray()) {
			return Array.newInstance(getTypeWithPackageName(componentType), 0)
					.getClass();
		} else {
			return getOriginalClass(componentType);
		}
	}

	private Class<?> getOriginalClass(Class<?> classWithPackage)
			throws ClassNotFoundException {
		if (classWithPackage.getName().startsWith(newPackageName)) {
			classWithPackage = classLoader.loadClass(classWithPackage.getName()
					.substring(classWithPackage.getName().indexOf(".")));
		}
		return classWithPackage;
	}

	private void fillArrayWithPackageName(Object newInstance, Object oldInstance)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, InterruptedException {
		for (int i = 0; i < Array.getLength(oldInstance); i++) {
			Object value = Array.get(oldInstance, i);
			if (value != null && !value.getClass().isPrimitive()) {
				value = internalDuplicate(value);
			}
			Array.set(newInstance, i, value);
		}
	}

	private void fillArrayWithoutPackageName(Object newInstance,
			Object oldInstance) throws ArrayIndexOutOfBoundsException,
			IllegalArgumentException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			NegativeArraySizeException, InterruptedException {
		for (int i = 0; i < Array.getLength(oldInstance); i++) {
			Object value = Array.get(oldInstance, i);
			if (value != null && !value.getClass().isPrimitive()) {
				value = internalRevert(value);
			}
			Array.set(newInstance, i, value);
		}
	}

	private Object instanciateByConstructor(Class c)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Object r = null;
		// Constructor<?>[] consts = c.getDeclaredConstructors();
		// for (Constructor co : consts) {
		// System.out.println(co);
		// }

		Constructor<?> constructor = c.getDeclaredConstructors()[0];
		// System.out.println(constructor + " ---> "
		// + constructor.getGenericParameterTypes().length);

		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
		}

		Type[] paramTypes = constructor.getGenericParameterTypes();

		Object args[] = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			Type type = paramTypes[i];
			if (type.toString().equals("long")) {
				args[i] = new Long(0);
			} else if (type.toString().equals("short")) {
				args[i] = new Short((short) 0);
			} else if (type.toString().equals("int")) {
				args[i] = new Integer(0);
			} else if (type.toString().equals("byte")) {
				args[i] = new Byte((byte) 0);
			} else if (type.toString().equals("char")) {
				args[i] = new Character((char) 0);
			} else if (type.toString().equals("double")) {
				args[i] = new Double(0);
			} else if (type.toString().equals("float")) {
				args[i] = new Float(0);
			} else if (type.toString().equals("boolean")) {
				args[i] = new Boolean(false);
			}
			// System.out.println(type.getClass());
		}
		// r = unsafe.allocateInstance(c);
		r = constructor.newInstance(args);

		return r;
	}

	private void copyFieldsFromToWithPackage(Object originalObject,
			Object copyObject) throws ClassNotFoundException,
			InstantiationException, InterruptedException {
		List<Field> originalFields = getAllFields(originalObject.getClass());
		List<Field> duplicateFields = getAllFields(copyObject.getClass());
		Iterator<Field> it1 = originalFields.iterator();
		Iterator<Field> it2 = duplicateFields.iterator();

		Field fOriginal = null;
		Field fDuplicate = null;

		while (it1.hasNext()) {
			try {
				fOriginal = it1.next();
				fDuplicate = it2.next();
				// System.out.println(f.getName() + " = " + f.get(a));
				copyFieldWithPackage(originalObject, copyObject, fOriginal,
						fDuplicate);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// e.printStackTrace();
			}
		}
	}

	private void copyFieldWithPackage(Object originalObject, Object copyObject,
			Field fOriginal, Field fDuplicate) throws IllegalAccessException,
			ClassNotFoundException, InstantiationException,
			IllegalArgumentException, InterruptedException {
		Object duplicateCopy;
		if (!fOriginal.getName().equals(fDuplicate.getName())) {
			throw new IllegalArgumentException();
		}

		Object originalFieldObject = fOriginal.get(originalObject);
		int index = originals.indexOf(originalFieldObject);
		if (fOriginal.getName().equals("serialVersionUID")) {
			duplicateCopy = new Long(1);
		} else {
			if (index != -1) {
				duplicateCopy = duplicates.get(index);
			} else if (originalFieldObject != null
					&& Utils.nonPrimitiveName(originalFieldObject.getClass()
							.getName())) {
				// Regular Type
				duplicateCopy = internalDuplicate(fOriginal.get(originalObject));
			} else {
				duplicateCopy = originalFieldObject;
			}
		}
		fDuplicate.set(copyObject, duplicateCopy);
	}

	private void copyFieldsFromToWithoutPackage(Object originalObject,
			Object copyObject) throws ClassNotFoundException,
			InstantiationException, NegativeArraySizeException,
			InterruptedException {
		List<Field> originalFields = getAllFields(originalObject.getClass());
		List<Field> duplicateFields = getAllFields(copyObject.getClass());
		Iterator<Field> it1 = originalFields.iterator();
		Iterator<Field> it2 = duplicateFields.iterator();

		Field fOriginal = null;
		Field fDuplicate = null;

		while (it1.hasNext()) {
			try {
				fOriginal = it1.next();
				fDuplicate = it2.next();
				// System.out.println(f.getName() + " = " + f.get(a));
				copyFieldWithoutPackage(originalObject, copyObject, fOriginal,
						fDuplicate);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// e.printStackTrace();
			}
		}
	}

	private void copyFieldWithoutPackage(Object originalObject,
			Object copyObject, Field fOriginal, Field fDuplicate)
			throws IllegalAccessException, ClassNotFoundException,
			InstantiationException, NegativeArraySizeException,
			IllegalArgumentException, InterruptedException {
		Object duplicateCopy;
		if (!fOriginal.getName().equals(fDuplicate.getName())) {
			throw new IllegalArgumentException();
		}
		int index = originals.indexOf(fOriginal.get(originalObject));

		if (index != -1) {
			duplicateCopy = duplicates.get(index);
		} else if (Utils.nonPrimitiveName(fOriginal.getType().getName())
				&& fOriginal.get(originalObject) != null) {
			// Regular Type
			duplicateCopy = internalRevert(fOriginal.get(originalObject));
		} else {
			duplicateCopy = fOriginal.get(originalObject);
		}

		fDuplicate.set(copyObject, duplicateCopy);
	}

	private List<Field> getAllFields(Class<?> clazz) {
		if (!clazz.equals(Object.class)) {
			List<Field> fields = new ArrayList<Field>();
			for (Field f : clazz.getDeclaredFields()) {
				f.setAccessible(true);
				fields.add(f);
			}
			fields.addAll(getAllFields(clazz.getSuperclass()));
			return fields;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

}
