package de.bright_side.filesystemfacade.remotefs;

/**
 * @author Philip Heyse
 *
 */
public class RemoteFSRequest {
	private String command;
	private Object[] parameters;
	private int version;
	private String absolutePath;
	
	public RemoteFSRequest(int version, String absolutePath, String command, Object[] parameters) {
		this.absolutePath = absolutePath;
		this.command = command;
		this.parameters = parameters;
		this.version = version;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

}
