package comm;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StreamsForCodeServer {
	ObjectInputStream in;
	ObjectOutputStream out;
	

	public StreamsForCodeServer(ObjectInputStream in,ObjectOutputStream out){
		this.out = out;
		this.in = in;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public ObjectOutputStream getOut() {
		return out;
	}
	

}
