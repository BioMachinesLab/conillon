package comm.translator;

import java.io.IOException;
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
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

public class DynamicTranslator {

	private HashMap<String, CtClass> translatedClasses = new HashMap<String, CtClass>();
	private ClassPool pool = ClassPool.getDefault();

	private ArrayList<Object> originals = new ArrayList<Object>();
	private ArrayList<Object> duplicates = new ArrayList<Object>();

	private DynamicClassLoader classLoader = new DynamicClassLoader(this);

	// private static final sun.misc.Unsafe unsafe = Unsafe.getUnsafe();

	private String packageName;

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Object duplicate(Object instance) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		// System.out.println("duplicating " + instance);
		String className = instance.getClass().getName();
		Object copyObject = null;
		if (!nonPrimitiveName(className)) {
			return instance;
		} else if (instance.getClass().isArray()) {
			Class array = instance.getClass();
			Class arrayType = getTypeWithPackageName(array);
			Object o = Array.newInstance(arrayType, Array.getLength(instance));
			fillArrayWithPackageName(o, instance);
			return o;
		} else {
			int index = originals.indexOf(instance);
			if (index != -1) {
				return duplicates.get(index);
			} else {
				Class copyClass = null;

				if (nonSystemName(className)) {
					// System.out.println("Need to duplicate " + instance);

					try {
						copyClass = classLoader.loadClass(packageName
								+ className);
					} catch (ClassNotFoundException e) {
						try {
							copyClass = translateClass(instance.getClass()).toClass();
						} catch (CannotCompileException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

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
						Class superClass = instance.getClass();
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
						}
					}
				}
				originals.add(instance);
				duplicates.add(copyObject);

				copyFieldsFromToWithPackage(instance, copyObject);
				return copyObject;
			}
		}
	}

	public Object revert(Object instance) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		// System.out.println("reverting " + instance);

		String className = instance.getClass().getName();
		if (!nonPrimitiveName(className)) {
			return instance;
		} else if (instance.getClass().isArray()) {
			Class array = instance.getClass();
			Class arrayType = getTypeWithoutPackageName(array);
			Object o = Array.newInstance(arrayType, Array.getLength(instance));
			fillArrayWithoutPackageName(o, instance);
			return o;
		} else {
			int index = originals.indexOf(instance);
			if (index != -1) {
				return duplicates.get(index);
			} else {
				Class copyClass = null;
				Object copyObject = instance;

				if (className.startsWith(packageName)) {
					try {
						copyClass = classLoader.loadClass(className
								.substring(packageName.length()));
					} catch (ClassNotFoundException e) {
						try {
							copyClass = translateClass(instance.getClass()).toClass();
						} catch (CannotCompileException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

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

				copyFieldsFromToWithoutPackage(instance, copyObject);
				return copyObject;
			}
		}
	}

	private void copyFieldsFromToWithPackage(Object originalObject,
			Object copyObject) throws ClassNotFoundException,
			InstantiationException {
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
			ClassNotFoundException, InstantiationException {
		Object duplicateCopy;
		if (!fOriginal.getName().equals(fDuplicate.getName())) {
			throw new IllegalArgumentException();
		}
		int index = originals.indexOf(fOriginal.get(originalObject));

		if (index != -1) {
			duplicateCopy = duplicates.get(index);
		} else if (nonPrimitiveName(fOriginal.getType().getName())
				&& fOriginal.get(originalObject) != null) {
			// Regular Type
			duplicateCopy = duplicate(fOriginal.get(originalObject));
		} else {
			duplicateCopy = fOriginal.get(originalObject);
		}

		fDuplicate.set(copyObject, duplicateCopy);
	}

	private void copyFieldsFromToWithoutPackage(Object originalObject,
			Object copyObject) throws ClassNotFoundException,
			InstantiationException {
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
			InstantiationException {
		Object duplicateCopy;
		if (!fOriginal.getName().equals(fDuplicate.getName())) {
			throw new IllegalArgumentException();
		}
		int index = originals.indexOf(fOriginal.get(originalObject));

		if (index != -1) {
			duplicateCopy = duplicates.get(index);
		} else if (nonPrimitiveName(fOriginal.getType().getName())
				&& fOriginal.get(originalObject) != null) {
			// Regular Type
			duplicateCopy = revert(fOriginal.get(originalObject));
		} else {
			duplicateCopy = fOriginal.get(originalObject);
		}

		fDuplicate.set(copyObject, duplicateCopy);
	}

	private Object instanciateByConstructor(Class c)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Object r = null;
		Constructor[] consts = c.getDeclaredConstructors();
		// for (Constructor co : consts) {
		// System.out.println(co);
		// }

		Constructor constructor = c.getDeclaredConstructors()[0];
		// System.out.println(constructor + " ---> "
		// + constructor.getGenericParameterTypes().length);
		try {
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
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return r;
	}

	private void fillArrayWithPackageName(Object newInstance, Object oldInstance)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		for (int i = 0; i < Array.getLength(oldInstance); i++) {
			Object value = Array.get(oldInstance, i);
			if (value != null && !value.getClass().isPrimitive()) {
				value = duplicate(value);
			}
			Array.set(newInstance, i, value);
		}
	}

	private void fillArrayWithoutPackageName(Object newInstance,
			Object oldInstance) throws ArrayIndexOutOfBoundsException,
			IllegalArgumentException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		for (int i = 0; i < Array.getLength(oldInstance); i++) {
			Object value = Array.get(oldInstance, i);
			if (value != null && !value.getClass().isPrimitive()) {
				value = revert(value);
			}
			Array.set(newInstance, i, value);
		}
	}

	private Class getTypeWithPackageName(Class arrayType)
			throws ClassNotFoundException {
		Class componentType = arrayType.getComponentType();
		if (componentType.isArray()) {
			return Array.newInstance(getTypeWithPackageName(componentType), 0)
					.getClass();
		} else {
			if (nonSystemName(componentType.getName())) {
				componentType = classLoader.loadClass(packageName
						+ componentType.getName());
			}
			return componentType;
		}
	}

	private Class getTypeWithoutPackageName(Class arrayType)
			throws ClassNotFoundException {
		Class componentType = arrayType.getComponentType();
		if (componentType.isArray()) {
			return Array.newInstance(getTypeWithPackageName(componentType), 0)
					.getClass();
		} else {
			return getOriginalClass(componentType);
		}
	}

	private Class getOriginalClass(Class classWithPackage)
			throws ClassNotFoundException {
		if (classWithPackage.getName().startsWith(packageName)) {
			classWithPackage = classLoader.loadClass(classWithPackage.getName()
					.substring(packageName.length()));
		}
		return classWithPackage;
	}

	// public Class translateClass(String name) throws ClassNotFoundException {
	// try {
	// CtClass cc = pool.get(name);
	//
	// return translateClass(cc);
	//
	//
	//
	// // if (nonSystemName(name)) {
	// // if (!translatedClasses.containsKey(name)) {
	// //
	// // translatedClasses.put(name, cc);
	// //
	// // // Super Class
	// // String superClassName = cc.getSuperclass().getName();
	// // translateClass(superClassName);
	// // if (nonSystemName(superClassName)) {
	// // CtClass superClass = pool.get(packageName
	// // + superClassName);
	// // cc.setSuperclass(superClass);
	// // }
	// //
	// // // Fields
	// // CtField[] oldFields = cc.getDeclaredFields();
	// // for (CtField aField : oldFields) {
	// // CtClass type = aField.getType();
	// // translateType(type, cc);
	// // }
	// //
	// // // Methods
	// // CtMethod[] methods = cc.getDeclaredMethods();
	// // for (CtMethod method : methods) {
	// // // Parameters
	// // CtClass[] params = method.getParameterTypes();
	// // for (CtClass param : params) {
	// // translateType(param, cc);
	// // }
	// // // ReturnType
	// // CtClass type = method.getReturnType();
	// // translateType(type, cc);
	// //
	// // // Local Variables
	// // Set<String> classNames = method.getMethodInfo()
	// // .getConstPool().getClassNames();
	// // // ConstPool cpool =
	// // // method.getMethodInfo().getConstPool();
	// // // cpool.
	// // // System.out.println("\n\n Class: " + name +
	// // // " Method: "
	// // // + method.getName());
	// // for (String cName : classNames) {
	// // cName = cName.replace('/', '.');
	// // // System.out.println(cName);
	// // if (nonSystemName(cName)) {
	// // type = pool.get(cName);
	// // translateType(type, cc);
	// // }
	// // }
	// // }
	// // String newName = packageName + name;
	// // cc.setName(newName);
	// // translatedClasses.put(newName, cc);
	// // return cc.toClass(classLoader);
	// //
	// // // } else {
	// // // return null;
	// // }
	// // // return translatedClasses.get(newPackage + name).toClass();
	// // }
	// //
	// } catch (NotFoundException e) {
	//
	// e.printStackTrace();
	// }
	// return null;
	//
	// }

	public CtClass translateClass(Class tClass) {
		try {
			String name = tClass.getName();
			CtClass newClass = pool.get(name);
			
			if (nonSystemName(name)) {
				if (!translatedClasses.containsKey(name)) {
					String newName = packageName + name;
					newClass.setName(newName);
					CtClass old = pool.get(name);

					translatedClasses.put(name, old);

					// Super Class
					Class superClassName = tClass.getSuperclass();
					// if not a top level interface
					if (superClassName != null) {
						CtClass superClass = translateClass(superClassName);
						if (nonSystemName(superClassName.getName())) {
//							CtClass superClass = pool.get(packageName
//									+ superClassName);
							newClass.setSuperclass(superClass);
						}

						boolean emptyConst = false;
						for (Constructor c : tClass.getConstructors()) {
							if (c.getParameterTypes().length == 0) {
								emptyConst = true;
							}
						}
						if (!emptyConst) {
							CtConstructor empty = new CtConstructor(null,
									newClass);
							empty.setBody("super();");
							newClass.addConstructor(empty);
						}
					}
					// Fields
					Field[] oldFields = tClass.getDeclaredFields();
					for (Field aField : oldFields) {
						Class type = aField.getType();
						translateType(type.getCanonicalName(), newClass);
					}

					//Constructors 
					CtConstructor[] constructors = old.getConstructors();
					for (CtConstructor constructor : constructors) {
						CtClass[] params = constructor.getParameterTypes();
						for (CtClass param : params) {
							translateType(param.getName(), newClass);
						}

						// Local Variables
						Set<String> classNames = constructor.getMethodInfo()
								.getConstPool().getClassNames();
						// System.out.("\n\n Class: " + name + " Method: "
						// + method.getName());
						for (String cName : classNames) {
							cName = cName.replace('/', '.');
							if (nonSystemName(cName)) {
								CtClass type = pool.get(cName);
								translateType(type.getName(), newClass);
							}
						}
					}
					// Methods
					CtMethod[] methods = old.getDeclaredMethods();
					for (CtMethod method : methods) {
						// Parameters
						CtClass[] params = method.getParameterTypes();
						for (CtClass param : params) {
							translateType(param.getName(), newClass);
						}
						// ReturnType
						CtClass type = method.getReturnType();
						translateType(type.getName(), newClass);

						// Local Variables
						Set<String> classNames = method.getMethodInfo()
								.getConstPool().getClassNames();
						// System.out.("\n\n Class: " + name + " Method: "
						// + method.getName());
						for (String cName : classNames) {
							cName = cName.replace('/', '.');
							if (nonSystemName(cName)) {
								type = pool.get(cName);
								translateType(type.getName(), newClass);
							}
						}
					}

					translatedClasses.put(newName, newClass);
					newClass.toClass();
					return newClass;

					// } else {
					// return null;
				}
				return translatedClasses.get(packageName + name);
			}
			return newClass;
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getbyteCodeForClass(String name) {
		System.out.println("Asked for code for class: " + name);

		if (!translatedClasses.containsKey(name)) {
			String newPackage = name.substring(0, name.indexOf("."));
			String originalName = name.substring(name.indexOf(".") + 1);
			try {
				Class tClass = classLoader.loadClass(originalName);
				translateClass(tClass);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			return translatedClasses.get(name).toBytecode();
		} catch (IOException e) {

		} catch (CannotCompileException e) {

		}
		return null;
	}

	private void translateType(String typeName, CtClass cc)
			throws NotFoundException {
		// while (type.isArray()) {
		// // System.out.println(cc.getName());
		// type = type.getComponentType();
		// }
		//

		while (typeName.endsWith("[]")) {
			typeName = typeName.substring(0, typeName.length() - 2);
		}
		if (nonSystemName(typeName)) {
			// translateClass(typeName, newPackage);
			cc.replaceClassName(typeName, packageName + typeName);
		}

	}

	// private void translateType(CtClass type, CtClass cc)
	// throws NotFoundException {
	// // while (type.isArray()) {
	// // // System.out.println(cc.getName());
	// // type = type.getComponentType();
	// // }
	// //
	// String typeName = type.getName();
	// if (nonSystemName(typeName)) {
	// // translateClass(typeName, newPackage);
	// cc.replaceClassName(typeName, packageName + typeName);
	// }
	//
	// }
	//
	// private void translateType(Class type, CtClass cc) throws
	// NotFoundException {
	// while (type.isArray()) {
	// // System.out.println(cc.getName());
	// type = type.getComponentType();
	// }
	//
	// String typeName = type.getName();
	// if (nonSystemName(typeName)) {
	// // translateClass(typeName, newPackage);
	// cc.replaceClassName(typeName, packageName + typeName);
	// }
	//
	// }

	private boolean nonSystemName(String name) {
		// System.out.println(name);
		return !name.startsWith("sun.") && !name.startsWith("java.")
				&& !name.equals("tasks.Task") && !name.equals("result.Result")
				&& !name.equals("comm.FileProvider")
				&& !name.equals("comm.ClientPriority")
				&& !name.startsWith(packageName) && nonPrimitiveName(name);
	}

	private boolean nonPrimitiveName(String name) {
		// System.out.println(name);
		return !name.equals("java.lang.Long")
				&& !name.equals("java.lang.Integer")
				&& !name.equals("java.lang.Character")
				&& !name.equals("java.lang.Double")
				&& !name.equals("java.lang.Float")
				&& !name.equals("java.lang.Short")
				&& !name.equals("java.lang.Boolean")
				&& !name.equals("java.lang.String")
				&& !name.equals("java.lang.Void") && !name.equals("long")
				&& !name.equals("int") && !name.equals("char")
				&& !name.equals("double") && !name.equals("float")
				&& !name.equals("short") && !name.equals("boolean")
				&& !name.equals("void");
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

}
