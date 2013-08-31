package comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CompressedObjectInputStream extends ObjectInputStream {

	private InputStream in;
	protected CompressedObjectInputStream() throws IOException,
			SecurityException {
		super();
		// TODO Auto-generated constructor stub 
	}

	public CompressedObjectInputStream(InputStream in) throws IOException{
		super(in);
		this.in = in;
		
	}
	
	public Object readCompressedObject() throws IOException, ClassNotFoundException{
		
		byte[] read = (byte[]) super.readObject();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	  
		  Inflater decompressor = new Inflater();
		    decompressor.setInput(read);
		    
		    // Create an expandable byte array to hold the decompressed data
		     bos = new ByteArrayOutputStream(read.length);

		    // Decompress the data
		    byte[] buf = new byte[1024];
		    while (!decompressor.finished()) {
		        try {
		            int count = decompressor.inflate(buf);
		            bos.write(buf, 0, count);
		        } catch (DataFormatException e) {
		        	e.printStackTrace();
		        }
		    }
		    try {
		        bos.close();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
		    
		    // Get the decompressed data
		    byte[] decompressedData = bos.toByteArray();
		
		    
		    ByteArrayInputStream byteInStream = new ByteArrayInputStream(decompressedData); 
	        ObjectInputStream objInStream = new ObjectInputStream(byteInStream); 
	        Object reCreatedObj = objInStream.readObject(); 
		    
		
		return reCreatedObj;
		
	}
	
}
