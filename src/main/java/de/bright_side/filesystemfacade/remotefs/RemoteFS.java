package de.bright_side.filesystemfacade.remotefs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.WrongVersionException;
import de.bright_side.filesystemfacade.util.FSFFileUtil;

/**
 * @author Philip Heyse
 *
 */
public class RemoteFS implements FSFSystem{
	protected static final String COMMAND_GET_TIME_LAST_MODIFIED = "getTimeLastModified";
	protected static final String COMMAND_GET_TIME_CREATED = "getTimeCreated";
	protected static final String COMMAND_IS_FILE = "isFile";
	protected static final String COMMAND_IS_DIRECTORY = "isDirectory";
	protected static final String COMMAND_EXISTS = "exists";
	protected static final String COMMAND_READ_BYTES = "readBytes";
	protected static final String COMMAND_READ_HISTORY_BYTES = "readHistoryBytes";
	protected static final String COMMAND_WRITE_BYTES = "writeBytes";
	protected static final String COMMAND_READ_BYTES_AND_VERSION =  "readBytesAndVersion";
	protected static final String COMMAND_WRITE_BYTES_FOR_VERSION = "writeBytesForVersion";
	protected static final String COMMAND_GET_VERSION = "getVersion";
	protected static final String COMMAND_SET_VERSION = "setVersion";
	protected static final String COMMAND_LIST_FILES = "listFiles";
	protected static final String COMMAND_RENAME = "rename";
	protected static final String COMMAND_MKDIRS = "mkdirs";
	protected static final String COMMAND_MKDIR = "mkdir";
	protected static final String COMMAND_DELETE = "delete";
	protected static final String COMMAND_MOVE_TO = "moveTo";
	protected static final String COMMAND_GET_LENGTH = "getLength";
	protected static final String COMMAND_SET_TIME_LAST_MODIFIED = "setTimeLastModified";
	protected static final String COMMAND_SET_TIME_CREATED = "setTimeCreated";
	protected static final String COMMAND_LIST_ROOTS = "listRoots";
	protected static final String COMMAND_DELETE_TREE = "deleteTree";
	protected static final String COMMAND_GET_HISTORY_TIMES = "getHistoryTimes";

	protected static final String VERSION_SEPARATOR = ";";
	private static final boolean LOGGING_ENABLED = false;

	private FSFEnvironment environment;
	private String separator;
	private String app;
	private String tennant;
	private String username;
	private String password;
	private RemoteFSConnectionProvider connectionProvider;


	public RemoteFS(FSFEnvironment environment, String separator, String app, String tennant, String username, String password, RemoteFSConnectionProvider connectionProvider) {
		this.environment = environment;
		this.separator = separator;
		this.app = app;
		this.tennant = tennant;
		this.username = username;
		this.password = password;
		this.connectionProvider = connectionProvider;
	}
	
	public RemoteFS(String separator, String app, String tennant, String username, String password, RemoteFSConnectionProvider connectionProvider) {
		this(FSFFileUtil.createDefaultEnvironment(), separator, app, tennant, username, password, connectionProvider);
	}
	
	public RemoteFS(String separator, String app, String tennant, String username, String password, String servletURL) throws Exception {
		this(FSFFileUtil.createDefaultEnvironment(), separator, app, tennant, username, password, new RemoteFSServletConnectionProvider(servletURL));
	}
	
	@Override
	public List<FSFFile> listRoots() {
		try {
			return RemoteFSUtil.toFileList(this, doRequest("", COMMAND_LIST_ROOTS, null));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int compareLocation(RemoteFS other) {
		if (other == null) {
			return 1;
		}
		if (!(other instanceof RemoteFS)) {
			return getClass().getName().compareTo(other.getClass().getName());
		}
		RemoteFS otherRemoteFS = (RemoteFS)other;
		
		int result = 0;
		result = connectionProvider.compareLocation(otherRemoteFS.connectionProvider);
		if (result != 0){
			return result;
		}
		result = FSFFileUtil.compareString(app, otherRemoteFS.app);
		if (result != 0){
			return result;
		}
		result = FSFFileUtil.compareString(tennant, otherRemoteFS.tennant);
		if (result != 0){
			return result;
		}
		result = FSFFileUtil.compareString(username, otherRemoteFS.username);
		if (result != 0){
			return result;
		}
		result = FSFFileUtil.compareString(password, otherRemoteFS.password);
		if (result != 0){
			return result;
		}
		return 0;
	}

	@Override
	public FSFFile createByPath(String path) throws Exception {
		String usePath = path;
		if (path.equals(separator)) {
			usePath = "";
		}
		if (usePath.endsWith(separator)) {
			usePath = usePath.substring(0, usePath.length() - 1);
		}
		return new RemoteFile(this, usePath, null);
	}

	@Override
	public String getSeparator() {
		return separator;
	}

	public FSFEnvironment getEnvironment() {
		return environment;
	}
	
	public boolean isSameLocation(RemoteFS otherRemoteFS) {
		return compareLocation(otherRemoteFS) == 0;
	}

	public RemoteFSResponse doRequest(String absolutePath, String command, InputStream payload, Object... parameters) throws RemoteFSAuthenticationException, RemoteFSRemoteException, Exception {
		log("doRequest: command = '" + command + "', absolutePath = '" + absolutePath + "'");
	
		OutputStream outputStream = connectionProvider.getOutputStream();
        GZIPOutputStream zipOutputStream = new GZIPOutputStream(outputStream);

        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, app);
        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, tennant);
        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, username);
        FSFFileUtil.writeStringWithLengthInfo(zipOutputStream, password);
        
        RemoteFSUtil.writeHandleRequestToStream(zipOutputStream, absolutePath, command, payload, parameters);
        zipOutputStream.finish();
        zipOutputStream.flush();
        zipOutputStream.close();

        InputStream inputStream = connectionProvider.getInputStream();
        GZIPInputStream gzipin;
        try {
        	gzipin = new GZIPInputStream(inputStream);
        } catch (Throwable e) {
        	throw new Exception("Could not read zipped data. Returned data as plain string: >>" + tryToReadAsPlainString(inputStream) + "<<", e);
		}
    
    	RemoteFSResponse result = RemoteFSUtil.readRemoteFileResponseFromStream(gzipin);
    	
    	if (result.getRemoteAuthenticationException() != null) {
    		throw new RemoteFSAuthenticationException(result.getRemoteAuthenticationException());
    	}
    	if (result.getRemoteGeneralException() != null) {
    		throw new RemoteFSRemoteException(result.getRemoteGeneralException());
    	}
    	if (result.getRemoteWrongVersionException() != null) {
    		throw new WrongVersionException(result.getRemoteWrongVersionException());
    	}
    	
    	result.setByteResponseInputStream(gzipin);
    	return result;
	}

	private String tryToReadAsPlainString(InputStream inputStream) {
		try {
			return FSFFileUtil.readString(inputStream);
		} catch (Exception e) {
			return "(could not read as string: " + e + ")";
		}
	}

	private void log(String message) {
		if (LOGGING_ENABLED) {
			System.out.println("RemoteFS> " + message);
		}
	}
	

}
