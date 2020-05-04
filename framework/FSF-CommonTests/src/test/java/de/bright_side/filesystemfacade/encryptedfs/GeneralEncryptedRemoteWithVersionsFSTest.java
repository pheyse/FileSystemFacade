package de.bright_side.filesystemfacade.encryptedfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.historyfs.HistoryFS;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;
import de.bright_side.filesystemfacade.remotefs.RemoteFS;
import de.bright_side.filesystemfacade.remotefs.RemoteFSAuthenticationException;
import de.bright_side.filesystemfacade.remotefs.RemoteFSConnectionProvider;
import de.bright_side.filesystemfacade.remotefs.RemoteFSFSystemProvider;
import de.bright_side.filesystemfacade.remotefs.RemoteFSResponder;

public class GeneralEncryptedRemoteWithVersionsFSTest extends GeneralFSTest {
	private static final String BASE_PATH = "/data/encryptedDir";

	private ByteArrayOutputStream responderResponseStream;
	private FSFSystem fsfSystem;
	private FSFEnvironment environment;
	private FSFSystem innerFS;
	private FSFSystem outerFS;

	private FSFSystem getAndInitFSFSystem() {
		if (fsfSystem == null) {
			innerFS = new MemoryFS(environment);
			fsfSystem = new HistoryFS(innerFS, true, 0);
		}
		return fsfSystem;
	}
	
	@Override
	public FSFSystem createFS(FSFEnvironment environment) throws Exception {
		this.environment = environment; 
		RemoteFSConnectionProvider connectionProvider = new RemoteFSConnectionProvider() {
			@Override
			public OutputStream getOutputStream() throws Exception {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
					@Override
					public void close() throws IOException {
						super.close();
						byte[] bytes = toByteArray();
						
						RemoteFSResponder responder = new RemoteFSResponder();
						responderResponseStream = new ByteArrayOutputStream();
						try {
							responder.respond(new ByteArrayInputStream(bytes), responderResponseStream, createFSProvider());
						} catch (Exception e) {
							throw new IOException(e);
						}
					}
				};
				
				return outputStream;
			}
			
			@Override
			public InputStream getInputStream() throws Exception {
				return new ByteArrayInputStream(responderResponseStream.toByteArray());
			}
			
			@Override
			public int compareLocation(RemoteFSConnectionProvider connectionProvider) {
				return 0;
			}
		};
		RemoteFS remoteFS = new RemoteFS(environment, "/", "myApp", "myTennant", "myUser", "myPassword", connectionProvider);
		
		
		String password = "This-is-the-password!";
		remoteFS.createByPath(BASE_PATH).mkdirs();
		outerFS = new EncryptedFS(remoteFS, password, BASE_PATH, environment); 
		return outerFS; 
	}

	private RemoteFSFSystemProvider createFSProvider() {
		return new RemoteFSFSystemProvider() {
			
			@Override
			public FSFSystem getFSFFystem(String app, String tennant, String username, String password) throws RemoteFSAuthenticationException, Exception {
				return getAndInitFSFSystem();
			}
		};
	}
	
	@Override
	public String listDir(FSFSystem fs) throws Exception {
//		return fsfSystem.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
		return outerFS.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public void afterTest() throws Exception {
	}

	@Override
	public void beforeClass() {
	}

	@Override
	public boolean supportsVersioning() throws Exception {
		return true;
	}

	@Override
	public boolean supportsHistory() throws Exception {
		return false;
	}

	@Override
	public boolean supportCopyHistoryFilesTree() {
		return false;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		return innerFS.createByPath(BASE_PATH).listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void logStatus(String location) throws Exception {
	}
	
	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) throws Exception{
		return innerFS;
	}

	@Override
	public void afterClass() throws Exception {
	}

	@Override
	public boolean hasInnerFS() throws Exception {
		return true;
	}

	@Override
	public boolean isInnerFSEncrypted() throws Exception {
		return true;
	}

	@Override
	public boolean isTimeCreatedSupported() throws Exception {
		return true;
	}

}
