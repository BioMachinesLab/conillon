package code;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import result.ClassNameRequest;
import result.ClassRequest;

public class ClassProvider {
	private HashMap<ClassRequest, byte[]> classes = new HashMap<ClassRequest, byte[]>();
	private ClassSolver classSolver;
	// private ClassManager classManager;

	private Map<Long, LinkedList<ClientInfo>> providers = new HashMap<Long, LinkedList<ClientInfo>>();
	private HashSet<ClassRequest> requestedClasses = new HashSet<ClassRequest>();

	// private Map<ClassNameRequest, ClassProviderController> classProviderMap =
	// Collections
	// .synchronizedMap(new HashMap<ClassNameRequest,
	// ClassProviderController>());
	// private Map<ClassRequest, ClassProviderController>
	// classProvidersByClassRequest = Collections
	// .synchronizedMap(new HashMap<ClassRequest, ClassProviderController>());
	//
	public ClassProvider(ClassSolver classSolver) {
		super();
		this.classSolver = classSolver;
		// this.classManager = classManager;
	}

	public synchronized byte[] getClassBy(ClassRequest request)
			throws InterruptedException {
		byte[] neededClass = null;

		if (classes.containsKey(request)) {
			neededClass = classes.get(request);
			// System.out.println(request + " -- FOUND!");
		} else {
			// System.out.println(request);
			while (neededClass == null) {
				if (!requestedClasses.contains(request)) {
					requestedClasses.add(request);
					ClientInfo clientInfo = null;
					if (request.getName().startsWith("__")) {
						long codeBase = Long.parseLong(request.getName()
								.substring(2, request.getName().indexOf(".")));
						LinkedList<ClientInfo> classProvidersForCodeBase = providers
								.get(codeBase);
						if (classProvidersForCodeBase == null) {
							System.out
									.println("Error: no provider for codebase: "
											+ codeBase);
						}
						clientInfo = classProvidersForCodeBase.getFirst();
					} else {
						clientInfo = classSolver.getClientInfo(request.getId());
					}
					clientInfo.requestClass(request);
				}
				System.out.println("wait" + Thread.currentThread());
				wait();
				System.out.println("Done wait" + Thread.currentThread());
				neededClass = classes.get(request);
			}
			requestedClasses.remove(request);
		}
		return neededClass;
	}

	public synchronized void addClass(ClassRequest request, byte[] neededClass) {
		classes.put(request, neededClass);
		notifyAll();

		System.out.println("Got class from client:" + request);
	}

	public synchronized void providerDied() {
		notifyAll();
		//make sure all pending request are resubmit
		requestedClasses.clear();
	}

	public synchronized void removeClass(ClassRequest request) {
		classes.remove(request);

	}

	public synchronized void addProvider(ClientInfo clientInfo) {
		classSolver.add(clientInfo);
		LinkedList<ClientInfo> povidersForCodeBase = providers.get(clientInfo
				.getCodeBase());
		if (povidersForCodeBase == null) {
			povidersForCodeBase = new LinkedList<ClientInfo>();
			providers.put(clientInfo.getCodeBase(), povidersForCodeBase);
		}
		povidersForCodeBase.add(clientInfo);
	}

	// public synchronized int getClassProvider(ClassNameRequest
	// classNameRequest,
	// int id) {
	// ClassProviderController provider = classProviderMap
	// .get(classNameRequest);
	// if (provider == null) {
	// int index = classNameRequest.getName().indexOf('$');
	// if (index > 0) {
	// int superProvider = getClassProvider(new ClassNameRequest(
	// classNameRequest.getName().substring(0, index),
	// classNameRequest.getHashcode()), id);
	// provider = new ClassProviderController(superProvider,
	// classNameRequest);
	// } else {
	// provider = new ClassProviderController(id, classNameRequest);
	// }
	// classProviderMap.put(classNameRequest, provider);
	// classProvidersByClassRequest.put(new ClassRequest(id, "__" +
	// classNameRequest.getHashcode()
	// + "." + classNameRequest.getName()), provider);
	// } else {
	// provider.add(id);
	// }
	// System.out.println(id + " asked for classname "
	// + classNameRequest.getName() + " and got "
	// + provider.getPackageName());
	// return provider.getPackageName();
	// }

	// public synchronized int getClassProvider(ClassRequest request) {
	// ClassProviderController provider = classProvidersByClassRequest
	// .get(request);
	// if (provider == null) {
	// System.out
	// .println("ERROR getClassProvider did not find any provider for class "
	// + request.getName());
	// return request.getId();
	// }
	// return provider.getProvider();
	// }

	public synchronized void clientDone(int client) {
		ClientInfo clientInfo = classSolver.getClientInfo(client);
		LinkedList<ClientInfo> providersCodeBase = providers.get(clientInfo
				.getCodeBase());
		providersCodeBase.remove(clientInfo);
		if (providersCodeBase.isEmpty()) {
			providers.remove(clientInfo.getCodeBase());
			removeAllClassesWith(clientInfo.getCodeBase());
		}

		// Iterator<ClassRequest> it = classProvidersByClassRequest.keySet()
		// .iterator();
		// while (it.hasNext()) {
		// ClassRequest request = it.next();
		// ClassProviderController provider = classProvidersByClassRequest
		// .get(request);
		// provider.remove(client);
		// if (provider.notInUse()) {
		// it.remove();
		// classProviderMap.remove(provider.getNameRequest());
		// //removeClass(request);
		// }
		// }
	}

	private void removeAllClassesWith(long codeBase) {
		Iterator<ClassRequest> it = classes.keySet().iterator();
		while (it.hasNext()) {
			ClassRequest request = it.next();
			if (request.getId() == codeBase) {
				it.remove();
			}
		}
	}

	private class ClassProviderController {
		private int packageName;
		private LinkedList<Integer> clients = new LinkedList<Integer>();
		private ClassNameRequest nameRequest;
		private int provider;

		public ClassProviderController(int provider,
				ClassNameRequest nameRequest) {
			packageName = provider;
			this.nameRequest = nameRequest;
			clients.add(provider);
			this.provider = provider;
		}

		public ClassNameRequest getNameRequest() {
			return nameRequest;
		}

		public synchronized void add(int client) {
			for (int i : clients) {
				if (i == client)
					return;
			}
			clients.add(client);
		}

		public synchronized void remove(int client) {
			Iterator<Integer> it = clients.iterator();
			while (it.hasNext()) {
				if (it.next() == client) {
					it.remove();
					break;
				}
			}
			if (client == provider) {
				if (!clients.isEmpty()) {
					provider = clients.peek();
				} else {
					provider = -1;
				}
				providerDied();
			}

		}

		public int getPackageName() {
			return packageName;
		}

		public boolean notInUse() {
			return clients.isEmpty();
		}

		public int getProvider() {
			return provider;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return nameRequest.getName();
		}
	}

}
