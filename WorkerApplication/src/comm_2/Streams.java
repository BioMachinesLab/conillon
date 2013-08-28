package comm_2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import worker.ClassLoaderObjectInputStream;


public class Streams {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private ClassLoaderObjectInputStream inObject;
	
	public Streams(ObjectInputStream in, ObjectOutputStream out) {
		super();
		this.in = in;
		this.out = out;
	}
	
	public Streams(ClassLoaderObjectInputStream in, ObjectOutputStream out) {
		super();
		this.inObject = in;
		this.out = out;
	}
	
	public ObjectInputStream returnObjectInputStream(){
		return this.in;
	}
	
	public ObjectOutputStream returnObjectOutputStream(){
		return this.out;
	}
	

	public ClassLoaderObjectInputStream returnClassLoaderObjectInputStream(){
		return this.inObject;
	}
}
