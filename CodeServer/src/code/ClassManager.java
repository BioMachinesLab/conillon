package code;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import result.ClassNameRequest;
import result.ClassRequest;

public class ClassManager {

//	private HashMap<ClassNameRequest, ClassProviderController> classProviderMap = new HashMap<ClassNameRequest, ClassProviderController>();
//	private HashMap<ClassRequest, ClassProviderController> classProvidersByClassRequest = new HashMap<ClassRequest, ClassProviderController>();
//	private ClassProvider classProvider;
//
//	public void setClassProvider(ClassProvider classProvider) {
//		this.classProvider = classProvider;
//	}
//
//	public int getClassProvider(ClassNameRequest classNameRequest, int id) {
//		ClassProviderController provider = classProviderMap.get(classNameRequest);
//		if (provider == null) {
//			provider = new ClassProviderController(id,classNameRequest);
//			classProviderMap.put(classNameRequest, provider);
//			classProvidersByClassRequest.put(new ClassRequest(id,
//					classNameRequest.getName()), provider);
//		} else {
//			provider.add(id);
//		}
//		return provider.getPackageName();
//	}
//
//	public int getClassProvider(ClassRequest request) {
//		ClassProviderController provider = classProvidersByClassRequest.get(request);
//		return provider.getProvider();
//	}
//	
//	public void clientDone(int client){
//		Iterator<ClassRequest> it =  classProvidersByClassRequest.keySet().iterator();
//		while(it.hasNext()){
//			ClassRequest request = it.next();
//			ClassProviderController provider = classProvidersByClassRequest.get(request);
//			provider.remove(client);
//			if(provider.notInUse()){
//				it.remove();
//				classProviderMap.remove(provider.getNameRequest());
//				classProvider.removeClass(request);
//			}
//		}
//	}
//
//	private class ClassProviderController {
//		private int packageName;
//		private LinkedList<Integer> clients = new LinkedList<Integer>();
//		private ClassNameRequest nameRequest;
//
//		public ClassProviderController(int provider, ClassNameRequest nameRequest) {
//			packageName = provider;
//			this.nameRequest = nameRequest;
//			clients.add(provider);
//		}
//
//		public ClassNameRequest getNameRequest() {
//			return nameRequest;
//		}
//		
//		public void add(int client) {
//			clients.add(client);
//		}
//
//		public void remove(int client) {
//			Iterator<Integer> it = clients.iterator();
//			int count = 0;
//			while (it.hasNext()) {
//				if (it.next() == client) {
//					it.remove();
//					if (count == 0) {
//						classProvider.providerDied();
//					}
//					return;
//				}
//				count++;
//			}
//
//		}
//
//		public int getPackageName() {
//			return packageName;
//		}
//
//		public boolean notInUse() {
//			return clients.isEmpty();
//		}
//
//		public int getProvider() {
//			return clients.peek();
//		}
//	}
}
