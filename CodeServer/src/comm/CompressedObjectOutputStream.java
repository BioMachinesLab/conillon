package comm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;

public class CompressedObjectOutputStream extends ObjectOutputStream {

	private OutputStream out;
	protected CompressedObjectOutputStream() throws IOException,
			SecurityException {
		super();
		// TODO Auto-generated constructor stub
	} 
	
	public CompressedObjectOutputStream(OutputStream out) throws IOException{
		super(out);
		this.out = out;
	}


	public void writeCompressedObject(Object toSend) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      ObjectOutputStream oos = new ObjectOutputStream(bos);
	      oos.writeObject(toSend);
	      oos.flush();
	      oos.close();
	      bos.close();
	      byte [] input = bos.toByteArray();
		    
		    // Compressor with highest level of compression
		    Deflater compressor = new Deflater();
		    compressor.setLevel(Deflater.BEST_COMPRESSION);
		    
		    // Give the compressor the data to compress
		    compressor.setInput(input);
		    compressor.finish();
		    
		    // Create an expandable byte array to hold the compressed data.
		    // It is not necessary that the compressed data will be smaller than
		    // the uncompressed data.
		     bos = new ByteArrayOutputStream(input.length);
		    
		    // Compress the data
		    byte[] buf = new byte[1024];
		    while (!compressor.finished()) {
		        int count = compressor.deflate(buf);
		        bos.write(buf, 0, count);
		    }
		    try {
		        bos.close();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
		    
		    // Get the compressed data
		    byte[] compressedData = bos.toByteArray();
		super.writeObject(compressedData);
		
	}
	
}
