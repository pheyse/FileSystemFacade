package de.bright_side.filesystemfacade.memoryfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Philip Heyse
 *
 */
public class MemoryFileOutputStream extends OutputStream {
	private MemoryFSItem item;
	private ByteArrayOutputStream byteArrayOutputStream;

	public MemoryFileOutputStream(MemoryFSItem item, byte[] currentBytes) throws IOException {
		this.item = item;
		byteArrayOutputStream = new ByteArrayOutputStream();
		if (currentBytes != null){
			byteArrayOutputStream.write(currentBytes);
		}
	}

	@Override
	public void write(int b) throws IOException {
		byteArrayOutputStream.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		byteArrayOutputStream.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		byteArrayOutputStream.write(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		item.setDataAsBytes(byteArrayOutputStream.toByteArray());
	}
	
	

}
