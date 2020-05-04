package de.bright_side.filesystemfacade.facade;

/**
 * @author Philip Heyse
 *
 */
public class VersionedData <K> {
	private long version;
	private K data;
	
	public VersionedData(long version, K data) {
		this.version = version;
		this.data = data;
	}

	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}
	
	public K getData() {
		return data;
	}
	
	public void setData(K data) {
		this.data = data;
	}
	
	
}
