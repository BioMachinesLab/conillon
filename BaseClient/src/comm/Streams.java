package comm;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Streams{

	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public Streams(ObjectInputStream in, ObjectOutputStream out) {
		super();
		this.in = in;
		this.out = out;
	}
	
	public ObjectInputStream returnObjectInputStream(){
		return this.in;
	}
	
	public ObjectOutputStream returnObjectOutputStream(){
		return this.out;
	}
	

}
