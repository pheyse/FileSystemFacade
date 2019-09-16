package de.bright_side.filesystemfacade.remotefs;

/**
 * @author Philip Heyse
 *
 */
public class RemoteFSAuthenticationException extends Exception{
	private static final long serialVersionUID = 8758037976163601624L;
	private String app;
	private String tenant;
	private String username;
	
	public RemoteFSAuthenticationException(String message) {
		super(message);
	}

	public RemoteFSAuthenticationException(String app, String tenant, String username) {
		super();
		this.app = app;
		this.tenant = tenant;
		this.username = username;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
