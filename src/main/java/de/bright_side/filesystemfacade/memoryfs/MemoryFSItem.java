package de.bright_side.filesystemfacade.memoryfs;

/**
 * contains the actual data and properties of the file while MemoryFile will be reading from this. It is required because it is possible to create a MemoryFile instance which does not exist in the FileSystem.
 * Also there may be multiple MemoryFile instances which point to the same MemoryFSItem and the MemoryFSItem may change.
 *
 * @author Philip Heyse
 *
 */
public class MemoryFSItem{
	private MemoryFS memoryFS;
	private boolean isDir;
	private long timeLastModified;
	private long timeCreated;
	private byte[] dataAsBytes;
	
	public MemoryFSItem(MemoryFS memoryFS,  boolean isDir, long timeLastModified, long timeCreated){
		this.memoryFS = memoryFS;
		this.isDir = isDir;
		this.timeLastModified = timeLastModified;
		this.timeCreated = timeCreated;
	}

	public MemoryFS getMemoryFS() {
		return memoryFS;
	}

	public void setMemoryFS(MemoryFS memoryFS) {
		this.memoryFS = memoryFS;
	}

	public boolean isDir() {
		return isDir;
	}

	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	public long getTimeCreated() {
		return timeCreated;
	}
	
	public void setTimeLastModified(long timeLastModified) {
		this.timeLastModified = timeLastModified;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}
	
	public byte[] getDataAsBytes() {
		return dataAsBytes;
	}

	public void setDataAsBytes(byte[] dataAsBytes) {
		this.dataAsBytes = dataAsBytes;
	}

}
