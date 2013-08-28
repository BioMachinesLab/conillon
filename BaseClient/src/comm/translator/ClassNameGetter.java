package comm.translator;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class ClassNameGetter extends ClassVisitor implements Opcodes {

	private Set<String> classNames = new HashSet<String>();

	public ClassNameGetter() {
		super(ASM4);
	}

	public Set<String> getClassNames() {
		return classNames;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		addNewName(name.replaceAll("/", "."));
		addNewName(superName.replaceAll("/", "."));
		addNames(interfaces);

		// cv.visit(version, access, name, signature, superName, interfaces);
	}

	private void addNewName(String newName) {
		System.out.println("addNewName " + newName);
		if (Utils.nonSystemName(newName)) {
			classNames.add(newName);
		}
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {

		return super.visitField(access, name, fixSimpleDesc(desc), signature,
				value);
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		desc = fixDesc(desc);

		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		MethodPackageAdder mpa = new MethodPackageAdder(mv);
		return mpa;
	}

	class MethodPackageAdder extends MethodVisitor {

		public MethodPackageAdder(final MethodVisitor mv) {
			super(ASM4, mv);
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
			fixName(name);
			// mv.visitTypeInsn(i, fixName(name));
		}

		public void visitFieldInsn(int opcode, String owner, String name,
				String desc) {

			fixName(owner);
			fixSimpleDesc(desc);
			// mv.visitFieldInsn(opcode, fixName(owner), name,
			// fixSimpleDesc(desc));
		}

		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			fixName(owner);
			fixDesc(desc);
			// mv.visitMethodInsn(opcode, fixName(owner), name, fixDesc(desc));
		}

		@Override
		public void visitLocalVariable(String name, String desc,
				String signature, Label start, Label end, int index) {
			fixSimpleDesc(desc);
			// super.visitLocalVariable(name, fixSimpleDesc(desc), signature,
			// start, end, index);
		}

		@Override
		public void visitLdcInsn(Object constant) {
			if (constant instanceof Type) {
				String className = ((Type) constant).getInternalName();
				addNewName(className.replaceAll("/", "."));
			}
			super.visitLdcInsn(constant);
		}
	}

	private String fixSimpleDesc(String desc) {
		String name = getClassName(Type.getType(desc));
		addNewName(name.replaceAll("/", "."));
		return desc;
	}

	private String fixDesc(String desc) {
		Type[] argumentTypes = Type.getArgumentTypes(desc);
		for (Type type : argumentTypes) {
			String name = getClassName(type);
			addNewName(name.replaceAll("/", "."));
		}
		String returnClassName = getClassName(Type.getReturnType(desc));
		addNewName(returnClassName.replaceAll("/", "."));
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
		addNewName(typeName.replaceAll("/", "."));

		return name;
	}

	private void addNames(String[] names) {
		for (String name : names) {
			addNewName(name.replaceAll("/", "."));
		}
	}

}
