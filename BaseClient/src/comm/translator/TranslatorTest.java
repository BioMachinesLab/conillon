package comm.translator;


public class TranslatorTest {

	private static Thread a;
	Utils utils;

	// class AA{
	// String a;
	//
	// public void aaa(AA aa){
	// System.out.println(a + aa);
	// }
	// }

	// public static void premain(String agentArgs, Instrumentation inst){
	// inst.addTransformer(new ClassFileTransformer() {
	//
	// @Override
	// public byte[] transform(ClassLoader loader, String className,
	// Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
	// byte[] bytes) throws IllegalClassFormatException {
	//
	//
	// ClassReader cr = new ClassReader(bytes);
	// ClassWriter cw = new ClassWriter(cr, 0);
	// ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cw) { };
	// ClassPackageAdder ca = new ClassPackageAdder(cv,"xpto/");
	// cr.accept(ca, 0);
	// return cw.toByteArray();
	// }
	// });
	// }
	//

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		byte[] m;
		try {
			m = Utils.getBytesFromClass("comm.translator.TranslatorTest");


			ClientTaskClassInformation map = Utils.getClassIn("tasks.CompletedTask");
			
			System.out.println(map.getTopClassName() + ": " + map.getClientHash());
			System.out.println("______________");
			for(String name:map.getClassHashs().keySet()){
				System.out.println(name + ": "+ map.getClassHashs().get(name));
			}
			
			long id = 0;
			for (byte b : m) {
				id += b;
			}
			System.out.println(System.currentTimeMillis() - time + ": " + id);

			// ASMTranslator t = new ASMTranslator("xpto/");
			// m = t.getbyteCodeForClass("xpto.comm.translator.TranslatorTest");
			//
			// id =0;
			// for(byte b: m){
			// id+=b;
			// }
			// System.out.println(id);
			//
			// String a = "aaaaa";
			// String b = "aaafeaa";
			// String c = "aadaaa";
			//
			// System.out.println(a.getClass().hashCode() + ", "
			// + b.getClass().hashCode() + ", " + c.getClass().hashCode()
			// + ", ");
			// Utils u = new Utils();
			// new TranslatorTest().init(u);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
//		 a = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				int i=0;
//				while(true){
//					System.out.println(i++);
//					for(int j=0; j<10000;j++);
//				}
//				
//			}
//		});
//		
//		Thread c = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				a.start();
//				try {
//					a.join();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				
//				System.out.println("DONE");
//				
//			}
//		});
//		c.start();
//		
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("Matar");
//		a.stop();
//		System.out.println("Done Main");
		
		
	}

	private void init(Utils u) {
		System.out.println("Ola");

	}

}
