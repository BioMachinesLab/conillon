package comm.translator;

import java.util.LinkedList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import comm.ClassNameManager;

class ClassPackageAdder extends ClassVisitor implements Opcodes {

	private String newPackageName;
	private boolean hasNullConstruct = false;
	public String superClassName;
	// private String newPackageNameRead;
	// private String newPackageNameReadTranslated;
	private ClassNameManager classNameManager;

	public ClassPackageAdder(ClassVisitor cv, String newPackageName,
			ClassNameManager classNameManager) {
		super(ASM4, cv);
		// this.newPackageNameRead = newPackageName;
		// this.newPackageNameReadTranslated = newPackageName.replaceAll("/",
		// ".");
		this.classNameManager = classNameManager;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {

		if ((access & ACC_INTERFACE) != 0) {
			hasNullConstruct = true;
		}

		superName = addPackage(superName);
		superClassName = superName;

		changeNames(interfaces);
		newPackageName = getPackageName(name);

		cv.visit(version, access, newPackageName + name, signature, superName,
				interfaces);
	}

	@Override
	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {
		if (outerName != null) {
			outerName = addPackage(outerName);
		}
		super.visitInnerClass(newPackageName + name, outerName, innerName,
				access);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		// if(name.equals("serialVersionUID")){
		// return null;
		// }
		return super.visitField(access, name, fixSimpleDesc(desc), signature,
				value);
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if (name.equals("<init>") && desc.equals("()V")) {
			hasNullConstruct = true;
		}

		desc = fixDesc(desc);

		MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);
		MethodPackageAdder mpa = new MethodPackageAdder(mv);
		return mpa;
	}

	@Override
	public void visitEnd() {
		if (!hasNullConstruct) {
			// generate the default constructor
			MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V",
					null, null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, "<init>",
					"()V");
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		// FieldVisitor fv = visitField(
		// ACC_PRIVATE + ACC_FINAL + ACC_STATIC,
		// "serialVersionUID",
		// "J",
		// null,
		// new Long(newPackageName.substring(2,
		// newPackageName.length() - 1)));
		// if (fv != null) {
		// fv.visitEnd();
		// }
		super.visitEnd();
	}

	class MethodPackageAdder extends MethodVisitor {

		public MethodPackageAdder(final MethodVisitor mv) {
			super(ASM4, mv);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

			return super.visitAnnotation(desc, visible);
		}

		@Override
		public void visitFrame(int type, int mLocal, Object[] local,
				int nStack, Object[] stack) {
			Object[] newLocal = new Object[local.length];
			Object[] newStack = new Object[stack.length];
			int i = 0;
			for (Object object : local) {
				if (object instanceof String) {
					object = fixName((String) object);
				}
				newLocal[i++] = object;
			}
			i = 0;
			for (Object object : stack) {
				if (object instanceof String) {
					object = fixName((String) object);
				}
				newStack[i++] = object;
			}

			super.visitFrame(type, mLocal, newLocal, nStack, newStack);
		}

		public void visitTypeInsn(int i, String name) {
			mv.visitTypeInsn(i, fixName(name));
		}

		public void visitFieldInsn(int opcode, String owner, String name,
				String desc) {
			mv.visitFieldInsn(opcode, fixName(owner), name, fixSimpleDesc(desc));
		}

		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			mv.visitMethodInsn(opcode, fixName(owner), name, fixDesc(desc));
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc,
				Handle bsm, Object... bsmArgs) {

			super.visitInvokeDynamicInsn(name, fixDesc(desc), bsm, bsmArgs);
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
			// TODO Auto-generated method stub
			super.visitMultiANewArrayInsn(fixSimpleDesc(desc), dims);
		}

		@Override
		public void visitLocalVariable(String name, String desc,
				String signature, Label start, Label end, int index) {
			super.visitLocalVariable(name, fixSimpleDesc(desc), signature,
					start, end, index);
		}

		@Override
		public void visitLdcInsn(Object constant) {
			if (constant instanceof Type) {
				String className = ((Type) constant).getInternalName();
				constant = Type.getType("L" + addPackage(className) + ";");
			}
			super.visitLdcInsn(constant);
		}
	}

	private String addPackage(String className) {
		return getPackageName(className) + className;
	}

	private String getPackageName(String className) {
		String packageName = "";
		// int index = className.indexOf('$');
		// if (index > 0) {
		// className = className.substring(0, index);
		// }
		if (Utils.nonSystemName(className.replace("/", "."), "__")) {
//			if (className.endsWith(";")) {
//				packageName = classNameManager.getPackegeName(className
//						.substring(0, className.length() - 1));
//			} else {
//				packageName = classNameManager.getPackegeName(className);
//			}
			packageName = classNameManager.getPackegeName(className);
		}
		return packageName;
	}

	private String fixSimpleDesc(String desc) {
		String name = getClassName(Type.getType(desc));
		desc = desc.replace(name, addPackage(name));
		return desc;
	}

	private String fixDesc(String desc) {
		Type[] argumentTypes = Type.getArgumentTypes(desc);
		LinkedList<String> replacedWords = new LinkedList<String>();
		for (Type type : argumentTypes) {
			String name = getClassName(type);
			if (!replacedWords.contains(name)) {
				desc = desc.replace(name, addPackage(name));
			}
			replacedWords.add(name);
		}
		String returnClassName = getClassName(Type.getReturnType(desc));
		if (!replacedWords.contains(returnClassName)) {
			desc = desc.replace(returnClassName, addPackage(returnClassName));

		}
		return desc;
	}

	private String getClassName(Type type) {
		while (type.getSort() == Type.ARRAY) {
			type = type.getElementType();
		}
		if (type.getSort() == Type.OBJECT) {
			return type.getInternalName();
		} else {
			return type.getDescriptor();
		}
	}

	private String fixName(String name) {
		String typeName = name;
		while (typeName.startsWith("[")) {
			if (typeName.startsWith("[L")) {
				typeName = typeName.substring(2);
			} else {
				typeName = typeName.substring(1);
			}
		}
		name = name.replace(typeName, addPackage(typeName));
		return name;
	}

	private void changeNames(String[] names) {

		for (int j = 0; j < names.length; j++) {
			names[j] = addPackage(names[j]);
		}
	}

}

// package comm.translator;
//
// import java.util.LinkedList;
//
// import org.objectweb.asm.ClassVisitor;
// import org.objectweb.asm.FieldVisitor;
// import org.objectweb.asm.Handle;
// import org.objectweb.asm.Label;
// import org.objectweb.asm.MethodVisitor;
// import org.objectweb.asm.Opcodes;
// import org.objectweb.asm.Type;
//
// import comm.ClassNameManager;
//
// class ClassPackageAdder extends ClassVisitor implements Opcodes {
//
// // private final String newPackageName;
// private boolean hasNullConstruct = false;
// public String superClassName;
// private String newPackageNameRead;
// private String newPackageNameReadTranslated;
// private ClassNameManager classNameManager;
//
// public ClassPackageAdder(ClassVisitor cv, String newPackageName,
// ClassNameManager classNameManager) {
// super(ASM4, cv);
// this.newPackageNameRead = newPackageName;
// this.newPackageNameReadTranslated = newPackageName.replaceAll("/", ".");
// // this.newPackageNameRead = newPackageName.replaceAll("/", ".");
// // this.classNameManager = classNameManager;
// }
//
// public void visit(int version, int access, String name, String signature,
// String superName, String[] interfaces) {
//
// if ((access & ACC_INTERFACE) != 0) {
// hasNullConstruct = true;
// }
// name = addPackage(name);
// superName = addPackage(superName);
// // // if (Utils.nonSystemName(name.replaceAll("/", "."),
// // // newPackageNameReadTranslated)) {
// // // String packageName = classNameManager.getPackegeName(name);
// // // name = packageName + name;
// // name = addPackage(name);
// // // }
// //
// // // if (Utils.nonSystemName(superName.replaceAll("/", "."),
// // // newPackageNameReadTranslated)) {
// // // // String packageName =
// // classNameManager.getPackegeName(superName);
// // // // superName = packageName + superName;
// // superName = addPackage(superName);
// // }
// superClassName = superName;
//
// changeNames(interfaces);
//
// cv.visit(version, access, name, signature, superName, interfaces);
// }
//
// @Override
// public FieldVisitor visitField(int access, String name, String desc,
// String signature, Object value) {
//
// return super.visitField(access, name, fixSimpleDesc(desc), signature,
// value);
// }
//
// public MethodVisitor visitMethod(int access, String name, String desc,
// String signature, String[] exceptions) {
// if (name.equals("<init>") && desc.equals("()V")) {
// hasNullConstruct = true;
// }
//
// desc = fixDesc(desc);
//
// MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
// exceptions);
// MethodPackageAdder mpa = new MethodPackageAdder(mv);
// return mpa;
// }
//
// @Override
// public void visitEnd() {
// if (!hasNullConstruct) {
// // generate the default constructor
// MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V",
// null, null);
// mv.visitCode();
// mv.visitVarInsn(Opcodes.ALOAD, 0);
// mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, "<init>",
// "()V");
// mv.visitInsn(Opcodes.RETURN);
// mv.visitMaxs(1, 1);
// mv.visitEnd();
// }
// super.visitEnd();
// }
//
// class MethodPackageAdder extends MethodVisitor {
//
// public MethodPackageAdder(final MethodVisitor mv) {
// super(ASM4, mv);
// }
//
// @Override
// public void visitFrame(int type, int mLocal, Object[] local,
// int nStack, Object[] stack) {
// Object[] newLocal = new Object[local.length];
// Object[] newStack = new Object[stack.length];
// int i = 0;
// for (Object object : local) {
// if (object instanceof String) {
// object = fixName((String) object);
// }
// newLocal[i++] = object;
// }
// i = 0;
// for (Object object : stack) {
// if (object instanceof String) {
// object = fixName((String) object);
// }
// newStack[i++] = object;
// }
//
// super.visitFrame(type, mLocal, newLocal, nStack, newStack);
// }
//
// public void visitTypeInsn(int i, String name) {
// mv.visitTypeInsn(i, fixName(name));
// }
//
// public void visitFieldInsn(int opcode, String owner, String name,
// String desc) {
// mv.visitFieldInsn(opcode, fixName(owner), name, fixSimpleDesc(desc));
// }
//
// public void visitMethodInsn(int opcode, String owner, String name,
// String desc) {
// mv.visitMethodInsn(opcode, fixName(owner), name, fixDesc(desc));
// }
//
// @Override
// public void visitInvokeDynamicInsn(String name, String desc,
// Handle bsm, Object... bsmArgs) {
//
// super.visitInvokeDynamicInsn(name, fixDesc(desc), bsm, bsmArgs);
// }
//
// @Override
// public void visitMultiANewArrayInsn(String desc, int dims) {
// // TODO Auto-generated method stub
// super.visitMultiANewArrayInsn(fixSimpleDesc(desc), dims);
// }
//
// @Override
// public void visitLocalVariable(String name, String desc,
// String signature, Label start, Label end, int index) {
// super.visitLocalVariable(name, fixSimpleDesc(desc), signature,
// start, end, index);
// }
//
// @Override
// public void visitLdcInsn(Object constant) {
// if (constant instanceof Type) {
// String className = ((Type) constant).getInternalName();
// constant = Type.getType("L" + addPackage(className) + ";");
// }
// super.visitLdcInsn(constant);
// }
// }
//
// private String addPackage(String className) {
// String packageName = classNameManager.getPackegeName(className);
// if (Utils.nonSystemName(className.replace("/", "."), packageName)) {
// className = packageName + className;
// }
// return className;
// }
//
// private String fixSimpleDesc(String desc) {
// String name = getClassName(Type.getType(desc));
// desc = desc.replace(name, addPackage(name));
// return desc;
// }
//
// private String fixDesc(String desc) {
// Type[] argumentTypes = Type.getArgumentTypes(desc);
// LinkedList<String> replacedWords = new LinkedList<String>();
// for (Type type : argumentTypes) {
// String name = getClassName(type);
// if (!replacedWords.contains(name)) {
// // && Utils.nonSystemName(name.replaceAll("/", "."),
// // newPackageNameReadTranslated)) {
// // String packageName = classNameManager.getPackegeName(name);
// // desc = desc.replace(name, packageName + name);
// desc = desc.replace(name, addPackage(name));
// // System.out.println("fixDesc1 " + name);
// }
// replacedWords.add(name);
// }
// String returnClassName = getClassName(Type.getReturnType(desc));
// if (!replacedWords.contains(returnClassName)) {
// // && Utils.nonSystemName(returnClassName.replaceAll("/", "."),
// // newPackageNameReadTranslated)) {
// // String packageName =
// // classNameManager.getPackegeName(returnClassName);
// // desc = desc.replace(returnClassName, packageName
// // + returnClassName);
// desc = desc.replace(returnClassName, addPackage(returnClassName));
// // System.out.println("fixDesc1 " + returnClassName);
//
// }
// return desc;
// }
//
// private String getClassName(Type type) {
// while (type.getSort() == Type.ARRAY) {
// type = type.getElementType();
// }
// if (type.getSort() == Type.OBJECT) {
// return type.getInternalName();
// } else {
// return type.getDescriptor();
// }
// }
//
// private String fixName(String name) {
// String typeName = name;
// while (typeName.startsWith("[")) {
// if (typeName.startsWith("[L")) {
// typeName = typeName.substring(2);
// } else {
// typeName = typeName.substring(1);
// }
// }
// // if (Utils.nonSystemName(typeName.replaceAll("/", "."),
// // newPackageNameReadTranslated)) {
// // String packageName = classNameManager.getPackegeName(typeName);
// // name = name.replace(typeName, packageName + typeName);
// name = name.replace(typeName, addPackage(typeName));
// // System.out.println(name +" --- "+ typeName);
// // }
// return name;
// }
//
// private void changeNames(String[] names) {
//
// for (int j = 0; j < names.length; j++) {
// names[j] = addPackage(names[j]);
// }
// // // String name = names[j];
// // // if (Utils.nonSystemName(name.replaceAll("/", "."),
// // // newPackageNameReadTranslated)) {
// // // System.out.println(name);
// // // String packageName = classNameManager.getPackegeName(name);
// // // names[j] = packageName + name;
// // names[j] = addPackage(names[j]);
// // // }
// // }
// }
//
// }
