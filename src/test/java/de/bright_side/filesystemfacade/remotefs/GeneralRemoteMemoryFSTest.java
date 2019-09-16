package de.bright_side.filesystemfacade.remotefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.memoryfs.MemoryFS;

public class GeneralRemoteMemoryFSTest extends GeneralFSTest {
	private ByteArrayOutputStream responderResponseStream;
	private FSFSystem fsfSystem;
	private FSFEnvironment environment;
	
	@Override
	public FSFSystem createFS(FSFEnvironment environment) {
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
		return new RemoteFS(environment, "/", "myApp", "myTennant", "myUser", "myPassword", connectionProvider);
	}

	private RemoteFSFSystemProvider createFSProvider() {
		return new RemoteFSFSystemProvider() {
			
			@Override
			public FSFSystem getFSFFystem(String app, String tennant, String username, String password) throws RemoteFSAuthenticationException, Exception {
				return getAndInitFSFSystem();
			}
		};
	}
	
	private FSFSystem getAndInitFSFSystem() {
		if (fsfSystem == null) {
			fsfSystem = new MemoryFS(environment);
		}
		return fsfSystem;
	}

	@Override
	public String listDir(FSFSystem fs) throws Exception {
		return fsfSystem.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
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
		return false;
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
		return null;
	}

	@Override
	public void logStatus(String location) throws Exception {
	}
	
	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) throws Exception{
		throw new Exception("RemoteFS has no inner FS");
	}

	@Override
	public void afterClass() throws Exception {
	}

	@Override
	public boolean hasInnerFS() throws Exception {
		return false;
	}

	@Override
	public boolean isInnerFSEncrypted() throws Exception {
		return false;
	}

}
