package comm;

import java.util.HashMap;

import result.ClassNameRequest;
import result.TaskClientNumberRequest;
import client.Client;

import comm.translator.ClientTaskClassInformation;
import comm.translator.Utils;

public class ClassNameManager {
	private HashMap<String, ClassId> classIds = new HashMap<String, ClassId>();
	private Client client;
	private ClientTaskClassInformation clientInfo;

	public ClassNameManager(Client client) {
		super();
		this.client = client;
		long time = System.currentTimeMillis();
		clientInfo = Utils.getClassInClassPath();
		time = System.currentTimeMillis() - time;
		System.out.println("Client Hash=" + clientInfo.getClientHash()
				+ " calculated in " + time + "ms");
	}

	public String getPackegeName(String className) {
		// className = className.replace("/", ".");
		// ClassId classId = classIds.get(className);
		// if (classId == null) {
		// // long hashcode = 0;
		// // try {
		// // hashcode = Utils.getClassHashCode(className);
		// // } catch (ClassNotFoundException e1) {
		// // // TODO Auto-generated catch block
		// // e1.printStackTrace();
		// // }
		//
		// //new Code
		// long hashcode = clientInfo.getClientHash();
		//
		// //end new code
		//
		// client.sendMessageToCodeServer(new ClassNameRequest(className,
		// hashcode));
		// synchronized (this) {
		// try {
		// while (!classIds.containsKey(className)) {
		// wait();
		// }
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// classId = classIds.get(className);
		// }
		// }
		// //System.out.println(className + " -> " + classId.getPackageName());
		// return classId.getPackageName();
		return "__" + clientInfo.getClientHash() + "/";
	}

	public synchronized void registerPackageName(ClassNameRequest request) {
		classIds.put(request.getName(), new ClassId((int)request.getHashcode(),request));
		notify();
	}

	public String getPackegeName(ClientTaskClassInformation taskClasses) {
		String className = taskClasses.getTopClassName();
		ClassId classId = classIds.get(className);
		if (classId == null) {
			client.sendMessageToCodeServer(new TaskClientNumberRequest(
					taskClasses));
			synchronized (this) {
				try {
					while (!classIds.containsKey(className)) {
						wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				classId = classIds.get(className);
			}
		}
		return classId.getPackageName();
	}

	public long getClientHash() {
		return clientInfo.getClientHash();
	}

}
