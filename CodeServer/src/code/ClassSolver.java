package code;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;

public class ClassSolver {
	private LinkedList<ClientInfo> clientInfos = new LinkedList<ClientInfo>();

	public synchronized ClientInfo getClientInfo(int id) {
		for (ClientInfo i : clientInfos) {
			if (i.checkId(id)) {
				return i;
			}
		}
		return null;
	}

	public synchronized void add(int id, ObjectOutputStream out,
			ObjectInputStream in, long codeBase) {
		clientInfos.add(new ClientInfo(id, out, in, codeBase));
	}

	public void add(ClientInfo clientInfo) {
		clientInfos.add(clientInfo);

	}

	public synchronized void remove(int id) {
		Iterator<ClientInfo> ci = clientInfos.iterator();
		while (ci.hasNext()) {
			if (ci.next().checkId(id)) {
				ci.remove();
			}
		}
	}

}
