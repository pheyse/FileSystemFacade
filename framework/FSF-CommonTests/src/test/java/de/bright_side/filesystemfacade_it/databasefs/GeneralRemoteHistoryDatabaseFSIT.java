package de.bright_side.filesystemfacade_it.databasefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.Tag;

import de.bright_side.filesystemfacade.databasefs.DatabaseFS;
import de.bright_side.filesystemfacade.databasefs.DatabaseFSConfig;
import de.bright_side.filesystemfacade.facade.FSFEnvironment;
import de.bright_side.filesystemfacade.facade.FSFFile;
import de.bright_side.filesystemfacade.facade.FSFSystem;
import de.bright_side.filesystemfacade.facade.GeneralFSTest;
import de.bright_side.filesystemfacade.historyfs.HistoryFS;
import de.bright_side.filesystemfacade.remotefs.RemoteFS;
import de.bright_side.filesystemfacade.remotefs.RemoteFSAuthenticationException;
import de.bright_side.filesystemfacade.remotefs.RemoteFSConnectionProvider;
import de.bright_side.filesystemfacade.remotefs.RemoteFSFSystemProvider;
import de.bright_side.filesystemfacade.remotefs.RemoteFSResponder;

@Tag("IT")
public class GeneralRemoteHistoryDatabaseFSIT extends GeneralFSTest {
	private ByteArrayOutputStream responderResponseStream;
	private FSFSystem fsfSystem;
	private DatabaseFS innerFS;
	private FSFEnvironment environment;
	private long connectionsBeforeTest;
	private long connectionsAfterTest;

	@Override
	public FSFSystem createFS(FSFEnvironment environment) throws Exception {
		this.environment = environment;
		innerFS = new DatabaseFS(createConfig(), environment);
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
							responder.respond(new ByteArrayInputStream(bytes), responderResponseStream,
									createFSProvider());
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

	private DatabaseFSConfig createConfig() {
		return GeneralDatabaseFSWithCredentialsIT.createConfig();
	}

	private FSFSystem getAndInitFSFSystem() throws Exception {
		if (fsfSystem == null) {
			fsfSystem = new HistoryFS(innerFS, true, HistoryFS.DEFAULT_MAX_NUMBER_OF_HISTORY_FILES, HistoryFS.DEFAULT_VERSION_DIR_NAME, HistoryFS.DEFAULT_HISTORY_DIR_NAME, environment);
		}
		return fsfSystem;
	}

	@Override
	public String listDir(FSFSystem fs) throws Exception {
		return fsfSystem.createByPath("").listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void beforeTest() throws Exception {
		connectionsBeforeTest = GeneralDatabaseFSWithCredentialsIT.listOpenConnectionsOfCofingUser(createConfig());
		GeneralDatabaseFSWithCredentialsIT.dropFileTableIfExists(createConfig());
	}

	@Override
	public void afterTest() throws Exception {
		connectionsAfterTest = GeneralDatabaseFSWithCredentialsIT.listOpenConnectionsOfCofingUser(createConfig());
		assertEquals(connectionsBeforeTest, connectionsAfterTest, "open connection remained");
	}

	@Override
	public void beforeClass() throws Exception {
		GeneralDatabaseFSWithCredentialsIT.dropFileTableIfExists(createConfig());
	}

	@Override
	public boolean supportsVersioning() throws Exception {
		return true;
	}
	
	@Override
	public boolean supportCopyHistoryFilesTree() {
		return false;
	}

	@Override
	public boolean supportsHistory() throws Exception {
		return true;
	}

	@Override
	public String listDirInnerFS(FSFSystem fs) throws Exception {
		if (innerFS == null) {
			throw new Exception("innerFS is null");
		}
		FSFFile root = innerFS.createByPath(""); 
		if (root == null) {
			throw new Exception("root of innerFS is null");
		}
		return root.listDirAsString(LIST_DIR_FORMATTING_SIMPLE);
	}

	@Override
	public void logStatus(String location) throws Exception {
	}

	@Override
	protected FSFSystem getInnerFS(FSFSystem fs) throws Exception {
		return innerFS;
//		return ((HistoryFS)fs).getInnerFS();
//		throw new Exception("RemoteFS has no inner FS");
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
		return false;
	}

	@Override
	public boolean isTimeCreatedSupported() throws Exception {
		return true;
	}
}
